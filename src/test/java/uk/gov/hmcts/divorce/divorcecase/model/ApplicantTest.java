package uk.gov.hmcts.divorce.divorcecase.model;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ContactDetailsType.PRIVATE;
import static uk.gov.hmcts.divorce.divorcecase.model.ContactDetailsType.PUBLIC;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.organisationPolicy;

class ApplicantTest {

    @Test
    void shouldReturnEnglishIfLanguagePreferenceWelshIsNoOrNull() {

        assertThat(Applicant.builder().languagePreferenceWelsh(NO).build()
            .getLanguagePreference())
            .isEqualTo(ENGLISH);

        assertThat(Applicant.builder().build()
            .getLanguagePreference())
            .isEqualTo(ENGLISH);
    }

    @Test
    void shouldReturnWelshIfLanguagePreferenceWelshIsYes() {

        assertThat(Applicant.builder().languagePreferenceWelsh(YES).build()
            .getLanguagePreference())
            .isEqualTo(WELSH);
    }

    @Test
    void shouldBeRepresentedIfSolicitorIsNotNullAndSolicitorEmailIsNotEmpty() {

        final Applicant applicant = Applicant.builder()
            .solicitor(Solicitor.builder()
                .email("solicitor@example.com")
                .build())
            .solicitorRepresented(YES)
            .build();

        assertThat(applicant.isRepresented()).isTrue();
    }

    @Test
    void shouldNotBeRepresentedIfSolicitorIsNullOrSolicitorEmailIsEmpty() {

        final Applicant applicantNoSolicitor = Applicant.builder().build();

        final Applicant applicantNoSolicitorEmail = Applicant.builder()
            .solicitor(Solicitor.builder().build())
            .build();

        assertThat(applicantNoSolicitor.isRepresented()).isFalse();
        assertThat(applicantNoSolicitorEmail.isRepresented()).isFalse();
    }

    @Test
    void shouldReturnTrueIfContactDetailsAreConfidential() {

        final Applicant applicant = Applicant.builder()
            .contactDetailsType(PRIVATE)
            .build();

        assertThat(applicant.isConfidentialContactDetails()).isTrue();
    }

    @Test
    void shouldReturnFalseIfContactDetailsAreNotConfidential() {

        final Applicant applicant = Applicant.builder()
            .contactDetailsType(PUBLIC)
            .build();

        assertThat(applicant.isConfidentialContactDetails()).isFalse();
    }

    @Test
    void shouldReturnFalseIfContactDetailsAreSetToNull() {

        final Applicant applicant = Applicant.builder()
            .build();

        assertThat(applicant.isConfidentialContactDetails()).isFalse();
    }

    @Test
    void shouldReturnTrueIfAppliedForFinancialOrder() {

        final Applicant applicant = Applicant.builder()
            .financialOrder(YES)
            .build();

        assertThat(applicant.appliedForFinancialOrder()).isTrue();
    }

    @Test
    void shouldReturnFalseIfNotAppliedForFinancialOrder() {

        final Applicant applicant = Applicant.builder()
            .financialOrder(NO)
            .build();

        assertThat(applicant.appliedForFinancialOrder()).isFalse();
    }

    @Test
    void shouldReturnFalseIfAppliedForFinancialOrderIsSetToNull() {

        final Applicant applicant1 = Applicant.builder()
            .address(AddressGlobalUK.builder().country("UK").build())
            .build();
        final Applicant applicant2 = Applicant.builder()
            .address(AddressGlobalUK.builder().country("United Kingdom").build())
            .build();

        assertThat(applicant1.appliedForFinancialOrder()).isFalse();
        assertThat(applicant2.appliedForFinancialOrder()).isFalse();
    }

    @Test
    void shouldReturnTrueIfNotUkOrUnitedKingdom() {
        final Applicant applicant = Applicant.builder()
            .address(AddressGlobalUK.builder().country("France").build())
            .build();

        assertThat(applicant.isBasedOverseas()).isTrue();
    }

