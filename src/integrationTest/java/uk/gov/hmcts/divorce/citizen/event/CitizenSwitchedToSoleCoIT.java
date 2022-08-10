package uk.gov.hmcts.divorce.citizen.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.divorce.common.config.WebMvcConfig;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderQuestions;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.notification.NotificationService;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDateTime;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.citizen.event.CitizenSwitchedToSoleCo.SWITCH_TO_SOLE_CO;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.CITIZEN_APPLIED_FOR_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.PARTNER_SWITCHED_TO_SOLE_CO;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTH_HEADER_VALUE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APPLICANT_2_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validJointApplicant1CaseData;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class CitizenSwitchedToSoleCoIT {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private CcdAccessService ccdAccessService;

    @MockBean
    private IdamService idamService;

    @MockBean
    private WebMvcConfig webMvcConfig;

    @BeforeAll
    static void setUp() {
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
    }

    @Test
    public void shouldSwitchApplicationTypeToSoleAndSendApplicant1Notifications() throws Exception {
        CaseData data = validJointApplicant1CaseData();
        data.setConditionalOrder(ConditionalOrder.builder()
            .conditionalOrderApplicant1Questions(ConditionalOrderQuestions.builder().submittedDate(LocalDateTime.now()).build())
            .build());
        setupMocks(true);

        String actualResponse = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .header(AUTHORIZATION, AUTH_HEADER_VALUE)
            .content(OBJECT_MAPPER.writeValueAsString(callbackRequest(data, SWITCH_TO_SOLE_CO, "ConditionalOrderPending")))
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(actualResponse)
            .inPath("$.data.applicationType")
            .isEqualTo(ApplicationType.SOLE_APPLICATION);

        assertThatJson(actualResponse)
            .inPath("$.data.switchedToSoleCo")
            .isEqualTo(YES);

        verify(notificationService)
            .sendEmail(eq(TEST_USER_EMAIL), eq(CITIZEN_APPLIED_FOR_CONDITIONAL_ORDER), anyMap(), eq(ENGLISH));
        verify(notificationService)
            .sendEmail(eq(TEST_APPLICANT_2_USER_EMAIL), eq(PARTNER_SWITCHED_TO_SOLE_CO), anyMap(), eq(ENGLISH));
        verifyNoMoreInteractions(notificationService);
    }

    @Test
    public void shouldSwitchApplicationTypeToSoleAndSendApplicant2Notifications() throws Exception {
        CaseData data = validJointApplicant1CaseData();
        data.setConditionalOrder(ConditionalOrder.builder()
            .conditionalOrderApplicant2Questions(ConditionalOrderQuestions.builder().submittedDate(LocalDateTime.now()).build())
            .build());
        setupMocks(false);

        String actualResponse = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .header(AUTHORIZATION, AUTH_HEADER_VALUE)
            .content(OBJECT_MAPPER.writeValueAsString(callbackRequest(data, SWITCH_TO_SOLE_CO, "ConditionalOrderPending")))
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(actualResponse)
            .inPath("$.data.applicationType")
            .isEqualTo(ApplicationType.SOLE_APPLICATION);

        assertThatJson(actualResponse)
            .inPath("$.data.switchedToSoleCo")
            .isEqualTo(YES);

        verify(notificationService)
            .sendEmail(eq(TEST_USER_EMAIL), eq(PARTNER_SWITCHED_TO_SOLE_CO), anyMap(), eq(ENGLISH));
        verify(notificationService)
            .sendEmail(eq(TEST_APPLICANT_2_USER_EMAIL), eq(CITIZEN_APPLIED_FOR_CONDITIONAL_ORDER), anyMap(), eq(ENGLISH));
        verifyNoMoreInteractions(notificationService);
    }

    private void setupMocks(boolean isApplicant1) {
        when(ccdAccessService.isApplicant1(anyString(), anyLong())).thenReturn(isApplicant1);
        when(idamService.retrieveSystemUpdateUserDetails())
            .thenReturn(new User("system-user-token", UserDetails.builder().build()));
    }
}
