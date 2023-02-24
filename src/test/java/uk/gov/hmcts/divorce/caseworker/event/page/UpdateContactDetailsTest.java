package uk.gov.hmcts.divorce.caseworker.event.page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.FEMALE;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.MALE;
import static uk.gov.hmcts.divorce.divorcecase.model.MarriageFormation.OPPOSITE_SEX_COUPLE;
import static uk.gov.hmcts.divorce.divorcecase.model.MarriageFormation.SAME_SEX_COUPLE;
import static uk.gov.hmcts.divorce.divorcecase.model.WhoDivorcing.HUSBAND;
import static uk.gov.hmcts.divorce.divorcecase.model.WhoDivorcing.WIFE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
public class UpdateContactDetailsTest {

    @InjectMocks
    private UpdateContactDetails updateContactDetails;

    @Test
    void shouldNotReturnErrorsIfFemaleHusbandOppositeSexCouple() {
        final CaseData caseData = CaseData.builder().build();
        caseData.getApplicant1().setGender(FEMALE);
        caseData.getApplicant2().setGender(MALE);
        caseData.getApplication().setDivorceWho(HUSBAND);
        caseData.getApplication().getMarriageDetails().setFormationType(OPPOSITE_SEX_COUPLE);
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response
            = updateContactDetails.midEvent(details, details);

        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldNotReturnErrorsIfMaleWifeOppositeSexCouple() {
        final CaseData caseData = CaseData.builder().build();
        caseData.getApplicant1().setGender(MALE);
        caseData.getApplicant2().setGender(FEMALE);
        caseData.getApplication().setDivorceWho(WIFE);
        caseData.getApplication().getMarriageDetails().setFormationType(OPPOSITE_SEX_COUPLE);
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response
            = updateContactDetails.midEvent(details, details);

        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldNotReturnErrorsIfFemaleWifeSameSexCouple() {
        final CaseData caseData = CaseData.builder().build();
        caseData.getApplicant1().setGender(FEMALE);
        caseData.getApplicant2().setGender(FEMALE);
        caseData.getApplication().setDivorceWho(WIFE);
        caseData.getApplication().getMarriageDetails().setFormationType(SAME_SEX_COUPLE);
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response
            = updateContactDetails.midEvent(details, details);

        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldNotReturnErrorsIfMaleHusbandSameSexCouple() {
        final CaseData caseData = CaseData.builder().build();
        caseData.getApplicant1().setGender(MALE);
        caseData.getApplicant2().setGender(MALE);
        caseData.getApplication().setDivorceWho(HUSBAND);
        caseData.getApplication().getMarriageDetails().setFormationType(SAME_SEX_COUPLE);
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response
            = updateContactDetails.midEvent(details, details);

        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldNotReturnErrorsIfGenderDivorceWhoOrRelationshipFormationTypeIsNull() {
        final CaseData caseData = CaseData.builder().build();
        caseData.getApplicant1().setGender(null);
        caseData.getApplicant2().setGender(null);
        caseData.getApplication().setDivorceWho(null);
        caseData.getApplication().getMarriageDetails().setFormationType(null);
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response
            = updateContactDetails.midEvent(details, details);

        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldNotReturnErrorsIfOneOfGenderDivorceWhoOrRelationshipFormationTypeIsNull() {
        final CaseData caseData = CaseData.builder().build();
        caseData.getApplicant1().setGender(MALE);
        caseData.getApplicant2().setGender(FEMALE);
        caseData.getApplication().setDivorceWho(WIFE);
        caseData.getApplication().getMarriageDetails().setFormationType(null);
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response
            = updateContactDetails.midEvent(details, details);

        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldNotReturnErrorsIfTwoOfGenderDivorceWhoOrRelationshipFormationTypeIsNull() {
        final CaseData caseData = CaseData.builder().build();
        caseData.getApplicant1().setGender(MALE);
        caseData.getApplicant2().setGender(FEMALE);
        caseData.getApplication().setDivorceWho(null);
        caseData.getApplication().getMarriageDetails().setFormationType(null);
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response
            = updateContactDetails.midEvent(details, details);

        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldNotReturnErrorsIfThreeOfGendersDivorceWhoOrRelationshipFormationTypeIsNull() {
        final CaseData caseData = CaseData.builder().build();
        caseData.getApplicant1().setGender(MALE);
        caseData.getApplicant2().setGender(null);
        caseData.getApplication().setDivorceWho(null);
        caseData.getApplication().getMarriageDetails().setFormationType(null);
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response
            = updateContactDetails.midEvent(details, details);

        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldReturnErrorsIfFemaleWifeOppositeSexCouple() {
        final CaseData caseData = CaseData.builder().build();
        caseData.getApplicant1().setGender(FEMALE);
        caseData.getApplicant2().setGender(FEMALE);
        caseData.getApplication().setDivorceWho(WIFE);
        caseData.getApplication().getMarriageDetails().setFormationType(OPPOSITE_SEX_COUPLE);
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response
            = updateContactDetails.midEvent(details, details);

        assertThat(response.getErrors()).isNotNull();
        assertThat(response.getErrors()).contains("""
                You have selected the applicant gender as Female and they are divorcing their Wife and they are an Opposite-sex couple.
                Please ensure this is correct before submitting.""");
    }

    @Test
    void shouldReturnErrorsIfMaleHusbandOppositeSexCouple() {
        final CaseData caseData = CaseData.builder().build();
        caseData.getApplicant1().setGender(MALE);
        caseData.getApplicant2().setGender(MALE);
        caseData.getApplication().setDivorceWho(HUSBAND);
        caseData.getApplication().getMarriageDetails().setFormationType(OPPOSITE_SEX_COUPLE);
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response
            = updateContactDetails.midEvent(details, details);

        assertThat(response.getErrors()).isNotNull();
        assertThat(response.getErrors()).contains("""
                You have selected the applicant gender as Male and they are divorcing their Husband and they are an Opposite-sex couple.
                Please ensure this is correct before submitting.""");
    }

    @Test
    void shouldReturnErrorsIfMaleWifeSameSexCouple() {
        final CaseData caseData = CaseData.builder().build();
        caseData.getApplicant1().setGender(MALE);
        caseData.getApplicant2().setGender(FEMALE);
        caseData.getApplication().setDivorceWho(WIFE);
        caseData.getApplication().getMarriageDetails().setFormationType(SAME_SEX_COUPLE);
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response
            = updateContactDetails.midEvent(details, details);

        assertThat(response.getErrors()).isNotNull();
        assertThat(response.getErrors()).contains("""
                You have selected the applicant gender as Male and they are divorcing their Wife and they are an Same-sex couple.
                Please ensure this is correct before submitting.""");
    }

    @Test
    void shouldReturnErrorsIfFemaleHusbandSameSexCouple() {
        final CaseData caseData = CaseData.builder().build();
        caseData.getApplicant1().setGender(FEMALE);
        caseData.getApplicant2().setGender(MALE);
        caseData.getApplication().setDivorceWho(HUSBAND);
        caseData.getApplication().getMarriageDetails().setFormationType(SAME_SEX_COUPLE);
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response
            = updateContactDetails.midEvent(details, details);

        assertThat(response.getErrors()).isNotNull();
        assertThat(response.getErrors()).contains("""
                You have selected the applicant gender as Female and they are divorcing their Husband and they are an Same-sex couple.
                Please ensure this is correct before submitting.""");
    }
}
