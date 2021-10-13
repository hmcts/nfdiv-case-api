package uk.gov.hmcts.divorce.systemupdate.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.divorce.citizen.notification.ApplicationIssuedNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
public class SystemProgressCaseToAosOverdueTest {

    @Mock
    private ApplicationIssuedNotification applicationIssuedNotification;

    @InjectMocks
    private SystemProgressCaseToAosOverdue systemProgressCaseToAosOverdue;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        systemProgressCaseToAosOverdue.configure(configBuilder);
        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SystemProgressCaseToAosOverdue.SYSTEM_PROGRESS_TO_AOS_OVERDUE);
    }

    @Test
    void shouldSendEmailIfPetitionHasNotBeenRead() {
        final CaseData caseData = caseData();
        caseData.getAcknowledgementOfService().setConfirmReadPetition(NO);
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(1L);
        details.setData(caseData);

        systemProgressCaseToAosOverdue.aboutToSubmit(details, details);

        verify(applicationIssuedNotification).sendReminderToSoleRespondent(caseData, 1L);
    }

    @Test
    void shouldNotSendEmailIfPetitionHasBeenRead() {
        final CaseData caseData = caseData();
        caseData.getAcknowledgementOfService().setConfirmReadPetition(YES);
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(1L);
        details.setData(caseData);

        systemProgressCaseToAosOverdue.aboutToSubmit(details, details);

        verifyNoInteractions(applicationIssuedNotification);
    }
}
