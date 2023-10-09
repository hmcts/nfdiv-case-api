package uk.gov.hmcts.divorce.citizen.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.citizen.event.CitizenUpdateCaseStateAat.CITIZEN_UPDATE_CASE_STATE_AAT;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.EMPTY_STRING;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;

@ExtendWith(MockitoExtension.class)
class CitizenUpdateCaseStateAatTest {

    @InjectMocks
    private CitizenUpdateCaseStateAat citizenUpdateCaseStateAat;

    @Test
    void shouldAddConfigurationToConfigBuilderIfCitizenUpdateCaseStateIsEnabled() throws Exception {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        withEnvironmentVariable("CITIZEN_UPDATE_CASE_STATE_ENABLED", "true")
            .execute(() -> citizenUpdateCaseStateAat.configure(configBuilder));

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CITIZEN_UPDATE_CASE_STATE_AAT);
    }

    @Test
    void shouldUpdateCaseStateWhenCitizenUpdateCaseStateIsEnabled() {
        final long caseId = TEST_CASE_ID;
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();
        caseData.setApplicant2(new Applicant());
        caseData.getApplicant2().setMiddleName("Holding");

        caseDetails.setData(caseData);
        caseDetails.setId(caseId);

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenUpdateCaseStateAat.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getState()).isEqualTo(State.Holding);
        assertThat(response.getData().getApplicant2().getMiddleName()).isEmpty();
    }

    @Test
    void shouldNotUpdateCaseStateWhenStateIsInvalid() {
        final long caseId = TEST_CASE_ID;
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();

        final State originalState = caseDetails.getState();

        caseData.setApplicant2(new Applicant());
        caseData.getApplicant2().setMiddleName("invalidState");

        caseDetails.setData(caseData);
        caseDetails.setId(caseId);

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenUpdateCaseStateAat.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getState()).isEqualTo(originalState);
        assertThat(response.getData().getApplicant2().getMiddleName()).isNotEmpty();
    }

    @Test
    void shouldUpdateApplicant1MiddleNameToEmptyStringWhenChangingState() {
        final long caseId = TEST_CASE_ID;
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();

        caseData.setApplicant2(new Applicant());
        caseData.getApplicant2().setMiddleName("Holding");

        caseDetails.setData(caseData);
        caseDetails.setId(caseId);

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenUpdateCaseStateAat.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getApplicant2().getMiddleName()).isEmpty();
    }

    @Test
    void shouldSuccessfullyUpdateCaseDataWhenApplicant1MiddleNameIsEmptyString() {
        final long caseId = TEST_CASE_ID;
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();

        caseData.setApplicant2(new Applicant());
        caseData.getApplicant2().setMiddleName(EMPTY_STRING);

        caseData.setApplicant1(new Applicant());
        caseData.getApplicant1().setEmail(TEST_USER_EMAIL);

        caseDetails.setData(caseData);
        caseDetails.setId(caseId);

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenUpdateCaseStateAat.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getApplicant1().getEmail()).isEqualTo(TEST_USER_EMAIL);
    }
}
