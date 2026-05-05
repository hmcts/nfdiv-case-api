package uk.gov.hmcts.divorce.caseworker.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerUpdateInappropriateMatches.NO_INAPPROPRIATE_MATCHES_ERROR;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerUpdateInappropriateMatches.UPDATE_INAPPROPRIATE_MATCHES;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

class CaseworkerUpdateInappropriateMatchesTest {

    @InjectMocks
    private CaseworkerUpdateInappropriateMatches caseworkerUpdateInappropriateMatches;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        ConfigBuilder<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerUpdateInappropriateMatches.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(UPDATE_INAPPROPRIATE_MATCHES);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getName)
            .contains("Update inappropriate matches");

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getDescription)
            .contains("Update inappropriate matches");

    }

    @Test
    void shouldReturnErrorOnAboutToStartIfNoInappropriateMatchesFound() {
        CaseData caseData = caseData();
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerUpdateInappropriateMatches.aboutToStart(caseDetails);

        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors()).contains(NO_INAPPROPRIATE_MATCHES_ERROR);
    }
}
