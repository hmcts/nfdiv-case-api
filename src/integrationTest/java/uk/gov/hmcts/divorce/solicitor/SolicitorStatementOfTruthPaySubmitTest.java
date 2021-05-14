package uk.gov.hmcts.divorce.solicitor;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
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
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.common.config.WebMvcConfig;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.notification.NotificationService;
import uk.gov.hmcts.divorce.payment.model.Payment;
import uk.gov.hmcts.divorce.payment.model.PaymentStatus;
import uk.gov.hmcts.divorce.testutil.CaseDataUtil;
import uk.gov.hmcts.divorce.testutil.DocumentManagementStoreUtil;
import uk.gov.hmcts.divorce.testutil.FeesUtil;
import uk.gov.hmcts.divorce.testutil.IdamUtil;
import uk.gov.hmcts.divorce.testutil.TestConstants;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.ccd.search.CaseFieldsConstants.APPLICANT_1_EMAIL;
import static uk.gov.hmcts.divorce.ccd.search.CaseFieldsConstants.APPLICANT_1_FIRST_NAME;
import static uk.gov.hmcts.divorce.ccd.search.CaseFieldsConstants.APPLICANT_1_LAST_NAME;
import static uk.gov.hmcts.divorce.ccd.search.CaseFieldsConstants.DIVORCE_COSTS_CLAIM;
import static uk.gov.hmcts.divorce.ccd.search.CaseFieldsConstants.DIVORCE_OR_DISSOLUTION;
import static uk.gov.hmcts.divorce.common.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.common.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.common.model.State.SOTAgreementPayAndSubmitRequired;
import static uk.gov.hmcts.divorce.document.model.DocumentType.DIVORCE_APPLICATION;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOL_APPLICANT_APPLICATION_SUBMITTED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOL_APPLICANT_SOLICITOR_APPLICATION_SUBMITTED;
import static uk.gov.hmcts.divorce.solicitor.event.SolicitorStatementOfTruthPaySubmit.SOLICITOR_STATEMENT_OF_TRUTH_PAY_SUBMIT;
import static uk.gov.hmcts.divorce.testutil.CaseDataUtil.CASE_DATA_SERVER;
import static uk.gov.hmcts.divorce.testutil.CaseDataUtil.stubForCcdCaseRoles;
import static uk.gov.hmcts.divorce.testutil.CaseDataUtil.stubForCcdCaseRolesUpdateFailure;
import static uk.gov.hmcts.divorce.testutil.DocumentManagementStoreUtil.DM_STORE_SERVER;
import static uk.gov.hmcts.divorce.testutil.DocumentManagementStoreUtil.stubForDocumentManagement;
import static uk.gov.hmcts.divorce.testutil.FeesUtil.FEES_SERVER;
import static uk.gov.hmcts.divorce.testutil.FeesUtil.stubForFeesLookup;
import static uk.gov.hmcts.divorce.testutil.FeesUtil.stubForFeesNotFound;
import static uk.gov.hmcts.divorce.testutil.IdamUtil.IDAM_SERVER;
import static uk.gov.hmcts.divorce.testutil.IdamUtil.stubForIdamDetails;
import static uk.gov.hmcts.divorce.testutil.IdamUtil.stubForIdamFailure;
import static uk.gov.hmcts.divorce.testutil.IdamUtil.stubForIdamToken;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_START_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTH_HEADER_VALUE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.CASEWORKER_USER_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SOLICITOR_USER_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseDataMap;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.documentWithType;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.expectedResponse;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ContextConfiguration(initializers = {
    FeesUtil.PropertiesInitializer.class,
    IdamUtil.PropertiesInitializer.class,
    DocumentManagementStoreUtil.PropertiesInitializer.class,
    CaseDataUtil.PropertiesInitializer.class})
public class SolicitorStatementOfTruthPaySubmitTest {

    private static final String CASE_WORKER_TOKEN = "test-caseworker-token";
    private static final String SOLICITOR_ROLE = "caseworker-divorce-solicitor";
    private static final String CASEWORKER_ROLE = "caseworker-divorce";
    private static final String SERVICE_AUTH_TOKEN = "test-service-auth-token";

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

    @BeforeAll
    static void setUp() {
        IDAM_SERVER.start();
        CASE_DATA_SERVER.start();
        FEES_SERVER.start();
        DM_STORE_SERVER.start();
    }

    @AfterAll
    static void tearDown() {
        IDAM_SERVER.stop();
        IDAM_SERVER.resetAll();

        CASE_DATA_SERVER.stop();
        CASE_DATA_SERVER.resetAll();

        FEES_SERVER.stop();
        FEES_SERVER.resetAll();

        DM_STORE_SERVER.stop();
        DM_STORE_SERVER.resetAll();
    }

