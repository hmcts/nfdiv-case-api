package uk.gov.hmcts.divorce.caseworker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.caseworker.event.CaseworkerRegenerateCourtOrders;
import uk.gov.hmcts.divorce.common.config.WebMvcConfig;
import uk.gov.hmcts.divorce.common.config.interceptors.RequestInterceptor;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.print.BulkPrintService;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.notification.NotificationService;
import uk.gov.hmcts.divorce.testutil.CdamWireMock;
import uk.gov.hmcts.divorce.testutil.DocAssemblyWireMock;
import uk.gov.hmcts.divorce.testutil.IdamWireMock;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderCourt.BURY_ST_EDMUNDS;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.SupplementaryCaseType.JUDICIAL_SEPARATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CERTIFICATE_OF_ENTITLEMENT;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED;
import static uk.gov.hmcts.divorce.document.model.DocumentType.FINAL_ORDER_GRANTED;
import static uk.gov.hmcts.divorce.testutil.CdamWireMock.stubCdamDownloadBinaryWith;
import static uk.gov.hmcts.divorce.testutil.DocAssemblyWireMock.stubForDocAssemblyWith;
import static uk.gov.hmcts.divorce.testutil.IdamWireMock.CASEWORKER_ROLE;
import static uk.gov.hmcts.divorce.testutil.IdamWireMock.SYSTEM_USER_ROLE;
import static uk.gov.hmcts.divorce.testutil.IdamWireMock.stubForIdamDetails;
import static uk.gov.hmcts.divorce.testutil.IdamWireMock.stubForIdamToken;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTH_HEADER_VALUE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.CASEWORKER_USER_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_USER_USER_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SYSTEM_AUTHORISATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getDivorceDocumentListValue;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.expectedResponse;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.resourceAsBytes;

@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ContextConfiguration(initializers = {
    DocAssemblyWireMock.PropertiesInitializer.class,
    IdamWireMock.PropertiesInitializer.class,
    CdamWireMock.PropertiesInitializer.class
})
public class CaseworkerRegenerateCourtOrdersIT {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    public static final String APPLICANT1 = "applicant1";
    public static final String APPLICANT2 = "applicant2";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BulkPrintService bulkPrintService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RequestInterceptor requestInterceptor;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private WebMvcConfig webMvcConfig;

    @MockBean
    private AuthTokenGenerator serviceTokenGenerator;

    @Autowired
    private NotificationDispatcher notificationDispatcher;

    @MockBean
    private Clock clock;

    @BeforeAll
    static void setUp() {
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
        DocAssemblyWireMock.start();
        IdamWireMock.start();
        CdamWireMock.start();
    }

    @AfterAll
    static void tearDown() {
        DocAssemblyWireMock.stopAndReset();
        IdamWireMock.stopAndReset();
        CdamWireMock.stopAndReset();
    }

    @BeforeEach
    void setClock() {
        LocalDateTime dateTime = LocalDateTime.of(2022, Month.FEBRUARY, 15, 13, 39);
        Instant instant = dateTime.atZone(ZoneId.of("Europe/London")).toInstant();
        when(clock.instant()).thenReturn(instant);
        when(clock.getZone()).thenReturn(ZoneId.of("Europe/London"));
    }

    private byte[] loadPdfAsBytes() throws IOException {
        return resourceAsBytes("classpath:Test.pdf");
    }

    @Test
    void shouldRegenerateCourtOrdersAndCoverLettersWhenCertificateOfEntitlementAndCOGrantedAndFOGrantedDocsExistAndOffline()
        throws Exception {
        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);
        stubForIdamToken(TEST_AUTHORIZATION_TOKEN);
        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubForDocAssemblyWith("81759115-d69e-4818-a0c5-a1f343d4f914",
            "FL-NFD-GOR-ENG-Entitlement-Cover-Letter-V4.docx");
        stubForDocAssemblyWith("411bfbe3-af6c-4458-92f7-7854d3d1de24",
            "FL-NFD-GOR-ENG-Entitlement-Cover-Letter-Offline-Respondent.docx");
        stubForDocAssemblyWith("5cd725e8-f053-4493-9cbe-bb69d1905ae3",
            "FL-NFD-GOR-ENG-Certificate_Of_Entitlement_V1.docx");
        stubForDocAssemblyWith("ea114af6-ed73-476b-8cc6-41ec8eb4f0b3",
            "FL-NFD-GOR-ENG-Conditional-Order-Granted-Cover-Letter_V2.docx");
        stubForDocAssemblyWith("b3d8d9de-8706-4b6e-881c-d8b400d6c533",
            "FL-NFD-GOR-ENG-CO-Pronounced-Cover-Letter-Offline-Respondent.docx");
        stubForDocAssemblyWith("d2fcd6f7-5365-4b8a-af15-ce3c949173aa",
            "FL-NFD-GOR-ENG-Conditional-Order-Pronounced_V4.docx");
        stubForDocAssemblyWith("959ddaf2-75d8-4d49-8a2d-bc29d451f921",
            "FL-NFD-GOR-ENG-Final-Order-Cover-Letter_V2.docx");
        stubForDocAssemblyWith("7aa5c8bb-1177-4b3e-af83-841c20b572c2",
            "FL-NFD-GOR-ENG-Final-Order-Granted_V1.docx");


