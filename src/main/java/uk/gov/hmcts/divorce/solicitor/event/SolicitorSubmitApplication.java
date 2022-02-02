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
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.payment.PaymentService;
import uk.gov.hmcts.divorce.payment.model.Payment;
import uk.gov.hmcts.divorce.payment.model.PbaResponse;
import uk.gov.hmcts.divorce.solicitor.event.page.HelpWithFeesPage;
import uk.gov.hmcts.divorce.solicitor.event.page.SolConfirmJointApplication;
import uk.gov.hmcts.divorce.solicitor.event.page.SolPayAccount;
import uk.gov.hmcts.divorce.solicitor.event.page.SolPayment;
import uk.gov.hmcts.divorce.solicitor.event.page.SolPaymentSummary;
import uk.gov.hmcts.divorce.solicitor.event.page.SolStatementOfTruth;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.lang.Integer.parseInt;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Applicant2Approved;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Draft;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.READ;
import static uk.gov.hmcts.divorce.divorcecase.validation.ApplicationValidation.validateReadyForPayment;
import static uk.gov.hmcts.divorce.payment.PaymentService.EVENT_ISSUE;
import static uk.gov.hmcts.divorce.payment.PaymentService.KEYWORD_DIVORCE;
import static uk.gov.hmcts.divorce.payment.PaymentService.SERVICE_DIVORCE;
import static uk.gov.hmcts.divorce.payment.model.PaymentStatus.SUCCESS;

@Slf4j
@Component
public class SolicitorSubmitApplication implements CCDConfig<CaseData, State, UserRole> {

    public static final String SOLICITOR_SUBMIT = "solicitor-submit-application";

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private SolPayment solPayment;

    @Autowired
    private SubmissionService submissionService;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        final List<CcdPageConfiguration> pages = asList(
            new SolConfirmJointApplication(),
            new SolStatementOfTruth(),
            solPayment,
            new HelpWithFeesPage(),
            new SolPayAccount(),
            new SolPaymentSummary());

        final PageBuilder pageBuilder = addEventConfig(configBuilder);

        pages.forEach(page -> page.addTo(pageBuilder));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(final CaseDetails<CaseData, State> details) {

        log.info("Submit application about to start callback invoked");

        log.info("Retrieving order summary");
        final OrderSummary orderSummary = paymentService.getOrderSummaryByServiceEvent(SERVICE_DIVORCE, EVENT_ISSUE, KEYWORD_DIVORCE);
        final CaseData caseData = details.getData();
        caseData.getApplication().setApplicationFeeOrderSummary(orderSummary);

        caseData.getApplication().setSolApplicationFeeInPounds(
            NumberFormat.getNumberInstance().format(
                new BigDecimal(orderSummary.getPaymentTotal()).movePointLeft(2)
            )
        );

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .errors(null)
            .warnings(null)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {

        final Long caseId = details.getId();
        final var caseData = details.getData();

        log.info("Validating case data CaseID: {}", caseId);
        final List<String> submittedErrors = validateReadyForPayment(caseData);

        if (!submittedErrors.isEmpty()) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(details.getData())
                .state(details.getState())
                .errors(submittedErrors)
                .build();
        }

        final var application = caseData.getApplication();
        final var applicationFeeOrderSummary = application.getApplicationFeeOrderSummary();

        if (caseData.getApplication().isSolicitorPaymentMethodPba()) {
            PbaResponse response = paymentService.processPbaPayment(caseData, caseId, caseData.getApplicant1().getSolicitor());

            if (response.getHttpStatus() == CREATED) {
                updateCaseDataWithPaymentDetails(applicationFeeOrderSummary, caseData, response.getPaymentReference());
            } else {
                return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                    .data(details.getData())
                    .errors(singletonList(response.getErrorMessage()))
                    .build();
            }
        }

        updateApplicant2DigitalDetails(caseData);

        final CaseDetails<CaseData, State> updatedCaseDetails = submissionService.submitApplication(details);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(updatedCaseDetails.getData())
            .state(updatedCaseDetails.getState())
            .build();
    }

    private void updateCaseDataWithPaymentDetails(
        OrderSummary applicationFeeOrderSummary,
        CaseData caseData,
        String paymentReference
    ) {
        var payment = Payment
            .builder()
            .amount(parseInt(applicationFeeOrderSummary.getPaymentTotal()))
            .channel("online")
            .feeCode(applicationFeeOrderSummary.getFees().get(0).getValue().getCode())
            .reference(paymentReference)
            .status(SUCCESS)
            .build();


        var application = caseData.getApplication();

        if (isEmpty(application.getApplicationPayments())) {
            List<ListValue<Payment>> payments = new ArrayList<>();
            payments.add(new ListValue<>(UUID.randomUUID().toString(), payment));
            application.setApplicationPayments(payments);
        } else {
            application.getApplicationPayments()
                .add(new ListValue<>(UUID.randomUUID().toString(), payment));
        }
    }

    private void updateApplicant2DigitalDetails(CaseData caseData) {
        if (caseData.getApplicant2().getSolicitor() != null
            && caseData.getApplicant2().getSolicitor().getOrganisationPolicy() != null) {
            log.info("Applicant 2 has a solicitor and is digital");

            caseData.getApplication().setApp2ContactMethodIsDigital(YES);
        }
    }

    private PageBuilder addEventConfig(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        return new PageBuilder(configBuilder.event(SOLICITOR_SUBMIT)
            .forStates(Draft, Applicant2Approved)
            .name("Sign and submit")
            .description("Agree statement of truth, pay & submit")
            .showSummary()
            .showEventNotes()
            .showCondition("applicationType=\"soleApplication\" OR [STATE]=\"Applicant2Approved\"")
            .endButtonLabel("Submit Application")
            .aboutToStartCallback(this::aboutToStart)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .grant(CREATE_READ_UPDATE, SOLICITOR)
            .grant(READ,
                CASE_WORKER,
                SUPER_USER,
                LEGAL_ADVISOR));
    }
}
