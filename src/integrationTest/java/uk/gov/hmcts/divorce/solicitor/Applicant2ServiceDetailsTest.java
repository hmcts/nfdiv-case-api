package uk.gov.hmcts.divorce.solicitor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.config.WebMvcConfig;
import uk.gov.hmcts.divorce.common.model.DivorceOrDissolution;
import uk.gov.hmcts.divorce.solicitor.client.organisation.OrganisationContactInformation;
import uk.gov.hmcts.divorce.solicitor.client.organisation.OrganisationsResponse;
import uk.gov.hmcts.divorce.testutil.PrdOrganisationWireMock;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.when;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;
import static org.skyscreamer.jsonassert.JSONCompareMode.STRICT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import static uk.gov.hmcts.divorce.testutil.PrdOrganisationWireMock.start;
import static uk.gov.hmcts.divorce.testutil.PrdOrganisationWireMock.stopAndReset;
import static uk.gov.hmcts.divorce.testutil.PrdOrganisationWireMock.stubGetOrganisationEndpoint;
import static uk.gov.hmcts.divorce.testutil.TestConstants.APP2_SERVICE_DETAILS_MID_EVENT_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.APPLICANT_1_ORGANISATION_POLICY;
import static uk.gov.hmcts.divorce.testutil.TestConstants.APPLICANT_2_ORGANISATION_POLICY;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTH_HEADER_VALUE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.LANGUAGE_PREFERENCE_WELSH;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SOL_APP2_SERVICE_DETAILS_MID_EVENT_ERROR;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SOL_APP2_SERVICE_DETAILS_MID_EVENT_RESPONSE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SOL_NOT_REPRESENTED_APP2_MID_EVENT_ERROR;
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
    PrdOrganisationWireMock.PropertiesInitializer.class
})
public class Applicant2ServiceDetailsTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthTokenGenerator serviceTokenGenerator;

    @MockBean
    private WebMvcConfig webMvcConfig;

    @BeforeAll
    static void setUp() {
        start();
    }

    @AfterAll
    static void tearDown() {
        stopAndReset();
    }

    @Test
    void shouldSetApplicant2OrgContactInformationWhenApplicant2IsRepresentedBySolicitorAndIsDigital() throws Exception {
        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubGetOrganisationEndpoint(getOrganisationResponseWith(TEST_ORG_ID));

        final String jsonStringResponse = mockMvc.perform(post(APP2_SERVICE_DETAILS_MID_EVENT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(callbackRequest(caseData(YES, YES), SOLICITOR_CREATE)))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk()
            )
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertEquals(jsonStringResponse, expectedResponse(SOL_APP2_SERVICE_DETAILS_MID_EVENT_RESPONSE), STRICT);
    }

    @Test
    public void shouldNotSetApplicant2OrgContactInformationWhenApplicant2IsRepresentedBySolicitorAndIsNotDigital() throws Exception {
        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubGetOrganisationEndpoint(getOrganisationResponseWith("TESTORG123"));

        final String jsonStringResponse = mockMvc.perform(post(APP2_SERVICE_DETAILS_MID_EVENT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(callbackRequest(caseData(NO, YES), SOLICITOR_CREATE)))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk()
            )
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertEquals(jsonStringResponse, expectedResponse(SOL_APP2_SERVICE_DETAILS_MID_EVENT_ERROR), STRICT);
    }

    @Test
    public void shouldNotSetApplicant2OrgContactInformationWhenApplicant2IsNotRepresentedBySolicitor() throws Exception {
        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubGetOrganisationEndpoint(getOrganisationResponseWith("TESTORG123"));

        final String jsonStringResponse = mockMvc.perform(post(APP2_SERVICE_DETAILS_MID_EVENT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(callbackRequest(caseData(NO, NO), SOLICITOR_CREATE)))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk()
            )
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertEquals(jsonStringResponse, expectedResponse(SOL_NOT_REPRESENTED_APP2_MID_EVENT_ERROR), STRICT);
    }

    private Map<String, Object> caseData(YesOrNo isApp2SolDigital, YesOrNo isApp2SolRepresented) {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(APPLICANT_1_FIRST_NAME, TEST_FIRST_NAME);
        caseData.put(APPLICANT_1_LAST_NAME, TEST_LAST_NAME);
        caseData.put(APPLICANT_1_EMAIL, TEST_USER_EMAIL);
        caseData.put(DIVORCE_OR_DISSOLUTION, DivorceOrDissolution.DIVORCE);
        caseData.put(DIVORCE_COSTS_CLAIM, YES);
        caseData.put(FINANCIAL_ORDER, NO);
        caseData.put(LANGUAGE_PREFERENCE_WELSH, NO);
        caseData.put(APPLICANT_1_ORGANISATION_POLICY, organisationPolicy());
        caseData.put(APPLICANT_2_ORGANISATION_POLICY, organisationPolicy());
        caseData.put("applicant2SolicitorRepresented", isApp2SolRepresented);
        caseData.put("app2SolDigital", isApp2SolDigital);
        return caseData;
    }

    private String getOrganisationResponseWith(final String organisationId) throws JsonProcessingException {
        return objectMapper.writeValueAsString(
            OrganisationsResponse.builder()
                .organisationIdentifier(organisationId)
                .contactInformation(singletonList(OrganisationContactInformation
                        .builder()
                        .addressLine1("addressLine1")
                        .addressLine2("addressLine2")
                        .postCode("postCode")
                        .build()
                    )
                )
                .build());
    }
}
