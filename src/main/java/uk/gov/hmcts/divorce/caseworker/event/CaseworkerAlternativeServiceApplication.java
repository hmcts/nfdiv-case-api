package uk.gov.hmcts.divorce.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.citizen.notification.GeneralApplicationReceivedNotification;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import java.time.Clock;
import java.time.LocalDate;

import static uk.gov.hmcts.divorce.divorcecase.model.State.AosDrafted;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AosOverdue;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAos;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingBailiffReferral;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingDocuments;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingServiceConsideration;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingServicePayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.GeneralApplicationReceived;
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
public class CaseworkerAlternativeServiceApplication implements CCDConfig<CaseData, State, UserRole> {
    public static final String CASEWORKER_SERVICE_RECEIVED = "caseworker-service-received";

    @Autowired
    private Clock clock;

    @Autowired
    private GeneralApplicationReceivedNotification generalApplicationReceivedNotification;

    @Autowired
    private NotificationDispatcher notificationDispatcher;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_SERVICE_RECEIVED)
            .forStates(AosOverdue, AwaitingAos, AosDrafted, Submitted, AwaitingDocuments, GeneralApplicationReceived)
            .name("Service application received")
            .description("Service application received")
            .showSummary()
            .showEventNotes()
            .aboutToSubmitCallback(this::aboutToSubmit)
            .grant(CREATE_READ_UPDATE, CASE_WORKER)
            .grantHistoryOnly(SUPER_USER, LEGAL_ADVISOR, JUDGE, SOLICITOR, CITIZEN))
            .page("serviceApplicationReceived")
            .pageLabel("Service application received")
            .complex(CaseData::getAlternativeService)
                .mandatory(AlternativeService::getReceivedServiceApplicationDate)
                .mandatory(AlternativeService::getAlternativeServiceType)
                .optional(AlternativeService::getAlternativeServiceJudgeOrLegalAdvisorDetails)
                .mandatory(AlternativeService::getAlternativeServiceFeeRequired)
                .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {
        log.info("Caseworker create service application about to submit callback invoked for Case Id: {}", details.getId());

        var caseData = details.getData();

        caseData.getAlternativeService().setReceivedServiceAddedDate(LocalDate.now(clock));

        State endState = AwaitingServiceConsideration;

        if (YesOrNo.YES == caseData.getAlternativeService().getAlternativeServiceFeeRequired()) {
            endState = AwaitingServicePayment;
        } else if (AlternativeServiceType.BAILIFF == caseData.getAlternativeService().getAlternativeServiceType()) {
            endState = AwaitingBailiffReferral;
        }

        notificationDispatcher.send(generalApplicationReceivedNotification, caseData, details.getId());

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(endState)
            .build();
    }
}
