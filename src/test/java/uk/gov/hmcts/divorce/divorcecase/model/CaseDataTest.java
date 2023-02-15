package uk.gov.hmcts.divorce.divorcecase.model;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.type.ListValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType.DEEMED;
import static uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType.DISPENSED;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolutionExtension.JUDICIAL_SEPARATION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolutionExtension.NA;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APPLICANT_2_USER_EMAIL;

class CaseDataTest {

    @Test
    void shouldReturnApplicant2EmailIfApplicant2EmailIsSet() {

        final CaseData caseData = CaseData.builder()
            .applicant2(Applicant.builder()
                .email(TEST_APPLICANT_2_USER_EMAIL)
                .build())
            .build();

        assertThat(caseData.getApplicant2EmailAddress()).isEqualTo(TEST_APPLICANT_2_USER_EMAIL);
    }

    @Test
    void shouldReturnApplicant2InviteEmailIfApplicant2EmailIsNullAndApplicant2InviteEmailAddressIsSet() {

        final CaseData caseData = CaseData.builder()
            .applicant2(Applicant.builder()
                .email("")
                .build())
            .caseInvite(CaseInvite.builder()
                .applicant2InviteEmailAddress(TEST_APPLICANT_2_USER_EMAIL)
                .build())
            .build();

        assertThat(caseData.getApplicant2EmailAddress()).isEqualTo(TEST_APPLICANT_2_USER_EMAIL);
    }

    @Test
    void shouldReturnApplicant2InviteEmailIfApplicant2EmailIsBlankAndApplicant2InviteEmailAddressIsSet() {

        final CaseData caseData = CaseData.builder()
            .caseInvite(CaseInvite.builder()
                .applicant2InviteEmailAddress(TEST_APPLICANT_2_USER_EMAIL)
                .build())
            .build();

        assertThat(caseData.getApplicant2EmailAddress()).isEqualTo(TEST_APPLICANT_2_USER_EMAIL);
    }

    @Test
    void shouldReturnNullIfApplicant2EmailIsNullAndCaseInviteIsNull() {

        final CaseData caseData = CaseData.builder()
            .build();

        assertThat(caseData.getApplicant2EmailAddress()).isNull();
    }

    @Test
    void shouldReturnTrueForIsWelshApplicationIfSoleAndApp1LanguagePreferenceWelshYes() {

        final CaseData caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .applicant1(Applicant.builder().languagePreferenceWelsh(YES).build())
            .build();

        assertThat(caseData.isWelshApplication()).isTrue();
    }

    @Test
    void shouldReturnFalseForIsWelshApplicationIfSoleAndApp1LanguagePreferenceWelshNo() {

        final CaseData caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .applicant1(Applicant.builder().languagePreferenceWelsh(NO).build())
            .build();

        assertThat(caseData.isWelshApplication()).isFalse();
    }

    @Test
    void shouldReturnTrueForIsWelshApplicationIfJointAndApp1LanguagePreferenceWelshYes() {

        final CaseData caseData = CaseData.builder()
            .applicationType(JOINT_APPLICATION)
            .applicant1(Applicant.builder().languagePreferenceWelsh(YES).build())
            .build();

        assertThat(caseData.isWelshApplication()).isTrue();
    }

    @Test
    void shouldReturnTrueForIsWelshApplicationIfJointAndApp2LanguagePreferenceWelshYes() {

        final CaseData caseData = CaseData.builder()
            .applicationType(JOINT_APPLICATION)
            .applicant2(Applicant.builder().languagePreferenceWelsh(YES).build())
            .build();

        assertThat(caseData.isWelshApplication()).isTrue();
    }

    @Test
    void shouldReturnFalseForIsWelshApplicationIfJointAndApp1AndApp2LanguagePreferenceWelshNo() {

        final CaseData caseData = CaseData.builder()
            .applicationType(JOINT_APPLICATION)
            .applicant1(Applicant.builder().languagePreferenceWelsh(NO).build())
            .applicant2(Applicant.builder().languagePreferenceWelsh(NO).build())
            .build();

        assertThat(caseData.isWelshApplication()).isFalse();
    }

    @Test
    void shouldReturnTrueForIsWelshApplicationIfSoleAndApp1UsedWelshTranslationOnSubmissionYes() {

        final CaseData caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .applicant1(Applicant.builder().usedWelshTranslationOnSubmission(YES).build())
            .build();

        assertThat(caseData.isWelshApplication()).isTrue();
    }

