package uk.gov.hmcts.divorce.document.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ClarificationReason;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution;
import uk.gov.hmcts.divorce.divorcecase.model.RefusalOption;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.divorce.notification.CommonContent;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.FEMALE;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.MALE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.APPLICANT_ADDRESS;

@ExtendWith(MockitoExtension.class)
class ConditionalOrderCommonContentTest {
    @Mock
    private CommonContent commonContent;

    @InjectMocks
    private ConditionalOrderCommonContent conditionalOrderCommonContent;

    @Test
    void shouldGenerateLegalAdvisorCommentsForMoreInfo() {
        final List<ConditionalOrderCommonContent.RefusalReason> refusalReasons =
            List.of(
                new ConditionalOrderCommonContent.RefusalReason("Jurisdiction details"),
                new ConditionalOrderCommonContent.RefusalReason("Court does not have jurisdiction"));

        CaseData caseData = CaseData.builder()
            .divorceOrDissolution(DivorceOrDissolution.DIVORCE)
            .applicationType(JOINT_APPLICATION)
            .isJudicialSeparation(YesOrNo.YES)
            .applicant1(
                Applicant.builder()
                    .firstName("Bob")
                    .lastName("Smith")
                    .address(APPLICANT_ADDRESS)
                    .languagePreferenceWelsh(NO)
                    .build()
            )
            .applicant2(
                Applicant.builder()
                    .gender(FEMALE)
                    .build()
            )
            .conditionalOrder(
                ConditionalOrder.builder()
                    .refusalDecision(RefusalOption.MORE_INFO)
                    .refusalClarificationReason(Collections.singleton(ClarificationReason.JURISDICTION_DETAILS))
                    .refusalClarificationAdditionalInfo("Court does not have jurisdiction")
                    .build()
            )
            .build();

        List<ConditionalOrderCommonContent.RefusalReason> result = conditionalOrderCommonContent.generateLegalAdvisorComments(
            caseData.getConditionalOrder());

        assertThat(result).isEqualTo(refusalReasons);
    }

    @Test
    void shouldGenerateLegalAdvisorCommentsForAmendment() {
        final List<ConditionalOrderCommonContent.RefusalReason> refusalReasons =
            List.of(new ConditionalOrderCommonContent.RefusalReason("Court does not have jurisdiction"));

        CaseData caseData = CaseData.builder()
            .divorceOrDissolution(DivorceOrDissolution.DIVORCE)
            .applicationType(JOINT_APPLICATION)
            .isJudicialSeparation(YesOrNo.YES)
            .applicant1(
                Applicant.builder()
                    .firstName("Bob")
                    .lastName("Smith")
                    .address(APPLICANT_ADDRESS)
                    .languagePreferenceWelsh(NO)
                    .build()
            )
            .applicant2(
                Applicant.builder()
                    .gender(FEMALE)
                    .build()
            )
            .conditionalOrder(
                ConditionalOrder.builder()
                    .refusalDecision(RefusalOption.REJECT)
                    .refusalRejectionAdditionalInfo("Court does not have jurisdiction")
                    .build()
            )
            .build();

        List<ConditionalOrderCommonContent.RefusalReason> result = conditionalOrderCommonContent.generateLegalAdvisorComments(
            caseData.getConditionalOrder());

        assertThat(result).isEqualTo(refusalReasons);
    }

    @Test
    void shouldReturnDocumentType() {
        CaseData caseData = CaseData.builder()
            .isJudicialSeparation(NO)
            .build();

        Applicant applicant = Applicant.builder()
            .build();

        final DocumentType result = conditionalOrderCommonContent.getCoverLetterDocumentType(caseData, applicant, false);

        assertThat(result).isEqualTo(DocumentType.CONDITIONAL_ORDER_REFUSAL_COVER_LETTER);
    }

    @Test
    void shouldReturnDocumentTypeForJudicialSeparationAmendment() {
        CaseData caseData = CaseData.builder()
            .isJudicialSeparation(YES)
            .build();

        Applicant applicant = Applicant.builder()
            .build();

        final DocumentType result = conditionalOrderCommonContent.getCoverLetterDocumentType(caseData, applicant, false);

        assertThat(result).isEqualTo(DocumentType.JUDICIAL_SEPARATION_CONDITIONAL_ORDER_REFUSAL_COVER_LETTER);
    }

    @Test
    void shouldReturnDocumentTypeForJudicialSeparationAmendmentOfflineRepresented() {
        CaseData caseData = CaseData.builder()
            .isJudicialSeparation(YES)
            .application(
                Application.builder()
                    .newPaperCase(YES)
                    .build()
            )
            .build();

        Applicant applicant = Applicant.builder()
            .solicitorRepresented(YES)
            .build();

        final DocumentType result = conditionalOrderCommonContent.getCoverLetterDocumentType(caseData, applicant, false);

        assertThat(result).isEqualTo(DocumentType.JUDICIAL_SEPARATION_CONDITIONAL_ORDER_REFUSAL_SOLICITOR_COVER_LETTER);
    }

    @Test
    void shouldReturnDocumentTypeForJudicialSeparationClarification() {
        CaseData caseData = CaseData.builder()
            .isJudicialSeparation(YES)
            .build();

        Applicant applicant = Applicant.builder()
            .build();

        final DocumentType result = conditionalOrderCommonContent.getCoverLetterDocumentType(caseData, applicant, true);

        assertThat(result).isEqualTo(DocumentType.CONDITIONAL_ORDER_REFUSAL_COVER_LETTER);
    }

    @Test
    void shouldReturnDocumentTypeForJudicialSeparationClarificationOfflineRepresented() {
        CaseData caseData = CaseData.builder()
            .isJudicialSeparation(YES)
            .application(
                Application.builder()
                    .newPaperCase(YES)
                    .build()
            )
            .build();

        Applicant applicant = Applicant.builder()
            .solicitorRepresented(YES)
            .build();

        final DocumentType result = conditionalOrderCommonContent.getCoverLetterDocumentType(caseData, applicant, true);

        assertThat(result).isEqualTo(DocumentType.JUDICIAL_SEPARATION_CONDITIONAL_ORDER_CLARIFICATION_REFUSAL_SOLICITOR_COVER_LETTER);
    }

    @Test
    void shouldReturnSpouseForOfflineApplicant() {
        CaseData caseData = CaseData.builder()
            .divorceOrDissolution(DivorceOrDissolution.DIVORCE)
            .applicant1(
                Applicant.builder()
                    .gender(MALE)
                    .languagePreferenceWelsh(NO)
                    .offline(YES)
                    .build()
            )
            .applicant2(
                Applicant.builder()
                    .gender(FEMALE)
                    .build()
            )
            .build();

        final String result = conditionalOrderCommonContent.getPartner(caseData);

        assertThat(result).isEqualTo(CommonContent.SPOUSE);
    }
}
