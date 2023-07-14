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
import uk.gov.hmcts.divorce.common.notification.AwaitingConditionalOrderNotification;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.applicantRepresentedBySolicitor;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
public class SystemProgressHeldCaseTest {

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
        caseData.setApplicationType(ApplicationType.SOLE_APPLICATION);
        final CaseDetails<CaseData, State> details = CaseDetails.<CaseData, State>builder().data(caseData).build();

        underTest.aboutToSubmit(details, details);

        verify(notificationDispatcher).send(awaitingConditionalOrderNotification, caseData, details.getId());
    }

    @Test
    void shouldSetDueDateToNullSend() {
        final CaseData caseData = caseData();
        caseData.setDueDate(LocalDate.now());
        caseData.setApplicant1(applicantRepresentedBySolicitor());
        caseData.setApplicationType(ApplicationType.SOLE_APPLICATION);
        final CaseDetails<CaseData, State> details = CaseDetails.<CaseData, State>builder().data(caseData).build();

        underTest.aboutToSubmit(details, details);

        Assertions.assertNull(caseData.getDueDate());
    }
}
