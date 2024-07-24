package uk.gov.hmcts.divorce.caseworker.event.page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.caseworker.event.page.UpdateContactDetails.SOLICITOR_DETAILS_REMOVED_ERROR;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.FEMALE;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.MALE;
import static uk.gov.hmcts.divorce.divorcecase.model.MarriageFormation.OPPOSITE_SEX_COUPLE;
import static uk.gov.hmcts.divorce.divorcecase.model.MarriageFormation.SAME_SEX_COUPLE;
import static uk.gov.hmcts.divorce.divorcecase.model.WhoDivorcing.HUSBAND;
import static uk.gov.hmcts.divorce.divorcecase.model.WhoDivorcing.WIFE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;

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

    @Test
    void shouldReturnErrorsIfApplicant1EmailHasBeenRemovedInOnlineCase() {
        final CaseData caseDataBefore = CaseData.builder()
            .applicant1(Applicant.builder()
                .email(TEST_USER_EMAIL)
                .build())
            .build();

        final CaseDetails<CaseData, State> detailsBefore = new CaseDetails<>();
        detailsBefore.setId(TEST_CASE_ID);
        detailsBefore.setData(caseDataBefore);

        final CaseData caseData = CaseData.builder()
            .applicant1(Applicant.builder().build())
            .build();

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = updateContactDetails.midEvent(details, detailsBefore);

        assertThat(response.getErrors())
            .isEqualTo(singletonList("Please use the 'Update offline status' event before removing the email address."));
    }

    @Test
    void shouldReturnErrorsIfApplicant2EmailHasBeenRemovedInOnlineCase() {
        final CaseData caseDataBefore = CaseData.builder()
            .applicant2(Applicant.builder()
                .email(TEST_USER_EMAIL)
                .build())
            .build();

        final CaseDetails<CaseData, State> detailsBefore = new CaseDetails<>();
        detailsBefore.setId(TEST_CASE_ID);
        detailsBefore.setData(caseDataBefore);

        final CaseData caseData = CaseData.builder()
            .applicant2(Applicant.builder().build())
            .build();

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = updateContactDetails.midEvent(details, detailsBefore);

        assertThat(response.getErrors())
            .isEqualTo(singletonList("Please use the 'Update offline status' event before removing the email address."));
    }

    @Test
    void shouldAllowApplicant1EmailRemovalInOfflineCase() {
        final CaseData caseDataBefore = CaseData.builder()
            .applicant1(Applicant.builder()
                .email(TEST_USER_EMAIL)
                .offline(YES)
                .build())
            .build();

        final CaseDetails<CaseData, State> detailsBefore = new CaseDetails<>();
        detailsBefore.setId(TEST_CASE_ID);
        detailsBefore.setData(caseDataBefore);

        final CaseData caseData = CaseData.builder()
            .applicant1(Applicant.builder()
                .offline(YES)
                .build())
            .build();

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = updateContactDetails.midEvent(details, detailsBefore);

        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldAllowApplicant2EmailRemovalInOfflineCase() {
        final CaseData caseDataBefore = CaseData.builder()
            .applicant2(Applicant.builder()
                .email(TEST_USER_EMAIL)
                .offline(YES)
                .build())
            .build();

        final CaseDetails<CaseData, State> detailsBefore = new CaseDetails<>();
        detailsBefore.setId(TEST_CASE_ID);
        detailsBefore.setData(caseDataBefore);

        final CaseData caseData = CaseData.builder()
            .applicant2(Applicant.builder()
                .offline(YES)
                .build())
            .build();

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = updateContactDetails.midEvent(details, detailsBefore);

        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldAllowApplicant1EmailRemovalIfRepresentedCase() {
        final CaseData caseDataBefore = CaseData.builder()
            .applicant1(Applicant.builder()
                .email(TEST_USER_EMAIL)
                .solicitorRepresented(YES)
                .build())
            .build();

        final CaseDetails<CaseData, State> detailsBefore = new CaseDetails<>();
        detailsBefore.setId(TEST_CASE_ID);
        detailsBefore.setData(caseDataBefore);

        final CaseData caseData = CaseData.builder()
            .applicant1(Applicant.builder()
                .solicitorRepresented(YES)
                .build())
            .build();

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = updateContactDetails.midEvent(details, detailsBefore);

        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldAllowApplicant2EmailRemovalIfRepresentedCase() {
        final CaseData caseDataBefore = CaseData.builder()
            .applicant2(Applicant.builder()
                .email(TEST_USER_EMAIL)
                .solicitorRepresented(YES)
                .build())
            .build();

        final CaseDetails<CaseData, State> detailsBefore = new CaseDetails<>();
        detailsBefore.setId(TEST_CASE_ID);
        detailsBefore.setData(caseDataBefore);

        final CaseData caseData = CaseData.builder()
            .applicant2(Applicant.builder()
                .solicitorRepresented(YES)
                .build())
            .build();

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = updateContactDetails.midEvent(details, detailsBefore);

        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldReturnErrorsWhenApplicant1SolicitorDetailsAreRemoved() {
        final CaseDetails<CaseData, State> detailsBefore = new CaseDetails<>();
        final CaseData caseData = CaseData.builder()
            .applicant1(applicantAndSolicitorWithContactDetails("name", "test@test.com", "testAddress", "testPhone"))
            .applicant2(Applicant.builder().build())
            .build();
        detailsBefore.setData(caseData);

        final CaseDetails<CaseData, State> detailsAfter = new CaseDetails<>();
        final CaseData caseDataAfter = CaseData.builder()
            .applicant1(applicantAndSolicitorWithContactDetails("", "", "", ""))
            .applicant2(Applicant.builder().build())
            .build();
        detailsAfter.setData(caseDataAfter);

        AboutToStartOrSubmitResponse<CaseData, State> response
            = updateContactDetails.midEvent(detailsAfter, detailsBefore);

        assertThat(response.getErrors()).contains(
            String.format(SOLICITOR_DETAILS_REMOVED_ERROR, "name"),
            String.format(SOLICITOR_DETAILS_REMOVED_ERROR, "email address"),
            String.format(SOLICITOR_DETAILS_REMOVED_ERROR, "phone number"),
            String.format(SOLICITOR_DETAILS_REMOVED_ERROR, "postal address")
        );
    }

    @Test
    void shouldReturnErrorsWhenApplicant2SolicitorDetailsAreRemoved() {
        final CaseDetails<CaseData, State> detailsBefore = new CaseDetails<>();
        final CaseData caseData = CaseData.builder()
            .applicant2(applicantAndSolicitorWithContactDetails("name", "test@test.com", "testAddress", "testPhone"))
            .build();
        detailsBefore.setData(caseData);

        final CaseDetails<CaseData, State> detailsAfter = new CaseDetails<>();
        final CaseData caseDataAfter = CaseData.builder()
            .applicant2(applicantAndSolicitorWithContactDetails("", "", "", ""))
            .build();
        detailsAfter.setData(caseDataAfter);

        AboutToStartOrSubmitResponse<CaseData, State> response
            = updateContactDetails.midEvent(detailsAfter, detailsBefore);

        assertThat(response.getErrors()).contains(
            String.format(SOLICITOR_DETAILS_REMOVED_ERROR, "name"),
            String.format(SOLICITOR_DETAILS_REMOVED_ERROR, "email address"),
            String.format(SOLICITOR_DETAILS_REMOVED_ERROR, "phone number"),
            String.format(SOLICITOR_DETAILS_REMOVED_ERROR, "postal address")
        );
    }

    @Test
    void shouldNotReturnErrorsWhenSolicitorDetailsWereBlankBefore() {
        final CaseDetails<CaseData, State> detailsBefore = new CaseDetails<>();
        final CaseData caseData = CaseData.builder()
            .applicant1(applicantAndSolicitorWithContactDetails("", "", "", ""))
            .build();
        detailsBefore.setData(caseData);

        final CaseDetails<CaseData, State> detailsAfter = new CaseDetails<>();
        final CaseData caseDataAfter = CaseData.builder()
            .applicant1(applicantAndSolicitorWithContactDetails("", "", "", ""))
            .build();
        detailsAfter.setData(caseDataAfter);

        AboutToStartOrSubmitResponse<CaseData, State> response
            = updateContactDetails.midEvent(detailsAfter, detailsBefore);

        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldNotReturnErrorsWhenSolicitorDetailsWereMissingBefore() {
        final CaseDetails<CaseData, State> detailsBefore = new CaseDetails<>();
        final CaseData caseData = CaseData.builder()
            .applicant1(applicantAndSolicitorWithContactDetails(null, null, null, null))
            .applicant2(Applicant.builder().build())
            .build();
        detailsBefore.setId(TEST_CASE_ID);
        detailsBefore.setData(caseData);

        final CaseDetails<CaseData, State> detailsAfter = new CaseDetails<>();
        final CaseData caseDataAfter = CaseData.builder()
            .applicant1(applicantAndSolicitorWithContactDetails("", "", "", ""))
            .applicant2(Applicant.builder().build())
            .build();
        detailsAfter.setId(TEST_CASE_ID);
        detailsAfter.setData(caseDataAfter);

        AboutToStartOrSubmitResponse<CaseData, State> response
            = updateContactDetails.midEvent(detailsAfter, detailsBefore);

        assertThat(response.getErrors()).isNull();
    }

    private Applicant applicantAndSolicitorWithContactDetails(String name, String email, String address, String phone) {
        return Applicant.builder()
                .solicitor(Solicitor.builder()
                    .name(name).email(email).address(address).phone(phone)
                    .build())
                .build();
    }
}
