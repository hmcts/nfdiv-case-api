package uk.gov.hmcts.divorce.document.print.documentpack;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution;
import uk.gov.hmcts.divorce.divorcecase.model.RefusalOption;
import uk.gov.hmcts.divorce.divorcecase.model.SupplementaryCaseType;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CLARIFICATION_REFUSAL_ORDER_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_APPLICANT;
import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_APPLICANT2_SOLICITOR;
import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.JUDICIAL_SEPARATION_ORDER_CLARIFICATION_REFUSAL_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.JUDICIAL_SEPARATION_ORDER_CLARIFICATION_REFUSAL_SOLICITOR_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.JUDICIAL_SEPARATION_ORDER_REFUSAL_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.JUDICIAL_SEPARATION_ORDER_REFUSAL_SOLICITOR_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.REJECTED_REFUSAL_ORDER_COVER_LETTER_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.REJECTED_REFUSAL_ORDER_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.APPLICATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_REFUSAL;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_REFUSAL_COVER_LETTER;
import static uk.gov.hmcts.divorce.document.model.DocumentType.COVERSHEET;
import static uk.gov.hmcts.divorce.document.model.DocumentType.JUDICIAL_SEPARATION_ORDER_CLARIFICATION_REFUSAL_COVER_LETTER;
import static uk.gov.hmcts.divorce.document.model.DocumentType.JUDICIAL_SEPARATION_ORDER_CLARIFICATION_REFUSAL_SOLICITOR_COVER_LETTER;
import static uk.gov.hmcts.divorce.document.model.DocumentType.JUDICIAL_SEPARATION_ORDER_REFUSAL_COVER_LETTER;
import static uk.gov.hmcts.divorce.document.model.DocumentType.JUDICIAL_SEPARATION_ORDER_REFUSAL_SOLICITOR_COVER_LETTER;
import static uk.gov.hmcts.divorce.document.model.DocumentType.SEPARATION_ORDER_CLARIFICATION_REFUSAL_COVER_LETTER;
import static uk.gov.hmcts.divorce.document.model.DocumentType.SEPARATION_ORDER_CLARIFICATION_REFUSAL_SOLICITOR_COVER_LETTER;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant1CaseData;

public class ConditionalOrderRefusalDocumentPackTest {

    private final ConditionalOrderRefusalDocumentPack conditionalOrderRefusalDocumentPack = new ConditionalOrderRefusalDocumentPack();

    @Test
    public void shouldFetchCorrectPackForClarificationJudicialSeparationWithRepresentedApplicant() {
        CaseData data = validApplicant1CaseData();
        data.getApplicant1().setSolicitorRepresented(YesOrNo.YES);
        data.setSupplementaryCaseType(SupplementaryCaseType.JUDICIAL_SEPARATION);
        data.setConditionalOrder(ConditionalOrder.builder().refusalDecision(RefusalOption.MORE_INFO).build());
        data.setDivorceOrDissolution(DivorceOrDissolution.DIVORCE);

        DocumentPackInfo documentPackInfo = conditionalOrderRefusalDocumentPack.getDocumentPack(data, data.getApplicant1());

        DocumentPackInfo expected = new DocumentPackInfo(ImmutableMap.of(
            COVERSHEET, Optional.of(COVERSHEET_APPLICANT2_SOLICITOR),
            JUDICIAL_SEPARATION_ORDER_CLARIFICATION_REFUSAL_SOLICITOR_COVER_LETTER,
            Optional.of(JUDICIAL_SEPARATION_ORDER_CLARIFICATION_REFUSAL_SOLICITOR_COVER_LETTER_TEMPLATE_ID),
            CONDITIONAL_ORDER_REFUSAL, Optional.empty(),
            APPLICATION, Optional.empty()
        ), ImmutableMap.of(
            COVERSHEET_APPLICANT2_SOLICITOR, COVERSHEET_DOCUMENT_NAME,
            JUDICIAL_SEPARATION_ORDER_CLARIFICATION_REFUSAL_SOLICITOR_COVER_LETTER_TEMPLATE_ID,
            REJECTED_REFUSAL_ORDER_COVER_LETTER_DOCUMENT_NAME
        ));

        assertThat(documentPackInfo).isEqualTo(expected);
    }

