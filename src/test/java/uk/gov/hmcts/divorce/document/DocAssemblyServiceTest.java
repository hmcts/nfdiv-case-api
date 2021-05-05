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
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.document.content.DraftPetitionTemplateContent;
import uk.gov.hmcts.divorce.document.model.DocAssemblyRequest;
import uk.gov.hmcts.divorce.document.model.DocAssemblyResponse;
import uk.gov.hmcts.divorce.document.model.DocumentInfo;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static feign.Request.HttpMethod.POST;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ENGLISH_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseDataMap;

@ExtendWith(MockitoExtension.class)
public class DocAssemblyServiceTest {

    private static final String DOC_STORE_BASE_URL_PATH = "http://localhost:4200/assets/";
    private static final String BINARY = "/binary";
    private static final String DRAFT_MINI_PETITION_FILENAME = "draft-mini-petition-1616591401473378.pdf";

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private DocAssemblyClient docAssemblyClient;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private DraftPetitionTemplateContent petitionTemplateMapper;

    @InjectMocks
    private DocAssemblyService docAssemblyService;

    @Test
    public void shouldGenerateAndStoreDraftPetitionAndReturnDocumentUrl() {
        CaseData caseData = caseData();
        Map<String, Object> caseDataMap = caseDataMap();

        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(petitionTemplateMapper.apply(caseData, TEST_CASE_ID, LOCAL_DATE)).thenReturn(caseDataMap);

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

        when(docAssemblyClient.generateAndStoreDraftPetition(
            TEST_AUTHORIZATION_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            docAssemblyRequest
        )).thenReturn(docAssemblyResponse);

        DocumentInfo documentInfo = docAssemblyService.renderDocument(
            caseData(),
            TEST_CASE_ID,
            LOCAL_DATE,
            TEST_AUTHORIZATION_TOKEN,
            ENGLISH_TEMPLATE_ID
        );

        assertThat(documentInfo.getUrl()).isEqualTo(DOC_STORE_BASE_URL_PATH + documentUuid);
        assertThat(documentInfo.getBinaryUrl()).isEqualTo(DOC_STORE_BASE_URL_PATH + documentUuid + BINARY);
        assertThat(documentInfo.getFilename()).isEqualTo(DRAFT_MINI_PETITION_FILENAME);

        verify(authTokenGenerator).generate();
        verify(docAssemblyClient).generateAndStoreDraftPetition(
            TEST_AUTHORIZATION_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            docAssemblyRequest
        );
        verify(petitionTemplateMapper).apply(caseData, TEST_CASE_ID, LOCAL_DATE);
        verifyNoMoreInteractions(authTokenGenerator, docAssemblyClient, petitionTemplateMapper);
    }

    @Test
    public void shouldReturn401UnauthorizedExceptionWhenServiceIsNotWhitelistedInDocAssemblyService() {
        CaseData caseData = caseData();
        Map<String, Object> caseDataMap = caseDataMap();

        when(petitionTemplateMapper.apply(caseData, TEST_CASE_ID, LOCAL_DATE)).thenReturn(caseDataMap);

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


        DocAssemblyRequest docAssemblyRequest =
            DocAssemblyRequest
                .builder()
                .templateId(ENGLISH_TEMPLATE_ID)
                .outputType("PDF")
                .formPayload(objectMapper.valueToTree(caseDataMap))
                .build();

        doThrow(feignException)
            .when(docAssemblyClient)
            .generateAndStoreDraftPetition(
                TEST_AUTHORIZATION_TOKEN,
                TEST_SERVICE_AUTH_TOKEN,
                docAssemblyRequest
            );

        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        assertThatThrownBy(() -> docAssemblyService
            .renderDocument(
                caseData(),
                TEST_CASE_ID,
                LOCAL_DATE,
                TEST_AUTHORIZATION_TOKEN,
                ENGLISH_TEMPLATE_ID
            ))
            .isExactlyInstanceOf(FeignException.Unauthorized.class)
            .hasMessageContaining("s2s service not whitelisted");
    }
}
