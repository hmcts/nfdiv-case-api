package uk.gov.hmcts.divorce.systemupdate.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.AwaitingConditionalOrderNotification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.applicantRepresentedBySolicitor;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getApplicant;

@ExtendWith(MockitoExtension.class)
public class SystemProgressHeldCaseTest {

    @Mock
    private AwaitingConditionalOrderNotification notification;

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
    void shouldSendNotificationToSolicitor() {
        final CaseData caseData = caseData();
        caseData.setApplicant1(applicantRepresentedBySolicitor());
        final CaseDetails<CaseData, State> details = CaseDetails.<CaseData, State>builder().data(caseData).build();

        underTest.aboutToSubmit(details, details);

        verify(notification).sendToSolicitor(caseData, details.getId());
    }

    @Test
    void shouldSendNotificationToBothApplicants() {
        final CaseData caseData = caseData();
        caseData.setApplicant1(getApplicant());
        caseData.setApplicationType(ApplicationType.JOINT_APPLICATION);
        final CaseDetails<CaseData, State> details = CaseDetails.<CaseData, State>builder().data(caseData).build();

        underTest.aboutToSubmit(details, details);

        verify(notification).sendToApplicant1(caseData, details.getId(), false);
        verify(notification).sendToApplicant2(caseData, details.getId(), false);
    }

    @Test
    void shouldSendNotificationOnlyToApplicant1() {
        final CaseData caseData = caseData();
        caseData.setApplicant1(getApplicant());
        caseData.setApplicationType(ApplicationType.SOLE_APPLICATION);
        final CaseDetails<CaseData, State> details = CaseDetails.<CaseData, State>builder().data(caseData).build();

        underTest.aboutToSubmit(details, details);

        verify(notification).sendToApplicant1(caseData, details.getId(), false);
        verifyNoMoreInteractions(notification);
    }
}
