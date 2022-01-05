package uk.gov.hmcts.divorce.citizen.event;

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
import java.util.List;

import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingApplicant2Response;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Draft;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.READ;
import static uk.gov.hmcts.divorce.divorcecase.util.AccessCodeGenerator.generateAccessCode;
import static uk.gov.hmcts.divorce.divorcecase.validation.ValidationUtil.validateApplicant1BasicCase;

@Slf4j
@Component
public class InviteApplicant2 implements CCDConfig<CaseData, State, UserRole> {

    public static final String INVITE_APPLICANT_2 = "invite-applicant2";

    @Autowired
    private ApplicationSentForReviewNotification applicationSentForReviewNotification;

    @Autowired
    private NotificationDispatcher notificationDispatcher;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder
            .event(INVITE_APPLICANT_2)
            .forStateTransition(Draft, AwaitingApplicant2Response)
            .name("Invite Applicant 2")
            .description("Invite Applicant 2")
            .showCondition("applicationType=\"jointApplication\"")
            .showSummary()
            .showEventNotes()
            .grant(CREATE_READ_UPDATE, CITIZEN, APPLICANT_1_SOLICITOR)
            .grant(READ,
                SUPER_USER,
                CASE_WORKER,
                LEGAL_ADVISOR)
            .retries(120, 120)
            .aboutToSubmitCallback(this::aboutToSubmit);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {
        log.info("Invite applicant 2 about to submit callback invoked");

        CaseData data = details.getData();

        if (!data.getApplicant2().isRepresented()) {

            log.info("Applicant 2 is not represented processing case data validation");
            final List<String> validationErrors = validateApplicant1BasicCase(data);

            if (!validationErrors.isEmpty()) {
                log.info("Validation errors: {} ", validationErrors);

                return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                    .data(data)
                    .errors(validationErrors)
                    .state(Draft)
                    .build();
            }

            log.info("Generating access code to allow the respondent to access the joint application");
            data.getCaseInvite().setAccessCode(generateAccessCode());
            data.setDueDate(LocalDate.now().plus(2, ChronoUnit.WEEKS));

            notificationDispatcher.send(applicationSentForReviewNotification, data, details.getId());

            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(data)
                .state(AwaitingApplicant2Response)
                .build();
        }
        log.info("Applicant 2 is represented hence skipping about to submit processing");
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .build();
    }
}
