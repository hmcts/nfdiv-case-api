package uk.gov.hmcts.divorce.document.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
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
import static uk.gov.hmcts.divorce.document.DocumentConstants.CLARIFICATION_REFUSAL_ORDER_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.JUDICIAL_SEPARATION_ORDER_CLARIFICATION_REFUSAL_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.JUDICIAL_SEPARATION_ORDER_CLARIFICATION_REFUSAL_SOLICITOR_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.JUDICIAL_SEPARATION_ORDER_REFUSAL_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.JUDICIAL_SEPARATION_ORDER_REFUSAL_SOLICITOR_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.REJECTED_REFUSAL_ORDER_COVER_LETTER_TEMPLATE_ID;
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
    void shouldReturnDocumentTypeAndTemplateId() {
        CaseData caseData = CaseData.builder()
            .isJudicialSeparation(NO)
            .build();

        Applicant applicant = Applicant.builder()
            .build();

        final DocumentType documentType = conditionalOrderCommonContent.getCoverLetterDocumentType(caseData, applicant, false);
        final String documentTemplateId = conditionalOrderCommonContent.getCoverLetterDocumentTemplateId(caseData, applicant, false);

        assertThat(documentType).isEqualTo(DocumentType.CONDITIONAL_ORDER_REFUSAL_COVER_LETTER);
        assertThat(documentTemplateId).isEqualTo(REJECTED_REFUSAL_ORDER_COVER_LETTER_TEMPLATE_ID);
    }

    @Test
    void shouldReturnDocumentTypeAndTemplateIdForClarification() {
        CaseData caseData = CaseData.builder()
            .isJudicialSeparation(NO)
            .build();

        Applicant applicant = Applicant.builder()
            .solicitorRepresented(YES)
            .build();

        final DocumentType documentType = conditionalOrderCommonContent.getCoverLetterDocumentType(caseData, applicant, true);
        final String documentTemplateId = conditionalOrderCommonContent.getCoverLetterDocumentTemplateId(caseData, applicant, true);

        assertThat(documentType).isEqualTo(DocumentType.CONDITIONAL_ORDER_REFUSAL_COVER_LETTER);
        assertThat(documentTemplateId).isEqualTo(CLARIFICATION_REFUSAL_ORDER_COVER_LETTER_TEMPLATE_ID);
    }

    @Test
    void shouldReturnDocumentTypeAndTemplateIdForJudicialSeparationAmendment() {
        CaseData caseData = CaseData.builder()
            .divorceOrDissolution(DivorceOrDissolution.DIVORCE)
            .isJudicialSeparation(YES)
            .build();

        Applicant applicant = Applicant.builder()
            .build();

        final DocumentType documentType = conditionalOrderCommonContent.getCoverLetterDocumentType(caseData, applicant, false);
        final String documentTemplateId = conditionalOrderCommonContent.getCoverLetterDocumentTemplateId(caseData, applicant, false);

        assertThat(documentType).isEqualTo(DocumentType.JUDICIAL_SEPARATION_ORDER_REFUSAL_COVER_LETTER);
        assertThat(documentTemplateId).isEqualTo(JUDICIAL_SEPARATION_ORDER_REFUSAL_COVER_LETTER_TEMPLATE_ID);
    }

    @Test
    void shouldReturnDocumentTypeAndTemplateIdForSeparationAmendment() {
        CaseData caseData = CaseData.builder()
            .divorceOrDissolution(DivorceOrDissolution.DISSOLUTION)
            .isJudicialSeparation(YES)
            .build();

        Applicant applicant = Applicant.builder()
            .build();

        final DocumentType documentType = conditionalOrderCommonContent.getCoverLetterDocumentType(caseData, applicant, false);
        final String documentTemplateId = conditionalOrderCommonContent.getCoverLetterDocumentTemplateId(caseData, applicant, false);

        assertThat(documentType).isEqualTo(DocumentType.SEPARATION_ORDER_REFUSAL_COVER_LETTER);
        assertThat(documentTemplateId).isEqualTo(JUDICIAL_SEPARATION_ORDER_REFUSAL_COVER_LETTER_TEMPLATE_ID);
    }

    @Test
    void shouldReturnDocumentTypeAndTemplateIdForJudicialSeparationAmendmentWhenApplicantRepresented() {
        CaseData caseData = CaseData.builder()
            .divorceOrDissolution(DivorceOrDissolution.DIVORCE)
            .isJudicialSeparation(YES)
            .build();

        Applicant applicant = Applicant.builder()
            .solicitorRepresented(YES)
            .build();

        final DocumentType documentType = conditionalOrderCommonContent.getCoverLetterDocumentType(caseData, applicant, false);
        final String documentTemplateId = conditionalOrderCommonContent.getCoverLetterDocumentTemplateId(caseData, applicant, false);

        assertThat(documentType).isEqualTo(DocumentType.JUDICIAL_SEPARATION_ORDER_REFUSAL_SOLICITOR_COVER_LETTER);
        assertThat(documentTemplateId).isEqualTo(JUDICIAL_SEPARATION_ORDER_REFUSAL_SOLICITOR_COVER_LETTER_TEMPLATE_ID);
    }

    @Test
    void shouldReturnDocumentTypeAndTemplateIdForSeparationAmendmentWhenApplicantRepresented() {
        CaseData caseData = CaseData.builder()
            .divorceOrDissolution(DivorceOrDissolution.DISSOLUTION)
            .isJudicialSeparation(YES)
            .build();

        Applicant applicant = Applicant.builder()
            .solicitorRepresented(YES)
            .build();

        final DocumentType documentType = conditionalOrderCommonContent.getCoverLetterDocumentType(caseData, applicant, false);
        final String documentTemplateId = conditionalOrderCommonContent.getCoverLetterDocumentTemplateId(caseData, applicant, false);

        assertThat(documentType).isEqualTo(DocumentType.SEPARATION_ORDER_REFUSAL_SOLICITOR_COVER_LETTER);
        assertThat(documentTemplateId).isEqualTo(JUDICIAL_SEPARATION_ORDER_REFUSAL_SOLICITOR_COVER_LETTER_TEMPLATE_ID);
    }

    @Test
    void shouldReturnDocumentTypeAndTemplateIdForJudicialSeparationClarification() {
        CaseData caseData = CaseData.builder()
            .divorceOrDissolution(DivorceOrDissolution.DIVORCE)
            .isJudicialSeparation(YES)
            .build();

        Applicant applicant = Applicant.builder()
            .build();

        final DocumentType documentType = conditionalOrderCommonContent.getCoverLetterDocumentType(caseData, applicant, true);
        final String documentTemplateId = conditionalOrderCommonContent.getCoverLetterDocumentTemplateId(caseData, applicant, true);

        assertThat(documentType).isEqualTo(DocumentType.JUDICIAL_SEPARATION_ORDER_CLARIFICATION_REFUSAL_COVER_LETTER);
        assertThat(documentTemplateId).isEqualTo(JUDICIAL_SEPARATION_ORDER_CLARIFICATION_REFUSAL_COVER_LETTER_TEMPLATE_ID);
    }

    @Test
    void shouldReturnDocumentTypeAndTemplateIdForSeparationClarification() {
        CaseData caseData = CaseData.builder()
            .divorceOrDissolution(DivorceOrDissolution.DISSOLUTION)
            .isJudicialSeparation(YES)
            .build();

        Applicant applicant = Applicant.builder()
            .build();

        final DocumentType documentType = conditionalOrderCommonContent.getCoverLetterDocumentType(caseData, applicant, true);
        final String documentTemplateId = conditionalOrderCommonContent.getCoverLetterDocumentTemplateId(caseData, applicant, true);

        assertThat(documentType).isEqualTo(DocumentType.SEPARATION_ORDER_CLARIFICATION_REFUSAL_COVER_LETTER);
        assertThat(documentTemplateId).isEqualTo(JUDICIAL_SEPARATION_ORDER_CLARIFICATION_REFUSAL_COVER_LETTER_TEMPLATE_ID);
    }

    @Test
    void shouldReturnDocumentTypeAndTemplateIdForJudicialSeparationClarificationWhenApplicantRepresented() {
        CaseData caseData = CaseData.builder()
            .divorceOrDissolution(DivorceOrDissolution.DIVORCE)
            .isJudicialSeparation(YES)
            .build();

        Applicant applicant = Applicant.builder()
            .solicitorRepresented(YES)
            .build();

        final DocumentType documentType = conditionalOrderCommonContent.getCoverLetterDocumentType(caseData, applicant, true);
        final String documentTemplateId = conditionalOrderCommonContent.getCoverLetterDocumentTemplateId(caseData, applicant, true);

        assertThat(documentType).isEqualTo(DocumentType.JUDICIAL_SEPARATION_ORDER_CLARIFICATION_REFUSAL_SOLICITOR_COVER_LETTER);
        assertThat(documentTemplateId)
            .isEqualTo(JUDICIAL_SEPARATION_ORDER_CLARIFICATION_REFUSAL_SOLICITOR_COVER_LETTER_TEMPLATE_ID);
    }

    @Test
    void shouldReturnDocumentTypeAndTemplateIdForSeparationClarificationWhenApplicantRepresented() {
        CaseData caseData = CaseData.builder()
            .divorceOrDissolution(DivorceOrDissolution.DISSOLUTION)
            .isJudicialSeparation(YES)
            .build();

        Applicant applicant = Applicant.builder()
            .solicitorRepresented(YES)
            .build();

        final DocumentType documentType = conditionalOrderCommonContent.getCoverLetterDocumentType(caseData, applicant, true);
        final String documentTemplateId = conditionalOrderCommonContent.getCoverLetterDocumentTemplateId(caseData, applicant, true);

        assertThat(documentType).isEqualTo(DocumentType.SEPARATION_ORDER_CLARIFICATION_REFUSAL_SOLICITOR_COVER_LETTER);
        assertThat(documentTemplateId)
            .isEqualTo(JUDICIAL_SEPARATION_ORDER_CLARIFICATION_REFUSAL_SOLICITOR_COVER_LETTER_TEMPLATE_ID);
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
