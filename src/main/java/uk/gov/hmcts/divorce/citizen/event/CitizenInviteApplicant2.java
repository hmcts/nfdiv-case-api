package uk.gov.hmcts.divorce.citizen.event;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.citizen.notification.ApplicationSentForReviewApplicant1Notification;
import uk.gov.hmcts.divorce.citizen.notification.ApplicationSentForReviewApplicant2Notification;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.State;
import uk.gov.hmcts.divorce.common.model.UserRole;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static uk.gov.hmcts.divorce.common.model.State.AwaitingApplicant2Response;
import static uk.gov.hmcts.divorce.common.model.State.Draft;
import static uk.gov.hmcts.divorce.common.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.common.model.access.Permissions.CREATE_READ_UPDATE;

@Slf4j
@Component
public class CitizenInviteApplicant2 implements CCDConfig<CaseData, State, UserRole> {

    private static final String ALLOWED_CHARS = "ABCDEFGHJKLMNPRSTVWXYZ23456789";
    public static final String CITIZEN_INVITE_APPLICANT_2 = "citizen-invite-applicant2";

    @Autowired
    private ApplicationSentForReviewApplicant1Notification applicationSentForReviewApplicant1Notification;

    @Autowired
    private ApplicationSentForReviewApplicant2Notification applicationSentForReviewApplicant2Notification;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder
            .event(CITIZEN_INVITE_APPLICANT_2)
            .forStateTransition(Draft, AwaitingApplicant2Response)
            .name("Invite The Respondent")
            .description("Send Application to the respondent for review")
            .grant(CREATE_READ_UPDATE, CITIZEN)
            .displayOrder(1)
            .retries(120, 120)
            .aboutToSubmitCallback(this::aboutToSubmit);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        log.info("Citizen invite applicant 2 about to submit callback invoked");

        CaseData data = details.getData();

        log.info("Validating case data");
        final List<String> validationErrors = AwaitingApplicant2Response.validate(data);

        if (!validationErrors.isEmpty()) {
            log.info("Validation errors: ");
            for (String error : validationErrors) {
                log.info(error);
            }

            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(data)
                .errors(validationErrors)
                .state(Draft)
                .build();
        }

        log.info("Generating access code to allow the respondent to access the joint application");
        final String accessCode = generateAccessCode();
        data.setAccessCode(accessCode);
        data.setDueDate(LocalDate.now().plus(2, ChronoUnit.WEEKS));

        applicationSentForReviewApplicant1Notification.send(data, details.getId());
        applicationSentForReviewApplicant2Notification.send(data, details.getId());

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .state(AwaitingApplicant2Response)
            .build();
    }

    private String generateAccessCode() {
        return RandomStringUtils.random(8, 0, ALLOWED_CHARS.length(), false, false, ALLOWED_CHARS.toCharArray(), new SecureRandom());
    }
}
