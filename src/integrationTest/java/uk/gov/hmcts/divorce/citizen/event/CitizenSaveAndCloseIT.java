package uk.gov.hmcts.divorce.citizen.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.divorce.citizen.notification.SaveAndSignOutNotificationHandler;
import uk.gov.hmcts.divorce.common.config.WebMvcConfig;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.notification.NotificationService;
import uk.gov.hmcts.divorce.notification.exception.NotificationException;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.service.notify.NotificationClientException;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.divorce.citizen.event.CitizenSaveAndClose.CITIZEN_SAVE_AND_CLOSE;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SAVE_SIGN_OUT;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTH_HEADER_VALUE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SUBMITTED_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseDataWithOrderSummary;


@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class CitizenSaveAndCloseIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    @SuppressWarnings("PMD.UnusedPrivateField")
    private SaveAndSignOutNotificationHandler saveAndSignOutNotificationHandler;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private IdamService idamService;

    @MockBean
    private CcdAccessService ccdAccessService;

    @MockBean
    private WebMvcConfig webMvcConfig;

    @BeforeEach
    public void setUp() {
        final var userDetails = UserDetails.builder()
            .email("test@test.com")
            .id("app1")
            .build();

        when(idamService.retrieveUser(eq(AUTH_HEADER_VALUE)))
            .thenReturn(new User(AUTH_HEADER_VALUE, userDetails));
        when(ccdAccessService.isApplicant1(anyString(), anyLong())).thenReturn(true);
    }

    @Test
    public void givenValidCaseDataWhenCallbackIsInvokedThenSendEmail() throws Exception {
        mockMvc.perform(post(SUBMITTED_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .header(AUTHORIZATION, AUTH_HEADER_VALUE)
            .content(objectMapper.writeValueAsString(callbackRequest(caseDataWithOrderSummary(), CITIZEN_SAVE_AND_CLOSE)))
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(notificationService)
            .sendEmail(eq(TEST_USER_EMAIL), eq(SAVE_SIGN_OUT), anyMap(), eq(ENGLISH));

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    public void givenRequestBodyIsNullWhenEndpointInvokedThenReturnBadRequest() throws Exception {
        mockMvc.perform(post(SUBMITTED_URL)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .header(AUTHORIZATION, AUTH_HEADER_VALUE)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void givenSendEmailThrowsExceptionWhenCallbackIsInvokedThenReturnBadRequest() throws Exception {
        doThrow(new NotificationException(new NotificationClientException("All template params not passed")))
            .when(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(SAVE_SIGN_OUT),
            anyMap(),
            eq(ENGLISH));

        mockMvc.perform(post(SUBMITTED_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .header(AUTHORIZATION, AUTH_HEADER_VALUE)
            .content(objectMapper.writeValueAsString(callbackRequest(caseDataWithOrderSummary(), CITIZEN_SAVE_AND_CLOSE)))
            .accept(APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("All template params not passed"));
    }
}