    @Test
    public void shouldFetchCorrectPackForClarificationSeparationOrderWithRepresentedApplicant() {
        CaseData data = validApplicant1CaseData();
        data.getApplicant1().setSolicitorRepresented(YesOrNo.YES);
        data.setSupplementaryCaseType(SupplementaryCaseType.JUDICIAL_SEPARATION);
        data.setConditionalOrder(ConditionalOrder.builder().refusalDecision(RefusalOption.MORE_INFO).build());
        data.setDivorceOrDissolution(DivorceOrDissolution.DISSOLUTION);

        DocumentPackInfo documentPackInfo = conditionalOrderRefusalDocumentPack.getDocumentPack(data, data.getApplicant1());

        DocumentPackInfo expected = new DocumentPackInfo(ImmutableMap.of(
            COVERSHEET, Optional.of(COVERSHEET_APPLICANT2_SOLICITOR),
            SEPARATION_ORDER_CLARIFICATION_REFUSAL_SOLICITOR_COVER_LETTER,
            Optional.of(JUDICIAL_SEPARATION_ORDER_CLARIFICATION_REFUSAL_SOLICITOR_COVER_LETTER_TEMPLATE_ID),
            CONDITIONAL_ORDER_REFUSAL, Optional.empty(),
            APPLICATION, Optional.empty()
        ), ImmutableMap.of(
            COVERSHEET_APPLICANT2_SOLICITOR, COVERSHEET_DOCUMENT_NAME,
            JUDICIAL_SEPARATION_ORDER_CLARIFICATION_REFUSAL_SOLICITOR_COVER_LETTER_TEMPLATE_ID,
            REJECTED_REFUSAL_ORDER_COVER_LETTER_DOCUMENT_NAME
        ));

        assertThat(documentPackInfo).isEqualTo(expected);
    }

    @Test
    public void shouldFetchCorrectPackForClarificationJudicialSeparationWithUnrepresentedApplicant() {
        CaseData data = validApplicant1CaseData();
        data.getApplicant1().setSolicitorRepresented(YesOrNo.NO);
        data.setSupplementaryCaseType(SupplementaryCaseType.JUDICIAL_SEPARATION);
        data.setConditionalOrder(ConditionalOrder.builder().refusalDecision(RefusalOption.MORE_INFO).build());
        data.setDivorceOrDissolution(DivorceOrDissolution.DIVORCE);

        DocumentPackInfo documentPackInfo = conditionalOrderRefusalDocumentPack.getDocumentPack(data, data.getApplicant1());

        DocumentPackInfo expected = new DocumentPackInfo(ImmutableMap.of(
            COVERSHEET, Optional.of(COVERSHEET_APPLICANT),
            JUDICIAL_SEPARATION_ORDER_CLARIFICATION_REFUSAL_COVER_LETTER,
            Optional.of(JUDICIAL_SEPARATION_ORDER_CLARIFICATION_REFUSAL_COVER_LETTER_TEMPLATE_ID),
            CONDITIONAL_ORDER_REFUSAL, Optional.empty(),
            APPLICATION, Optional.empty()
        ), ImmutableMap.of(
            COVERSHEET_APPLICANT, COVERSHEET_DOCUMENT_NAME,
            JUDICIAL_SEPARATION_ORDER_CLARIFICATION_REFUSAL_COVER_LETTER_TEMPLATE_ID, REJECTED_REFUSAL_ORDER_COVER_LETTER_DOCUMENT_NAME
        ));

        assertThat(documentPackInfo).isEqualTo(expected);
    }

    @Test
    public void shouldFetchCorrectPackForClarificationSeparationOrderWithUnrepresentedApplicant() {
        CaseData data = validApplicant1CaseData();
        data.getApplicant1().setSolicitorRepresented(YesOrNo.NO);
        data.setSupplementaryCaseType(SupplementaryCaseType.JUDICIAL_SEPARATION);
        data.setConditionalOrder(ConditionalOrder.builder().refusalDecision(RefusalOption.MORE_INFO).build());
        data.setDivorceOrDissolution(DivorceOrDissolution.DISSOLUTION);

        DocumentPackInfo documentPackInfo = conditionalOrderRefusalDocumentPack.getDocumentPack(data, data.getApplicant1());

        DocumentPackInfo expected = new DocumentPackInfo(ImmutableMap.of(
            COVERSHEET, Optional.of(COVERSHEET_APPLICANT),
            SEPARATION_ORDER_CLARIFICATION_REFUSAL_COVER_LETTER,
            Optional.of(JUDICIAL_SEPARATION_ORDER_CLARIFICATION_REFUSAL_COVER_LETTER_TEMPLATE_ID),
            CONDITIONAL_ORDER_REFUSAL, Optional.empty(),
            APPLICATION, Optional.empty()
        ), ImmutableMap.of(
            COVERSHEET_APPLICANT, COVERSHEET_DOCUMENT_NAME,
            JUDICIAL_SEPARATION_ORDER_CLARIFICATION_REFUSAL_COVER_LETTER_TEMPLATE_ID, REJECTED_REFUSAL_ORDER_COVER_LETTER_DOCUMENT_NAME
        ));

        assertThat(documentPackInfo).isEqualTo(expected);
    }

