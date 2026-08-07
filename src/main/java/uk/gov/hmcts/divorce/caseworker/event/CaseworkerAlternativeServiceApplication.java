package uk.gov.hmcts.divorce.caseworker.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.citizen.notification.GeneralApplicationReceivedNotification;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import static uk.gov.hmcts.divorce.divorcecase.model.State.AosDrafted;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AosOverdue;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAos;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingDocuments;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingRequestedInformation;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingServiceConsideration;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingServicePayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.GeneralApplicationReceived;
import static uk.gov.hmcts.divorce.divorcecase.model.State.InformationRequested;
import static uk.gov.hmcts.divorce.divorcecase.model.State.RequestedInformationSubmitted;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
@RequiredArgsConstructor
public class CaseworkerAlternativeServiceApplication implements CCDConfig<CaseData, State, UserRole> {
    public static final String CASEWORKER_SERVICE_RECEIVED = "caseworker-service-received";
    private static final String SERVICE_APPLICATION_RECEIVED = "Service application received";

    private final Clock clock;

    private final GeneralApplicationReceivedNotification generalApplicationReceivedNotification;

    private final NotificationDispatcher notificationDispatcher;

    public static final String WARNING_SERVICE_APP_IN_PROGRESS = """
        A digital application is currently in progress. Proceeding will archive the existing service application.
        """;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_SERVICE_RECEIVED)
            .forStates(
                AosOverdue,
                AwaitingAos,
                AosDrafted,
                Submitted,
                AwaitingDocuments,
                AwaitingRequestedInformation,
                InformationRequested,
                RequestedInformationSubmitted,
                GeneralApplicationReceived
            )
            .name(SERVICE_APPLICATION_RECEIVED)
            .description(SERVICE_APPLICATION_RECEIVED)
            .showSummary()
            .showEventNotes()
            .aboutToStartCallback(this::aboutToStart)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .grant(CREATE_READ_UPDATE, CASE_WORKER)
            .grantHistoryOnly(SUPER_USER, LEGAL_ADVISOR, JUDGE, SOLICITOR, CITIZEN))
            .page("serviceApplicationReceived")
            .pageLabel(SERVICE_APPLICATION_RECEIVED)
            .complex(CaseData::getAlternativeService)
                .mandatory(AlternativeService::getReceivedServiceApplicationDate)
                .mandatory(AlternativeService::getAlternativeServiceType)
                .optional(AlternativeService::getAlternativeServiceJudgeOrLegalAdvisorDetails)
                .mandatory(AlternativeService::getAlternativeServiceFeeRequired)
            .done()
            .page("serviceApplicationReceivedDocs")
            .pageLabel("Upload Service Application Documents")
            .complex(CaseData::getAlternativeService)
                .optional(AlternativeService::getServiceApplicationDocuments)
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(final CaseDetails<CaseData, State> details) {
        log.info("{} about to start callback invoked for Case Id: {}", CASEWORKER_SERVICE_RECEIVED, details.getId());

        final CaseData caseData = details.getData();

        if (hasServiceApplicationInProgress(caseData)) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .warnings(List.of(WARNING_SERVICE_APP_IN_PROGRESS))
                .build();
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {
        log.info("Caseworker create service application about to submit callback invoked for Case Id: {}", details.getId());

        final CaseData beforeData = beforeDetails.getData();
        final CaseData afterData = details.getData();

        if (hasServiceApplicationInProgress(beforeData)) {
            beforeData.archiveAlternativeServiceApplicationOnCompletion();
            afterData.setAlternativeServiceOutcomes(beforeData.getAlternativeServiceOutcomes());
        }

        afterData.setAlternativeService(buildServiceApplication(afterData));

        final AlternativeService serviceApplication = afterData.getAlternativeService();

        State endState = AwaitingServiceConsideration;

        if (YesOrNo.YES == serviceApplication.getAlternativeServiceFeeRequired()) {
            endState = AwaitingServicePayment;
        }

        notificationDispatcher.send(generalApplicationReceivedNotification, afterData, details.getId());

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(afterData)
            .state(endState)
            .build();
    }

    private boolean hasServiceApplicationInProgress(CaseData caseData) {
        return caseData.getAlternativeService().getAlternativeServiceType() != null;
    }

    private AlternativeService buildServiceApplication(CaseData caseData) {
        final AlternativeService serviceApplicationAnswers = caseData.getAlternativeService();

        return AlternativeService.builder()
            .receivedServiceAddedDate(LocalDate.now(clock))
            .receivedServiceApplicationDate(serviceApplicationAnswers.getReceivedServiceApplicationDate())
            .alternativeServiceType(serviceApplicationAnswers.getAlternativeServiceType())
            .alternativeServiceJudgeOrLegalAdvisorDetails(serviceApplicationAnswers.getAlternativeServiceJudgeOrLegalAdvisorDetails())
            .alternativeServiceFeeRequired(serviceApplicationAnswers.getAlternativeServiceFeeRequired())
            .serviceApplicationDocuments(serviceApplicationAnswers.getServiceApplicationDocuments())
            .build();
    }
}
