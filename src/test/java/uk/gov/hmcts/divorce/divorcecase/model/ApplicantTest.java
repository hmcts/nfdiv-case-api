package uk.gov.hmcts.divorce.divorcecase.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ContactDetailsType.PRIVATE;
import static uk.gov.hmcts.divorce.divorcecase.model.ContactDetailsType.PUBLIC;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;

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

        final Applicant applicant = Applicant.builder()
            .build();

        assertThat(applicant.appliedForFinancialOrder()).isFalse();
    }
}
