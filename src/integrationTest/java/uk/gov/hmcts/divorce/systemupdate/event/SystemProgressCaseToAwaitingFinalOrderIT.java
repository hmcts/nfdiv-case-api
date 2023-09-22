package uk.gov.hmcts.divorce.systemupdate.event;

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
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.notification.NotificationService;
import uk.gov.hmcts.divorce.testutil.DocAssemblyWireMock;
import uk.gov.hmcts.divorce.testutil.DocManagementStoreWireMock;
import uk.gov.hmcts.divorce.testutil.IdamWireMock;
import uk.gov.hmcts.divorce.testutil.SendLetterWireMock;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.document.DocumentUploadClientApi;
import uk.gov.hmcts.reform.sendletter.api.LetterStatus;
import uk.gov.hmcts.reform.sendletter.api.SendLetterResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.core.Option.TREATING_NULL_AS_ABSENT;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderPronounced;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.APPLICANT_APPLY_FOR_FINAL_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.APPLY_FOR_FINAL_ORDER_SOLICITOR;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemProgressCaseToAwaitingFinalOrder.SYSTEM_PROGRESS_CASE_TO_AWAITING_FINAL_ORDER;
import static uk.gov.hmcts.divorce.testutil.DocAssemblyWireMock.stubForDocAssemblyWith;
import static uk.gov.hmcts.divorce.testutil.DocManagementStoreWireMock.stubDownloadBinaryFromDocumentManagement;
import static uk.gov.hmcts.divorce.testutil.IdamWireMock.SYSTEM_USER_ROLE;
import static uk.gov.hmcts.divorce.testutil.IdamWireMock.stubForIdamDetails;
import static uk.gov.hmcts.divorce.testutil.IdamWireMock.stubForIdamToken;
import static uk.gov.hmcts.divorce.testutil.SendLetterWireMock.stubSendLetters;
import static uk.gov.hmcts.divorce.testutil.SendLetterWireMock.stubStatusOfSendLetter;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTH_HEADER_VALUE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SUBMITTED_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_USER_USER_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APPLICANT_2_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SYSTEM_AUTHORISATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validJointApplicant1CaseData;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.expectedResponse;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.resourceAsBytes;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ContextConfiguration(initializers = {
    DocAssemblyWireMock.PropertiesInitializer.class,
    IdamWireMock.PropertiesInitializer.class,
    DocManagementStoreWireMock.PropertiesInitializer.class,
    SendLetterWireMock.PropertiesInitializer.class
})
public class SystemProgressCaseToAwaitingFinalOrderIT {

    private static final String COVERSHEET_DOC_ID = "af678800-4c5c-491c-9b7f-22056412ff94";
    public static final String CAN_APPLY_FOR_FINAL_ORDER_DOC_ID = "c35b1868-e397-457a-aa67-ac1422bb8100";

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
    private DocumentUploadClientApi documentUploadClientApi;

    @BeforeAll
    static void setUp() {
        DocAssemblyWireMock.start();
        IdamWireMock.start();
        DocManagementStoreWireMock.start();
        SendLetterWireMock.start();
    }

    @AfterAll
    static void tearDown() {
        DocAssemblyWireMock.stopAndReset();
        IdamWireMock.stopAndReset();
        DocManagementStoreWireMock.stopAndReset();
        SendLetterWireMock.stopAndReset();
    }

    @Test
    void shouldSendLettersToOfflineApplicantsInJointCase() throws Exception {

        final CaseData caseData = validJointApplicant1CaseData();
        caseData.getApplicant1().setEmail(null);
        caseData.getApplicant2().setEmail(null);
        caseData.getApplicant1().setOffline(YES);
        caseData.getApplicant2().setOffline(YES);
        caseData.setFinalOrder(FinalOrder.builder()
            .dateFinalOrderEligibleFrom(LocalDate.now())
            .build());

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubForDocAssemblyWith(COVERSHEET_DOC_ID, "NFD_Applicant_Coversheet.docx");
        stubForDocAssemblyWith(CAN_APPLY_FOR_FINAL_ORDER_DOC_ID, "FL-NFD-GOR-ENG-Can-Apply-Final-Order_V3.docx");
        stubApplyForFinalOrderPackSendLetter();

        final var jsonStringResponse = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                .header(AUTHORIZATION, TEST_SYSTEM_AUTHORISATION_TOKEN)
                .content(
                    objectMapper.writeValueAsString(
                        callbackRequest(caseData, SYSTEM_PROGRESS_CASE_TO_AWAITING_FINAL_ORDER, ConditionalOrderPronounced.name())))
                .accept(APPLICATION_JSON))
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(jsonStringResponse)
            .when(TREATING_NULL_AS_ABSENT)
            .isEqualTo(expectedResponse(
                "classpath:wiremock/responses/about-to-submit-system-progress-case-to-awaiting-final-order-response.json"));

