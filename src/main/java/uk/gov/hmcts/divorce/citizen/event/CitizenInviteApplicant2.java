package uk.gov.hmcts.divorce.citizen.event;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.State;
import uk.gov.hmcts.divorce.common.model.UserRole;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static uk.gov.hmcts.divorce.common.model.State.AwaitingApplicant2Response;
import static uk.gov.hmcts.divorce.common.model.State.Draft;
import static uk.gov.hmcts.divorce.common.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.common.model.access.Permissions.CREATE_READ_UPDATE;

@Slf4j
@Component
public class CitizenInviteApplicant2 implements CCDConfig<CaseData, State, UserRole> {

    private static final String ALLOWED_CHARS = "ABCDEFGJKLMNPRSTVWXYZ23456789";
    public static final String CITIZEN_INVITE_APPLICANT_2 = "citizen-invite-applicant2";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder
            .event(CITIZEN_INVITE_APPLICANT_2)
            .initialState(Draft)
            .name("Invite Applicant 2")
            .description("Send Application to Applicant 2 for review")
            .grant(CREATE_READ_UPDATE, CITIZEN)
            .displayOrder(1)
            .retries(120, 120)
            .aboutToSubmitCallback(this::aboutToSubmit);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {

        CaseData data = details.getData();

        log.info("Generating pin to allow Applicant 2 to access the joint application");
        final String pin = generatePin();
        data.setInvitePin(pin);
        data.setApplicant2DueDate(LocalDateTime.now().plus(2, ChronoUnit.WEEKS));

        // TODO - send email to applicant 2 (to be done in NFDIV-689)

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .state(AwaitingApplicant2Response)
            .build();
    }

    private String generatePin() {
        return RandomStringUtils.random(8, 0, ALLOWED_CHARS.length(), false, false, ALLOWED_CHARS.toCharArray(), new SecureRandom());
    }
}
