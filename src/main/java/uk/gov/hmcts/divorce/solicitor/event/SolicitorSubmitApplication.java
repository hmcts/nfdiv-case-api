package uk.gov.hmcts.divorce.solicitor.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.service.SubmissionService;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.payment.PaymentService;
import uk.gov.hmcts.divorce.payment.model.Payment;
import uk.gov.hmcts.divorce.solicitor.event.page.HelpWithFeesPage;
import uk.gov.hmcts.divorce.solicitor.event.page.SolPayAccount;
import uk.gov.hmcts.divorce.solicitor.event.page.SolPayment;
import uk.gov.hmcts.divorce.solicitor.event.page.SolPaymentSummary;
import uk.gov.hmcts.divorce.solicitor.event.page.SolStatementOfTruth;
import uk.gov.hmcts.divorce.solicitor.event.page.SolSummary;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

import static java.lang.Integer.parseInt;
import static java.util.Arrays.asList;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Draft;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_COURTADMIN_CTSC;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_COURTADMIN_RDU;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_SUPERUSER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.READ;
import static uk.gov.hmcts.divorce.payment.model.PaymentStatus.SUCCESS;

@Slf4j
@Component
public class SolicitorSubmitApplication implements CCDConfig<CaseData, State, UserRole> {

    public static final String SOLICITOR_SUBMIT = "solicitor-submit-application";

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private CcdAccessService ccdAccessService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private SubmissionService submissionService;

    private final List<CcdPageConfiguration> pages = asList(
        new SolStatementOfTruth(),
        new SolPayment(),
        new HelpWithFeesPage(),
        new SolPayAccount(),
        new SolPaymentSummary(),
        new SolSummary());

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        final PageBuilder pageBuilder = addEventConfig(configBuilder);
        pages.forEach(page -> page.addTo(pageBuilder));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(final CaseDetails<CaseData, State> details) {

        log.info("Submit application about to start callback invoked");

        log.info("Retrieving order summary");
        final OrderSummary orderSummary = paymentService.getOrderSummary();
        final CaseData caseData = details.getData();
        caseData.getApplication().setApplicationFeeOrderSummary(orderSummary);

        log.info("Adding the applicant's solicitor case roles");
        ccdAccessService.addApplicant1SolicitorRole(
            httpServletRequest.getHeader(AUTHORIZATION),
            details.getId()
        );

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }
    
    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {

        final CaseData caseData = details.getData();
        final Application application = caseData.getApplication();
        final Long caseId = details.getId();

        log.info("Setting dummy payment to mock payment process. CaseID: {}", caseId);
        if (application.getApplicationPayments() == null || application.getApplicationPayments().isEmpty()) {
            List<ListValue<Payment>> payments = new ArrayList<>();
            payments.add(new ListValue<>(null,
                getDummyPayment(application.getApplicationFeeOrderSummary())));
            application.setApplicationPayments(payments);
        } else {
            application.getApplicationPayments()
                .add(new ListValue<>(null,
                    getDummyPayment(application.getApplicationFeeOrderSummary())));
        }

        updateApplicant2DigitalDetails(caseData);

        log.info("Validating case data CaseID: {}", caseId);
        final List<String> submittedErrors = Submitted.validate(caseData);

        if (!submittedErrors.isEmpty()) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(details.getData())
                .state(details.getState())
                .errors(submittedErrors)
                .build();
        }

        final CaseDetails<CaseData, State> updatedCaseDetails = submissionService.submitApplication(details);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(updatedCaseDetails.getData())
            .state(updatedCaseDetails.getState())
            .build();
    }

    private void updateApplicant2DigitalDetails(CaseData caseData) {
        if (caseData.getApplicant2().getSolicitor() != null) {
            log.info("Applicant 2 has a solicitor and is digital");

            caseData.getApplication().setApp2ContactMethodIsDigital(YES);
            caseData.getApplicant2().setSolicitorRepresented(YES);
        }
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {
        final CaseData caseData = details.getData();

        if (caseData.getApplication().hasBeenPaidFor()) {
            details.setState(Submitted);
        } else {
            details.setState(AwaitingPayment);
        }

        return SubmittedCallbackResponse.builder().build();
    }

    private PageBuilder addEventConfig(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        return new PageBuilder(configBuilder.event(SOLICITOR_SUBMIT)
            .forStates(Draft)
            .name("Case submission")
            .description("Agree Statement of Truth, Pay & Submit")
            .showSummary()
            .endButtonLabel("Submit Application")
            .aboutToStartCallback(this::aboutToStart)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .explicitGrants()
            .grant(CREATE_READ_UPDATE, SOLICITOR)
            .grant(READ,
                CASEWORKER_COURTADMIN_CTSC,
                CASEWORKER_COURTADMIN_RDU,
                CASEWORKER_SUPERUSER,
                CASEWORKER_LEGAL_ADVISOR));
    }

    private Payment getDummyPayment(final OrderSummary orderSummary) {
        return Payment
            .builder()
            .amount(parseInt(orderSummary.getPaymentTotal()))
            .channel("online")
            .feeCode("FEE0001")
            .reference(orderSummary.getPaymentReference())
            .status(SUCCESS)
            .transactionId("ge7po9h5bhbtbd466424src9tk")
            .build();
    }
}
