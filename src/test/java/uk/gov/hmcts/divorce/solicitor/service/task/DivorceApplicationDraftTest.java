package uk.gov.hmcts.divorce.solicitor.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.DraftApplicationTemplateContent;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.document.DocumentConstants.DIVORCE_DRAFT_APPLICATION_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.DIVORCE_JOINT_APPLICANT_1_ANSWERS;
import static uk.gov.hmcts.divorce.document.DocumentConstants.DIVORCE_SOLE_APPLICANT_1_ANSWERS;
import static uk.gov.hmcts.divorce.document.DocumentConstants.JOINT_DIVORCE_APPLICANT_1_ANSWERS_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.model.DocumentType.APPLICATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE_TIME;

@ExtendWith(MockitoExtension.class)
public class DivorceApplicationDraftTest {

    @Mock
    private CaseDataDocumentService caseDataDocumentService;

    @Mock
    private DraftApplicationTemplateContent draftApplicationTemplateContent;

    @InjectMocks
    private DivorceApplicationDraft divorceApplicationDraft;

    @Test
    void shouldCallDocAssemblyServiceWithSoleTemplateAndReturnCaseDataWithMiniDraftApplicationDocument() {

        final var caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .applicant1(Applicant.builder()
                .languagePreferenceWelsh(NO)
                .build())
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);

        final Map<String, Object> templateContent = new HashMap<>();

        when(draftApplicationTemplateContent.apply(caseData, TEST_CASE_ID)).thenReturn(templateContent);

        final var result = divorceApplicationDraft.apply(caseDetails);

        verify(caseDataDocumentService)
            .renderDocumentAndUpdateCaseData(
                caseData,
                APPLICATION,
                templateContent,
                TEST_CASE_ID,
                DIVORCE_SOLE_APPLICANT_1_ANSWERS,
                ENGLISH,
                DIVORCE_DRAFT_APPLICATION_DOCUMENT_NAME + TEST_CASE_ID
            );

        assertThat(result.getData()).isEqualTo(caseData);
    }

    @Test
    void shouldCallDocAssemblyServiceWithJointTemplateAndReturnCaseDataWithMiniDraftApplicationDocument() {

        final var caseData = CaseData.builder()
            .applicationType(JOINT_APPLICATION)
            .applicant1(Applicant.builder()
                .languagePreferenceWelsh(NO)
                .build())
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);

        final Map<String, Object> templateContent = new HashMap<>();

        when(draftApplicationTemplateContent.apply(caseData, TEST_CASE_ID)).thenReturn(templateContent);

        final var result = divorceApplicationDraft.apply(caseDetails);

        verify(caseDataDocumentService)
            .renderDocumentAndUpdateCaseData(
                caseData,
                APPLICATION,
                templateContent,
                TEST_CASE_ID,
                DIVORCE_JOINT_APPLICANT_1_ANSWERS,
                ENGLISH,
                JOINT_DIVORCE_APPLICANT_1_ANSWERS_DOCUMENT_NAME + TEST_CASE_ID
            );

        assertThat(result.getData()).isEqualTo(caseData);
    }
}