    @Test
    void shouldReturnFalseIfUkOrUnitedKingdom() {
        final Applicant applicant1 = Applicant.builder()
            .address(AddressGlobalUK.builder().country("UK").build())
            .build();
        final Applicant applicant2 = Applicant.builder()
            .address(AddressGlobalUK.builder().country("United Kingdom").build())
            .build();

        assertThat(applicant1.isBasedOverseas()).isFalse();
        assertThat(applicant2.isBasedOverseas()).isFalse();
    }

    @Test
    void shouldReturnTrueIfScottishAddress() {
        final Applicant applicant1 = Applicant.builder()
            .address(AddressGlobalUK.builder().country("UK").postCode("KA27 8AB").build())
            .build();
        final Applicant applicant2 = Applicant.builder()
            .address(AddressGlobalUK.builder().country("United Kingdom").postCode("TD11 3AA").build())
            .build();
        final Applicant applicant3 = Applicant.builder()
            .address(AddressGlobalUK.builder().country("Scotland").build())
            .build();

        assertThat(applicant1.isBasedOverseas()).isTrue();
        assertThat(applicant2.isBasedOverseas()).isTrue();
        assertThat(applicant3.isBasedOverseas()).isTrue();
    }

    @Test
    void shouldReturnFalseIfGreatBritain() {
        final Applicant applicant = Applicant.builder()
            .address(AddressGlobalUK.builder().country("Great Britain").build())
            .build();

        assertThat(applicant.isBasedOverseas()).isFalse();
    }

    @Test
    void shouldReturnFalseIfGreatBritainAndCaseInsensitive() {
        final Applicant applicant = Applicant.builder()
            .address(AddressGlobalUK.builder().country("great britain").build())
            .build();

        assertThat(applicant.isBasedOverseas()).isFalse();
    }

    @Test
    void shouldReturnFalseIfApplicantIsRepresentedWhenCheckingIsBasedOverseas() {
        final Applicant applicant = Applicant.builder()
            .address(AddressGlobalUK.builder().country("France").build())
            .solicitorRepresented(YES)
            .solicitor(Solicitor.builder()
                .address("solicitor address")
                .build())
            .build();

        assertThat(applicant.isBasedOverseas()).isFalse();
    }

    @Test
    void shouldReturnReturnFalseIfAddressNotSet() {
        final Applicant applicant = Applicant.builder().build();

        assertThat(applicant.isBasedOverseas()).isFalse();
    }

    @Test
    void shouldReturnThrowErrorIfCountryIsBlank() {
        final Applicant applicant = Applicant.builder()
            .address(AddressGlobalUK.builder().build())
            .build();

        assertThat(applicant.isBasedOverseas()).isFalse();
    }

    @Test
    void shouldReturnTrueForIs2Offline() {
        assertThat(Applicant.builder().offline(YES).build()
            .isApplicantOffline()).isTrue();
    }

    @Test
    void shouldReturnFalseForIsOffline() {
        assertThat(Applicant.builder().offline(NO).build()
            .isApplicantOffline()).isFalse();
    }

    @Test
    void shouldReturnSolicitorAddressIfRepresentedWhenRequestingCorrespondenceAddress() {
        final Applicant applicant = Applicant.builder()
            .solicitorRepresented(YES)
            .solicitor(Solicitor.builder()
                .address("solicitor address")
                .build())
            .build();

        assertThat(applicant.getCorrespondenceAddress()).isEqualTo("solicitor address");
    }

    @Test
    void shouldReturnSolicitorAddressWithFirmNameIfRepresentedWhenRequestingCorrespondenceAddress() {
        final Applicant applicant = Applicant.builder()
            .solicitorRepresented(YES)
            .solicitor(Solicitor.builder()
                .organisationPolicy(organisationPolicy())
                .address("solicitor address")
                .build())
            .build();

        assertThat(applicant.getCorrespondenceAddress()).isEqualTo("Test Organisation\nsolicitor address");
    }

