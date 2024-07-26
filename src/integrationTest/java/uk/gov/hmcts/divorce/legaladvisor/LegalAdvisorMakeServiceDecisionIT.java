package uk.gov.hmcts.divorce.legaladvisor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
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
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.config.WebMvcConfig;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.notification.EmailTemplateName;
import uk.gov.hmcts.divorce.notification.NotificationService;
import uk.gov.hmcts.divorce.testutil.DocAssemblyWireMock;
import uk.gov.hmcts.divorce.testutil.IdamWireMock;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.time.Clock;
import java.time.LocalDate;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static net.javacrumbs.jsonunit.core.Option.TREATING_NULL_AS_ABSENT;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType.DEEMED;
import static uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType.DISPENSED;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceApplicationRefusalReason.ADMIN_REFUSAL;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ServiceAdminRefusal;
import static uk.gov.hmcts.divorce.legaladvisor.event.LegalAdvisorMakeServiceDecision.LEGAL_ADVISOR_SERVICE_DECISION;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SERVICE_APPLICATION_GRANTED_SOLICITOR;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SERVICE_APPLICATION_REJECTED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SERVICE_APPLICATION_REJECTED_SOLICITOR;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.DocAssemblyWireMock.stubForDocAssemblyWith;
import static uk.gov.hmcts.divorce.testutil.IdamWireMock.SYSTEM_USER_ROLE;
import static uk.gov.hmcts.divorce.testutil.IdamWireMock.stubForIdamDetails;
import static uk.gov.hmcts.divorce.testutil.IdamWireMock.stubForIdamToken;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_USER_USER_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SYSTEM_AUTHORISATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getApplicant;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.expectedResponse;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@DirtiesContext
@ContextConfiguration(initializers = {
    DocAssemblyWireMock.PropertiesInitializer.class,
    IdamWireMock.PropertiesInitializer.class
})
public class LegalAdvisorMakeServiceDecisionIT {

    private static final String UUID = "5cd725e8-f053-4493-9cbe-bb69d1905ae3";
    private static final String SERVICE_ORDER_TEMPLATE_FILE = "FL-NFD-GOR-ENG-Service-Order-V4.docx";
    private static final String SERVICE_ORDER_REFUSAL_TEMPLATE_FILE = "NFD_Refusal_Order_Deemed_Dispensed_Service_V2.docx";
    private static final String SERVICE_ORDER_REFUSAL_TEMPLATE_FILE_WELSH = "NFD_Refusal_Order_Deemed_Dispensed_Service_V2_Cy.docx";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private Clock clock;

    @MockBean
    private AuthTokenGenerator serviceTokenGenerator;

    @MockBean
    private WebMvcConfig webMvcConfig;

    @MockBean
    private NotificationService notificationService;

    @BeforeAll
    static void setUp() {
        DocAssemblyWireMock.start();
        IdamWireMock.start();
    }

    @AfterAll
    static void tearDown() {
        DocAssemblyWireMock.stopAndReset();
        IdamWireMock.stopAndReset();
    }

