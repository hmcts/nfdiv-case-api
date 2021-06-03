package uk.gov.hmcts.divorce.citizen.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.citizen.notification.SaveAndSignOutNotificationHandler;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.State;
import uk.gov.hmcts.divorce.common.model.UserRole;

import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.divorce.citizen.event.CitizenSaveAndClose.CITIZEN_SAVE_AND_CLOSE;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;


@ExtendWith(SpringExtension.class)
public class CitizenSaveAndCloseTest {
    @Mock
    private SaveAndSignOutNotificationHandler saveAndSignOutNotificationHandler;

    @InjectMocks
    private CitizenSaveAndClose citizenSaveAndClose;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final Set<State> stateSet = Set.of(State.class.getEnumConstants());
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = new ConfigBuilderImpl<>(CaseData.class, stateSet);

        citizenSaveAndClose.configure(configBuilder);

        assertThat(configBuilder.getEvents().get(0).getId(), is(CITIZEN_SAVE_AND_CLOSE));
    }

    @Test
    public void givenValidCaseDataWhenCallbackIsInvokedThenSendEmail() throws Exception {
        final CaseData caseData = caseData();
        caseData.getApplicant1().setEmail("test@test.com");

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        citizenSaveAndClose.submitted(details, details);

        verify(saveAndSignOutNotificationHandler).notifyApplicant(caseData);
    }
}
