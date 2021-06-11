package uk.gov.hmcts.divorce.citizen.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.citizen.notification.ApplicationSentForReviewApplicant1Notification;
import uk.gov.hmcts.divorce.citizen.notification.ApplicationSentForReviewApplicant2Notification;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.State;
import uk.gov.hmcts.divorce.common.model.UserRole;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.divorce.citizen.event.CitizenInviteApplicant2.CITIZEN_INVITE_APPLICANT_2;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(SpringExtension.class)
public class CitizenInviteApplicant2Test {

    @Mock
    private ApplicationSentForReviewApplicant1Notification applicationSentForReviewApplicant1Notification;

    @Mock
    private ApplicationSentForReviewApplicant2Notification applicationSentForReviewApplicant2Notification;

    @InjectMocks
    private CitizenInviteApplicant2 citizenInviteApplicant2;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final Set<State> stateSet = Set.of(State.class.getEnumConstants());
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = new ConfigBuilderImpl<>(CaseData.class, stateSet);

        citizenInviteApplicant2.configure(configBuilder);

        assertThat(configBuilder.getEvents().get(0).getId()).isEqualTo(CITIZEN_INVITE_APPLICANT_2);
    }

    @Test
    void shouldAddApplicant2DueDateToCaseData() {
        final CaseData caseData = caseData();

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenInviteApplicant2.aboutToSubmit(details, details);

        assertThat(response.getData().getDueDate()).isNotNull();
    }

    @Test
    public void givenValidCaseDataWhenCallbackIsInvokedThenGeneratePinAndSendEmails() {
        final CaseData caseData = caseData();

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenInviteApplicant2.aboutToSubmit(details, details);

        verify(applicationSentForReviewApplicant1Notification).send(caseData, details.getId());
        verify(applicationSentForReviewApplicant2Notification).send(caseData, details.getId());

        assertThat(response.getData().getInvitePin()).isNotBlank();
        assertThat(response.getData().getInvitePin().length()).isEqualTo(8);
        assertThat(response.getData().getInvitePin()).doesNotContain("I","O","U","0","1");
    }
}
