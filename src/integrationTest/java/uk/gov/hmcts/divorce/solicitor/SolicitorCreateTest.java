package uk.gov.hmcts.divorce.solicitor;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
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
import uk.gov.hmcts.divorce.common.model.DivorceOrDissolution;
import uk.gov.hmcts.divorce.document.DocumentIdProvider;
import uk.gov.hmcts.divorce.testutil.DocumentAssemblyUtil;
import uk.gov.hmcts.divorce.testutil.PrdOrganisationUtil;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;
import static org.skyscreamer.jsonassert.JSONCompareMode.STRICT;
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
import static uk.gov.hmcts.divorce.solicitor.event.SolicitorCreate.SOLICITOR_CREATE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.APPLICANT_1_ORGANISATION_POLICY;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTH_HEADER_VALUE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.LANGUAGE_PREFERENCE_WELSH;
import static uk.gov.hmcts.divorce.testutil.TestConstants.MID_EVENT_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_ORG_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.organisationPolicy;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.expectedResponse;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ContextConfiguration(initializers = {
    PrdOrganisationUtil.PropertiesInitializer.class,
    DocumentAssemblyUtil.PropertiesInitializer.class})
class SolicitorCreateTest {

    private static final String SOLICITOR_CREATE_ABOUT_TO_SUBMIT = "classpath:solicitor-create-about-to-submit-response.json";
    private static final String SOLICITOR_CREATE_MID_EVENT = "classpath:solicitor-create-mid-event-response.json";
    private static final String SOLICITOR_CREATE_MID_EVENT_ERROR = "classpath:solicitor-create-mid-event-error-response.json";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PrdOrganisationUtil prdOrganisationUtil;

    @Autowired
    private DocumentAssemblyUtil documentAssemblyUtil;

    @MockBean
    private AuthTokenGenerator serviceTokenGenerator;

    @MockBean
    private WebMvcConfig webMvcConfig;

    @MockBean
    private DocumentIdProvider documentIdProvider;

    @BeforeAll
    static void setUp() {
        DocumentAssemblyUtil.start();
        PrdOrganisationUtil.start();
    }

    @AfterAll
    static void tearDown() {
        DocumentAssemblyUtil.stopAndReset();
        PrdOrganisationUtil.stopAndReset();
    }

    @Test
    void givenValidCaseDataWhenAboutToSubmitCallbackIsInvokedCaseDataIsSetCorrectly() throws Exception {

        when(serviceTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
        when(documentIdProvider.documentId()).thenReturn("Divorce application");

        documentAssemblyUtil.stubForDocAssembly();

        final var jsonStringResponse = mockMvc.perform(MockMvcRequestBuilders.post(ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(callbackRequest(caseData(), SOLICITOR_CREATE)))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk()
            )
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertEquals(jsonStringResponse, expectedResponse(SOLICITOR_CREATE_ABOUT_TO_SUBMIT), STRICT);
    }

    @Test
    void shouldValidateApplicant1SolicitorOrgAndReturnNoErrorsWhenSolicitorBelongsToSelectedOrg() throws Exception {
        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        prdOrganisationUtil.stubGetOrganisationEndpoint(TEST_ORG_ID);

        final var jsonStringResponse = mockMvc.perform(MockMvcRequestBuilders.post(MID_EVENT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(callbackRequest(caseDataWithApplicant1Org(), SOLICITOR_CREATE)))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk()
            )
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertEquals(jsonStringResponse, expectedResponse(SOLICITOR_CREATE_MID_EVENT), STRICT);
    }

    @Test
    public void shouldValidateApplicant1SolicitorOrgAndReturnErrorWhenSolicitorDoesNotBelongsToSelectedOrg() throws Exception {
        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        prdOrganisationUtil.stubGetOrganisationEndpoint("TESTORG123");

        final var jsonStringResponse = mockMvc.perform(MockMvcRequestBuilders.post(MID_EVENT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(callbackRequest(caseDataWithApplicant1Org(), SOLICITOR_CREATE)))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk()
            )
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertEquals(jsonStringResponse, expectedResponse(SOLICITOR_CREATE_MID_EVENT_ERROR), STRICT);
    }

    @Test
    public void shouldThrow403ForbiddenExceptionWhenServiceIsNotWhitelistedInReferenceData() throws Exception {
        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        prdOrganisationUtil.stubGetOrganisationEndpointForFailure();

        mockMvc.perform(MockMvcRequestBuilders.post(MID_EVENT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(callbackRequest(caseDataWithApplicant1Org(), SOLICITOR_CREATE)))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isForbidden()
            )
            .andExpect(
                result -> assertThat(result.getResolvedException()).isExactlyInstanceOf(FeignException.Forbidden.class)
            );
    }

    private Map<String, Object> caseDataWithApplicant1Org() {
        Map<String, Object> caseData = caseData();
        caseData.put(APPLICANT_1_ORGANISATION_POLICY, organisationPolicy());

        return caseData;
    }

    private Map<String, Object> caseData() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(APPLICANT_1_FIRST_NAME, TEST_FIRST_NAME);
        caseData.put(APPLICANT_1_LAST_NAME, TEST_LAST_NAME);
        caseData.put(APPLICANT_1_EMAIL, TEST_USER_EMAIL);
        caseData.put(DIVORCE_OR_DISSOLUTION, DivorceOrDissolution.DIVORCE);
        caseData.put(DIVORCE_COSTS_CLAIM, YES);
        caseData.put(FINANCIAL_ORDER, NO);
        caseData.put(LANGUAGE_PREFERENCE_WELSH, NO);
        return caseData;
    }
}
