package uk.gov.hmcts.divorce.systemupdate.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.divorce.citizen.notification.conditionalorder.ConditionalOrderPronouncedNotification;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemPronounceCase.SYSTEM_PRONOUNCE_CASE;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.applicantRepresentedBySolicitor;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(SpringExtension.class)
public class SystemPronounceCaseTest {

    @Mock
    private ConditionalOrderPronouncedNotification conditionalOrderPronouncedNotification;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @InjectMocks
    private SystemPronounceCase systemPronounceCase;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        systemPronounceCase.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SYSTEM_PRONOUNCE_CASE);
    }

    @Test
    void shouldSendNotifications() {
        final CaseData caseData = caseData();
        caseData.setApplicant1(applicantRepresentedBySolicitor());
        caseData.setApplicationType(ApplicationType.SOLE_APPLICATION);
        final CaseDetails<CaseData, State> details = CaseDetails.<CaseData, State>builder().data(caseData).build();

        systemPronounceCase.aboutToSubmit(details, details);

        verify(notificationDispatcher).send(conditionalOrderPronouncedNotification, caseData, details.getId());
    }
}
