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
import uk.gov.hmcts.divorce.common.config.DocmosisTemplatesConfig;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.updater.CaseDataContext;
import uk.gov.hmcts.divorce.common.updater.CaseDataUpdaterChain;
import uk.gov.hmcts.divorce.document.DocAssemblyService;
import uk.gov.hmcts.divorce.document.DocumentIdProvider;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.model.DocumentInfo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static feign.Request.HttpMethod.POST;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.common.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.common.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.document.DocumentConstants.DIVORCE_MINI_APPLICATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.DIVORCE_APPLICATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ENGLISH_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.WELSH_TEMPLATE_ID;
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
    private DocmosisTemplatesConfig docmosisTemplatesConfig;

    @Mock
    private DocumentIdProvider documentIdProvider;

    @Mock
    private CaseDataUpdaterChain caseDataUpdaterChain;

    @InjectMocks
    private MiniApplicationDraft miniApplicationDraft;

    @Test
    void shouldReturnDocumentInfoWhenDocumentIsStoredAndGeneratedSuccessfullyForEnglishLanguage() {

        final var caseData = caseData();
        caseData.setLanguagePreferenceWelsh(NO);

        final var caseDataContext = caseDataContext(caseData);
        final var id = "doc id";

        when(docAssemblyService.renderDocument(
            caseData,
            TEST_CASE_ID,
            LOCAL_DATE,
            TEST_AUTHORIZATION_TOKEN,
            ENGLISH_TEMPLATE_ID
        )).thenReturn(documentInfo());

        mockDocmosisTemplateConfig();

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
    void shouldReturnDocumentInfoWhenDocumentIsStoredAndGeneratedSuccessfullyForWelshLanguage() {
        final var caseData = caseData();
        caseData.setLanguagePreferenceWelsh(YES);

        final var caseDataContext = caseDataContext(caseData);
        final var id = "doc id";

        when(docAssemblyService
            .renderDocument(
                caseData,
                TEST_CASE_ID,
                LOCAL_DATE,
                TEST_AUTHORIZATION_TOKEN,
                WELSH_TEMPLATE_ID))
            .thenReturn(documentInfo());

        mockDocmosisTemplateConfig();

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
    void shouldReturnDocumentInfoIfOtherDocumentsAlreadySet() {

        final DivorceDocument originalDocument = mock(DivorceDocument.class);
        final String originalId = "original Id";
        final String id = "doc id";

        final var caseData = caseData();
        final List<ListValue<DivorceDocument>> documents = new ArrayList<>();
        documents.add(new ListValue<>(originalId, originalDocument));
        caseData.setDocumentsGenerated(documents);
        caseData.setLanguagePreferenceWelsh(NO);

        final var caseDataContext = caseDataContext(caseData);

        when(docAssemblyService.renderDocument(
            caseData,
            TEST_CASE_ID,
            LOCAL_DATE,
            TEST_AUTHORIZATION_TOKEN,
            ENGLISH_TEMPLATE_ID
        )).thenReturn(documentInfo());

        mockDocmosisTemplateConfig();

        when(caseDataUpdaterChain.processNext(caseDataContext)).thenReturn(caseDataContext);
        when(documentIdProvider.documentId()).thenReturn(id);

        final var result = miniApplicationDraft.updateCaseData(caseDataContext, caseDataUpdaterChain);
        assertThat(result.getCaseData().getDocumentsGenerated()).hasSize(2);

        final ListValue<DivorceDocument> originalDocumentListValue = result.getCaseData().getDocumentsGenerated().get(0);
        final ListValue<DivorceDocument> documentListValue = result.getCaseData().getDocumentsGenerated().get(1);

        assertThat(originalDocumentListValue.getId()).isEqualTo(originalId);
        assertThat(originalDocumentListValue.getValue()).isEqualTo(originalDocument);

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

        mockDocmosisTemplateConfig();

        CaseData caseData = caseData();
        caseData.setLanguagePreferenceWelsh(NO);

        doThrow(feignException).when(docAssemblyService).renderDocument(
            caseData,
            TEST_CASE_ID,
            LOCAL_DATE,
            TEST_AUTHORIZATION_TOKEN,
            ENGLISH_TEMPLATE_ID
        );

        assertThatThrownBy(() -> miniApplicationDraft.updateCaseData(caseDataContext(caseData), caseDataUpdaterChain))
            .isExactlyInstanceOf(FeignException.Unauthorized.class)
            .hasMessageContaining("s2s service not whitelisted");
    }

    private void mockDocmosisTemplateConfig() {
        when(docmosisTemplatesConfig.getTemplates()).thenReturn(
            Map.of(
                ENGLISH, Map.of(
                    DIVORCE_MINI_APPLICATION, ENGLISH_TEMPLATE_ID
                ),
                WELSH, Map.of(
                    DIVORCE_MINI_APPLICATION, WELSH_TEMPLATE_ID
                )
            )
        );
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
