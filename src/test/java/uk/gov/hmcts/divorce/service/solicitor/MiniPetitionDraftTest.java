package uk.gov.hmcts.divorce.service.solicitor;

import feign.FeignException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.ccd.model.CaseData;
import uk.gov.hmcts.divorce.ccd.model.DivorceDocument;
import uk.gov.hmcts.divorce.config.DocmosisTemplatesConfig;
import uk.gov.hmcts.divorce.model.DocumentInfo;
import uk.gov.hmcts.divorce.service.DocAssemblyService;
import uk.gov.hmcts.divorce.util.CaseDataContext;
import uk.gov.hmcts.divorce.util.CaseDataUpdaterChain;

import java.util.Collections;
import java.util.Map;

import static feign.Request.HttpMethod.POST;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.TestConstants.ENGLISH_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.TestConstants.WELSH_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.ccd.model.enums.DocumentType.Petition;
import static uk.gov.hmcts.divorce.constants.DocumentConstants.DIVORCE_MINI_PETITION;
import static uk.gov.hmcts.divorce.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.util.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
public class MiniPetitionDraftTest {

    private static final String DOC_URL = "http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130003";
    private static final String DOC_BINARY_URL = "http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130003/binary";
    private static final String PDF_FILENAME = "draft-mini-petition-1616591401473378.pdf";
    private static final String URL = "url";
    private static final String FILENAME = "filename";
    private static final String BINARY_URL = "binaryUrl";

    @Mock
    private DocAssemblyService docAssemblyService;

    @Mock
    private DocmosisTemplatesConfig docmosisTemplatesConfig;

    @Mock
    private CaseDataUpdaterChain caseDataUpdaterChain;

    @InjectMocks
    private MiniPetitionDraft miniPetitionDraft;

    @Test
    void shouldReturnDocumentInfoWhenDocumentIsStoredAndGeneratedSuccessfullyForEnglishLanguage() {
        CaseData caseData = caseData();
        caseData.setLanguagePreferenceWelsh(NO);

        CaseDataContext caseDataContext = caseDataContext(caseData);

        when(docAssemblyService.generateAndStoreDraftPetition(
            caseData,
            TEST_CASE_ID,
            TEST_AUTHORIZATION_TOKEN,
            ENGLISH_TEMPLATE_ID
        )).thenReturn(documentInfo());

        mockDocmosisTemplateConfig();

        when(caseDataUpdaterChain.processNext(caseDataContext)).thenReturn(caseDataContext);

        CaseDataContext result = miniPetitionDraft.updateCaseData(caseDataContext, caseDataUpdaterChain);
        assertThat(result.getCaseData().getDocumentsGenerated()).hasSize(1);

        DivorceDocument divorceDocument = result.getCaseData().getDocumentsGenerated().get(0);

        assertThat(divorceDocument.getDocumentType()).isEqualTo(Petition);
        assertThat(divorceDocument
            .getDocumentLink())
            .extracting(URL, FILENAME, BINARY_URL)
            .contains(
                DOC_URL,
                PDF_FILENAME,
                DOC_BINARY_URL);
    }

    @Test
    void shouldReturnDocumentInfoWhenDocumentIsStoredAndGeneratedSuccessfullyForWelshLanguage() {
        CaseData caseData = caseData();
        caseData.setLanguagePreferenceWelsh(YES);

        CaseDataContext caseDataContext = caseDataContext(caseData);

        DocumentInfo documentInfo = documentInfo();

        when(docAssemblyService
            .generateAndStoreDraftPetition(
                caseData,
                TEST_CASE_ID,
                TEST_AUTHORIZATION_TOKEN,
                WELSH_TEMPLATE_ID))
            .thenReturn(documentInfo);

        mockDocmosisTemplateConfig();

        when(caseDataUpdaterChain.processNext(caseDataContext)).thenReturn(caseDataContext);

        CaseDataContext updatedCaseDataContext = miniPetitionDraft.updateCaseData(caseDataContext, caseDataUpdaterChain);
        assertThat(updatedCaseDataContext.getCaseData().getDocumentsGenerated()).hasSize(1);

        DivorceDocument divorceDocument = updatedCaseDataContext.getCaseData().getDocumentsGenerated().get(0);

        assertThat(divorceDocument.getDocumentType()).isEqualTo(Petition);
        assertThat(divorceDocument
            .getDocumentLink())
            .extracting(URL, FILENAME, BINARY_URL)
            .contains(
                DOC_URL,
                PDF_FILENAME,
                DOC_BINARY_URL);
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

        doThrow(feignException).when(docAssemblyService).generateAndStoreDraftPetition(
            caseData,
            TEST_CASE_ID,
            TEST_AUTHORIZATION_TOKEN,
            ENGLISH_TEMPLATE_ID
        );

        assertThatThrownBy(() -> miniPetitionDraft.updateCaseData(caseDataContext(caseData), caseDataUpdaterChain))
            .isExactlyInstanceOf(FeignException.Unauthorized.class)
            .hasMessageContaining("s2s service not whitelisted");
    }

    private void mockDocmosisTemplateConfig() {
        when(docmosisTemplatesConfig.getTemplates()).thenReturn(
            Map.of(
                ENGLISH, Map.of(
                    DIVORCE_MINI_PETITION, ENGLISH_TEMPLATE_ID
                ),
                WELSH, Map.of(
                    DIVORCE_MINI_PETITION, WELSH_TEMPLATE_ID
                )
            )
        );
    }

    private CaseDataContext caseDataContext(CaseData caseData) {
        return CaseDataContext
            .builder()
            .caseData(caseData)
            .caseId(TEST_CASE_ID)
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