        byte[] byteArray = loadPdfAsBytes();
        stubCdamDownloadBinaryWith("81759115-d69e-4818-a0c5-a1f343d4f914", byteArray);
        stubCdamDownloadBinaryWith("5baa7e23-6b3e-4f4a-82de-1d4e3dc1759d", byteArray);
        final ListValue<DivorceDocument> coGrantedDoc =
            getDivorceDocumentListValue(
                "http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130003",
                "co_granted.pdf",
                CONDITIONAL_ORDER_GRANTED
            );

        final ListValue<DivorceDocument> foGrantedDoc =
            getDivorceDocumentListValue(
                "http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130003",
                "fo_granted.pdf",
                FINAL_ORDER_GRANTED
            );

        List<ListValue<DivorceDocument>> documentsGenerated = new ArrayList<>();
        documentsGenerated.add(coGrantedDoc);
        documentsGenerated.add(foGrantedDoc);

        final CaseData caseData = CaseData
            .builder()
            .applicationType(SOLE_APPLICATION)
            .divorceOrDissolution(DIVORCE)
            .applicant1(Applicant.builder().firstName(APPLICANT1).offline(YES).build())
            .applicant2(Applicant.builder().firstName(APPLICANT2).offline(YES).build())
            .conditionalOrder(
                ConditionalOrder.builder()
                    .court(BURY_ST_EDMUNDS)
                    .dateAndTimeOfHearing(LocalDateTime.now())
                    .certificateOfEntitlementDocument(
                        divorceDocumentWithFileName("certificateOfEntitlement-1641906321238843-2022-02-22:16:06.pdf")
                    )
                    .build()
            )
            .documents(
                CaseDocuments
                    .builder()
                    .documentsGenerated(documentsGenerated)
                    .build()
            )
            .build();

