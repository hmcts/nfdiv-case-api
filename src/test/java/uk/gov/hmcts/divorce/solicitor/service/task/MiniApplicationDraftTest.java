package uk.gov.hmcts.divorce.solicitor.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.document.DocumentConstants.DIVORCE_MINI_DRAFT_APPLICATION;
import static uk.gov.hmcts.divorce.document.DocumentConstants.DIVORCE_MINI_DRAFT_APPLICATION_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.model.DocumentType.DIVORCE_APPLICATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE_TIME;

@ExtendWith(MockitoExtension.class)
public class MiniApplicationDraftTest {

    @Mock
    private CaseDataDocumentService caseDataDocumentService;

    @Mock
    private DraftApplicationTemplateContent templateContent;

    @InjectMocks
    private MiniApplicationDraft miniApplicationDraft;

    @SuppressWarnings("unchecked")
    @Test
    void shouldCallDocAssemblyServiceAndReturnCaseDataWithMiniDraftApplicationDocument() {

        final var caseData = CaseData.builder()
            .applicant1(Applicant.builder()
                .languagePreferenceWelsh(NO)
                .build())
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);

        final Supplier<Map<String, Object>> templateContentSupplier = HashMap::new;

        when(templateContent.apply(caseData, TEST_CASE_ID, LOCAL_DATE)).thenReturn(templateContentSupplier);

        final var result = miniApplicationDraft.apply(caseDetails);

        final ArgumentCaptor<Supplier<String>> filename = ArgumentCaptor.forClass(Supplier.class);
        verify(caseDataDocumentService)
            .renderDocumentAndUpdateCaseData(
                eq(caseData),
                eq(DIVORCE_APPLICATION),
                eq(templateContentSupplier),
                eq(TEST_CASE_ID),
                eq(DIVORCE_MINI_DRAFT_APPLICATION),
                eq(ENGLISH),
                filename.capture()
            );

        assertThat(filename.getValue().get()).isEqualTo(DIVORCE_MINI_DRAFT_APPLICATION_DOCUMENT_NAME + TEST_CASE_ID);
        assertThat(result.getData()).isEqualTo(caseData);
    }
}
