package uk.gov.hmcts.divorce.api.ccd.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.divorce.api.clients.FeesAndPaymentsClient;
import uk.gov.hmcts.divorce.api.config.WebMvcConfig;
import uk.gov.hmcts.divorce.api.config.interceptors.RequestInterceptor;
import uk.gov.hmcts.divorce.api.exceptions.NotificationException;
import uk.gov.hmcts.divorce.api.notification.handler.SaveAndSignOutNotificationHandler;
import uk.gov.hmcts.divorce.api.service.NotificationService;
import uk.gov.service.notify.NotificationClientException;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.divorce.api.TestConstants.AUTH_HEADER_VALUE;
import static uk.gov.hmcts.divorce.api.TestConstants.SAVE_AND_CLOSE_API_URL;
import static uk.gov.hmcts.divorce.api.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.api.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.api.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.api.util.TestDataHelper.callbackRequest;


@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class SaveAndCloseTest {

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
    private RequestInterceptor requestInterceptor;

    @MockBean
    private WebMvcConfig webMvcConfig;

    @MockBean
    private FeesAndPaymentsClient feesAndPaymentsClient;

    @Test
    public void givenValidCaseDataWhenCallbackIsInvokedThenSendEmail() throws Exception {
        mockMvc.perform(post(SAVE_AND_CLOSE_API_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .content(objectMapper.writeValueAsString(callbackRequest()))
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(notificationService)
            .sendEmail(eq(TEST_USER_EMAIL), eq("70dd0a1e-047f-4baa-993a-e722db17d8ac"), anyMap(), eq(ENGLISH));

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    public void givenRequestBodyIsNullWhenEndpointInvokedThenReturnBadRequest() throws Exception {
        mockMvc.perform(post(SAVE_AND_CLOSE_API_URL)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void givenSendEmailThrowsExceptionWhenCallbackIsInvokedThenReturnBadRequest() throws Exception {
        doThrow(new NotificationException(new NotificationClientException("All template params not passed")))
            .when(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq("70dd0a1e-047f-4baa-993a-e722db17d8ac"),
            anyMap(),
            eq(ENGLISH));

        mockMvc.perform(post(SAVE_AND_CLOSE_API_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .content(objectMapper.writeValueAsString(callbackRequest()))
            .accept(APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("All template params not passed"));
    }
}
