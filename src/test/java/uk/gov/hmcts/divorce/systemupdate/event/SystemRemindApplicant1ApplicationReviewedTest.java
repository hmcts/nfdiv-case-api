package uk.gov.hmcts.divorce.systemupdate.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.citizen.notification.JointApplicationOverdueNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import javax.servlet.http.HttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemRemindApplicant1ApplicationReviewed.SYSTEM_REMIND_APPLICANT_1_APPLICATION_REVIEWED;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(SpringExtension.class)
public class SystemRemindApplicant1ApplicationReviewedTest {

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private JointApplicationOverdueNotification jointApplicationOverdueNotification;

    @InjectMocks
    private SystemRemindApplicant1ApplicationReviewed systemRemindApplicant1ApplicationReviewed;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        systemRemindApplicant1ApplicationReviewed.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SYSTEM_REMIND_APPLICANT_1_APPLICATION_REVIEWED);
    }

    @Test
    void shouldSetOverdueNotificationSentToYes() {
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(1L);
        details.setData(caseData);

        when(httpServletRequest.getHeader(AUTHORIZATION))
            .thenReturn("auth header");

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            systemRemindApplicant1ApplicationReviewed.aboutToSubmit(details, details);

        verify(jointApplicationOverdueNotification).sendApplicationApprovedReminderToApplicant1(caseData, details.getId());
        assertThat(response.getData().getApplication().getApplicant1ReminderSent()).isEqualTo(YesOrNo.YES);
    }
}