    @Test
    void shouldReturnFalseForIsWelshApplicationIfSoleAndApp1UsedWelshTranslationOnSubmissionNull() {

        final CaseData caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .applicant1(Applicant.builder().usedWelshTranslationOnSubmission(null).build())
            .build();

        assertThat(caseData.isWelshApplication()).isFalse();
    }

    @Test
    void shouldReturnTrueForIsWelshApplicationIfJointAndApp1UsedWelshTranslationOnSubmissionYes() {

        final CaseData caseData = CaseData.builder()
            .applicationType(JOINT_APPLICATION)
            .applicant1(Applicant.builder().usedWelshTranslationOnSubmission(YES).build())
            .build();

        assertThat(caseData.isWelshApplication()).isTrue();
    }

    @Test
    void shouldReturnTrueForIsWelshApplicationIfJointAndApp2UsedWelshTranslationOnSubmissionYes() {

        final CaseData caseData = CaseData.builder()
            .applicationType(JOINT_APPLICATION)
            .applicant2(Applicant.builder().usedWelshTranslationOnSubmission(YES).build())
            .build();

        assertThat(caseData.isWelshApplication()).isTrue();
    }

    @Test
    void shouldReturnFalseForIsWelshApplicationIfJointAndApp1AndApp2UsedWelshTranslationOnSubmissionNull() {

        final CaseData caseData = CaseData.builder()
            .applicationType(JOINT_APPLICATION)
            .applicant1(Applicant.builder().usedWelshTranslationOnSubmission(null).build())
            .applicant2(Applicant.builder().usedWelshTranslationOnSubmission(null).build())
            .build();

        assertThat(caseData.isWelshApplication()).isFalse();
    }

    @Test
    void shouldReturnTrueIfCaseDataIsDivorce() {

        final CaseData caseData = CaseData.builder()
            .divorceOrDissolution(DIVORCE)
            .build();

        assertThat(caseData.isDivorce()).isTrue();
    }

    @Test
    void shouldReturnFalseIfCaseDataIsDissolution() {

        final CaseData caseData = CaseData.builder()
            .divorceOrDissolution(DISSOLUTION)
            .build();

        assertThat(caseData.isDivorce()).isFalse();
    }

    @Test
    void shouldReturnFirstAlternativeServiceOutcomeIfPresent() {

        final CaseData caseData = CaseData.builder().build();

        assertThat(caseData.getFirstAlternativeServiceOutcome()).isEqualTo(Optional.empty());

        final AlternativeServiceOutcome alternativeServiceOutcome1 = AlternativeService
            .builder()
            .alternativeServiceType(DEEMED)
            .build()
            .getOutcome();
        final AlternativeServiceOutcome alternativeServiceOutcome2 = AlternativeService
            .builder()
            .alternativeServiceType(DISPENSED)
            .build()
            .getOutcome();

        final List<ListValue<AlternativeServiceOutcome>> alternativeServiceOutcomes = new ArrayList<>();
        alternativeServiceOutcomes.add(0, ListValue.<AlternativeServiceOutcome>builder()
            .value(alternativeServiceOutcome2)
            .build());
        alternativeServiceOutcomes.add(1, ListValue.<AlternativeServiceOutcome>builder()
            .value(alternativeServiceOutcome1)
            .build());

        caseData.setAlternativeServiceOutcomes(alternativeServiceOutcomes);

        assertThat(caseData.getFirstAlternativeServiceOutcome()).isEqualTo(Optional.of(alternativeServiceOutcome2));
    }

    @Test
    void shouldReturnTrueIfJudicialSeparationCase() {
        final CaseData caseData = CaseData.builder()
            .divorceOrDissolutionExtension(JUDICIAL_SEPARATION)
            .build();

        assertThat(caseData.isJudicialSeparationCase()).isTrue();
    }

    @Test
    void shouldReturnFalseIfNotJudicialSeparationCase() {
        final CaseData caseData = CaseData.builder()
            .divorceOrDissolutionExtension(NA)
            .build();

        assertThat(caseData.isJudicialSeparationCase()).isFalse();
    }

    @Test
    void shouldReturnFalseIfJudicialSeparationIsNotSet() {
        final CaseData caseData = CaseData.builder().build();

        assertThat(caseData.isJudicialSeparationCase()).isFalse();
    }
}
