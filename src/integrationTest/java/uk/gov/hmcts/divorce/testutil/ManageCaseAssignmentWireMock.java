package uk.gov.hmcts.divorce.testutil;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.EqualToJsonPattern;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.ccd.sdk.type.ChangeOrganisationRequest;
import uk.gov.hmcts.ccd.sdk.type.DynamicListItem;
import uk.gov.hmcts.ccd.sdk.type.Organisation;
import uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseRoleID;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.noticeofchange.model.AcaRequest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_NAME;

public final class ManageCaseAssignmentWireMock {

    private static final String TEST_ORGANISATION_ID = "HB12345";
    private static final String TEST_ORGANISATION_ID_REMOVE = "AB4567";
    private static final String TEST_ORGANISATION_NAME = "NFD solicitors org";
    private static final String TEST_ORGANISATION_NAME_REMOVE = "Private solicitors Ltd";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final WireMockServer MANAGE_CASE_ASSIGNMENT_SERVER = new WireMockServer(wireMockConfig().dynamicPort());

    private ManageCaseAssignmentWireMock() {
    }

    public static void start() {
        if (!MANAGE_CASE_ASSIGNMENT_SERVER.isRunning()) {
            MANAGE_CASE_ASSIGNMENT_SERVER.start();
        }
    }

    public static void stopAndReset() {
        if (MANAGE_CASE_ASSIGNMENT_SERVER.isRunning()) {
            MANAGE_CASE_ASSIGNMENT_SERVER.stop();
            MANAGE_CASE_ASSIGNMENT_SERVER.resetAll();
        }
    }

    public static void stubForCheckNocApprovalEndpoint(String authorisation,
                                                       String s2sToken,
                                                       AcaRequest request) throws JsonProcessingException {

        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String requestBody = OBJECT_MAPPER.writeValueAsString(request);

        MANAGE_CASE_ASSIGNMENT_SERVER.stubFor(WireMock.post("/noc/check-noc-approval")
                .withHeader(AUTHORIZATION, new EqualToPattern(authorisation))
                .withHeader(SERVICE_AUTHORIZATION, new EqualToPattern(s2sToken))
                .withRequestBody(new EqualToJsonPattern(requestBody, true, true))
                .willReturn(aResponse()
                        .withStatus(OK.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBody(OBJECT_MAPPER.writeValueAsString(SubmittedCallbackResponse
                                .builder().confirmationBody("Approval Applied")
                                .confirmationHeader("Approval Applied").build()))
                )
        );
    }

    public static void stubForCheckNocApprovalEndpointForFailure(String authorisation,
                                                                 String s2sToken,
                                                                 AcaRequest request) throws JsonProcessingException {

        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String requestBody = OBJECT_MAPPER.writeValueAsString(request);

        MANAGE_CASE_ASSIGNMENT_SERVER.stubFor(WireMock.post("/noc/check-noc-approval")
                .withHeader(AUTHORIZATION, new EqualToPattern(authorisation))
                .withHeader(SERVICE_AUTHORIZATION, new EqualToPattern(s2sToken))
                .withRequestBody(new EqualToJsonPattern(requestBody, true, true))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.FORBIDDEN.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                )
        );
    }

    public static void stubForNoCApplyDecisionEndpoint(String authorisation, String s2sToken,
                                                       AcaRequest request) throws JsonProcessingException {

        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String requestBody = OBJECT_MAPPER.writeValueAsString(request);

        MANAGE_CASE_ASSIGNMENT_SERVER.stubFor(WireMock.post("/noc/apply-decision")
                .withHeader(AUTHORIZATION, new EqualToPattern(authorisation))
                .withHeader(SERVICE_AUTHORIZATION, new EqualToPattern(s2sToken))
                .withRequestBody(new EqualToJsonPattern(requestBody, true, true))
                .willReturn(aResponse()
                        .withStatus(OK.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBody(OBJECT_MAPPER.writeValueAsString(AboutToStartOrSubmitCallbackResponse
                                .builder().data(buildResponseData())
                                .errors(new ArrayList<>()).build()))
                )
        );
    }

    public static void stubForNoCApplyDecisionEndpointEndpointForFailure(String authorisation,
                                                                         String s2sToken,
                                                                         AcaRequest request) throws JsonProcessingException {
        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String requestBody = OBJECT_MAPPER.writeValueAsString(request);

        MANAGE_CASE_ASSIGNMENT_SERVER.stubFor(WireMock.post("/noc/apply-decision")
                .withHeader(AUTHORIZATION, new EqualToPattern(authorisation))
                .withHeader(SERVICE_AUTHORIZATION, new EqualToPattern(s2sToken))
                .withRequestBody(new EqualToJsonPattern(requestBody, true, true))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.FORBIDDEN.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                )
        );
    }

    public static class PropertiesInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertyValues
                    .of("aac.api.url=" + "http://localhost:" + MANAGE_CASE_ASSIGNMENT_SERVER.port())
                    .applyTo(applicationContext.getEnvironment());
        }
    }

    private static Map<String, Object> buildResponseData() {
        Solicitor solicitor = Solicitor.builder()
                .name(TEST_SOLICITOR_NAME)
                .email(TEST_SOLICITOR_EMAIL)
                .address(TEST_SOLICITOR_ADDRESS)
                .organisationPolicy(OrganisationPolicy.<UserRole>builder().organisation(Organisation.builder()
                                .organisationId(TEST_ORGANISATION_ID)
                                .organisationName(TEST_ORGANISATION_NAME)
                                .build())
                        .build())
                .build();

        CaseData caseData = CaseData.builder()
                .applicant1(Applicant.builder()
                        .solicitor(solicitor)
                        .build())
                .applicant2(Applicant.builder()
                        .solicitor(solicitor)
                        .build())
                .changeOrganisationRequestField(getChangeOrganisationRequestField())
                .build();

        return OBJECT_MAPPER.convertValue(caseData, new TypeReference<>() {
        });
    }

    private static ChangeOrganisationRequest<CaseRoleID> getChangeOrganisationRequestField() {
        DynamicListItem dynamicListItem = DynamicListItem.builder().label(
                "APPLICANT_1_SOLICITOR").code("[APPONESOLICITOR]").build();
        List<DynamicListItem> dynamicListItemList = new ArrayList<>();
        dynamicListItemList.add(dynamicListItem);

        ChangeOrganisationRequest<CaseRoleID> changeOrganisationRequest = ChangeOrganisationRequest.<CaseRoleID>builder().build();
        changeOrganisationRequest.setCaseRoleId(CaseRoleID.builder().value(dynamicListItem).listItems(dynamicListItemList).build());
        changeOrganisationRequest.setCreatedBy(TEST_SOLICITOR_EMAIL);
        changeOrganisationRequest.setOrganisationToAdd(Organisation
                .builder().organisationId(TEST_ORGANISATION_ID).organisationName(TEST_ORGANISATION_NAME).build());
        changeOrganisationRequest.setOrganisationToRemove(Organisation
                .builder().organisationId(TEST_ORGANISATION_ID_REMOVE).organisationName(TEST_ORGANISATION_NAME_REMOVE).build());
        return changeOrganisationRequest;
    }
}
