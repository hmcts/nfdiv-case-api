package uk.gov.hmcts.reform.divorce.caseapi.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.caseapi.exceptions.NotificationException;
import uk.gov.hmcts.reform.divorce.caseapi.notification.handler.SaveAndSignOutNotificationHandler;
import uk.gov.hmcts.reform.divorce.caseapi.service.NotificationService;
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
import static uk.gov.hmcts.reform.divorce.caseapi.TestConstants.API_URL;
import static uk.gov.hmcts.reform.divorce.caseapi.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.reform.divorce.caseapi.caseapi.util.TestDataHelper.caseData;
import static uk.gov.hmcts.reform.divorce.caseapi.enums.LanguagePreference.ENGLISH;


@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class CcdCallbackControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @Autowired
    @SuppressWarnings("PMD.UnusedPrivateField")
    private SaveAndSignOutNotificationHandler saveAndSignOutNotificationHandler;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void givenValidCaseDataWhenCallbackIsInvokedThenSendEmail() throws Exception {
        mockMvc.perform(post(API_URL)
            .contentType(APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(caseData()))
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(notificationService)
            .sendEmail(eq(TEST_USER_EMAIL), eq("70dd0a1e-047f-4baa-993a-e722db17d8ac"), anyMap(), eq(ENGLISH));
        verifyNoMoreInteractions(notificationService);
    }

    @Test
    public void givenRequestBodyIsNullWhenEndpointInvokedThenReturnBadRequest() throws Exception {
        mockMvc.perform(post(API_URL)
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

        mockMvc.perform(post(API_URL)
            .contentType(APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(caseData()))
            .accept(APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("All template params not passed"));

    }
}