        String actualResponse = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .content(OBJECT_MAPPER.writeValueAsString(
                        callbackRequest(caseData, CaseworkerRegenerateCourtOrders.CASEWORKER_REGENERATE_COURT_ORDERS)
                    )
                )
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(actualResponse)
            .when(IGNORING_EXTRA_FIELDS, IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedCcdAboutToSubmitCallbackOfflineSuccess()));
        assertThatJson(actualResponse)
            .when(IGNORING_EXTRA_FIELDS, IGNORING_ARRAY_ORDER)
            .node("data.letterPacks")
            .isArray()
            .hasSize(6);
        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldRegenerateCourtOrdersWithoutCoverLettersForOnlineApplicants()
        throws Exception {
        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);
        stubForIdamToken(TEST_AUTHORIZATION_TOKEN);
        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubForDocAssemblyWith("5cd725e8-f053-4493-9cbe-bb69d1905ae3",
            "FL-NFD-GOR-ENG-Certificate_Of_Entitlement_V1.docx");
        stubForDocAssemblyWith("d2fcd6f7-5365-4b8a-af15-ce3c949173aa",
            "FL-NFD-GOR-ENG-Conditional-Order-Pronounced_V4.docx");
        stubForDocAssemblyWith("7aa5c8bb-1177-4b3e-af83-841c20b572c2",
            "FL-NFD-GOR-ENG-Final-Order-Granted_V1.docx");
        stubForDocAssemblyWith("7aa5c8bb-1177-4b3e-af83-841c20b572c2", "FL-NFD-GOR-ENG-Final-Order-Cover-Letter_V2.docx");

        final ListValue<DivorceDocument> coGrantedDoc =
            getDivorceDocumentListValue(
                "http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130003",
                "co_granted.pdf",
                CONDITIONAL_ORDER_GRANTED
            );

        final ListValue<DivorceDocument> foGrantedDoc =
            getDivorceDocumentListValue(
                "http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130003",
                "fo_granted.pdf",
                FINAL_ORDER_GRANTED
            );

        List<ListValue<DivorceDocument>> documentsGenerated = new ArrayList<>();
        documentsGenerated.add(coGrantedDoc);
        documentsGenerated.add(foGrantedDoc);

        final CaseData caseData = CaseData
            .builder()
            .applicationType(SOLE_APPLICATION)
            .divorceOrDissolution(DIVORCE)
            .applicant1(Applicant.builder().offline(NO).build())
            .applicant2(Applicant.builder().offline(NO).email("test@email.com").build())
            .conditionalOrder(
                ConditionalOrder.builder()
                    .dateAndTimeOfHearing(LocalDateTime.now())
                    .certificateOfEntitlementDocument(
                        divorceDocumentWithFileName("certificateOfEntitlement-1641906321238843-2022-02-22:16:06.pdf")
                    )
                    .build()
            )
            .documents(
                CaseDocuments
                    .builder()
                    .documentsGenerated(documentsGenerated)
                    .build()
            )
            .build();

        String actualResponse = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .content(OBJECT_MAPPER.writeValueAsString(
                        callbackRequest(caseData, CaseworkerRegenerateCourtOrders.CASEWORKER_REGENERATE_COURT_ORDERS)
                    )
                )
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(actualResponse)
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedCcdAboutToSubmitCallbackSuccess()));
    }

    @Test
    void shouldRegenerateCourtOrdersWhenCertificateOfEntitlementAndCOGrantedAndFOGrantedDocsExistForDigitalCivilPartnershipCase()
        throws Exception {
        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);
        stubForIdamToken(TEST_AUTHORIZATION_TOKEN);
        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubForDocAssemblyWith("5cd725e8-f053-4493-9cbe-bb69d1905ae3",
            "FL-NFD-GOR-ENG-Certificate_Of_Entitlement_V1.docx");
        stubForDocAssemblyWith("d2fcd6f7-5365-4b8a-af15-ce3c949173aa",
            "FL-NFD-GOR-ENG-Conditional-Order-Pronounced_V4.docx");
        stubForDocAssemblyWith("7aa5c8bb-1177-4b3e-af83-841c20b572c2",
            "FL-NFD-GOR-ENG-Final-Order-Granted_V1.docx");

        final ListValue<DivorceDocument> coGrantedDoc =
            getDivorceDocumentListValue(
                "http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130003",
                "conditionalOrderGranted.pdf",
                CONDITIONAL_ORDER_GRANTED
            );

        final ListValue<DivorceDocument> foGrantedDoc =
            getDivorceDocumentListValue(
                "http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130003",
                "finalOrderGranted.pdf",
                FINAL_ORDER_GRANTED
            );

        List<ListValue<DivorceDocument>> documentsGenerated = new ArrayList<>();
        documentsGenerated.add(coGrantedDoc);
        documentsGenerated.add(foGrantedDoc);

        final CaseData caseData = CaseData
            .builder()
            .applicationType(SOLE_APPLICATION)
            .divorceOrDissolution(DISSOLUTION)
            .applicant1(Applicant.builder().offline(NO).build())
            .applicant2(Applicant.builder().offline(NO).email("test@email.com").build())
            .conditionalOrder(
                ConditionalOrder.builder()
                    .dateAndTimeOfHearing(LocalDateTime.now())
                    .certificateOfEntitlementDocument(
                        divorceDocumentWithFileName("certificateOfEntitlement-1641906321238843-2022-02-22:16:06.pdf")
                    )
                    .build()
            )
            .documents(
                CaseDocuments
                    .builder()
                    .documentsGenerated(documentsGenerated)
                    .build()
            )
            .build();

        String actualResponse = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .content(OBJECT_MAPPER.writeValueAsString(
                        callbackRequest(caseData, CaseworkerRegenerateCourtOrders.CASEWORKER_REGENERATE_COURT_ORDERS)
                    )
                )
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        DocumentContext jsonDocument = JsonPath.parse(expectedCcdAboutToSubmitCallbackSuccess());
        jsonDocument.set("data.divorceOrDissolution", "dissolution");

        assertThatJson(actualResponse)
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(jsonDocument.json());

    }

    @Test
    void shouldRegenerateRequiredDocsWhenCertificateOfEntitlementAndCOGrantedAndFOGrantedDocsExistAndOfflineJudicialSeparation()
        throws Exception {
        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);
        stubForIdamToken(TEST_AUTHORIZATION_TOKEN);
        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);

        stubForDocAssemblyWith("411bfbe3-af6c-4458-92f7-7854d3d1de24",
            "FL-NFD-GOR-ENG-Entitlement-Cover-Letter-JS.docx");
        stubForDocAssemblyWith("b9dbf3b2-bda8-11ed-afa1-0242ac120002",
            "FL-NFD-GOR-ENG-Certificate-Of-Entitlement-Judicial-Separation_V2.docx");
        stubForDocAssemblyWith("31f23884-a707-4983-9bb9-5ce33e13d8c2",
            "FL-NFD-GOR-ENG-Conditional-Order-Granted-Cover-Letter-JS.docx");
        stubForDocAssemblyWith("b3d8d9de-8706-4b6e-881c-d8b400d6c533",
            "FL-NFD-GOR-ENG-CO-Pronounced-Cover-Letter-Offline-Respondent.docx");
        stubForDocAssemblyWith("90ac37a0-b723-4722-9723-86157e4ee450",
            "FL-NFD-GOR-ENG-Conditional-Order-Pronounced-JS.docx");
        stubForDocAssemblyWith("959ddaf2-75d8-4d49-8a2d-bc29d451f921",
            "FL-NFD-GOR-ENG-Final-Order-Cover-Letter_V2.docx");
        stubForDocAssemblyWith("7aa5c8bb-1177-4b3e-af83-841c20b572c2",
            "FL-NFD-GOR-ENG-Final-Order-Granted_V1.docx");

        final ListValue<DivorceDocument> coGrantedDoc =
            getDivorceDocumentListValue(
                "http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130003",
                "co_granted.pdf",
                CONDITIONAL_ORDER_GRANTED
            );

        final ListValue<DivorceDocument> foGrantedDoc =
            getDivorceDocumentListValue(
                "http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130003",
                "fo_granted.pdf",
                FINAL_ORDER_GRANTED
            );

        List<ListValue<DivorceDocument>> documentsGenerated = new ArrayList<>();
        documentsGenerated.add(coGrantedDoc);
        documentsGenerated.add(foGrantedDoc);

        final CaseData caseData = CaseData
            .builder()
            .supplementaryCaseType(JUDICIAL_SEPARATION)
            .applicationType(SOLE_APPLICATION)
            .divorceOrDissolution(DIVORCE)
            .applicant1(Applicant.builder().firstName(APPLICANT1).offline(YES).build())
            .applicant2(Applicant.builder().firstName(APPLICANT2).offline(YES).build())
            .conditionalOrder(
                ConditionalOrder.builder()
                    .court(BURY_ST_EDMUNDS)
                    .dateAndTimeOfHearing(LocalDateTime.now())
                    .certificateOfEntitlementDocument(
                        divorceDocumentWithFileName("certificateOfEntitlement-1641906321238843-2022-02-22:16:06.pdf")
                    )
                    .build()
            )
            .documents(
                CaseDocuments
                    .builder()
                    .documentsGenerated(documentsGenerated)
                    .build()
            )
            .build();

        String actualResponse = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .content(OBJECT_MAPPER.writeValueAsString(
                        callbackRequest(caseData, CaseworkerRegenerateCourtOrders.CASEWORKER_REGENERATE_COURT_ORDERS)
                    )
                )
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(actualResponse)
            .when(IGNORING_EXTRA_FIELDS, IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedCcdAboutToSubmitCallbackOfflineJudicialSeparationSuccess()));

        assertThatJson(actualResponse)
            .when(IGNORING_EXTRA_FIELDS, IGNORING_ARRAY_ORDER)
            .node("data.letterPacks")
            .isArray()
            .hasSize(6);
        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldRegenerateCourtOrdersAndCoverLettersWhenOfflineJudicialSeparationAndApplicantsAreRepresented()
        throws Exception {
        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);
        stubForIdamToken(TEST_AUTHORIZATION_TOKEN);
        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);

        stubForDocAssemblyWith("5baa7e23-6b3e-4f4a-82de-1d4e3dc1759d",
            "FL-NFD-GOR-ENG-Entitlement-Cover-Letter-JS-Solicitor.docx");
        stubForDocAssemblyWith("b9dbf3b2-bda8-11ed-afa1-0242ac120002",
            "FL-NFD-GOR-ENG-Certificate-Of-Entitlement-Judicial-Separation_V2.docx");
        stubForDocAssemblyWith("94e63858-bda1-11ed-afa1-0242ac120002",
            "FL-NFD-GOR-ENG-Conditional-Order-Granted-Solicitor-Cover-Letter-JS.docx");
        stubForDocAssemblyWith("90ac37a0-b723-4722-9723-86157e4ee450",
            "FL-NFD-GOR-ENG-Conditional-Order-Pronounced-JS.docx");
        stubForDocAssemblyWith("959ddaf2-75d8-4d49-8a2d-bc29d451f921",
            "FL-NFD-GOR-ENG-Final-Order-Cover-Letter_V2.docx");
        stubForDocAssemblyWith("7aa5c8bb-1177-4b3e-af83-841c20b572c2",
            "FL-NFD-GOR-ENG-Final-Order-Granted_V1.docx");

        String testString = "a simple test";
        // Convert the string to byte array
        byte[] byteArray = testString.getBytes();
        stubCdamDownloadBinaryWith("81759115-d69e-4818-a0c5-a1f343d4f914", byteArray);
        stubCdamDownloadBinaryWith("5baa7e23-6b3e-4f4a-82de-1d4e3dc1759d", byteArray);
        stubCdamDownloadBinaryWith("b9dbf3b2-bda8-11ed-afa1-0242ac120002", byteArray);

        final ListValue<DivorceDocument> coGrantedDoc =
            getDivorceDocumentListValue(
                "http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130003",
                "co_granted.pdf",
                CONDITIONAL_ORDER_GRANTED
            );

        final ListValue<DivorceDocument> foGrantedDoc =
            getDivorceDocumentListValue(
                "http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130003",
                "fo_granted.pdf",
                FINAL_ORDER_GRANTED
            );

        List<ListValue<DivorceDocument>> documentsGenerated = new ArrayList<>();
        documentsGenerated.add(coGrantedDoc);
        documentsGenerated.add(foGrantedDoc);

        final CaseData caseData = CaseData
            .builder()
            .supplementaryCaseType(JUDICIAL_SEPARATION)
            .applicationType(SOLE_APPLICATION)
            .divorceOrDissolution(DIVORCE)
            .applicant1(Applicant.builder().offline(YES).firstName(APPLICANT1).solicitorRepresented(YES).build())
            .applicant2(Applicant.builder().offline(YES).firstName(APPLICANT2).solicitorRepresented(YES).build())
            .conditionalOrder(
                ConditionalOrder.builder()
                    .court(BURY_ST_EDMUNDS)
                    .dateAndTimeOfHearing(LocalDateTime.now())
                    .certificateOfEntitlementDocument(
                        divorceDocumentWithFileName("certificateOfEntitlement-1641906321238843-2022-02-22:16:06.pdf")
                    )
                    .build()
            )
            .documents(
                CaseDocuments
                    .builder()
                    .documentsGenerated(documentsGenerated)
                    .build()
            )
            .build();

        String actualResponse = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .content(OBJECT_MAPPER.writeValueAsString(
                        callbackRequest(caseData, CaseworkerRegenerateCourtOrders.CASEWORKER_REGENERATE_COURT_ORDERS)
                    )
                )
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(actualResponse)
            .when(IGNORING_EXTRA_FIELDS, IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedCcdAboutToSubmitCallbackOfflineSolicitorJudicialSeparationSuccess()));

        verifyNoInteractions(notificationService);
    }

    private String expectedCcdAboutToSubmitCallbackSuccess() throws IOException {
        return expectedResponse("classpath:wiremock/responses/about-to-submit-system-regenerate-court-orders.json");
    }

    private String expectedCcdAboutToSubmitCallbackOfflineSuccess() throws IOException {
        return expectedResponse("classpath:wiremock/responses/about-to-submit-system-regenerate-court-orders-offline.json");
    }

    private String expectedCcdAboutToSubmitCallbackOfflineJudicialSeparationSuccess() throws IOException {
        return expectedResponse("classpath:wiremock/responses/about-to-submit-system-regenerate-court-orders-offline-js.json");
    }

    private String expectedCcdAboutToSubmitCallbackOfflineSolicitorJudicialSeparationSuccess() throws IOException {
        return expectedResponse("classpath:wiremock/responses/about-to-submit-system-regenerate-court-orders-offline-solicitor-js.json");
    }

    private DivorceDocument divorceDocumentWithFileName(String fileName) {
        return DivorceDocument
            .builder()
            .documentLink(certificateOfEntitlementDocumentLink())
            .documentType(CERTIFICATE_OF_ENTITLEMENT)
            .documentFileName(fileName)
            .build();
    }

    private Document certificateOfEntitlementDocumentLink() {
        final Document certificateOfEntitlementDocumentLink = Document.builder()
            .url("http://dm-store-aat.service.core-compute-aat.internal/documents/fa1c052a-20ed-4eb2-a2dd-01322553d5a3")
            .filename("certificateOfEntitlement-1641906321238843-2022-01-11:13:06.pdf")
            .binaryUrl("http://dm-store-aat.service.core-compute-aat.internal/documents/fa1c052a-20ed-4eb2-a2dd-01322553d5a3/binary")
            .build();
        return certificateOfEntitlementDocumentLink;
    }
}
