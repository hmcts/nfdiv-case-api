package uk.gov.hmcts.divorce.solicitor.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Fee;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.divorce.caseworker.service.CaseFlagsService;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.service.SubmissionService;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.payment.model.PbaResponse;
import uk.gov.hmcts.divorce.payment.model.ServiceRequestDto;
import uk.gov.hmcts.divorce.payment.service.PaymentService;
import uk.gov.hmcts.divorce.payment.service.PaymentSetupService;
import uk.gov.hmcts.divorce.payment.service.ServiceRequestSearchService;
import uk.gov.hmcts.divorce.solicitor.event.page.HelpWithFeesPage;
import uk.gov.hmcts.divorce.solicitor.event.page.SolConfirmJointApplication;
import uk.gov.hmcts.divorce.solicitor.event.page.SolPayAccount;
import uk.gov.hmcts.divorce.solicitor.event.page.SolPayment;
import uk.gov.hmcts.divorce.solicitor.event.page.SolPaymentSummary;
import uk.gov.hmcts.divorce.solicitor.event.page.SolStatementOfTruth;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.springframework.http.HttpStatus.CREATED;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class SolicitorSubmitApplication implements CCDConfig<CaseData, State, UserRole> {

    public static final String SOLICITOR_SUBMIT = "solicitor-submit-application";

    private final PaymentService paymentService;
    private final SolPayment solPayment;
    private final SubmissionService submissionService;
    private final PaymentSetupService paymentSetupService;
    private final CaseFlagsService caseFlagsService;
    private final ServiceRequestSearchService serviceRequestSearchService;

    @Value("${idam.client.redirect_uri}")
    private String redirectUrl;

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
        final CaseData caseData = details.getData();

        if (caseData.getApplicant1().getAddress() == null) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .errors(List.of("Please enter an address for the applicant using the 'Amend divorce application' event."))
                .warnings(null)
                .build();
        }

        setOrderSummary(caseData, details.getId());

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
            final Optional<String> pbaNumber = application.getPbaNumber();
            if (pbaNumber.isPresent()) {
                String responsibleParty = caseData.getApplicant1().getSolicitor().getName();
                String serviceRequest = findOrCreatePaymentServiceRequest(caseData, responsibleParty, caseId);

                application.setApplicationFeeServiceRequestReference(serviceRequest);

                final PbaResponse response = paymentService.processPbaPayment(
                    caseId,
                    serviceRequest,
                    caseData.getApplicant1().getSolicitor(),
                    pbaNumber.get(),
                    application.getApplicationFeeOrderSummary(),
                    application.getFeeAccountReference()
                );

                if (response.getHttpStatus() == CREATED) {
                    caseData.updateCaseDataWithPaymentDetails(
                        applicationFeeOrderSummary,
                        caseData,
                        response.getPaymentReference(),
                        serviceRequest
                    );
                } else {
                    return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                        .data(details.getData())
                        .errors(singletonList(response.getErrorMessage()))
                        .build();
                }
            } else {
                log.error(
                    "PBA number not present when payment method is 'Solicitor fee account (PBA)' for CaseId: {}",
                    details.getId());

                return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                    .data(details.getData())
                    .errors(singletonList("PBA number not present when payment method is 'Solicitor fee account (PBA)'"))
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

    public SubmittedCallbackResponse submitted(final CaseDetails<CaseData, State> details,
                                               final CaseDetails<CaseData, State> beforeDetails) {
        log.info("{} submitted callback invoked CaseID: {}", SOLICITOR_SUBMIT, details.getId());
        caseFlagsService.setSupplementaryDataForCaseFlags(details.getId());
        return SubmittedCallbackResponse.builder().build();
    }

    private void updateApplicant2DigitalDetails(CaseData caseData) {
        if (caseData.getApplicant2().getSolicitor() != null
            && caseData.getApplicant2().getSolicitor().getOrganisationPolicy() != null) {
            log.info("Applicant 2 has a solicitor and is digital");

            caseData.getApplication().setApp2ContactMethodIsDigital(YES);
        }
    }

    private void setOrderSummary(CaseData caseData, long caseId) {
        Application application = caseData.getApplication();

        // Stale order summary and SR are blanked out to ensure sols pay the fee at time of submit rather than creation (NFDIV-4857)
        application.setApplicationFeeOrderSummary(null);
        application.setApplicationFeeServiceRequestReference(null);

        OrderSummary orderSummary = paymentSetupService.createApplicationFeeOrderSummary(caseData, caseId);

        application.setApplicationFeeOrderSummary(orderSummary);
        application.setSolApplicationFeeInPounds(
            NumberFormat.getNumberInstance().format(
                new BigDecimal(application.getApplicationFeeOrderSummary().getPaymentTotal()).movePointLeft(2)
            )
        );
    }

    private String findOrCreatePaymentServiceRequest(CaseData data, String responsibleParty, long caseId) {
        Application application = data.getApplication();
        Fee fee = application.getApplicationFeeOrderSummary().getFees().get(0).getValue();

        Optional<ServiceRequestDto> unpaidServiceRequest = serviceRequestSearchService.findUnpaidServiceRequest(
            caseId, fee, responsibleParty
        );

        String serviceRequest;
        if (unpaidServiceRequest.isPresent()) {
            serviceRequest = unpaidServiceRequest.get().getPaymentGroupReference();

            log.info("Found unpaid service request: {}, for case: {}", serviceRequest, caseId);
        } else {
            serviceRequest = paymentSetupService.createApplicationFeeServiceRequest(
                data, caseId, redirectUrl
            );

            log.info("Created new service request: {}, for case: {}", serviceRequest, caseId);
        }

        return serviceRequest;
    }

    private PageBuilder addEventConfig(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        return new PageBuilder(configBuilder.event(SOLICITOR_SUBMIT)
            .forStates(Draft, Applicant2Approved, Archived)
            .name("Sign and submit")
            .description("Agree statement of truth, pay & submit")
            .showSummary()
            .showEventNotes()
            .ttlIncrement(36524)
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