    @Test
    public void shouldUpdateStateToHoldingAndSetDecisionDateAndGenerateOrderToDispenseDocIfApplicationIsGrantedAndTypeIsDispensed()
        throws Exception {
        setMockClock(clock);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubForDocAssemblyWith(UUID, SERVICE_ORDER_TEMPLATE_FILE);

        final CaseData caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .divorceOrDissolution(DivorceOrDissolution.DIVORCE)
            .alternativeService(
                AlternativeService
                    .builder()
                    .deemedServiceDate(LocalDate.now(clock))
                    .alternativeServiceType(DISPENSED)
                    .serviceApplicationGranted(YES)
                    .receivedServiceApplicationDate(LocalDate.of(2021, 6, 18))
                    .build()
            )
            .build();
        caseData.getApplication().setIssueDate(LocalDate.of(2021, 7, 1));

        String response = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                        callbackRequest(
                            caseData,
                            LEGAL_ADVISOR_SERVICE_DECISION)
                    )
                )
                .accept(APPLICATION_JSON))
            .andDo(print())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response)
            .when(TREATING_NULL_AS_ABSENT)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(
                "classpath:legal-advisor-service-decision-dispensed-response.json"
            )));

        verify(notificationService, never()).sendEmail(anyString(), any(EmailTemplateName.class), anyMap(), eq(ENGLISH), anyLong());
    }

    @Test
    public void shouldUpdateStateToHoldingAndSetDecisionDateAndGenerateDeemedServiceOrderDocIfApplicationIsGrantedAndTypeIsDeemed()
        throws Exception {
        setMockClock(clock);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubForDocAssemblyWith(UUID, SERVICE_ORDER_TEMPLATE_FILE);

        final CaseData caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .divorceOrDissolution(DivorceOrDissolution.DIVORCE)
            .alternativeService(
                AlternativeService
                    .builder()
                    .deemedServiceDate(LocalDate.now(clock))
                    .alternativeServiceType(DEEMED)
                    .serviceApplicationGranted(YES)
                    .deemedServiceDate(LocalDate.of(2021, 6, 20))
                    .receivedServiceApplicationDate(LocalDate.of(2021, 6, 18))
                    .build()
            )
            .build();
        caseData.getApplication().setIssueDate(LocalDate.of(2021, 7, 1));

        String response = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                        callbackRequest(
                            caseData,
                            LEGAL_ADVISOR_SERVICE_DECISION)
                    )
                )
                .accept(APPLICATION_JSON))
            .andDo(print())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response)
            .when(TREATING_NULL_AS_ABSENT)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(
                "classpath:legal-advisor-service-decision-deemed-response.json"
            )));

        verify(notificationService, never()).sendEmail(anyString(), any(EmailTemplateName.class), anyMap(), eq(ENGLISH), anyLong());
    }

    @Test
    public void shouldUpdateStateToAwaitingAoSAndSetDecisionDateIfApplicationIsNotGrantedAndTypeIsDeemed()
        throws Exception {
        setMockClock(clock);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubForDocAssemblyWith(UUID, SERVICE_ORDER_REFUSAL_TEMPLATE_FILE);

        final CaseData caseData = CaseData.builder()
            .applicant1(getApplicant())
            .alternativeService(
                AlternativeService
                    .builder()
                    .alternativeServiceType(DEEMED)
                    .serviceApplicationGranted(NO)
                    .serviceApplicationRefusalReason("refusal reasons")
                    .receivedServiceApplicationDate(LocalDate.of(2021, 6, 18))
                    .build()
            ).application(Application.builder().issueDate(LocalDate.now(clock)).build())
            .dueDate(LocalDate.of(2021, 6, 20))
            .divorceOrDissolution(DivorceOrDissolution.DIVORCE)
            .build();

        String response = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                        callbackRequest(
                            caseData,
                            LEGAL_ADVISOR_SERVICE_DECISION)
                    )
                )
                .accept(APPLICATION_JSON))
            .andDo(print())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response)
            .when(TREATING_NULL_AS_ABSENT)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(
                "classpath:legal-advisor-service-decision-deemed-not-granted-response.json"
            )));

        verify(notificationService).sendEmail(eq(TEST_USER_EMAIL), eq(SERVICE_APPLICATION_REJECTED), anyMap(), eq(ENGLISH), anyLong());
    }

    @Test
    public void shouldUpdateStateToAwaitingAoSAndSetDecisionDateIfApplicationIsNotGrantedAndTypeIsDispensed()
        throws Exception {
        setMockClock(clock);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubForDocAssemblyWith(UUID, SERVICE_ORDER_REFUSAL_TEMPLATE_FILE);

        final CaseData caseData = CaseData.builder()
            .applicant1(getApplicant())
            .alternativeService(
                AlternativeService
                    .builder()
                    .alternativeServiceType(DISPENSED)
                    .serviceApplicationGranted(NO)
                    .serviceApplicationRefusalReason("refusal reasons")
                    .receivedServiceApplicationDate(LocalDate.of(2021, 6, 18))
                    .build()
            ).application(Application.builder().issueDate(LocalDate.now(clock)).build())
            .dueDate(LocalDate.of(2021, 6, 20))
            .divorceOrDissolution(DivorceOrDissolution.DIVORCE)
            .build();

        String response = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                        callbackRequest(
                            caseData,
                            LEGAL_ADVISOR_SERVICE_DECISION)
                    )
                )
                .accept(APPLICATION_JSON))
            .andDo(print())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response)
            .when(TREATING_NULL_AS_ABSENT)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(
                "classpath:legal-advisor-service-decision-dispensed-not-granted-response.json"
            )));

        verify(notificationService).sendEmail(eq(TEST_USER_EMAIL), eq(SERVICE_APPLICATION_REJECTED), anyMap(), eq(ENGLISH), anyLong());
    }

    @Test
    public void shouldUpdateStateToServiceAdminRefusalIfApplicationIsNotGrantedAndRefusalReasonIsAdminRefusal()
        throws Exception {
        setMockClock(clock);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubForDocAssemblyWith(UUID, SERVICE_ORDER_REFUSAL_TEMPLATE_FILE);

        final CaseData caseData = CaseData.builder()
            .applicant1(getApplicant())
            .alternativeService(
                AlternativeService
                    .builder()
                    .alternativeServiceType(DEEMED)
                    .serviceApplicationGranted(NO)
                    .refusalReason(ADMIN_REFUSAL)
                    .serviceApplicationRefusalReason("refusal reasons")
                    .receivedServiceApplicationDate(LocalDate.of(2021, 6, 18))
                    .build()
            )
            .dueDate(LocalDate.of(2021, 6, 20))
            .divorceOrDissolution(DivorceOrDissolution.DIVORCE)
            .build();

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                        callbackRequest(
                            caseData,
                            LEGAL_ADVISOR_SERVICE_DECISION)
                    )
                )
                .accept(APPLICATION_JSON))
            .andDo(print())
            .andExpect(
                status().isOk())
            .andExpect(
                jsonPath("$.state").value(ServiceAdminRefusal.name()));
    }

    @Test
    public void shouldGenerateWelshRefusalDocumentIfApplicant1LanguagePreferenceIsWelshIfApplicationIsNotGrantedAndTypeIsDeemed()
        throws Exception {
        setMockClock(clock);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubForDocAssemblyWith(UUID, SERVICE_ORDER_REFUSAL_TEMPLATE_FILE_WELSH);

        final CaseData caseData = CaseData.builder()
            .applicant1(Applicant.builder().languagePreferenceWelsh(YES).build())
            .alternativeService(
                AlternativeService
                    .builder()
                    .alternativeServiceType(DEEMED)
                    .serviceApplicationGranted(NO)
                    .serviceApplicationRefusalReason("refusal reasons")
                    .receivedServiceApplicationDate(LocalDate.of(2021, 6, 18))
                    .build()
            )
            .dueDate(LocalDate.of(2021, 6, 20))
            .divorceOrDissolution(DivorceOrDissolution.DIVORCE)
            .build();

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                        callbackRequest(
                            caseData,
                            LEGAL_ADVISOR_SERVICE_DECISION)
                    )
                )
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void shouldGenerateWelshRefusalDocumentIfApplicant1LanguagePreferenceIsWelshIfApplicationIsNotGrantedAndTypeIsDispensed()
        throws Exception {
        setMockClock(clock);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubForDocAssemblyWith(UUID, SERVICE_ORDER_REFUSAL_TEMPLATE_FILE_WELSH);

        final CaseData caseData = CaseData.builder()
            .applicant1(Applicant.builder().languagePreferenceWelsh(YES).build())
            .alternativeService(
                AlternativeService
                    .builder()
                    .alternativeServiceType(DISPENSED)
                    .serviceApplicationGranted(NO)
                    .serviceApplicationRefusalReason("refusal reasons")
                    .receivedServiceApplicationDate(LocalDate.of(2021, 6, 18))
                    .build()
            )
            .dueDate(LocalDate.of(2021, 6, 20))
            .divorceOrDissolution(DivorceOrDissolution.DIVORCE)
            .build();

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                        callbackRequest(
                            caseData,
                            LEGAL_ADVISOR_SERVICE_DECISION)
                    )
                )
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void shouldSendRejectedEmailNotificationToSolicitorIfApplicantRepresentedAndApplicationIsNotGrantedAndTypeIsDeemed()
        throws Exception {
        setMockClock(clock);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubForDocAssemblyWith(UUID, SERVICE_ORDER_REFUSAL_TEMPLATE_FILE);

        final CaseData caseData = CaseData.builder()
            .applicant1(getApplicant())
            .alternativeService(
                AlternativeService
                    .builder()
                    .alternativeServiceType(DEEMED)
                    .serviceApplicationGranted(NO)
                    .serviceApplicationRefusalReason("refusal reasons")
                    .receivedServiceApplicationDate(LocalDate.of(2021, 6, 18))
                    .build()
            )
            .dueDate(LocalDate.of(2021, 6, 20))
            .divorceOrDissolution(DivorceOrDissolution.DIVORCE)
            .build();

        caseData.getApplicant1().setSolicitor(Solicitor.builder()
            .name(TEST_SOLICITOR_NAME)
            .email(TEST_SOLICITOR_EMAIL)
            .build());
        caseData.getApplicant1().setSolicitorRepresented(YesOrNo.YES);
        caseData.getApplication().setIssueDate(LocalDate.of(2022, 8, 10));
        caseData.setApplicationType(SOLE_APPLICATION);

        String response = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                        callbackRequest(
                            caseData,
                            LEGAL_ADVISOR_SERVICE_DECISION)
                    )
                )
                .accept(APPLICATION_JSON))
            .andDo(print())
            .andReturn()
            .getResponse()
            .getContentAsString();

        verify(notificationService).sendEmail(eq(TEST_SOLICITOR_EMAIL), eq(SERVICE_APPLICATION_REJECTED_SOLICITOR),
            anyMap(), eq(ENGLISH), anyLong());
    }

    @Test
    public void shouldSendGrantedEmailNotificationToSolicitorIfApplicantRepresentedAndApplicationIsGrantedAndTypeIsDeemed()
        throws Exception {
        setMockClock(clock);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubForDocAssemblyWith(UUID, SERVICE_ORDER_TEMPLATE_FILE);

        final CaseData caseData = CaseData.builder()
            .applicant1(getApplicant())
            .alternativeService(
                AlternativeService
                    .builder()
                    .alternativeServiceType(DEEMED)
                    .serviceApplicationGranted(YES)
                    .receivedServiceApplicationDate(LocalDate.of(2021, 6, 18))
                    .build()
            )
            .dueDate(LocalDate.of(2021, 6, 20))
            .divorceOrDissolution(DivorceOrDissolution.DIVORCE)
            .build();

        caseData.getApplicant1().setSolicitor(Solicitor.builder()
            .name(TEST_SOLICITOR_NAME)
            .email(TEST_SOLICITOR_EMAIL)
            .build());
        caseData.getApplicant1().setSolicitorRepresented(YesOrNo.YES);
        caseData.getApplication().setIssueDate(LocalDate.of(2022, 8, 10));
        caseData.setApplicationType(SOLE_APPLICATION);

        String response = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                        callbackRequest(
                            caseData,
                            LEGAL_ADVISOR_SERVICE_DECISION)
                    )
                )
                .accept(APPLICATION_JSON))
            .andDo(print())
            .andReturn()
            .getResponse()
            .getContentAsString();

        verify(notificationService).sendEmail(eq(TEST_SOLICITOR_EMAIL), eq(SERVICE_APPLICATION_GRANTED_SOLICITOR),
            anyMap(), eq(ENGLISH), anyLong());
    }
}
