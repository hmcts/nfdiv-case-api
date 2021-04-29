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
import uk.gov.hmcts.divorce.common.model.State;
import uk.gov.hmcts.divorce.common.model.UserRole;

import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.hmcts.divorce.citizen.event.PetitionerStatementOfTruth.PETITIONER_STATEMENT_OF_TRUTH;

@ExtendWith(MockitoExtension.class)
class PetitionerStatementOfTruthTest {

    @InjectMocks
    private PetitionerStatementOfTruth petitionerStatementOfTruth;

    @Test
    void shouldAddConfigurationToConfigBuilder() {

        final Set<State> stateSet = Set.of(State.class.getEnumConstants());
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = new ConfigBuilderImpl<>(CaseData.class, stateSet);

        petitionerStatementOfTruth.configure(configBuilder);

        assertThat(configBuilder.getEvents().get(0).getEventID(), is(PETITIONER_STATEMENT_OF_TRUTH));
    }

    @Test
    public void givenEventStartedWithEmptyCaseThenGiveValidationErrors() {
        final long caseId = 1L;
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();
        caseDetails.setData(caseData);
        caseDetails.setId(caseId);

        final AboutToStartOrSubmitResponse<CaseData, State> response = petitionerStatementOfTruth.aboutToStart(caseDetails);

        assertThat(response.getErrors().size(), is(11));
        assertThat(response.getErrors().get(0), is("PetitionerFirstName cannot be empty or null"));
    }

    @Test
    public void givenEventStartedWithInvalidCaseThenGiveValidationErrors() {
        final long caseId = 1L;
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        CaseData caseData = CaseData.builder().build();
        setValidCaseData(caseData).setPrayerHasBeenGiven(YesOrNo.NO);

        caseDetails.setData(caseData);
        caseDetails.setId(caseId);

        final AboutToStartOrSubmitResponse<CaseData, State> response = petitionerStatementOfTruth.aboutToStart(caseDetails);

        assertThat(response.getErrors().size(), is(1));
        assertThat(response.getErrors().get(0), is("PrayerHasBeenGiven must be YES"));
    }

    @Test
    public void givenEventStartedWithValidCaseThenGiveNoValidationErrors() {
        final long caseId = 1L;
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        CaseData caseData = CaseData.builder().build();
        setValidCaseData(caseData);

        caseDetails.setData(caseData);
        caseDetails.setId(caseId);

        final AboutToStartOrSubmitResponse<CaseData, State> response = petitionerStatementOfTruth.aboutToStart(caseDetails);

        assertThat(response.getErrors().size(), is(0));
    }

    private CaseData setValidCaseData(CaseData caseData) {
        caseData.setPetitionerFirstName("First Name");
        caseData.setPetitionerLastName("Last Name");
        caseData.setRespondentFirstName("First Name");
        caseData.setRespondentLastName("Last Name");
        caseData.setFinancialOrder(YesOrNo.NO);
        caseData.setInferredPetitionerGender(Gender.FEMALE);
        caseData.setInferredRespondentGender(Gender.MALE);
        caseData.setPetitionerContactDetailsConfidential(ConfidentialAddress.KEEP);
        caseData.setPrayerHasBeenGiven(YesOrNo.YES);
        caseData.setMarriagePetitionerName("Full name");
        caseData.setStatementOfTruth(YesOrNo.YES);
        return caseData;
    }

}
