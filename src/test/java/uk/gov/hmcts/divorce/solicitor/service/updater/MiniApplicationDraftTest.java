package uk.gov.hmcts.divorce.solicitor.service.updater;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.updater.CaseDataContext;
import uk.gov.hmcts.divorce.divorcecase.updater.CaseDataUpdaterChain;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.DraftApplicationTemplateContent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.document.DocumentConstants.DIVORCE_MINI_DRAFT_APPLICATION;
import static uk.gov.hmcts.divorce.document.DocumentConstants.DIVORCE_MINI_DRAFT_APPLICATION_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.model.DocumentType.DIVORCE_APPLICATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE;

@ExtendWith(MockitoExtension.class)
public class MiniApplicationDraftTest {

    @Mock
    private CaseDataDocumentService caseDataDocumentService;

    @Mock
    private DraftApplicationTemplateContent templateContent;

    @Mock
    private CaseDataUpdaterChain caseDataUpdaterChain;

    @InjectMocks
    private MiniApplicationDraft miniApplicationDraft;

    @Test
    void shouldCallDocAssemblyServiceAndReturnCaseDataWithMiniDraftApplicationDocument() {

        final var caseData = CaseData.builder()
            .applicant1(Applicant.builder()
                .languagePreferenceWelsh(NO)
                .build())
            .build();
        final var caseDataContext = caseDataContext(caseData);
        final Supplier<Map<String, Object>> templateContentSupplier = HashMap::new;

        when(templateContent.apply(caseData, TEST_CASE_ID, LOCAL_DATE)).thenReturn(templateContentSupplier);
        when(caseDataDocumentService
            .renderDocumentAndUpdateCaseData(
                caseData,
                DIVORCE_APPLICATION,
                templateContentSupplier,
                TEST_CASE_ID,
                TEST_AUTHORIZATION_TOKEN,
                DIVORCE_MINI_DRAFT_APPLICATION,
                DIVORCE_MINI_DRAFT_APPLICATION_DOCUMENT_NAME,
                ENGLISH))
            .thenReturn(caseData);

        when(caseDataUpdaterChain.processNext(caseDataContext)).thenReturn(caseDataContext);

        final var result = miniApplicationDraft.updateCaseData(caseDataContext, caseDataUpdaterChain);

        assertThat(result.getCaseData()).isEqualTo(caseData);
    }

    private CaseDataContext caseDataContext(CaseData caseData) {
        return CaseDataContext
            .builder()
            .caseData(caseData)
            .caseId(TEST_CASE_ID)
            .createdDate(LOCAL_DATE)
            .userAuthToken(TEST_AUTHORIZATION_TOKEN)
            .build();
    }
}
