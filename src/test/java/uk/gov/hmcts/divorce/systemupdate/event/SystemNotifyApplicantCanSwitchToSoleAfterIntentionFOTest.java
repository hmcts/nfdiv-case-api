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
import uk.gov.hmcts.divorce.common.notification.ApplicantSwitchToSoleAfterIntentionFONotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.FinalOrder.IntendsToSwitchToSole.I_INTEND_TO_SWITCH_TO_SOLE;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemNotifyApplicantCanSwitchToSoleAfterIntentionFO.SYSTEM_APPLICANT_SWITCH_TO_SOLE_AFTER_INTENTION;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDate;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(SpringExtension.class)
public class SystemNotifyApplicantCanSwitchToSoleAfterIntentionFOTest {

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @Mock
    private ApplicantSwitchToSoleAfterIntentionFONotification applicantSwitchToSoleAfterIntentionFONotification;

    @InjectMocks
    private SystemNotifyApplicantCanSwitchToSoleAfterIntentionFO systemNotifyApplicantCanSwitchToSoleAfterIntentionFO;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        systemNotifyApplicantCanSwitchToSoleAfterIntentionFO.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SYSTEM_APPLICANT_SWITCH_TO_SOLE_AFTER_INTENTION);
    }

    @Test
    void shouldSendNotificationToApplicant1SwitchToSoleAfterIntention() {
        final CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.setFinalOrder(FinalOrder.builder()
            .applicant1IntendsToSwitchToSole(Set.of(I_INTEND_TO_SWITCH_TO_SOLE))
            .dateApplicant1DeclaredIntentionToSwitchToSoleFo(getExpectedLocalDate().minusDays(15))
            .build());
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            systemNotifyApplicantCanSwitchToSoleAfterIntentionFO.aboutToSubmit(details, details);

        verify(notificationDispatcher).send(applicantSwitchToSoleAfterIntentionFONotification, caseData, details.getId());
        assertThat(response.getData().getFinalOrder().getFinalOrderApplicant1NotifiedCanSwitchToSoleAfterIntention()).isEqualTo(YES);
    }

    @Test
    void shouldSendNotificationToApplicant2SwitchToSoleAfterIntention() {
        final CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.setFinalOrder(FinalOrder.builder()
            .applicant2IntendsToSwitchToSole(Set.of(I_INTEND_TO_SWITCH_TO_SOLE))
            .dateApplicant2DeclaredIntentionToSwitchToSoleFo(getExpectedLocalDate().minusDays(15))
            .build());
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            systemNotifyApplicantCanSwitchToSoleAfterIntentionFO.aboutToSubmit(details, details);

        verify(notificationDispatcher).send(applicantSwitchToSoleAfterIntentionFONotification, caseData, details.getId());
        assertThat(response.getData().getFinalOrder().getFinalOrderApplicant2NotifiedCanSwitchToSoleAfterIntention()).isEqualTo(YES);
    }
}
