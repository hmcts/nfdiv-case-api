package uk.gov.hmcts.divorce.divorcecase.model;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.ScannedDocument;
import uk.gov.hmcts.divorce.document.model.DocumentType;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType.DEEMED;
import static uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType.DISPENSED;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.SupplementaryCaseType.JUDICIAL_SEPARATION;
import static uk.gov.hmcts.divorce.divorcecase.model.SupplementaryCaseType.NA;
import static uk.gov.hmcts.divorce.divorcecase.model.SupplementaryCaseType.SEPARATION;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
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
    void shouldReturnTrueForIsWelshApplicationIfSoleAndapp1LanguagePreferenceWelshYes() {

        final CaseData caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .applicant1(Applicant.builder().languagePreferenceWelsh(YES).build())
            .build();

        assertThat(caseData.isWelshApplication()).isTrue();
    }

    @Test
    void shouldReturnFalseForIsWelshApplicationIfSoleAndapp1LanguagePreferenceWelshNo() {

        final CaseData caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .applicant1(Applicant.builder().languagePreferenceWelsh(NO).build())
            .build();

        assertThat(caseData.isWelshApplication()).isFalse();
    }

    @Test
    void shouldReturnTrueForIsWelshApplicationIfJointAndapp1LanguagePreferenceWelshYes() {

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
    void shouldSetSupplementalCaseType() {
        final CaseData caseData = CaseData.builder().build();

        caseData.setSupplementaryCaseType(NA);
        assertThat(caseData.getSupplementaryCaseType()).isEqualTo(NA);

        caseData.setSupplementaryCaseType(JUDICIAL_SEPARATION);
        assertThat(caseData.getSupplementaryCaseType()).isEqualTo(JUDICIAL_SEPARATION);

        caseData.setSupplementaryCaseType(SEPARATION);
        assertThat(caseData.getSupplementaryCaseType()).isEqualTo(SEPARATION);
    }

    @Test
    void shouldSetNAWithoutAlteringDivorce() {
        final CaseData caseData = CaseData.builder()
            .divorceOrDissolution(DIVORCE)
            .build();
        caseData.setSupplementaryCaseType(NA);

        assertThat(caseData.getSupplementaryCaseType()).isEqualTo(NA);
        assertThat(caseData.getDivorceOrDissolution()).isEqualTo(DIVORCE);
    }

    @Test
    void shouldSetNAWithoutAlteringDissolution() {
        final CaseData caseData = CaseData.builder()
            .divorceOrDissolution(DISSOLUTION)
            .build();
        caseData.setSupplementaryCaseType(NA);

        assertThat(caseData.getSupplementaryCaseType()).isEqualTo(NA);
        assertThat(caseData.getDivorceOrDissolution()).isEqualTo(DISSOLUTION);
    }

    @Test
    void shouldSetDivorceIfSettingJudicialSeparation() {
        final CaseData caseData = CaseData.builder()
            .build();
        caseData.setSupplementaryCaseType(JUDICIAL_SEPARATION);

        assertThat(caseData.getSupplementaryCaseType()).isEqualTo(JUDICIAL_SEPARATION);
        assertThat(caseData.getDivorceOrDissolution()).isEqualTo(DIVORCE);
    }

    @Test
    void shouldSetDissolutionIfSettingSeparation() {
        final CaseData caseData = CaseData.builder()
            .build();
        caseData.setSupplementaryCaseType(SEPARATION);

        assertThat(caseData.getSupplementaryCaseType()).isEqualTo(SEPARATION);
        assertThat(caseData.getDivorceOrDissolution()).isEqualTo(DISSOLUTION);
    }

    @Test
    void isJudicialSeparationCaseShouldReturnTrueIfJudicialSeparationOrSeparationCase() {
        final CaseData caseData = CaseData.builder()
            .supplementaryCaseType(JUDICIAL_SEPARATION)
            .build();
        assertThat(caseData.isJudicialSeparationCase()).isTrue();

        caseData.setSupplementaryCaseType(SEPARATION);
        assertThat(caseData.isJudicialSeparationCase()).isTrue();
    }

    @Test
    void isJudicialSeparationCaseShouldReturnFalseIfNotJudicialSeparationOrSeparationCase() {
        CaseData caseData = CaseData.builder().build();
        assertThat(caseData.isJudicialSeparationCase()).isFalse();

        caseData.setSupplementaryCaseType(NA);
        assertThat(caseData.isJudicialSeparationCase()).isFalse();
    }

    @Test
    void hasNaOrNullSupplementaryCaseTypeShouldReturnTrueIfSupplementaryCaseTypeIsNullOrNA() {
        CaseData caseData = CaseData.builder().build();
        assertThat(caseData.hasNaOrNullSupplementaryCaseType()).isTrue();

        caseData.setSupplementaryCaseType(NA);
        assertThat(caseData.hasNaOrNullSupplementaryCaseType()).isTrue();
    }

    @Test
    void hasNaOrNullSupplementaryCaseTypeShouldReturnFalseIfSupplementaryCaseTypeIsNotNullOrNA() {
        final CaseData caseData = CaseData.builder()
            .supplementaryCaseType(SEPARATION)
            .build();
        assertThat(caseData.hasNaOrNullSupplementaryCaseType()).isFalse();

        caseData.setSupplementaryCaseType(JUDICIAL_SEPARATION);
        assertThat(caseData.hasNaOrNullSupplementaryCaseType()).isFalse();
    }

    @Test
    void shouldSetScannedD36FormOnFinalOrder() {
        final CaseData caseData = new CaseData();
        final Clock clock = mock(Clock.class);
        setMockClock(clock);
        final Document document = Document.builder().build();
        ScannedDocument scannedDocument = ScannedDocument.builder().url(document).build();

        caseData.reclassifyScannedDocumentToChosenDocumentType(DocumentType.FINAL_ORDER_APPLICATION, clock, scannedDocument);

        assertThat(caseData.getFinalOrder().getScannedD36Form()).isEqualTo(document);
    }
}