    @Test
    public void shouldFetchCorrectPackForClarificationDivorceWithUnrepresentedApplicant() {
        CaseData data = validApplicant1CaseData();
        data.getApplicant1().setSolicitorRepresented(YesOrNo.NO);
        data.setSupplementaryCaseType(SupplementaryCaseType.NA);
        data.setConditionalOrder(ConditionalOrder.builder().refusalDecision(RefusalOption.MORE_INFO).build());
        data.setDivorceOrDissolution(DivorceOrDissolution.DIVORCE);

        DocumentPackInfo documentPackInfo = conditionalOrderRefusalDocumentPack.getDocumentPack(data, data.getApplicant1());

        DocumentPackInfo expected = new DocumentPackInfo(ImmutableMap.of(
                COVERSHEET, Optional.of(COVERSHEET_APPLICANT),
                CONDITIONAL_ORDER_REFUSAL_COVER_LETTER, Optional.of(CLARIFICATION_REFUSAL_ORDER_COVER_LETTER_TEMPLATE_ID),
                CONDITIONAL_ORDER_REFUSAL, Optional.empty()),
            ImmutableMap.of(
                COVERSHEET_APPLICANT, COVERSHEET_DOCUMENT_NAME,
                CLARIFICATION_REFUSAL_ORDER_COVER_LETTER_TEMPLATE_ID, REJECTED_REFUSAL_ORDER_COVER_LETTER_DOCUMENT_NAME
            ));

        assertThat(documentPackInfo).isEqualTo(expected);
    }

    @Test
    public void shouldFetchCorrectPackForAmendmentJudicialSeparationWithRepresentedApplicant() {
        CaseData data = validApplicant1CaseData();
        data.getApplicant1().setSolicitorRepresented(YesOrNo.YES);
        data.setSupplementaryCaseType(SupplementaryCaseType.JUDICIAL_SEPARATION);
        data.setConditionalOrder(ConditionalOrder.builder().refusalDecision(RefusalOption.REJECT).build());
        data.setDivorceOrDissolution(DivorceOrDissolution.DIVORCE);

        DocumentPackInfo documentPackInfo = conditionalOrderRefusalDocumentPack.getDocumentPack(data, data.getApplicant1());

        DocumentPackInfo expected = new DocumentPackInfo(ImmutableMap.of(
            COVERSHEET, Optional.of(COVERSHEET_APPLICANT2_SOLICITOR),
            JUDICIAL_SEPARATION_ORDER_REFUSAL_COVER_LETTER,
            Optional.of(JUDICIAL_SEPARATION_ORDER_REFUSAL_SOLICITOR_COVER_LETTER_TEMPLATE_ID),
            CONDITIONAL_ORDER_REFUSAL, Optional.empty(),
            APPLICATION, Optional.empty()
        ), ImmutableMap.of(
            COVERSHEET_APPLICANT2_SOLICITOR, COVERSHEET_DOCUMENT_NAME,
            JUDICIAL_SEPARATION_ORDER_REFUSAL_SOLICITOR_COVER_LETTER_TEMPLATE_ID, REJECTED_REFUSAL_ORDER_COVER_LETTER_DOCUMENT_NAME
        ));

        assertThat(documentPackInfo).isEqualTo(expected);
    }

    @Test
    public void shouldFetchCorrectPackForAmendmentSeparationOrderWithRepresentedApplicant() {
        CaseData data = validApplicant1CaseData();
        data.getApplicant1().setSolicitorRepresented(YesOrNo.YES);
        data.setSupplementaryCaseType(SupplementaryCaseType.JUDICIAL_SEPARATION);
        data.setConditionalOrder(ConditionalOrder.builder().refusalDecision(RefusalOption.REJECT).build());
        data.setDivorceOrDissolution(DivorceOrDissolution.DISSOLUTION);

        DocumentPackInfo documentPackInfo = conditionalOrderRefusalDocumentPack.getDocumentPack(data, data.getApplicant1());

        DocumentPackInfo expected = new DocumentPackInfo(ImmutableMap.of(
            COVERSHEET, Optional.of(COVERSHEET_APPLICANT2_SOLICITOR),
            JUDICIAL_SEPARATION_ORDER_REFUSAL_SOLICITOR_COVER_LETTER,
            Optional.of(JUDICIAL_SEPARATION_ORDER_REFUSAL_SOLICITOR_COVER_LETTER_TEMPLATE_ID),
            CONDITIONAL_ORDER_REFUSAL, Optional.empty(),
            APPLICATION, Optional.empty()
        ), ImmutableMap.of(
            COVERSHEET_APPLICANT2_SOLICITOR, COVERSHEET_DOCUMENT_NAME,
            JUDICIAL_SEPARATION_ORDER_REFUSAL_SOLICITOR_COVER_LETTER_TEMPLATE_ID, REJECTED_REFUSAL_ORDER_COVER_LETTER_DOCUMENT_NAME
        ));

        assertThat(documentPackInfo).isEqualTo(expected);
    }