    @Test
    public void givenValidCaseDataWhenCallbackIsInvokedThenOrderSummaryAndSolicitorRolesAreSet()
        throws Exception {
        stubForFeesLookup(objectMapper);

        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, SOLICITOR_USER_ID, SOLICITOR_ROLE);

        stubForIdamDetails(CASE_WORKER_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);

        stubForIdamToken();

        when(serviceTokenGenerator.generate()).thenReturn(SERVICE_AUTH_TOKEN);

        stubForCcdCaseRoles();

        mockMvc.perform(MockMvcRequestBuilders.post(ABOUT_TO_START_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .header(TestConstants.AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(callbackRequest(caseDataMap(), SOLICITOR_STATEMENT_OF_TRUTH_PAY_SUBMIT)))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk()
            )
            .andExpect(
                content().json(expectedCcdCallbackResponse())
            );

        verify(serviceTokenGenerator).generate();
        verifyNoMoreInteractions(serviceTokenGenerator);
    }

    @Test
    public void givenFeeEventIsNotAvailableWhenCallbackIsInvokedThenReturn404FeeEventNotFound()
        throws Exception {
        stubForFeesNotFound();

        mockMvc.perform(MockMvcRequestBuilders.post(ABOUT_TO_START_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .header(TestConstants.AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(callbackRequest(caseDataMap(), SOLICITOR_STATEMENT_OF_TRUTH_PAY_SUBMIT)))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isNotFound()
            )
            .andExpect(
                result -> assertThat(result.getResolvedException()).isExactlyInstanceOf(FeignException.NotFound.class)
            )
            .andExpect(
                result -> assertThat(requireNonNull(result.getResolvedException()).getMessage())
                    .contains("404 Fee event not found")
            );
    }

    @Test
    public void givenValidCaseDataWhenCallbackIsInvokedAndIdamUserRetrievalThrowsUnauthorizedThen401IsReturned()
        throws Exception {
        stubForFeesLookup(objectMapper);

        stubForIdamFailure();

        mockMvc.perform(MockMvcRequestBuilders.post(ABOUT_TO_START_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .header(TestConstants.AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(callbackRequest(caseDataMap(), SOLICITOR_STATEMENT_OF_TRUTH_PAY_SUBMIT)))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isUnauthorized()
            )
            .andExpect(
                result -> assertThat(result.getResolvedException()).isExactlyInstanceOf(FeignException.Unauthorized.class)
            )
            .andExpect(
                result -> assertThat(requireNonNull(result.getResolvedException()).getMessage())
                    .contains("Invalid idam credentials")
            );
    }

    @Test
    public void givenValidCaseDataWhenCallbackIsInvokedAndCcdCaseRolesUpdateThrowsForbiddenExceptionThen403IsReturned()
        throws Exception {
        stubForFeesLookup(objectMapper);

        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, SOLICITOR_USER_ID, SOLICITOR_ROLE);

        stubForIdamDetails(CASE_WORKER_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);

        stubForIdamToken();

        when(serviceTokenGenerator.generate()).thenReturn(SERVICE_AUTH_TOKEN);

        stubForCcdCaseRolesUpdateFailure();

        mockMvc.perform(MockMvcRequestBuilders.post(ABOUT_TO_START_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .header(TestConstants.AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(callbackRequest(caseDataMap(), SOLICITOR_STATEMENT_OF_TRUTH_PAY_SUBMIT)))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isForbidden()
            )
            .andExpect(
                result -> assertThat(result.getResolvedException()).isExactlyInstanceOf(FeignException.Forbidden.class)
            );
    }

    @Test
    void givenValidCaseDataWhenAboutToSubmitCallbackIsInvokedThenStateIsChangedAndEmailIsSentToApplicant()
        throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.post(ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .header(TestConstants.AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(callbackRequest(
                caseDataWithStatementOfTruth(),
                SOLICITOR_STATEMENT_OF_TRUTH_PAY_SUBMIT,
                SOTAgreementPayAndSubmitRequired.name())))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk()
            )
            .andExpect(
                content().json(expectedCcdAboutToSubmitCallbackResponse())
            );

        verify(notificationService)
            .sendEmail(
                eq(TEST_USER_EMAIL),
                eq(SOL_APPLICANT_APPLICATION_SUBMITTED),
                anyMap(),
                eq(ENGLISH));
        verify(notificationService)
            .sendEmail(
                eq(TEST_SOLICITOR_EMAIL),
                eq(SOL_APPLICANT_SOLICITOR_APPLICATION_SUBMITTED),
                anyMap(),
                eq(ENGLISH));

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void givenValidCaseDataAndValidPaymentWhenAboutToSubmitCallbackIsInvokedThenStateIsChangedAndEmailIsSentToApplicant()
        throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.post(ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .header(TestConstants.AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(callbackRequest(
                caseDataWithStatementOfTruth(),
                SOLICITOR_STATEMENT_OF_TRUTH_PAY_SUBMIT,
                SOTAgreementPayAndSubmitRequired.name())))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk()
            )
            .andExpect(
                content().json(expectedCcdAboutToSubmitCallbackResponse())
            );

        verify(notificationService)
            .sendEmail(
                eq(TEST_USER_EMAIL),
                eq(SOL_APPLICANT_APPLICATION_SUBMITTED),
                anyMap(),
                eq(ENGLISH));
        verify(notificationService)
            .sendEmail(
                eq(TEST_SOLICITOR_EMAIL),
                eq(SOL_APPLICANT_SOLICITOR_APPLICATION_SUBMITTED),
                anyMap(),
                eq(ENGLISH));

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void givenValidCaseDataAndIncompletePaymentWhenAboutToSubmitCallbackIsInvokedThenStateIsNotChangedAndErrorIsReturned()
        throws Exception {

        Map<String, Object> caseData = caseDataWithStatementOfTruth();

        ListValue<Payment> payment = new ListValue<>(null, Payment
            .builder()
            .paymentAmount(100)
            .paymentChannel("online")
            .paymentFeeId("FEE0001")
            .paymentReference("paymentRef")
            .paymentSiteId("AA04")
            .paymentStatus(PaymentStatus.SUCCESS)
            .paymentTransactionId("ge7po9h5bhbtbd466424src9tk")
            .build());
        caseData.put("payments", singletonList(payment));

        mockMvc.perform(MockMvcRequestBuilders.post(ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .header(TestConstants.AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(callbackRequest(
                caseData,
                SOLICITOR_STATEMENT_OF_TRUTH_PAY_SUBMIT,
                SOTAgreementPayAndSubmitRequired.name())))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk()
            )
            .andExpect(
                content().json(expectedCcdAboutToSubmitCallbackPaymentErrorResponse())
            );

        verify(notificationService)
            .sendEmail(
                eq(TEST_USER_EMAIL),
                eq(SOL_APPLICANT_APPLICATION_SUBMITTED),
                anyMap(),
                eq(ENGLISH));
        verify(notificationService)
            .sendEmail(
                eq(TEST_SOLICITOR_EMAIL),
                eq(SOL_APPLICANT_SOLICITOR_APPLICATION_SUBMITTED),
                anyMap(),
                eq(ENGLISH));

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void givenValidCaseDataContainingDraftApplicationDocumentWhenAboutToSubmitCallbackIsInvokedThenDraftApplicationDocumentIsRemoved()
        throws Exception {
        Map<String, Object> caseData = caseDataWithStatementOfTruth();

        ListValue<DivorceDocument> documentListValue = documentWithType(DIVORCE_APPLICATION);

        List<ListValue<DivorceDocument>> generatedDocuments = singletonList(documentListValue);
        caseData.put("documentsGenerated", generatedDocuments);

        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, SOLICITOR_USER_ID, SOLICITOR_ROLE);

        when(serviceTokenGenerator.generate()).thenReturn(SERVICE_AUTH_TOKEN);

        String documentUuid = FilenameUtils.getName(documentListValue.getValue().getDocumentLink().getUrl());
        stubForDocumentManagement(documentUuid, OK);

        mockMvc.perform(MockMvcRequestBuilders.post(ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .header(TestConstants.AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(callbackRequest(
                caseData,
                SOLICITOR_STATEMENT_OF_TRUTH_PAY_SUBMIT,
                SOTAgreementPayAndSubmitRequired.name())))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk()
            )
            .andExpect(
                content().json(expectedCcdAboutToSubmitCallbackResponse())
            );

        verify(notificationService)
            .sendEmail(eq(TEST_USER_EMAIL), eq(SOL_APPLICANT_APPLICATION_SUBMITTED), anyMap(), eq(ENGLISH));

        verify(notificationService)
            .sendEmail(eq(TEST_SOLICITOR_EMAIL), eq(SOL_APPLICANT_SOLICITOR_APPLICATION_SUBMITTED), anyMap(), eq(ENGLISH));

        verify(serviceTokenGenerator).generate();

        verifyNoMoreInteractions(notificationService, serviceTokenGenerator);
    }

    @Test
    void givenCaseDataWithApplicationDocumentAndServiceNotWhitelistedInDocStoreWhenAboutToSubmitCallbackIsInvokedThen403IsReturned()
        throws Exception {
        Map<String, Object> caseData = caseDataWithStatementOfTruth();

        ListValue<DivorceDocument> documentListValue = documentWithType(DIVORCE_APPLICATION);

        List<ListValue<DivorceDocument>> generatedDocuments = singletonList(documentListValue);
        caseData.put("documentsGenerated", generatedDocuments);

        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, SOLICITOR_USER_ID, SOLICITOR_ROLE);

        when(serviceTokenGenerator.generate()).thenReturn(SERVICE_AUTH_TOKEN);

        String documentUuid = FilenameUtils.getName(documentListValue.getValue().getDocumentLink().getUrl());
        stubForDocumentManagement(documentUuid, FORBIDDEN);

        mockMvc.perform(MockMvcRequestBuilders.post(ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .header(TestConstants.AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(callbackRequest(
                caseData,
                SOLICITOR_STATEMENT_OF_TRUTH_PAY_SUBMIT,
                SOTAgreementPayAndSubmitRequired.name())))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isForbidden()
            );

        verify(serviceTokenGenerator).generate();
        verifyNoMoreInteractions(serviceTokenGenerator);
    }

    @Test
    void givenCaseDataWithApplicationDocumentAndServiceAuthValidationFailsInDocStoreWhenAboutToSubmitCallbackIsInvokedThen401IsReturned()
        throws Exception {
        Map<String, Object> caseData = caseDataWithStatementOfTruth();

        ListValue<DivorceDocument> documentListValue = documentWithType(DIVORCE_APPLICATION);

        List<ListValue<DivorceDocument>> generatedDocuments = singletonList(documentListValue);
        caseData.put("documentsGenerated", generatedDocuments);

        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, SOLICITOR_USER_ID, SOLICITOR_ROLE);

        when(serviceTokenGenerator.generate()).thenReturn(SERVICE_AUTH_TOKEN);

        String documentUuid = FilenameUtils.getName(documentListValue.getValue().getDocumentLink().getUrl());
        stubForDocumentManagement(documentUuid, UNAUTHORIZED);

        mockMvc.perform(MockMvcRequestBuilders.post(ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .header(TestConstants.AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(callbackRequest(
                caseData,
                SOLICITOR_STATEMENT_OF_TRUTH_PAY_SUBMIT,
                SOTAgreementPayAndSubmitRequired.name())))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isUnauthorized()
            );

        verify(serviceTokenGenerator).generate();

        verifyNoMoreInteractions(serviceTokenGenerator);
    }

    @Test
    void givenInValidCaseDataWhenAboutToSubmitCallbackIsInvokedThenStateIsNotChangedAndErrorIsReturned()
        throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.post(ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .header(TestConstants.AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(callbackRequest(
                caseDataMap(),
                SOLICITOR_STATEMENT_OF_TRUTH_PAY_SUBMIT,
                SOTAgreementPayAndSubmitRequired.name())))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk()
            )
            .andExpect(
                content().json(expectedCcdAboutToSubmitCallbackErrorResponse())
            );

        verifyNoInteractions(notificationService);
    }

    private String expectedCcdCallbackResponse() throws IOException {
        return expectedResponse("classpath:wiremock/responses/issue-fees-response.json");
    }

    private String expectedCcdAboutToSubmitCallbackResponse() throws IOException {
        return expectedResponse("classpath:wiremock/responses/about-to-submit-statement-of-truth.json");
    }

    private String expectedCcdAboutToSubmitCallbackPaymentErrorResponse() throws IOException {
        return expectedResponse("classpath:wiremock/responses/about-to-submit-statement-of-truth-payment-error.json");
    }

    private String expectedCcdAboutToSubmitCallbackErrorResponse() throws IOException {
        return expectedResponse("classpath:wiremock/responses/about-to-submit-statement-of-truth-error.json");
    }

    private Map<String, Object> caseDataWithStatementOfTruth() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(APPLICANT_1_FIRST_NAME, TEST_FIRST_NAME);
        caseData.put(APPLICANT_1_LAST_NAME, TEST_LAST_NAME);
        caseData.put(APPLICANT_1_EMAIL, TEST_USER_EMAIL);
        caseData.put(DIVORCE_OR_DISSOLUTION, DIVORCE);
        caseData.put(DIVORCE_COSTS_CLAIM, YES);
        caseData.put("statementOfTruth", YES);
        caseData.put("solSignStatementOfTruth", YES);
        caseData.put("applicant1SolicitorEmail", TEST_SOLICITOR_EMAIL);

        ListValue<Payment> payment = new ListValue<>(null, Payment
            .builder()
            .paymentAmount(55000)
            .paymentChannel("online")
            .paymentFeeId("FEE0001")
            .paymentReference("paymentRef")
            .paymentSiteId("AA04")
            .paymentStatus(PaymentStatus.SUCCESS)
            .paymentTransactionId("ge7po9h5bhbtbd466424src9tk")
            .build());
        caseData.put("payments", singletonList(payment));

        return caseData;
    }
}

