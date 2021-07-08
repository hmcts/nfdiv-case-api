package uk.gov.hmcts.divorce.document;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution;
import uk.gov.hmcts.divorce.document.content.DocmosisTemplateProvider;
import uk.gov.hmcts.divorce.document.model.DocAssemblyRequest;
import uk.gov.hmcts.divorce.document.model.DocAssemblyResponse;
import uk.gov.hmcts.divorce.document.model.DocumentInfo;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import static feign.Request.HttpMethod.POST;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.document.DocumentConstants.DIVORCE_MINI_DRAFT_APPLICATION;
import static uk.gov.hmcts.divorce.document.DocumentConstants.DIVORCE_MINI_DRAFT_APPLICATION_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ENGLISH_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_MIDDLE_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;

@ExtendWith(MockitoExtension.class)
public class DocAssemblyServiceTest {

    private static final String DOC_STORE_BASE_URL_PATH = "http://localhost:4200/assets/";
    private static final String BINARY = "/binary";
    private static final String DRAFT_MINI_APPLICATION_FILENAME = "draft-mini-application-1616591401473378.pdf";

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private DocAssemblyClient docAssemblyClient;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private DocmosisTemplateProvider docmosisTemplateProvider;

    @InjectMocks
    private DocAssemblyService docAssemblyService;

    @Test
    public void shouldGenerateAndStoreDraftApplicationAndReturnDocumentUrl() {

        Supplier<Map<String, Object>> templateContentSupplier = HashMap::new;
        Map<String, Object> caseDataMap = expectedCaseData();

        when(docmosisTemplateProvider.templateNameFor(DIVORCE_MINI_DRAFT_APPLICATION, ENGLISH)).thenReturn(ENGLISH_TEMPLATE_ID);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        DocAssemblyRequest docAssemblyRequest =
            DocAssemblyRequest
                .builder()
                .templateId(ENGLISH_TEMPLATE_ID)
                .outputType("PDF")
                .formPayload(objectMapper.valueToTree(caseDataMap))
                .build();

        String documentUuid = UUID.randomUUID().toString();

        DocAssemblyResponse docAssemblyResponse = new DocAssemblyResponse(
            DOC_STORE_BASE_URL_PATH + documentUuid
        );

        when(docAssemblyClient.generateAndStoreDraftApplication(
            TEST_AUTHORIZATION_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            docAssemblyRequest
        )).thenReturn(docAssemblyResponse);

        DocumentInfo documentInfo = docAssemblyService.renderDocument(
            templateContentSupplier,
            TEST_CASE_ID,
            TEST_AUTHORIZATION_TOKEN,
            DIVORCE_MINI_DRAFT_APPLICATION,
            DIVORCE_MINI_DRAFT_APPLICATION_DOCUMENT_NAME,
            ENGLISH
        );

        assertThat(documentInfo.getUrl()).isEqualTo(DOC_STORE_BASE_URL_PATH + documentUuid);
        assertThat(documentInfo.getBinaryUrl()).isEqualTo(DOC_STORE_BASE_URL_PATH + documentUuid + BINARY);
        assertThat(documentInfo.getFilename()).isEqualTo(DRAFT_MINI_APPLICATION_FILENAME);

        verify(authTokenGenerator).generate();
        verify(docAssemblyClient).generateAndStoreDraftApplication(
            TEST_AUTHORIZATION_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            docAssemblyRequest
        );
        verifyNoMoreInteractions(authTokenGenerator, docAssemblyClient);
    }

    @Test
    public void shouldReturn401UnauthorizedExceptionWhenServiceIsNotWhitelistedInDocAssemblyService() {

        Supplier<Map<String, Object>> templateContentSupplier = HashMap::new;
        Map<String, Object> caseDataMap = expectedCaseData();

        byte[] emptyBody = {};
        Request request = Request.create(POST, EMPTY, Map.of(), emptyBody, UTF_8, null);

        when(docmosisTemplateProvider.templateNameFor(DIVORCE_MINI_DRAFT_APPLICATION, ENGLISH)).thenReturn(ENGLISH_TEMPLATE_ID);

        FeignException feignException = FeignException.errorStatus(
            "s2sServiceNotWhitelisted",
            Response.builder()
                .request(request)
                .status(401)
                .headers(Collections.emptyMap())
                .reason("s2s service not whitelisted")
                .build()
        );

        DocAssemblyRequest docAssemblyRequest =
            DocAssemblyRequest
                .builder()
                .templateId(ENGLISH_TEMPLATE_ID)
                .outputType("PDF")
                .formPayload(objectMapper.valueToTree(caseDataMap))
                .build();

        doThrow(feignException)
            .when(docAssemblyClient)
            .generateAndStoreDraftApplication(
                TEST_AUTHORIZATION_TOKEN,
                TEST_SERVICE_AUTH_TOKEN,
                docAssemblyRequest
            );

        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        assertThatThrownBy(() -> docAssemblyService
            .renderDocument(
                templateContentSupplier,
                TEST_CASE_ID,
                TEST_AUTHORIZATION_TOKEN,
                DIVORCE_MINI_DRAFT_APPLICATION,
                DIVORCE_MINI_DRAFT_APPLICATION_DOCUMENT_NAME,
                ENGLISH
            ))
            .isExactlyInstanceOf(FeignException.Unauthorized.class)
            .hasMessageContaining("s2s service not whitelisted");
    }

    public static Map<String, Object> expectedCaseData() {
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("petitionerFirstName", TEST_FIRST_NAME);
        caseDataMap.put("petitionerMiddleName", TEST_MIDDLE_NAME);
        caseDataMap.put("petitionerLastName", TEST_LAST_NAME);
        caseDataMap.put("divorceOrDissolution", DivorceOrDissolution.DIVORCE);
        caseDataMap.put("petitionerEmail", TEST_USER_EMAIL);
        return caseDataMap;
    }
}