    @Test
    public void shouldFetchCorrectPackForAmendmentJudicialSeparationWithUnrepresentedApplicant() {
        CaseData data = validApplicant1CaseData();
        data.getApplicant1().setSolicitorRepresented(YesOrNo.NO);
        data.setSupplementaryCaseType(SupplementaryCaseType.JUDICIAL_SEPARATION);
        data.setConditionalOrder(ConditionalOrder.builder().refusalDecision(RefusalOption.REJECT).build());
        data.setDivorceOrDissolution(DivorceOrDissolution.DIVORCE);

        DocumentPackInfo documentPackInfo = conditionalOrderRefusalDocumentPack.getDocumentPack(data, data.getApplicant1());

        DocumentPackInfo expected = new DocumentPackInfo(
            ImmutableMap.of(
                COVERSHEET, Optional.of(COVERSHEET_APPLICANT),
                JUDICIAL_SEPARATION_ORDER_REFUSAL_COVER_LETTER, Optional.of(JUDICIAL_SEPARATION_ORDER_REFUSAL_COVER_LETTER_TEMPLATE_ID),
                CONDITIONAL_ORDER_REFUSAL, Optional.empty(),
                APPLICATION, Optional.empty()
            ),
            ImmutableMap.of(
                COVERSHEET_APPLICANT, COVERSHEET_DOCUMENT_NAME,
                JUDICIAL_SEPARATION_ORDER_REFUSAL_COVER_LETTER_TEMPLATE_ID, REJECTED_REFUSAL_ORDER_COVER_LETTER_DOCUMENT_NAME));

        assertThat(documentPackInfo).isEqualTo(expected);
    }

    @Test
    public void shouldFetchCorrectPackForAmendmentSeparationOrderWithUnrepresentedApplicant() {
        CaseData data = validApplicant1CaseData();
        data.getApplicant1().setSolicitorRepresented(YesOrNo.NO);
        data.setSupplementaryCaseType(SupplementaryCaseType.JUDICIAL_SEPARATION);
        data.setConditionalOrder(ConditionalOrder.builder().refusalDecision(RefusalOption.REJECT).build());
        data.setDivorceOrDissolution(DivorceOrDissolution.DISSOLUTION);

        DocumentPackInfo documentPackInfo = conditionalOrderRefusalDocumentPack.getDocumentPack(data, data.getApplicant1());

        DocumentPackInfo expected = new DocumentPackInfo(ImmutableMap.of(
            COVERSHEET, Optional.of(COVERSHEET_APPLICANT),
            JUDICIAL_SEPARATION_ORDER_REFUSAL_COVER_LETTER, Optional.of(JUDICIAL_SEPARATION_ORDER_REFUSAL_COVER_LETTER_TEMPLATE_ID),
            CONDITIONAL_ORDER_REFUSAL, Optional.empty(),
            APPLICATION, Optional.empty()
        ), ImmutableMap.of(
            COVERSHEET_APPLICANT, COVERSHEET_DOCUMENT_NAME,
            JUDICIAL_SEPARATION_ORDER_REFUSAL_COVER_LETTER_TEMPLATE_ID, REJECTED_REFUSAL_ORDER_COVER_LETTER_DOCUMENT_NAME
        ));

        assertThat(documentPackInfo).isEqualTo(expected);
    }

    @Test
    public void shouldFetchCorrectPackForAmendmentDivorceWithUnrepresentedApplicant() {
        CaseData data = validApplicant1CaseData();
        data.getApplicant1().setSolicitorRepresented(YesOrNo.NO);
        data.setSupplementaryCaseType(SupplementaryCaseType.NA);
        data.setConditionalOrder(ConditionalOrder.builder().refusalDecision(RefusalOption.REJECT).build());
        data.setDivorceOrDissolution(DivorceOrDissolution.DIVORCE);

        DocumentPackInfo documentPackInfo = conditionalOrderRefusalDocumentPack.getDocumentPack(data, data.getApplicant1());

        DocumentPackInfo expected = new DocumentPackInfo(ImmutableMap.of(
            COVERSHEET, Optional.of(COVERSHEET_APPLICANT),
            CONDITIONAL_ORDER_REFUSAL_COVER_LETTER, Optional.of(REJECTED_REFUSAL_ORDER_COVER_LETTER_TEMPLATE_ID),
            CONDITIONAL_ORDER_REFUSAL, Optional.empty(),
            APPLICATION, Optional.empty()
        ), ImmutableMap.of(
            COVERSHEET_APPLICANT, COVERSHEET_DOCUMENT_NAME,
            REJECTED_REFUSAL_ORDER_COVER_LETTER_TEMPLATE_ID, REJECTED_REFUSAL_ORDER_COVER_LETTER_DOCUMENT_NAME
        ));

        assertThat(documentPackInfo).isEqualTo(expected);
    }
}
