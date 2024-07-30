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
import uk.gov.hmcts.divorce.common.config.WebMvcConfig;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.solicitor.client.organisation.OrganisationsResponse;
import uk.gov.hmcts.divorce.testutil.PrdOrganisationWireMock;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import static org.mockito.Mockito.when;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;
import static org.skyscreamer.jsonassert.JSONCompareMode.STRICT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.divorce.solicitor.event.Applicant1SolicitorUpdateContactDetails.APP1_SOLICITOR_UPDATE_CONTACT_DETAILS;
import static uk.gov.hmcts.divorce.testutil.PrdOrganisationWireMock.start;
import static uk.gov.hmcts.divorce.testutil.PrdOrganisationWireMock.stopAndReset;
import static uk.gov.hmcts.divorce.testutil.PrdOrganisationWireMock.stubGetOrganisationEndpoint;
import static uk.gov.hmcts.divorce.testutil.TestConstants.APP1_SOLICITOR_UPDATE_CONTACT_DETAILS_MID_EVENT_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTH_HEADER_VALUE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SOLICITOR_MID_EVENT_RESPONSE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SOLICITOR_UPDATE_CONTACT_DETAILS_ERROR_RESPONSE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_ORG_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getApplicantWithAddress;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.organisationPolicy;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.expectedResponse;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ContextConfiguration(initializers = {
    PrdOrganisationWireMock.PropertiesInitializer.class
})
public class Applicant1SolicitorUpdateContactDetailsIT {

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
    void shouldValidateApplicant1SolicitorEmailAndReturnNoErrorsWhenEmailIsLinkedToTheirOrg() throws Exception {
        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubGetOrganisationEndpoint(getOrganisationsResponseWithOrgID(TEST_ORG_ID));

        final String response = performUpdateApplicant1ContactDetailsRequest();

        assertEquals(expectedResponse(SOLICITOR_MID_EVENT_RESPONSE), response, STRICT);
    }

    @Test
    public void shouldValidateApplicant1SolicitorEmailAndReturnErrorWhenEmailIsNotLinkedToTheirOrg() throws Exception {
        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubGetOrganisationEndpoint(getOrganisationsResponseWithOrgID("TESTORG123"));

        final String response = performUpdateApplicant1ContactDetailsRequest();

        assertEquals(expectedResponse(SOLICITOR_UPDATE_CONTACT_DETAILS_ERROR_RESPONSE), response, STRICT);
    }

    private String performUpdateApplicant1ContactDetailsRequest() throws Exception {
        return mockMvc.perform(post(APP1_SOLICITOR_UPDATE_CONTACT_DETAILS_MID_EVENT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(caseDataWithApplicant1Org(), APP1_SOLICITOR_UPDATE_CONTACT_DETAILS))
                )
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    }

    private CaseData caseDataWithApplicant1Org() {
        CaseData caseData = CaseData.builder()
            .applicant1(getApplicantWithAddress())
            .build();

        caseData.getApplicant1().setSolicitor(Solicitor.builder()
            .email(TEST_SOLICITOR_EMAIL)
            .organisationPolicy(organisationPolicy())
            .build());

        return caseData;
    }

    private String getOrganisationsResponseWithOrgID(final String organisationId) throws JsonProcessingException {
        return objectMapper.writeValueAsString(
            OrganisationsResponse.builder()
                .organisationIdentifier(organisationId)
                .build());
    }
}
