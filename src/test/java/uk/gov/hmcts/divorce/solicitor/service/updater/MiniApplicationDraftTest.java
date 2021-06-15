package uk.gov.hmcts.divorce.solicitor.service.updater;

import feign.FeignException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.updater.CaseDataContext;
import uk.gov.hmcts.divorce.common.updater.CaseDataUpdaterChain;
import uk.gov.hmcts.divorce.document.DocAssemblyService;
import uk.gov.hmcts.divorce.document.DocumentIdProvider;
import uk.gov.hmcts.divorce.document.content.DocmosisTemplateProvider;
import uk.gov.hmcts.divorce.document.content.DraftApplicationTemplateContent;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.model.DocumentInfo;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static feign.Request.HttpMethod.POST;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.divorce.common.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.document.DocumentConstants.DIVORCE_MINI_DRAFT_APPLICATION;
import static uk.gov.hmcts.divorce.document.DocumentConstants.DIVORCE_MINI_DRAFT_APPLICATION_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.model.DocumentType.DIVORCE_APPLICATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ENGLISH_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
public class MiniApplicationDraftTest {

    private static final LocalDateTime LOCAL_DATE_TIME = LocalDateTime.of(2021, 04, 28, 1, 0);
    private static final String DOC_URL = "http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130003";
    private static final String DOC_BINARY_URL = "http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130003/binary";
    private static final String PDF_FILENAME = "draft-mini-application-1616591401473378.pdf";
    private static final String URL = "url";
    private static final String FILENAME = "filename";
    private static final String BINARY_URL = "binaryUrl";

    @Mock
    private DocAssemblyService docAssemblyService;

    @Mock
    private DocmosisTemplateProvider docmosisTemplateProvider;

    @Mock
    private DraftApplicationTemplateContent templateContent;

    @Mock
    private DocumentIdProvider documentIdProvider;

    @Mock
    private CaseDataUpdaterChain caseDataUpdaterChain;

    @InjectMocks
    private MiniApplicationDraft miniApplicationDraft;

    @Test
    void shouldReturnDocumentInfoWhenDocumentIsStoredAndGeneratedSuccessfullyForEnglishLanguage() {

        final var caseData = caseData();
        caseData.getApplicant1().setLanguagePreferenceWelsh(NO);

        final var caseDataContext = caseDataContext(caseData);
        final var id = "doc id";

        final Map<String, Object> templateData = new HashMap<>();

        when(templateContent.apply(caseData, TEST_CASE_ID, LOCAL_DATE)).thenReturn(templateData);
        when(docAssemblyService.renderDocument(
            templateData,
            TEST_CASE_ID,
            TEST_AUTHORIZATION_TOKEN,
            ENGLISH_TEMPLATE_ID,
            DIVORCE_MINI_DRAFT_APPLICATION_DOCUMENT_NAME
        )).thenReturn(documentInfo());

        when(docmosisTemplateProvider.templateNameFor(DIVORCE_MINI_DRAFT_APPLICATION, ENGLISH)).thenReturn(ENGLISH_TEMPLATE_ID);
        when(caseDataUpdaterChain.processNext(caseDataContext)).thenReturn(caseDataContext);
        when(documentIdProvider.documentId()).thenReturn(id);

        final var result = miniApplicationDraft.updateCaseData(caseDataContext, caseDataUpdaterChain);
        assertThat(result.getCaseData().getDocumentsGenerated()).hasSize(1);

        final ListValue<DivorceDocument> documentListValue = result.getCaseData().getDocumentsGenerated().get(0);
        final var divorceDocument = documentListValue.getValue();

        assertThat(documentListValue.getId()).isEqualTo(id);
        assertThat(divorceDocument.getDocumentType()).isEqualTo(DIVORCE_APPLICATION);
        assertThat(divorceDocument
            .getDocumentLink())
            .extracting(URL, FILENAME, BINARY_URL)
            .contains(
                DOC_URL,
                PDF_FILENAME,
                DOC_BINARY_URL);

        verify(documentIdProvider).documentId();
    }
    
    @Test
    void shouldThrow401UnauthorizedExceptionWhenDocumentGenerationFails() {
        byte[] emptyBody = {};
        Request request = Request.create(POST, EMPTY, Map.of(), emptyBody, UTF_8, null);

        FeignException feignException = FeignException.errorStatus(
            "s2sServiceNotWhitelisted",
            Response.builder()
                .request(request)
                .status(401)
                .headers(Collections.emptyMap())
                .reason("s2s service not whitelisted")
                .build()
        );

        when(docmosisTemplateProvider.templateNameFor(DIVORCE_MINI_DRAFT_APPLICATION, ENGLISH)).thenReturn(ENGLISH_TEMPLATE_ID);

        CaseData caseData = caseData();
        caseData.getApplicant1().setLanguagePreferenceWelsh(NO);

        final Map<String, Object> templateData = new HashMap<>();

        when(templateContent.apply(caseData, TEST_CASE_ID, LOCAL_DATE)).thenReturn(templateData);
        doThrow(feignException).when(docAssemblyService).renderDocument(
            templateData,
            TEST_CASE_ID,
            TEST_AUTHORIZATION_TOKEN,
            ENGLISH_TEMPLATE_ID,
            DIVORCE_MINI_DRAFT_APPLICATION_DOCUMENT_NAME
        );

        assertThatThrownBy(() -> miniApplicationDraft.updateCaseData(caseDataContext(caseData), caseDataUpdaterChain))
            .isExactlyInstanceOf(FeignException.Unauthorized.class)
            .hasMessageContaining("s2s service not whitelisted");
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

    private DocumentInfo documentInfo() {
        return new DocumentInfo(
            DOC_URL,
            PDF_FILENAME,
            DOC_BINARY_URL
        );
    }
}
