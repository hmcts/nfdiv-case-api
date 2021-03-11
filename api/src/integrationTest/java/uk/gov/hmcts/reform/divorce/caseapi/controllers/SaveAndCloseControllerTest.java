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
import uk.gov.hmcts.reform.authorisation.validators.AuthTokenValidator;
import uk.gov.hmcts.reform.divorce.caseapi.exceptions.NotificationException;
import uk.gov.hmcts.reform.divorce.caseapi.notification.handler.SaveAndSignOutNotificationHandler;
import uk.gov.hmcts.reform.divorce.caseapi.service.NotificationService;
import uk.gov.service.notify.NotificationClientException;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.caseapi.TestConstants.API_URL;
import static uk.gov.hmcts.reform.divorce.caseapi.TestConstants.AUTH_HEADER_VALUE;
import static uk.gov.hmcts.reform.divorce.caseapi.TestConstants.CCD_DATA;
import static uk.gov.hmcts.reform.divorce.caseapi.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.reform.divorce.caseapi.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.reform.divorce.caseapi.caseapi.util.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.reform.divorce.caseapi.enums.LanguagePreference.ENGLISH;


@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class SaveAndCloseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @Autowired
    @SuppressWarnings("PMD.UnusedPrivateField")
    private SaveAndSignOutNotificationHandler saveAndSignOutNotificationHandler;

    @MockBean
    private AuthTokenValidator validator;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void givenValidCaseDataWhenCallbackIsInvokedThenSendEmail() throws Exception {
        when(validator.getServiceName(AUTH_HEADER_VALUE)).thenReturn(CCD_DATA);

        mockMvc.perform(post(API_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .content(objectMapper.writeValueAsString(callbackRequest()))
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(notificationService)
            .sendEmail(eq(TEST_USER_EMAIL), eq("70dd0a1e-047f-4baa-993a-e722db17d8ac"), anyMap(), eq(ENGLISH));
        verify(validator).getServiceName(AUTH_HEADER_VALUE);

        verifyNoMoreInteractions(notificationService, validator);
    }

    @Test
    public void givenRequestBodyIsNullWhenEndpointInvokedThenReturnBadRequest() throws Exception {
        mockMvc.perform(post(API_URL)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void givenSendEmailThrowsExceptionWhenCallbackIsInvokedThenReturnBadRequest() throws Exception {
        when(validator.getServiceName(AUTH_HEADER_VALUE)).thenReturn(CCD_DATA);

        doThrow(new NotificationException(new NotificationClientException("All template params not passed")))
            .when(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq("70dd0a1e-047f-4baa-993a-e722db17d8ac"),
            anyMap(),
            eq(ENGLISH));

        mockMvc.perform(post(API_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .content(objectMapper.writeValueAsString(callbackRequest()))
            .accept(APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("All template params not passed"));

        verify(validator).getServiceName(AUTH_HEADER_VALUE);
    }
}
