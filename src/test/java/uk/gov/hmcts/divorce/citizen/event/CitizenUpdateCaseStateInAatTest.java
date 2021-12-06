package uk.gov.hmcts.divorce.citizen.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import javax.servlet.http.HttpServletRequest;

import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.citizen.event.CitizenUpdateCaseStateInAat.CITIZEN_UPDATE_CASE_STATE_IN_AAT;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;

@ExtendWith(MockitoExtension.class)
public class CitizenUpdateCaseStateInAatTest {

    @InjectMocks
    private CitizenUpdateCaseStateInAat citizenUpdateCaseStateInAat;

    @Mock
    private HttpServletRequest request;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        citizenUpdateCaseStateInAat.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CITIZEN_UPDATE_CASE_STATE_IN_AAT);
    }

    @Test
    public void shouldUpdateCaseStateWhenEnvironmentIsAat() throws Exception {

        final long caseId = 1L;
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();
        caseDetails.setData(caseData);
        caseDetails.setId(caseId);

        when(request.getParameter("State"))
            .thenReturn("Holding");

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            withEnvironmentVariable("ENVIRONMENT", "aat")
            .execute(() -> citizenUpdateCaseStateInAat.aboutToSubmit(caseDetails, caseDetails));

        assertThat(response.getState()).isEqualTo(State.Holding);
    }
}