        verify(documentUploadClientApi, times(2)).upload(
            anyString(),
            eq(TEST_SERVICE_AUTH_TOKEN),
            eq(SYSTEM_USER_USER_ID),
            anyList()
        );
    }

    @Test
    public void shouldSendEmailsToOnlineApplicantsInJointCase() throws Exception {
        final CaseData caseData = validJointApplicant1CaseData();
        caseData.getApplicant1().setEmail(TEST_USER_EMAIL);
        caseData.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);
        caseData.setFinalOrder(FinalOrder.builder()
            .dateFinalOrderEligibleFrom(LocalDate.now())
            .dateFinalOrderEligibleToRespondent(LocalDate.now())
            .build());

        mockMvc.perform(post(SUBMITTED_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(caseData, SYSTEM_PROGRESS_CASE_TO_AWAITING_FINAL_ORDER, ConditionalOrderPronounced.name())))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(notificationService)
            .sendEmail(eq(TEST_USER_EMAIL), eq(APPLICANT_APPLY_FOR_FINAL_ORDER), anyMap(), eq(ENGLISH));

        verify(notificationService)
            .sendEmail(eq(TEST_APPLICANT_2_USER_EMAIL), eq(APPLICANT_APPLY_FOR_FINAL_ORDER), anyMap(), eq(ENGLISH));

        verifyNoMoreInteractions(notificationService);

        verifyNoInteractions(documentUploadClientApi);
    }

    @Test
    public void shouldSendEmailsToSolicitorsInJointCaseWhenBothApplicantsAreRepresented() throws Exception {
        final CaseData caseData = validJointApplicant1CaseData();
        caseData.getApplicant1().setSolicitorRepresented(YES);
        caseData.getApplicant2().setSolicitorRepresented(YES);
        caseData.getApplicant1().setSolicitor(Solicitor.builder()
                .email("app1sol@email.com")
                .name("app1 sol")
                .address("app1 sol address")
            .build());
        caseData.getApplicant2().setSolicitor(Solicitor.builder()
            .email("app2sol@email.com")
            .name("app2 sol")
            .address("app2 sol address")
            .build());
        caseData.setFinalOrder(FinalOrder.builder()
            .dateFinalOrderEligibleFrom(LocalDate.now())
            .dateFinalOrderEligibleToRespondent(LocalDate.now())
            .build());
        caseData.getConditionalOrder().setGrantedDate(LocalDate.now());
        caseData.getApplication().setIssueDate(LocalDate.now());

        mockMvc.perform(post(SUBMITTED_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(caseData, SYSTEM_PROGRESS_CASE_TO_AWAITING_FINAL_ORDER, ConditionalOrderPronounced.name())))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(notificationService)
            .sendEmail(eq("app1sol@email.com"), eq(APPLY_FOR_FINAL_ORDER_SOLICITOR), anyMap(), eq(ENGLISH));

        verify(notificationService)
            .sendEmail(eq("app2sol@email.com"), eq(APPLY_FOR_FINAL_ORDER_SOLICITOR), anyMap(), eq(ENGLISH));

        verifyNoMoreInteractions(notificationService);

        verifyNoInteractions(documentUploadClientApi);
    }

    private void stubApplyForFinalOrderPackSendLetter() throws IOException {
        final List<String> documentIds = List.of(
            COVERSHEET_DOC_ID, // Coversheet id
            CAN_APPLY_FOR_FINAL_ORDER_DOC_ID // You can apply for final order doc id
        );

        final byte[] pdfAsBytes = loadPdfAsBytes();

        for (String documentId : documentIds) {
            stubDownloadBinaryFromDocumentManagement(documentId, pdfAsBytes);
        }

        final SendLetterResponse sendLetterResponse = new SendLetterResponse(UUID.randomUUID());
        final ZonedDateTime now = ZonedDateTime.now();
        final LetterStatus letterStatus = new LetterStatus(
            sendLetterResponse.letterId,
            "OK",
            "",
            now,
            now,
            now,
            null,
            1);

        stubSendLetters(objectMapper.writeValueAsString(sendLetterResponse));
        stubStatusOfSendLetter(sendLetterResponse.letterId, objectMapper.writeValueAsString(letterStatus));
    }

    private byte[] loadPdfAsBytes() throws IOException {
        return resourceAsBytes("classpath:Test.pdf");
    }
}
