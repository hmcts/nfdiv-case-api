package uk.gov.hmcts.divorce.systemupdate.event;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.notification.AwaitingConditionalOrderNotification;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.applicantRepresentedBySolicitor;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class SystemProgressHeldCaseTest {

    @Mock
    private AwaitingConditionalOrderNotification awaitingConditionalOrderNotification;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @InjectMocks
    private SystemProgressHeldCase underTest;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        underTest.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SystemProgressHeldCase.SYSTEM_PROGRESS_HELD_CASE);
    }

    @Test
    void shouldSendNotifications() {
        final CaseData caseData = caseData();
        caseData.setApplicant1(applicantRepresentedBySolicitor());
        caseData.setApplicationType(ApplicationType.JOINT_APPLICATION);
        caseData.getApplicant1().setOffline(YesOrNo.NO);
        caseData.getApplicant2().setOffline(YesOrNo.NO);
        final CaseDetails<CaseData, State> details = CaseDetails.<CaseData, State>builder().data(caseData).build();

        underTest.aboutToSubmit(details, details);

        verify(notificationDispatcher).send(awaitingConditionalOrderNotification, caseData, details.getId());
    }

    @Test
    void shouldSetDueDateToNullSend() {
        final CaseData caseData = caseData();
        caseData.setDueDate(LocalDate.now());
        caseData.getApplicant1().setOffline(YesOrNo.NO);
        caseData.getApplicant2().setOffline(YesOrNo.NO);
        caseData.setApplicant1(applicantRepresentedBySolicitor());
        caseData.setApplicationType(ApplicationType.JOINT_APPLICATION);
        final CaseDetails<CaseData, State> details = CaseDetails.<CaseData, State>builder().data(caseData).build();

        underTest.aboutToSubmit(details, details);

        Assertions.assertNull(caseData.getDueDate());
    }

    @Test
    void errorShouldNotSendNotificationsWhenValidationFails() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = caseData();
        caseData.setApplicationType(ApplicationType.JOINT_APPLICATION);
        caseData.getApplicant1().setOffline(YesOrNo.YES);
        caseData.getApplicant2().setOffline(YesOrNo.NO);
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        final AboutToStartOrSubmitResponse<CaseData, State> response = underTest.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors()).containsExactly("Applicants have different offline status in a joint case."
            + " Both applicants needs to be either online or offline for caseID: " +  TEST_CASE_ID);
        verifyNoInteractions(notificationDispatcher);
    }
}
