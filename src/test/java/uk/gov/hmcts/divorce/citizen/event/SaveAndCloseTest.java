package uk.gov.hmcts.divorce.citizen.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.ccd.model.CaseData;
import uk.gov.hmcts.divorce.ccd.model.State;
import uk.gov.hmcts.divorce.ccd.model.UserRole;
import uk.gov.hmcts.divorce.citizen.notification.SaveAndSignOutNotificationHandler;

import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.divorce.citizen.event.SaveAndClose.SAVE_AND_CLOSE;
import static uk.gov.hmcts.divorce.util.TestDataHelper.caseData;


@ExtendWith(SpringExtension.class)
public class SaveAndCloseTest {
    @Mock
    private SaveAndSignOutNotificationHandler saveAndSignOutNotificationHandler;

    @InjectMocks
    private SaveAndClose saveAndClose;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final Set<State> stateSet = Set.of(State.class.getEnumConstants());
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = new ConfigBuilderImpl<>(CaseData.class, stateSet);

        saveAndClose.configure(configBuilder);

        assertThat(configBuilder.getEvents().get(0).getEventID(), is(SAVE_AND_CLOSE));
    }

    @Test
    public void givenValidCaseDataWhenCallbackIsInvokedThenSendEmail() throws Exception {
        final CaseData caseData = caseData();
        caseData.setPetitionerEmail("test@test.com");

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        saveAndClose.submitted(details, details);

        verify(saveAndSignOutNotificationHandler).notifyApplicant(caseData);
    }
}
