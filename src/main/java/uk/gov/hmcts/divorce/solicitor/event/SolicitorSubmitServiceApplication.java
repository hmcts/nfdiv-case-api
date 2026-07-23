package uk.gov.hmcts.divorce.solicitor.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.citizen.notification.interimapplications.SolicitorServiceApplicationSubmittedNotification;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FeeDetails;
import uk.gov.hmcts.divorce.divorcecase.model.ServicePaymentMethod;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.solicitor.event.page.ServiceApplicationStatementOfTruthPage;
import uk.gov.hmcts.divorce.solicitor.event.page.SolServiceApplicationPaymentSummaryPage;
import uk.gov.hmcts.divorce.solicitor.service.ServiceApplicationSubmitPaymentService;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.util.List;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AosOverdue;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Slf4j
@Component
@RequiredArgsConstructor
public class SolicitorSubmitServiceApplication implements CCDConfig<CaseData, State, UserRole> {

    private final ServiceApplicationSubmitPaymentService serviceApplicationSubmitPaymentService;

    private final NotificationDispatcher notificationDispatcher;

    private final SolicitorServiceApplicationSubmittedNotification solicitorServiceApplicationSubmittedNotification;

    public static final String SOLICITOR_SUBMIT_SERVICE_APPLICATION = "solicitor-submit-service-application";

    private static  final String ERROR_ALREADY_SUBMITTED
        = "The ongoing service application on this case has already been submitted and you cannot submit it again or amend it.";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        final PageBuilder pageBuilder = addEventConfig(configBuilder);

        final List<CcdPageConfiguration> pages = List.of(new ServiceApplicationStatementOfTruthPage(),
            new SolServiceApplicationPaymentSummaryPage());

        pages.forEach(page -> page.addTo(pageBuilder));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails) {

        log.info("{} about to submit callback invoked for Case Id: {}", SOLICITOR_SUBMIT_SERVICE_APPLICATION, details.getId());

        CaseData caseData = details.getData();

        Optional<String> paymentError = serviceApplicationSubmitPaymentService.processSubmitPayment(details.getId(), caseData);
        if (paymentError.isPresent()) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .errors(singletonList(paymentError.get()))
                .build();
        }

        caseData.getApplicant1().archiveInterimApplicationOptions();

        ServicePaymentMethod paymentMethod = Optional.ofNullable(caseData.getAlternativeService())
            .map(AlternativeService::getServicePaymentFee)
            .map(FeeDetails::getPaymentMethod)
            .orElse(null);

        State targetState = ServicePaymentMethod.FEE_PAY_BY_ACCOUNT.equals(paymentMethod)
            ? State.AwaitingServiceConsideration
            : State.AwaitingServicePayment;

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(targetState)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(final CaseDetails<CaseData, State> details) {

        log.info("{} About to Submit callback invoked for Case Id: {}", SOLICITOR_SUBMIT_SERVICE_APPLICATION, details.getId());

        CaseData caseData = details.getData();
        boolean alreadySubmitted = Optional.ofNullable(caseData.getAlternativeService())
            .map(AlternativeService::getServicePaymentFee)
            .map(fee -> hasText(fee.getPaymentReference()) || hasText(fee.getHelpWithFeesReferenceNumber()))
            .orElse(false);

        if (alreadySubmitted) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .errors(singletonList(ERROR_ALREADY_SUBMITTED))
                .build();
        }
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData).build();
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {
        log.info("{} submitted callback invoked for Case Id: {}", SOLICITOR_SUBMIT_SERVICE_APPLICATION, details.getId());

        notificationDispatcher.send(solicitorServiceApplicationSubmittedNotification, details.getData(), details.getId());

        return SubmittedCallbackResponse.builder().build();
    }

    private PageBuilder addEventConfig(
        final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        return new PageBuilder(configBuilder
            .event(SOLICITOR_SUBMIT_SERVICE_APPLICATION)
            .forState(AosOverdue)
            .name("Submit Service App")
            .description("Submit Service App")
            .showCondition("serviceApplicationSubmittedOnline=\"Yes\" AND alternativeServiceType=\"*\"")
            .showSummary()
            .showEventNotes()
            .endButtonLabel("Save Application")
            .aboutToStartCallback(this::aboutToStart)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted)
            .grant(CREATE_READ_UPDATE, APPLICANT_1_SOLICITOR)
            .grantHistoryOnly(CASE_WORKER, SUPER_USER, LEGAL_ADVISOR, JUDGE));
    }
}
