package uk.gov.hmcts.divorce.common.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.citizen.notification.ApplicationSentForReviewNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;

import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingApplicant2Response;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Slf4j
@Component
public class InviteApplicant1 implements CCDConfig<CaseData, State, UserRole> {

    public static final String INVITE_APPLICANT_1 = "invite-applicant1";

    @Autowired
    private ApplicationSentForReviewNotification applicationSentForReviewNotification;

    @Autowired
    private NotificationDispatcher notificationDispatcher;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        //TODO: need to find out which state transitions this should be, I've put forAllStates for now
        // //.forStateTransition(Draft, AwaitingApplicant1Response)
        //  might need to be enum set and AwaitingApplicant1Response
        // might need different showCondition too but thinking only show when sols gone and showing offline
        configBuilder
            .event(INVITE_APPLICANT_1)
            .forAllStates()
            .name("Invite Applicant 1")
            .description("Invite Applicant 1 back online")
            .showSummary()
            .showCondition("applicant1SolicitorRepresented=\"No\" AND applicant1Offline=\"Yes\"")
            .showEventNotes()
            .grant(CREATE_READ_UPDATE, CASE_WORKER, SYSTEMUPDATE)
            .grantHistoryOnly(
                SUPER_USER,
                CASE_WORKER,
                LEGAL_ADVISOR,
                JUDGE)
            .retries(120, 120)
            .aboutToSubmitCallback(this::aboutToSubmit);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {
        log.info("Invite applicant 1 about to submit callback invoked for Case Id: {}", details.getId());

        CaseData data = details.getData();

        if (!data.getApplicant1().isRepresented()) {

            log.info("Applicant 1 is not represented processing case data validation");
            //TODO more thorough validation
            // final List<String> validationErrors = validateApplicant1BasicCase(data);
            if (null == data.getApplicant1().getEmail()) {
                return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                    .data(data)
                    .state(details.getState())
                    .errors(Collections.singletonList("Cannot invite applicant1 to online as need no email address for applicant1"))
                    .build();
            }

            log.info("Generating access code to allow app1 to go online");
            data.setCaseInviteApp1(data.getCaseInviteApp1().generateAccessCode());
        }

        data.setDueDate(LocalDate.now().plus(2, ChronoUnit.WEEKS));
        notificationDispatcher.send(applicationSentForReviewNotification, data, details.getId());

        if (!data.getApplicant2().isRepresented()) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(data)
                .state(AwaitingApplicant2Response)
                .build();
        } else {
            log.info("Applicant 2 is represented so skipping state update");
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(data)
                .build();
        }
    }
}
