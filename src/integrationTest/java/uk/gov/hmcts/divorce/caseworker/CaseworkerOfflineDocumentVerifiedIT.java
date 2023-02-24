package uk.gov.hmcts.divorce.caseworker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
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
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.ScannedDocument;
import uk.gov.hmcts.ccd.sdk.type.ScannedDocumentType;
import uk.gov.hmcts.divorce.caseworker.service.print.AosPackPrinter;
import uk.gov.hmcts.divorce.caseworker.service.print.AppliedForCoPrinter;
import uk.gov.hmcts.divorce.common.config.WebMvcConfig;
import uk.gov.hmcts.divorce.divorcecase.model.AcknowledgementOfService;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments;
import uk.gov.hmcts.divorce.divorcecase.model.CaseInvite;
import uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution;
import uk.gov.hmcts.divorce.divorcecase.model.HelpWithFees;
import uk.gov.hmcts.divorce.divorcecase.model.NoticeOfChange;
import uk.gov.hmcts.divorce.divorcecase.model.RetiredFields;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.notification.NotificationService;
import uk.gov.hmcts.divorce.testutil.DocManagementStoreWireMock;
import uk.gov.hmcts.divorce.testutil.SendLetterWireMock;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.time.LocalDate;

import static java.util.Collections.singletonList;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.core.Option.TREATING_NULL_AS_ABSENT;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerOfflineDocumentVerified.CASEWORKER_OFFLINE_DOCUMENT_VERIFIED;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments.OfflineDocumentReceived.AOS_D10;
import static uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments.OfflineDocumentReceived.CO_D84;
import static uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments.OfflineDocumentReceived.FO_D36;
import static uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments.OfflineDocumentReceived.OTHER;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.FEMALE;
import static uk.gov.hmcts.divorce.divorcecase.model.HowToRespondApplication.DISPUTE_DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.HowToRespondApplication.WITHOUT_DISPUTE_DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.State.FinalOrderRequested;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Holding;
import static uk.gov.hmcts.divorce.divorcecase.model.State.IssuedToBailiff;
import static uk.gov.hmcts.divorce.divorcecase.model.State.OfflineDocumentReceived;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDate;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_START_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SUBMITTED_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SYSTEM_AUTHORISATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getApplicant;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.expectedResponse;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ContextConfiguration(initializers = {
    DocManagementStoreWireMock.PropertiesInitializer.class,
    SendLetterWireMock.PropertiesInitializer.class
})
public class CaseworkerOfflineDocumentVerifiedIT {

    private static final String CASEWORKER_OFFLINE_DOCUMENT_VERIFIED_D10_RESPONSE =
        "classpath:caseworker-offline-document-verified-d10-response.json";

    private static final String CASEWORKER_OFFLINE_DOCUMENT_VERIFIED_D84_RESPONSE =
        "classpath:caseworker-offline-document-verified-d84-response.json";

    private static final String CASEWORKER_OFFLINE_DOCUMENT_VERIFIED_OTHER_RESPONSE =
        "classpath:caseworker-offline-document-verified-other-response.json";
    public static final String FILENAME = "doc1.pdf";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WebMvcConfig webMvcConfig;

    @MockBean
    private AuthTokenGenerator serviceTokenGenerator;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private AppliedForCoPrinter appliedForCoPrinter;

    @MockBean
    private AosPackPrinter aosPackPrinter;

    @BeforeAll
    static void setUp() {
        DocManagementStoreWireMock.start();
        SendLetterWireMock.start();
    }

    @AfterAll
    static void tearDown() {
        DocManagementStoreWireMock.stopAndReset();
        SendLetterWireMock.stopAndReset();
    }

