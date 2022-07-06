package uk.gov.hmcts.divorce.legaladvisor.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.ConditionalOrderRefusalContent;

import java.time.Clock;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.FEMALE;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.RejectionReason.NO_JURISDICTION;
import static uk.gov.hmcts.divorce.document.DocumentConstants.REJECTED_REFUSAL_ORDER_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.content.ConditionalOrderRefusalContent.LEGAL_ADVISOR_COMMENTS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONTACT_DIVORCE_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CP_CASE_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_CIVIL_PARTNERSHIP_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.LAST_NAME;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_REFUSAL_COVER_LETTER;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_JOINT;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
public class GenerateCoRefusedCoverLetterTest {

    @Mock
    private CaseDataDocumentService caseDataDocumentService;

    @Mock
    private ConditionalOrderRefusalContent conditionalOrderRefusalContent;

    @Mock
    private Clock clock;

    @InjectMocks
    private GenerateCoRefusedCoverLetter generateCoRefusedCoverLetter;

    @Test
    void shouldGenerateCoRefusedCoverLetterWithDivorceContent() {
        setMockClock(clock);

        final List<ConditionalOrderRefusalContent.RefusalReason> refusalReasons =
            List.of(new ConditionalOrderRefusalContent.RefusalReason("Court does not have jurisdiction"));

        final Map<String, Object> templateContent = new HashMap<>();
        templateContent.put(CASE_REFERENCE, formatId(TEST_CASE_ID));
        templateContent.put(FIRST_NAME, "Bob");
        templateContent.put(LAST_NAME, "Smith");
        templateContent.put(IS_JOINT, false);
        templateContent.put(LEGAL_ADVISOR_COMMENTS, refusalReasons);
        templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP_EMAIL, CONTACT_DIVORCE_EMAIL);

        CaseData caseData = CaseData.builder()
            .divorceOrDissolution(DIVORCE)
            .applicationType(SOLE_APPLICATION)
            .applicant1(
                Applicant.builder()
                    .firstName("Bob")
                    .lastName("Smith")
                    .build()
            )
            .applicant2(
                Applicant.builder()
                    .gender(FEMALE)
                    .build()
            )
            .conditionalOrder(
                ConditionalOrder.builder()
                    .refusalRejectionReason(Set.of(NO_JURISDICTION))
                    .build()
            )
            .build();

        when(conditionalOrderRefusalContent.generateLegalAdvisorComments(caseData.getConditionalOrder()))
            .thenReturn(refusalReasons);

        generateCoRefusedCoverLetter.generateAndUpdateCaseData(
            caseData,
            TEST_CASE_ID,
            REJECTED_REFUSAL_ORDER_COVER_LETTER_TEMPLATE_ID,
            caseData.getApplicant1()
        );

        verify(caseDataDocumentService).renderDocumentAndUpdateCaseData(
            eq(caseData),
            eq(CONDITIONAL_ORDER_REFUSAL_COVER_LETTER),
            eq(templateContent),
            eq(TEST_CASE_ID),
            eq(REJECTED_REFUSAL_ORDER_COVER_LETTER_TEMPLATE_ID),
            eq(ENGLISH),
            anyString()
        );
    }

    @Test
    void shouldGenerateCoRefusedCoverLetterWithDissolutionContent() {
        setMockClock(clock);

        final List<ConditionalOrderRefusalContent.RefusalReason> refusalReasons =
            List.of(new ConditionalOrderRefusalContent.RefusalReason("Court does not have jurisdiction"));

        final Map<String, Object> templateContent = new HashMap<>();
        templateContent.put(CASE_REFERENCE, formatId(TEST_CASE_ID));
        templateContent.put(FIRST_NAME, "Bob");
        templateContent.put(LAST_NAME, "Smith");
        templateContent.put(IS_JOINT, true);
        templateContent.put(LEGAL_ADVISOR_COMMENTS, refusalReasons);
        templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP_EMAIL, CP_CASE_EMAIL);

        CaseData caseData = CaseData.builder()
            .divorceOrDissolution(DISSOLUTION)
            .applicationType(JOINT_APPLICATION)
            .applicant1(
                Applicant.builder()
                    .firstName("Bob")
                    .lastName("Smith")
                    .build()
            )
            .applicant2(
                Applicant.builder()
                    .gender(FEMALE)
                    .build()
            )
            .conditionalOrder(
                ConditionalOrder.builder()
                    .refusalRejectionReason(Set.of(NO_JURISDICTION))
                    .build()
            )
            .build();

        when(conditionalOrderRefusalContent.generateLegalAdvisorComments(caseData.getConditionalOrder()))
            .thenReturn(refusalReasons);

        generateCoRefusedCoverLetter.generateAndUpdateCaseData(
            caseData,
            TEST_CASE_ID,
            REJECTED_REFUSAL_ORDER_COVER_LETTER_TEMPLATE_ID,
            caseData.getApplicant1()
        );

        verify(caseDataDocumentService).renderDocumentAndUpdateCaseData(
            eq(caseData),
            eq(CONDITIONAL_ORDER_REFUSAL_COVER_LETTER),
            eq(templateContent),
            eq(TEST_CASE_ID),
            eq(REJECTED_REFUSAL_ORDER_COVER_LETTER_TEMPLATE_ID),
            eq(ENGLISH),
            anyString()
        );
    }
}