    @Test
    void shouldReturnApplicantAddressIfNotRepresentedWhenRequestingCorrespondenceAddress() {
        final Applicant applicant = Applicant.builder()
            .solicitorRepresented(NO)
            .contactDetailsType(PUBLIC)
            .address(AddressGlobalUK.builder()
                .addressLine1("Correspondence Address")
                .addressLine2("Line 2")
                .addressLine3("Line 3")
                .postTown("Post Town")
                .county("County")
                .postCode("Post Code")
                .country("UK")
                .build())
            .build();

        assertThat(applicant.getCorrespondenceAddress())
            .isEqualTo("Correspondence Address\nLine 2\nLine 3\nPost Town\nCounty\nUK\nPost Code");
    }

    @Test
    void shouldReturnNullIfNotRepresentedAndAddressIsNullWhenRequestingCorrespondenceAddress() {
        final Applicant applicant = Applicant.builder()
            .solicitorRepresented(NO)
            .contactDetailsType(PUBLIC)
            .build();

        assertThat(applicant.getCorrespondenceAddress()).isNull();
    }

    @Test
    void shouldReturnNullIfNotRepresentedAndPrivateContactDetailsWhenRequestingCorrespondenceAddress() {
        final Applicant applicant = Applicant.builder()
            .solicitorRepresented(NO)
            .contactDetailsType(PRIVATE)
            .address(AddressGlobalUK.builder()
                .addressLine1("Correspondence Address")
                .addressLine2("Line 2")
                .addressLine3("Line 3")
                .postTown("Post Town")
                .county("County")
                .postCode("Post Code")
                .country("UK")
                .build())
            .build();

        assertThat(applicant.getCorrespondenceAddress()).isNull();
    }

    @Test
    void shouldReturnSolicitorAddressIfRepresentedWhenRequestingPostalAddress() {
        final Applicant applicant = Applicant.builder()
            .solicitorRepresented(YES)
            .solicitor(Solicitor.builder()
                .address("solicitor address")
                .build())
            .build();

        assertThat(applicant.getCorrespondenceAddressWithoutConfidentialCheck()).isEqualTo("solicitor address");
    }

    @Test
    void shouldReturnApplicantAddressIfNotRepresentedWhenRequestingPostalAddress() {
        final Applicant applicant = Applicant.builder()
            .solicitorRepresented(NO)
            .contactDetailsType(PUBLIC)
            .address(AddressGlobalUK.builder()
                .addressLine1("Correspondence Address")
                .addressLine2("Line 2")
                .addressLine3("Line 3")
                .postTown("Post Town")
                .county("County")
                .postCode("Post Code")
                .country("UK")
                .build())
            .build();

        assertThat(applicant.getCorrespondenceAddressWithoutConfidentialCheck())
            .isEqualTo("Correspondence Address\nLine 2\nLine 3\nPost Town\nCounty\nUK\nPost Code");
    }

    @Test
    void shouldReturnNullIfNotRepresentedAndAddressIsNullWhenRequestingPostalAddress() {
        final Applicant applicant = Applicant.builder()
            .solicitorRepresented(NO)
            .contactDetailsType(PUBLIC)
            .build();

        assertThat(applicant.getCorrespondenceAddressWithoutConfidentialCheck()).isNull();
    }

    @Test
    void shouldReturnApplicantAddressIfNotRepresentedAndPrivateContactDetailsWhenRequestingCorrespondenceAddress() {
        final Applicant applicant = Applicant.builder()
            .solicitorRepresented(NO)
            .contactDetailsType(PRIVATE)
            .address(AddressGlobalUK.builder()
                .addressLine1("Correspondence Address")
                .addressLine2("Line 2")
                .addressLine3("Line 3")
                .postTown("Post Town")
                .county("County")
                .postCode("Post Code")
                .country("UK")
                .build())
            .build();

        assertThat(applicant.getCorrespondenceAddressWithoutConfidentialCheck())
            .isEqualTo("Correspondence Address\nLine 2\nLine 3\nPost Town\nCounty\nUK\nPost Code");
    }
}