    @Test
    void shouldTriggerAosSubmissionAndMoveCaseStateToHoldingIfD10Verified() throws Exception {

        final AcknowledgementOfService acknowledgementOfService = AcknowledgementOfService.builder()
            .howToRespondApplication(DISPUTE_DIVORCE)
            .build();

        final ListValue<ScannedDocument> doc1 = ListValue.<ScannedDocument>builder()
            .value(
                ScannedDocument
                    .builder()
                    .url(
                        Document
                            .builder()
                            .filename(FILENAME)
                            .url("http://localhost:8080/f62d42fd-a5f0-43ff-874b-d1666c1bf00d")
                            .binaryUrl("http://localhost:8080/f62d42fd-a5f0-43ff-874b-d1666c1bf00d/binary")
                            .build()
                    )
                    .fileName(FILENAME)
                    .type(ScannedDocumentType.OTHER)
                    .subtype("aos")
                    .build()
            )
            .build();

        final CaseData caseData = caseData();
        caseData.getApplication().setIssueDate(getExpectedLocalDate());
        caseData.setAcknowledgementOfService(acknowledgementOfService);
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.setIsJudicialSeparation(NO);

        caseData.getApplicant2().setLegalProceedings(YES);
        caseData.getApplicant2().setLegalProceedingsDetails("some description");
        caseData.setDocuments(
            CaseDocuments.builder()
                .typeOfDocumentAttached(AOS_D10)
                .scannedDocuments(singletonList(doc1))
                .scannedDocumentNames(
                    DynamicList
                        .builder()
                        .value(
                            DynamicListElement
                                .builder()
                                .label(FILENAME)
                                .build()
                        )
                        .build()
                )
                .build()
        );

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        final var jsonStringResponse = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                .header(AUTHORIZATION, TEST_SYSTEM_AUTHORISATION_TOKEN)
                .content(
                    objectMapper.writeValueAsString(
                        callbackRequest(caseData, CASEWORKER_OFFLINE_DOCUMENT_VERIFIED, OfflineDocumentReceived.name())))
                .accept(APPLICATION_JSON))
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(jsonStringResponse)
            .when(TREATING_NULL_AS_ABSENT)
            .isEqualTo(expectedResponse(CASEWORKER_OFFLINE_DOCUMENT_VERIFIED_D10_RESPONSE));
    }

    @Test
    void shouldTriggerCoSubmissionAndMoveCaseStateToJSAwaitingLAIfD84VerifiedAndJudicialSeparation() throws Exception {

        final ListValue<ScannedDocument> doc1 = ListValue.<ScannedDocument>builder()
            .value(
                ScannedDocument
                    .builder()
                    .url(
                        Document
                            .builder()
                            .filename(FILENAME)
                            .url("http://localhost:8080/f62d42fd-a5f0-43ff-874b-d1666c1bf00d")
                            .binaryUrl("http://localhost:8080/f62d42fd-a5f0-43ff-874b-d1666c1bf00d/binary")
                            .build()
                    )
                    .fileName(FILENAME)
                    .type(ScannedDocumentType.FORM)
                    .build()
            )
            .build();

        final CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.setApplicant2(getApplicant(FEMALE));
        caseData.setIsJudicialSeparation(YES);
        caseData.setDocuments(
            CaseDocuments.builder()
                .typeOfDocumentAttached(CO_D84)
                .scannedDocuments(singletonList(doc1))
                .scannedDocumentNames(
                    DynamicList
                        .builder()
                        .value(
                            DynamicListElement
                                .builder()
                                .label(FILENAME)
                                .build()
                        )
                        .build()
                )
                .build()
        );

        final var jsonStringResponse = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
            .header(AUTHORIZATION, TEST_SYSTEM_AUTHORISATION_TOKEN)
            .content(
                objectMapper.writeValueAsString(
                    callbackRequest(caseData, CASEWORKER_OFFLINE_DOCUMENT_VERIFIED, OfflineDocumentReceived.name())))
            .accept(APPLICATION_JSON))
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(jsonStringResponse)
            .when(TREATING_NULL_AS_ABSENT)
            .isEqualTo(expectedResponse(CASEWORKER_OFFLINE_DOCUMENT_VERIFIED_D84_RESPONSE));
    }

    @Test
    void shouldTriggerCoSubmissionAndMoveCaseStateToAwaitingFinalOrderIfSoleCaseAndD36Verified() throws Exception {

        final ListValue<ScannedDocument> doc1 = ListValue.<ScannedDocument>builder()
            .value(
                ScannedDocument
                    .builder()
                    .url(
                        Document
                            .builder()
                            .filename(FILENAME)
                            .url("http://localhost:8080/f62d42fd-a5f0-43ff-874b-d1666c1bf00d")
                            .binaryUrl("http://localhost:8080/f62d42fd-a5f0-43ff-874b-d1666c1bf00d/binary")
                            .build()
                    )
                    .fileName(FILENAME)
                    .type(ScannedDocumentType.FORM)
                    .build()
            )
            .build();

        final CaseData caseData = caseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.setApplicant2(getApplicant(FEMALE));
        caseData.setDocuments(
            CaseDocuments.builder()
                .typeOfDocumentAttached(FO_D36)
                .scannedDocuments(singletonList(doc1))
                .scannedDocumentNames(
                    DynamicList
                        .builder()
                        .value(
                            DynamicListElement
                                .builder()
                                .label(FILENAME)
                                .build()
                        )
                        .build()
                )
                .build()
        );

        final var jsonStringResponse = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
            .header(AUTHORIZATION, TEST_SYSTEM_AUTHORISATION_TOKEN)
            .content(
                objectMapper.writeValueAsString(
                    callbackRequest(caseData, CASEWORKER_OFFLINE_DOCUMENT_VERIFIED, OfflineDocumentReceived.name())))
            .accept(APPLICATION_JSON))
            .andDo(print())
            .andExpect(
                status().isOk())
            .andExpect(
                jsonPath("$.state").value(FinalOrderRequested.name()));
    }

    @Test
    void shouldTriggerCoSubmissionAndMoveCaseStateToFinalOrderRequestedIfJointCaseAndD36Verified() throws Exception {

        final ListValue<ScannedDocument> doc1 = ListValue.<ScannedDocument>builder()
            .value(
                ScannedDocument
                    .builder()
                    .url(
                        Document
                            .builder()
                            .filename(FILENAME)
                            .url("http://localhost:8080/f62d42fd-a5f0-43ff-874b-d1666c1bf00d")
                            .binaryUrl("http://localhost:8080/f62d42fd-a5f0-43ff-874b-d1666c1bf00d/binary")
                            .build()
                    )
                    .fileName(FILENAME)
                    .type(ScannedDocumentType.FORM)
                    .build()
            )
            .build();

        final CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.setApplicant2(getApplicant(FEMALE));
        caseData.setDocuments(
            CaseDocuments.builder()
                .typeOfDocumentAttached(FO_D36)
                .scannedDocuments(singletonList(doc1))
                .scannedDocumentNames(
                    DynamicList
                        .builder()
                        .value(
                            DynamicListElement
                                .builder()
                                .label(FILENAME)
                                .build()
                        )
                        .build()
                )
                .build()
        );

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
            .header(AUTHORIZATION, TEST_SYSTEM_AUTHORISATION_TOKEN)
            .content(
                objectMapper.writeValueAsString(
                    callbackRequest(caseData, CASEWORKER_OFFLINE_DOCUMENT_VERIFIED, OfflineDocumentReceived.name())))
            .accept(APPLICATION_JSON))
            .andDo(print())
            .andExpect(
                status().isOk())
            .andExpect(
                jsonPath("$.state").value(FinalOrderRequested.name()));
    }

    @Test
    void shouldTriggerAboutToSubmitCallbackAndMoveCaseStateToUserSelectedStateIfDocumentTypeOtherSelected()
        throws Exception {

        final CaseData caseData = caseData();
        caseData.setDocuments(CaseDocuments.builder().typeOfDocumentAttached(OTHER).build());
        caseData.setApplication(Application.builder()
            .stateToTransitionApplicationTo(IssuedToBailiff)
            .build());

        caseData.getApplicant2().setLegalProceedings(YES);
        caseData.getApplicant2().setLegalProceedingsDetails("some description");

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        final var jsonStringResponse = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                .header(AUTHORIZATION, TEST_SYSTEM_AUTHORISATION_TOKEN)
                .content(
                    objectMapper.writeValueAsString(
                        callbackRequest(caseData, CASEWORKER_OFFLINE_DOCUMENT_VERIFIED)))
                .accept(APPLICATION_JSON))
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(jsonStringResponse)
            .when(TREATING_NULL_AS_ABSENT)
            .isEqualTo(expectedResponse(CASEWORKER_OFFLINE_DOCUMENT_VERIFIED_OTHER_RESPONSE));
    }

    @Test
    void shouldTriggerAboutToSubmitCallbackAndSetDueDateAndMoveCaseStateToUserSelectedStateIfDocumentTypeOtherAndIsTransitioningToHolding()
        throws Exception {

        final CaseData caseData = caseData();
        caseData.setDocuments(CaseDocuments.builder().typeOfDocumentAttached(OTHER).build());
        caseData.setApplication(Application.builder()
            .issueDate(LocalDate.of(2022, 01, 01))
            .stateToTransitionApplicationTo(Holding)
            .build());

        caseData.getApplicant2().setLegalProceedings(YES);
        caseData.getApplicant2().setLegalProceedingsDetails("some description");

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        final var jsonStringResponse = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                .header(AUTHORIZATION, TEST_SYSTEM_AUTHORISATION_TOKEN)
                .content(
                    objectMapper.writeValueAsString(
                        callbackRequest(caseData, CASEWORKER_OFFLINE_DOCUMENT_VERIFIED)))
                .accept(APPLICATION_JSON))
            .andReturn()
            .getResponse()
            .getContentAsString();

        DocumentContext jsonDocument = JsonPath.parse(expectedResponse(CASEWORKER_OFFLINE_DOCUMENT_VERIFIED_OTHER_RESPONSE));
        jsonDocument.set("data.stateToTransitionApplicationTo", "Holding");
        jsonDocument.set("state", "Holding");
        jsonDocument.put("data", "dueDate", "2022-05-22");
        jsonDocument.put("data", "issueDate", "2022-01-01");


        assertThatJson(jsonStringResponse)
            .when(TREATING_NULL_AS_ABSENT)
            .isEqualTo(jsonDocument.json());
    }

    @Test
    void shouldTriggerAboutToStartAndSetDocumentNamesInDynamicListWhenThereAreScannedDocuments() throws Exception {
        final ListValue<ScannedDocument> doc1 = ListValue.<ScannedDocument>builder()
            .value(
                ScannedDocument
                    .builder()
                    .fileName(FILENAME)
                    .type(ScannedDocumentType.OTHER)
                    .subtype("aos")
                    .build()
            )
            .build();

        final CaseData caseData = CaseData.builder()
            .documents(CaseDocuments.builder()
                .scannedDocuments(singletonList(doc1))
                .build())
            .build();

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        final var jsonStringResponse = mockMvc.perform(post(ABOUT_TO_START_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                .header(AUTHORIZATION, TEST_SYSTEM_AUTHORISATION_TOKEN)
                .content(
                    objectMapper.writeValueAsString(
                        callbackRequest(caseData, CASEWORKER_OFFLINE_DOCUMENT_VERIFIED)))
                .accept(APPLICATION_JSON))
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(jsonStringResponse)
            .when(TREATING_NULL_AS_ABSENT)
            .isEqualTo(expectedResponse("classpath:caseworker-offline-document-about-to-start-response.json"));
    }

    @Test
    public void shouldTriggerSubmittedCallbackAndSendAosResponseLetterToApplicant() throws Exception {

        RetiredFields retiredFields = new RetiredFields();
        retiredFields.setDataVersion(5);

        CaseData data = CaseData.builder()
            .divorceOrDissolution(DivorceOrDissolution.DIVORCE)
            .applicationType(JOINT_APPLICATION)
            .application(Application.builder()
                    .issueDate(LOCAL_DATE)
                    .applicant1HelpWithFees(HelpWithFees.builder()
                            .build())
                    .applicant2HelpWithFees(HelpWithFees.builder()
                            .build())
                    .build())
            .applicant1(Applicant.builder()
                    .solicitorRepresented(NO)
                    .offline(YES)
                    .solicitor(Solicitor.builder()
                            .build())
                    .build())
            .acknowledgementOfService(AcknowledgementOfService.builder()
                    .howToRespondApplication(WITHOUT_DISPUTE_DIVORCE)
                    .build())
            .applicant2(Applicant.builder()
                    .solicitorRepresented(NO)
                    .solicitor(Solicitor.builder()
                            .build())
                    .offline(YES)
                    .build())
            .dueDate(LOCAL_DATE)
            .noticeOfChange(NoticeOfChange.builder()
                    .build())
            .retiredFields(retiredFields)
            .caseInvite(CaseInvite.builder()
                    .build())
            .build();

        final ListValue<ScannedDocument> doc1 = ListValue.<ScannedDocument>builder()
                .value(
                        ScannedDocument
                                .builder()
                                .fileName(FILENAME)
                                .type(ScannedDocumentType.OTHER)
                                .subtype("aos")
                                .build()
                )
                .build();

        data.setDocuments(CaseDocuments.builder()
                        .scannedDocuments(singletonList(doc1))
                        .typeOfDocumentAttached(AOS_D10)
                        .build());

        mockMvc.perform(post(SUBMITTED_URL)
                        .contentType(APPLICATION_JSON)
                        .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                        .header(AUTHORIZATION, TEST_SYSTEM_AUTHORISATION_TOKEN)
                        .content(objectMapper.writeValueAsString(callbackRequest(data, CASEWORKER_OFFLINE_DOCUMENT_VERIFIED)))
                        .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(aosPackPrinter).sendAosResponseLetterToApplicant(any(), eq(TEST_CASE_ID));
        verifyNoMoreInteractions(aosPackPrinter);
    }

    @Test
    public void shouldTriggerSubmittedCallbackAndSendConditionalOrderLetters() throws Exception {

        RetiredFields retiredFields = new RetiredFields();
        retiredFields.setDataVersion(5);

        CaseData data = CaseData.builder()
            .divorceOrDissolution(DivorceOrDissolution.DIVORCE)
            .applicationType(JOINT_APPLICATION)
            .application(Application.builder()
                .issueDate(LOCAL_DATE)
                .applicant1HelpWithFees(HelpWithFees.builder()
                    .build())
                .applicant2HelpWithFees(HelpWithFees.builder()
                    .build())
                .build())
            .applicant1(Applicant.builder()
                .solicitorRepresented(NO)
                .offline(YES)
                .solicitor(Solicitor.builder()
                    .build())
                .build())
            .acknowledgementOfService(AcknowledgementOfService.builder()
                .howToRespondApplication(WITHOUT_DISPUTE_DIVORCE)
                .build())
            .applicant2(Applicant.builder()
                .solicitorRepresented(NO)
                .solicitor(Solicitor.builder()
                    .build())
                .offline(YES)
                .build())
            .dueDate(LOCAL_DATE)
            .noticeOfChange(NoticeOfChange.builder()
                .build())
            .retiredFields(retiredFields)
            .caseInvite(CaseInvite.builder()
                .build())
            .build();

        final ListValue<ScannedDocument> doc1 = ListValue.<ScannedDocument>builder()
            .value(
                ScannedDocument
                    .builder()
                    .fileName(FILENAME)
                    .type(ScannedDocumentType.OTHER)
                    .subtype("aos")
                    .build()
            )
            .build();

        data.setDocuments(CaseDocuments.builder()
            .scannedDocuments(singletonList(doc1))
            .typeOfDocumentAttached(CO_D84)
            .build());

        mockMvc.perform(post(SUBMITTED_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
            .header(AUTHORIZATION, TEST_SYSTEM_AUTHORISATION_TOKEN)
            .content(objectMapper.writeValueAsString(callbackRequest(data, CASEWORKER_OFFLINE_DOCUMENT_VERIFIED)))
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        verify(appliedForCoPrinter, times(2)).print(any(CaseData.class), anyLong(), any(Applicant.class));
        verifyNoMoreInteractions(appliedForCoPrinter);
    }
}
