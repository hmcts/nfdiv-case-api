package uk.gov.hmcts.divorce.solicitor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.divorce.common.config.WebMvcConfig;
import uk.gov.hmcts.divorce.document.DocumentIdProvider;
import uk.gov.hmcts.divorce.testutil.DocumentAssemblyUtil;
import uk.gov.hmcts.divorce.testutil.DocumentManagementStoreUtil;
import uk.gov.hmcts.divorce.testutil.IdamUtil;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.when;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;
import static org.skyscreamer.jsonassert.JSONCompareMode.STRICT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.ccd.search.CaseFieldsConstants.APPLICANT_1_EMAIL;
import static uk.gov.hmcts.divorce.ccd.search.CaseFieldsConstants.APPLICANT_1_FIRST_NAME;
import static uk.gov.hmcts.divorce.ccd.search.CaseFieldsConstants.APPLICANT_1_LAST_NAME;
import static uk.gov.hmcts.divorce.ccd.search.CaseFieldsConstants.DIVORCE_COSTS_CLAIM;
import static uk.gov.hmcts.divorce.ccd.search.CaseFieldsConstants.DIVORCE_OR_DISSOLUTION;
import static uk.gov.hmcts.divorce.ccd.search.CaseFieldsConstants.FINANCIAL_ORDER;
import static uk.gov.hmcts.divorce.common.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.solicitor.event.SolicitorUpdate.SOLICITOR_UPDATE;
import static uk.gov.hmcts.divorce.testutil.DocumentAssemblyUtil.DOC_ASSEMBLY_SERVER;
import static uk.gov.hmcts.divorce.testutil.DocumentAssemblyUtil.stubForDocAssembly;
import static uk.gov.hmcts.divorce.testutil.DocumentManagementStoreUtil.DM_STORE_SERVER;
import static uk.gov.hmcts.divorce.testutil.DocumentManagementStoreUtil.stubForDocumentManagement;
import static uk.gov.hmcts.divorce.testutil.IdamUtil.IDAM_SERVER;
import static uk.gov.hmcts.divorce.testutil.IdamUtil.stubForIdamDetails;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTH_HEADER_VALUE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.LANGUAGE_PREFERENCE_WELSH;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SOLICITOR_USER_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.expectedResponse;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ContextConfiguration(initializers = {
    DocumentAssemblyUtil.PropertiesInitializer.class,
    IdamUtil.PropertiesInitializer.class,
    DocumentManagementStoreUtil.PropertiesInitializer.class})
public class SolicitorUpdateTest {

    private static final String SOLICITOR_UPDATE_ABOUT_TO_SUBMIT = "classpath:solicitor-update-about-to-submit-response.json";
    private static final String SERVICE_AUTH_TOKEN = "test-service-auth-token";
    private static final String SOLICITOR_ROLE = "caseworker-divorce-solicitor";
    private static final String DOCUMENT_URL =
        "http://dm-store-aat.service.core-compute-aat.internal/documents/8d2bd0f2-80e9-4b0f-b38d-2c138b243e27";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthTokenGenerator serviceTokenGenerator;

    @MockBean
    private WebMvcConfig webMvcConfig;

    @MockBean
    private DocumentIdProvider documentIdProvider;

    @BeforeAll
    static void setUp() {
        DOC_ASSEMBLY_SERVER.start();
        IDAM_SERVER.start();
        DM_STORE_SERVER.start();
    }

    @AfterAll
    static void tearDown() {
        DOC_ASSEMBLY_SERVER.stop();
        DOC_ASSEMBLY_SERVER.resetAll();

        IDAM_SERVER.stop();
        IDAM_SERVER.resetAll();

        DM_STORE_SERVER.stop();
        DM_STORE_SERVER.resetAll();
    }

    @Test
    void givenValidCaseDataWithNoDraftDocumentWhenAboutToSubmitCallbackIsInvokedCaseDataIsSetCorrectly() throws Exception {

        when(serviceTokenGenerator.generate()).thenReturn(SERVICE_AUTH_TOKEN);
        when(documentIdProvider.documentId()).thenReturn("V2");

        stubForDocAssembly();

        final String jsonStringResponse = mockMvc.perform(MockMvcRequestBuilders.post(ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(callbackRequest(caseDataWithNoDocument(), SOLICITOR_UPDATE)))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk()
            )
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertEquals(jsonStringResponse, expectedResponse(SOLICITOR_UPDATE_ABOUT_TO_SUBMIT), STRICT);
    }

    @Test
    void givenValidCaseDataWithDraftDocumentWhenAboutToSubmitCallbackIsInvokedCaseDataIsSetCorrectly() throws Exception {

        when(serviceTokenGenerator.generate()).thenReturn(SERVICE_AUTH_TOKEN);
        when(documentIdProvider.documentId()).thenReturn("V2");

        stubForDocAssembly();
        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, SOLICITOR_USER_ID, SOLICITOR_ROLE);

        String documentUuid = FilenameUtils.getName(DOCUMENT_URL);
        stubForDocumentManagement(documentUuid, OK);

        when(serviceTokenGenerator.generate()).thenReturn(SERVICE_AUTH_TOKEN);

        final String jsonStringResponse = mockMvc.perform(MockMvcRequestBuilders.post(ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(callbackRequest(caseDataWithDocument(), SOLICITOR_UPDATE)))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk()
            )
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertEquals(jsonStringResponse, expectedResponse(SOLICITOR_UPDATE_ABOUT_TO_SUBMIT), STRICT);
    }

    private Map<String, Object> caseDataWithNoDocument() {
        final Map<String, Object> caseData = new HashMap<>();
        caseData.put(APPLICANT_1_FIRST_NAME, TEST_FIRST_NAME);
        caseData.put(APPLICANT_1_LAST_NAME, TEST_LAST_NAME);
        caseData.put(APPLICANT_1_EMAIL, TEST_USER_EMAIL);
        caseData.put(DIVORCE_OR_DISSOLUTION, DIVORCE);
        caseData.put(DIVORCE_COSTS_CLAIM, YES);
        caseData.put(FINANCIAL_ORDER, NO);
        caseData.put(LANGUAGE_PREFERENCE_WELSH, NO);
        caseData.put("divorceClaimFrom", singletonList("applicant2"));
        caseData.put("divorceUnit", "serviceCentre");
        caseData.put("selectedDivorceCentreSiteId", "AA07");
        return caseData;
    }

    private Map<String, Object> caseDataWithDocument() {
        final Map<String, Object> caseDate = caseDataWithNoDocument();
        final Map<String, Object> documentLink = new HashMap<>();
        documentLink.put("document_url", DOCUMENT_URL);
        documentLink.put("document_filename", "draft-mini-application-1616591401473378.pdf");
        documentLink.put("document_binary_url",
            "http://dm-store-aat.service.core-compute-aat.internal/documents/8d2bd0f2-80e9-4b0f-b38d-2c138b243e27/binary");

        final Map<String, Object> value = new HashMap<>();
        value.put("documentDateAdded", null);
        value.put("documentComment", null);
        value.put("documentFileName", "draft-mini-application-1616591401473378.pdf");
        value.put("documentType", "divorceApplication");
        value.put("documentEmailContent", null);
        value.put("documentLink", documentLink);

        final Map<String, Object> documentsGenerated = new HashMap<>();
        documentsGenerated.put("id", "V1");
        documentsGenerated.put("value", value);

        caseDate.put("documentsGenerated", singletonList(documentsGenerated));
        return caseDate;
    }
}
