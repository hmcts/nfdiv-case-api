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
import uk.gov.hmcts.ccd.sdk.type.CaseLink;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.Fee;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.ccd.sdk.type.Organisation;
import uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy;
import uk.gov.hmcts.divorce.common.config.WebMvcConfig;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments;
import uk.gov.hmcts.divorce.divorcecase.model.FeeDetails;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplication;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.payment.model.CreditAccountPaymentResponse;
import uk.gov.hmcts.divorce.solicitor.client.organisation.OrganisationContactInformation;
import uk.gov.hmcts.divorce.solicitor.client.organisation.OrganisationsResponse;
import uk.gov.hmcts.divorce.testutil.CdamWireMock;
import uk.gov.hmcts.divorce.testutil.PaymentWireMock;
import uk.gov.hmcts.divorce.testutil.PrdOrganisationWireMock;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.divorce.divorcecase.model.PaymentStatus.SUCCESS;
import static uk.gov.hmcts.divorce.divorcecase.model.ServicePaymentMethod.FEE_PAY_BY_ACCOUNT;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAos;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPronouncement;
import static uk.gov.hmcts.divorce.solicitor.event.SolicitorGeneralApplication.SOLICITOR_GENERAL_APPLICATION;
import static uk.gov.hmcts.divorce.testutil.PaymentWireMock.stubCreditAccountPayment;
import static uk.gov.hmcts.divorce.testutil.PrdOrganisationWireMock.stubGetOrganisationEndpoint;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTH_HEADER_VALUE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_ORG_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getListOfDivorceDocumentListValue;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.expectedResponse;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ContextConfiguration(initializers = {
    CdamWireMock.PropertiesInitializer.class,
    PrdOrganisationWireMock.PropertiesInitializer.class,
    PaymentWireMock.PropertiesInitializer.class
})
public class SolicitorGeneralApplicationIT {

    private static final String SOLICITOR_GENERAL_APPLICATION_RESPONSE =
        "classpath:solicitor-general-application-response.json";

    private static final String SOLICITOR_GENERAL_APPLICATION_PAYMENT_RESPONSE =
        "classpath:solicitor-general-application-payment-response.json";

    private static final String SOLICITOR_GENERAL_APPLICATION_ERRORS_RESPONSE =
        "classpath:solicitor-general-application-errors-response.json";

    private static final String PBA_NUMBER = "PBA0012345";

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
        CdamWireMock.start();
        PrdOrganisationWireMock.start();
        PaymentWireMock.start();
    }

    @AfterAll
    static void tearDown() {
        CdamWireMock.stopAndReset();
        PrdOrganisationWireMock.stopAndReset();
        PaymentWireMock.stopAndReset();
    }

    @Test
    public void shouldAddDocumentToDocumentsUploadedWhenGeneralApplicationSubmitted() throws Exception {

        List<ListValue<DivorceDocument>> docs = getListOfDivorceDocumentListValue(1);
        docs.get(0).getValue().setDocumentFileName("Testfile");
        docs.get(0).getValue().setDocumentDateAdded(LOCAL_DATE);

        final CaseData caseData = CaseData.builder()
            .documents(CaseDocuments.builder().documentsUploaded(new ArrayList<>()).build())
            .generalApplication(GeneralApplication.builder()
                .generalApplicationDocuments(docs)
                .build()
            )
            .build();

        CallbackRequest request = callbackRequest(caseData, SOLICITOR_GENERAL_APPLICATION);
        request.getCaseDetails().setState(AwaitingAos.name());

        String actualResponse = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .content(objectMapper.writeValueAsString(request))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(actualResponse)
            .isEqualTo(json(expectedResponse(SOLICITOR_GENERAL_APPLICATION_RESPONSE)));
    }

    @Test
    public void shouldAddPaymentWhenGeneralApplicationSubmitted() throws Exception {

        List<ListValue<DivorceDocument>> docs = getListOfDivorceDocumentListValue(1);
        docs.get(0).getValue().setDocumentFileName("Testfile");
        docs.get(0).getValue().setDocumentDateAdded(LOCAL_DATE);

        final CaseData caseData = CaseData.builder()
            .documents(CaseDocuments.builder().documentsUploaded(new ArrayList<>()).build())
            .generalApplication(GeneralApplication.builder()
                .generalApplicationDocuments(docs)
                .generalApplicationFee(
                    FeeDetails.builder()
                        .orderSummary(
                            OrderSummary.builder()
                                .paymentTotal("55000")
                                .fees(List.of(ListValue
                                    .<Fee>builder()
                                    .id("1")
                                    .value(Fee.builder()
                                        .code("FEE002")
                                        .description("fees for divorce")
                                        .build())
                                    .build())
                                )
                                .build())
                        .pbaNumbers(
                            DynamicList.builder()
                                .value(
                                    DynamicListElement.builder()
                                        .label(PBA_NUMBER)
                                        .build())
                                .build()
                        )
                        .paymentMethod(FEE_PAY_BY_ACCOUNT)
                        .build()
                )
                .build()
            )
            .build();
        final Solicitor applicant1Solicitor = Solicitor.builder()
            .organisationPolicy(
                OrganisationPolicy.<UserRole>builder()
                    .organisation(Organisation.builder()
                        .organisationId("App1OrgPolicy")
                        .organisationName(TEST_ORG_NAME)
                        .build())
                    .build()
            )
            .build();
        final Solicitor applicant2Solicitor = Solicitor.builder()
            .organisationPolicy(
                OrganisationPolicy.<UserRole>builder()
                    .organisation(Organisation.builder()
                        .organisationId("App2OrgPolicy")
                        .build())
                    .build()
            )
            .build();
        caseData.getApplicant1().setSolicitor(applicant1Solicitor);
        caseData.getApplicant2().setSolicitor(applicant2Solicitor);

        CallbackRequest request = callbackRequest(caseData, SOLICITOR_GENERAL_APPLICATION);
        request.getCaseDetails().setState(AwaitingAos.name());

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        stubGetOrganisationEndpoint(getOrganisationResponseWith("App1OrgPolicy"));
        stubCreditAccountPayment(
            CREATED,
            CreditAccountPaymentResponse
                .builder()
                .status(SUCCESS.toString())
                .caseReference(TEST_CASE_ID.toString())
                .build()
        );

        String actualResponse = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(request))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(actualResponse)
            .isEqualTo(json(expectedResponse(SOLICITOR_GENERAL_APPLICATION_PAYMENT_RESPONSE)));
    }

    @Test
    public void shouldReturnErrorsIfCaseIsPartOfActiveBulkCase() throws Exception {

        final CaseData caseData = CaseData.builder()
            .bulkListCaseReferenceLink(CaseLink
                .builder()
                .caseReference("1234")
                .build())
            .build();

        CallbackRequest request = callbackRequest(caseData, SOLICITOR_GENERAL_APPLICATION);
        request.getCaseDetails().setState(AwaitingPronouncement.name());

        String actualResponse = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .content(objectMapper.writeValueAsString(request))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(actualResponse)
            .isEqualTo(json(expectedResponse(SOLICITOR_GENERAL_APPLICATION_ERRORS_RESPONSE)));
    }

    private String getOrganisationResponseWith(String organisationId) throws JsonProcessingException {
        return objectMapper.writeValueAsString(
            OrganisationsResponse.builder()
                .organisationIdentifier(organisationId)
                .contactInformation(singletonList(OrganisationContactInformation.builder()
                    .addressLine1("Line 1")
                    .addressLine2("Line 2")
                    .townCity("Town")
                    .postCode("WC1 2TG")
                    .build()))
                .build());
    }
}
