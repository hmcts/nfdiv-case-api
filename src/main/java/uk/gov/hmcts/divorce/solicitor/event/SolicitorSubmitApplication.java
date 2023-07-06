package uk.gov.hmcts.divorce.solicitor.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.payment.PaymentService;
import uk.gov.hmcts.divorce.solicitor.event.page.HelpWithFeesPage;
import uk.gov.hmcts.divorce.solicitor.event.page.SolConfirmJointApplication;
import uk.gov.hmcts.divorce.solicitor.event.page.SolPayAccount;
import uk.gov.hmcts.divorce.solicitor.event.page.SolPayment;
import uk.gov.hmcts.divorce.solicitor.event.page.SolPaymentSummary;
import uk.gov.hmcts.divorce.solicitor.event.page.SolStatementOfTruth;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.idam.client.models.User;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;

import static java.util.Arrays.asList;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Applicant2Approved;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Archived;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Draft;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.validation.ApplicationValidation.validateReadyForPayment;
import static uk.gov.hmcts.divorce.payment.PaymentService.EVENT_ISSUE;
import static uk.gov.hmcts.divorce.payment.PaymentService.KEYWORD_DIVORCE;
import static uk.gov.hmcts.divorce.payment.PaymentService.SERVICE_DIVORCE;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemSolicitorPayment.SYSTEM_SOLICITOR_PAYMENT;

@Slf4j
@Component
public class SolicitorSubmitApplication implements CCDConfig<CaseData, State, UserRole> {

    public static final String SOLICITOR_SUBMIT = "solicitor-submit-application";

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private SolPayment solPayment;

    @Autowired
    private CcdUpdateService ccdUpdateService;

    @Autowired
    private IdamService idamService;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

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

        log.info("{} about to start callback invoked for Case Id: {}", SOLICITOR_SUBMIT, details.getId());

        log.info("Retrieving order summary");
        final OrderSummary orderSummary = paymentService.getOrderSummaryByServiceEvent(SERVICE_DIVORCE, EVENT_ISSUE, KEYWORD_DIVORCE);
        final CaseData caseData = details.getData();
        caseData.getApplication().setApplicationFeeOrderSummary(orderSummary);

        caseData.getApplication().setSolApplicationFeeInPounds(
            NumberFormat.getNumberInstance().format(
                new BigDecimal(orderSummary.getPaymentTotal()).movePointLeft(2)
            )
        );

        caseData.getApplication().setSolStatementOfReconciliationCertify(null);
        caseData.getApplication().setSolStatementOfReconciliationDiscussed(null);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .errors(null)
            .warnings(null)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {

        log.info("{} about to submit callback invoked for Case Id: {}", SOLICITOR_SUBMIT, details.getId());
        final Long caseId = details.getId();
        final var caseData = details.getData();

        log.info("Validating case data CaseID: {}", caseId);
        final List<String> submittedErrors = validateReadyForPayment(caseData);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .state(details.getState())
            .errors(submittedErrors)
            .build();
    }

    public SubmittedCallbackResponse submitted(final CaseDetails<CaseData, State> details,
                                               final CaseDetails<CaseData, State> beforeDetails) {

        log.info("Solicitor submit application submitted callback invoked for case id: {}", details.getId());

        final User user = idamService.retrieveSystemUpdateUserDetails();
        final String serviceAuthorization = authTokenGenerator.generate();

        log.info("Submitting system-solicitor-payment event for case id: {}", details.getId());
        ccdUpdateService.submitEvent(details, SYSTEM_SOLICITOR_PAYMENT, user, serviceAuthorization);

        return SubmittedCallbackResponse.builder().build();
    }

    private PageBuilder addEventConfig(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        return new PageBuilder(configBuilder.event(SOLICITOR_SUBMIT)
            .forStates(Draft, Applicant2Approved, Archived)
            .name("Sign and submit")
            .description("Agree statement of truth, pay & submit")
            .showSummary()
            .showEventNotes()
            .showCondition("applicationType=\"soleApplication\" OR [STATE]=\"Applicant2Approved\"")
            .endButtonLabel("Submit Application")
            .aboutToStartCallback(this::aboutToStart)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted)
            .grant(CREATE_READ_UPDATE, APPLICANT_1_SOLICITOR)
            .grantHistoryOnly(
                CASE_WORKER,
                SUPER_USER,
                LEGAL_ADVISOR,
                JUDGE));
    }
}
