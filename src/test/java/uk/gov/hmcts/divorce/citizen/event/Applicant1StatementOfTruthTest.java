package uk.gov.hmcts.divorce.citizen.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.ConfidentialAddress;
import uk.gov.hmcts.divorce.common.model.Gender;
import uk.gov.hmcts.divorce.common.model.Jurisdiction;
import uk.gov.hmcts.divorce.common.model.JurisdictionConnections;
import uk.gov.hmcts.divorce.common.model.State;
import uk.gov.hmcts.divorce.common.model.UserRole;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.hmcts.divorce.citizen.event.Applicant1StatementOfTruth.APPLICANT_1_STATEMENT_OF_TRUTH;

@ExtendWith(MockitoExtension.class)
class Applicant1StatementOfTruthTest {

    @InjectMocks
    private Applicant1StatementOfTruth applicant1StatementOfTruth;

    @Test
    void shouldAddConfigurationToConfigBuilder() {

        final Set<State> stateSet = Set.of(State.class.getEnumConstants());
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = new ConfigBuilderImpl<>(CaseData.class, stateSet);

        applicant1StatementOfTruth.configure(configBuilder);

        assertThat(configBuilder.getEvents().get(0).getId(), is(APPLICANT_1_STATEMENT_OF_TRUTH));
    }

    @Test
    public void givenEventStartedWithEmptyCaseThenGiveValidationErrors() {
        final long caseId = 1L;
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();
        caseDetails.setData(caseData);
        caseDetails.setId(caseId);

        final AboutToStartOrSubmitResponse<CaseData, State> response = applicant1StatementOfTruth.aboutToStart(caseDetails);

        assertThat(response.getErrors().size(), is(13));
        assertThat(response.getErrors().get(0), is("Applicant1FirstName cannot be empty or null"));
    }

    @Test
    public void givenEventStartedWithInvalidCaseThenGiveValidationErrors() {
        final long caseId = 1L;
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        CaseData caseData = CaseData.builder().build();
        setValidCaseData(caseData).setPrayerHasBeenGiven(YesOrNo.NO);

        caseDetails.setData(caseData);
        caseDetails.setId(caseId);

        final AboutToStartOrSubmitResponse<CaseData, State> response = applicant1StatementOfTruth.aboutToStart(caseDetails);

        assertThat(response.getErrors().size(), is(1));
        assertThat(response.getErrors().get(0), is("PrayerHasBeenGiven must be YES"));
    }

    @Test
    public void givenEventStartedWithValidCaseThenChangeState() {
        final long caseId = 1L;
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        CaseData caseData = CaseData.builder().build();
        setValidCaseData(caseData);

        caseDetails.setData(caseData);
        caseDetails.setId(caseId);

        final AboutToStartOrSubmitResponse<CaseData, State> response = applicant1StatementOfTruth.aboutToStart(caseDetails);

        assertThat(response.getState(), is(State.AwaitingPayment));
    }

    private CaseData setValidCaseData(CaseData caseData) {
        caseData.setApplicant1FirstName("First Name");
        caseData.setApplicant1LastName("Last Name");
        caseData.setApplicant2FirstName("First Name");
        caseData.setApplicant2LastName("Last Name");
        caseData.setFinancialOrder(YesOrNo.NO);
        caseData.setInferredApplicant1Gender(Gender.FEMALE);
        caseData.setInferredApplicant2Gender(Gender.MALE);
        caseData.setApplicant1ContactDetailsConfidential(ConfidentialAddress.KEEP);
        caseData.setPrayerHasBeenGiven(YesOrNo.YES);
        caseData.setMarriageApplicant1Name("Full name");
        caseData.setStatementOfTruth(YesOrNo.YES);
        caseData.setMarriageDate(LocalDate.now().minus(2, ChronoUnit.YEARS));
        Jurisdiction jurisdiction = new Jurisdiction();
        jurisdiction.setConnections(Set.of(JurisdictionConnections.APP_1_APP_2_LAST_RESIDENT));
        jurisdiction.setBothLastHabituallyResident(YesOrNo.YES);
        caseData.setJurisdiction(jurisdiction);
        return caseData;
    }

}
