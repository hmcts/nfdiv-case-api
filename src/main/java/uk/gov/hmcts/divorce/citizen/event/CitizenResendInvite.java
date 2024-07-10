package uk.gov.hmcts.divorce.citizen.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.citizen.notification.ApplicationSentForReviewNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import static uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration.NEVER_SHOW;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingApplicant2Response;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CREATOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.validation.ValidationUtil.validateCitizenResendInvite;

@Slf4j
@Component
public class CitizenResendInvite implements CCDConfig<CaseData, State, UserRole> {

    public static final String CITIZEN_RESEND_INVITE = "citizen-resend-invite";

    @Value("${applicant_2.to_link_to_case_offset_days}")
    private long toLinkToCaseOffsetDays;

    @Autowired
    private ApplicationSentForReviewNotification applicationSentForReviewNotification;

    @Autowired
    private Clock clock;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder
            .event(CITIZEN_RESEND_INVITE)
            .forStates(AwaitingApplicant2Response)
            .showCondition(NEVER_SHOW)
            .name("Update applicant 2 email")
            .description("Citizen event for applicant 1 to update applicant 2 email pre-submission")
            .aboutToSubmitCallback(this::aboutToSubmit)
            .grant(CREATE_READ_UPDATE, CREATOR)
            .grantHistoryOnly(CASE_WORKER, SUPER_USER, LEGAL_ADVISOR);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {
        log.info("Update applicant 2 email aboutToSubmit callback invoked for Case Id: {}", details.getId());
        CaseData data = details.getData();

        if (!data.getApplicant2().isRepresented()) {
            log.info("Validating case for citizen-resend-invite event. Case Id: {}", details.getId());
            final List<String> validationErrors = validateCitizenResendInvite(details);

            if (!validationErrors.isEmpty()) {
                log.info("Validation errors: {} ", validationErrors);

                return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                    .data(data)
                    .errors(validationErrors)
                    .build();
            }
            log.info("Setting new due date for Case Id: {} (citizen-resend-invite)", details.getId());
            data.setDueDate(LocalDate.now(clock).plusDays(toLinkToCaseOffsetDays));
            data.getApplication().setApplicant2ReminderSent(null);

            log.info("Resetting access code and sending notification to allow joining. Case Id: {}", details.getId());
            data.setCaseInvite(data.getCaseInvite().generateAccessCode());
            applicationSentForReviewNotification.sendToApplicant2(data, details.getId());
        } else {
            log.info("Applicant 2 is represented in case: {} therefore skipping case invite notification", details.getId());
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .build();
    }
}
