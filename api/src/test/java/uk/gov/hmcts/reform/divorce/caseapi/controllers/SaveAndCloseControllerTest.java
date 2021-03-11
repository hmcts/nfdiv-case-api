package uk.gov.hmcts.reform.divorce.caseapi.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.authorisation.exceptions.InvalidTokenException;
import uk.gov.hmcts.reform.authorisation.validators.AuthTokenValidator;
import uk.gov.hmcts.reform.divorce.caseapi.exceptions.NotificationException;
import uk.gov.hmcts.reform.divorce.caseapi.notification.handler.SaveAndSignOutNotificationHandler;
import uk.gov.service.notify.NotificationClientException;

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
import static uk.gov.hmcts.reform.divorce.caseapi.TestConstants.INVALID_AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.caseapi.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.reform.divorce.caseapi.caseapi.util.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.reform.divorce.caseapi.caseapi.util.TestDataHelper.caseData;


@ExtendWith(SpringExtension.class)
@WebMvcTest
public class SaveAndCloseControllerTest {

    @MockBean
    private SaveAndSignOutNotificationHandler saveAndSignOutNotificationHandler;

    @MockBean
    private AuthTokenValidator validator;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void givenValidCaseDataWhenCallbackIsInvokedThenSendEmail() throws Exception {
        when(validator.getServiceName(AUTH_HEADER_VALUE)).thenReturn(CCD_DATA);

        mockMvc.perform(
            post(API_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .content(objectMapper.writeValueAsBytes(callbackRequest()))
        ).andExpect(
            status().isOk()
        );

        verify(saveAndSignOutNotificationHandler).notifyApplicant(caseData());
        verify(validator).getServiceName(AUTH_HEADER_VALUE);
        verifyNoMoreInteractions(saveAndSignOutNotificationHandler, validator);
    }

    @Test
    public void givenHandlerThrowsExceptionWhenCallbackIsInvokedThenReturnBadRequest() throws Exception {
        when(validator.getServiceName(AUTH_HEADER_VALUE)).thenReturn(CCD_DATA);

        doThrow(new NotificationException(new NotificationClientException("All template params not passed")))
            .when(saveAndSignOutNotificationHandler).notifyApplicant(eq(caseData()));


        mockMvc.perform(
            post(API_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .content(objectMapper.writeValueAsBytes(callbackRequest()))
        ).andExpect(
            status().isBadRequest()
        ).andExpect(
            content().string("All template params not passed")
        );

        verify(validator).getServiceName(AUTH_HEADER_VALUE);
        verifyNoMoreInteractions(validator);
    }

    @Test
    public void shouldReturn401WhenAuthTokenIsInvalid() throws Exception {
        when(validator.getServiceName(INVALID_AUTH_TOKEN))
            .thenThrow(InvalidTokenException.class);

        mockMvc.perform(
            post(API_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, INVALID_AUTH_TOKEN)
                .content(objectMapper.writeValueAsBytes(callbackRequest()))
        ).andExpect(
            status().isUnauthorized()
        );

        verify(validator).getServiceName(INVALID_AUTH_TOKEN);
        verifyNoMoreInteractions(validator);
    }

    @Test
    public void shouldReturn403WhenServiceIsNotAllowedToInvokeCallback() throws Exception {
        when(validator.getServiceName(AUTH_HEADER_VALUE)).thenReturn("some_service");
        mockMvc.perform(
            post(API_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .content(objectMapper.writeValueAsBytes(callbackRequest()))
        ).andExpect(
            status().isForbidden()
        ).andExpect(
            content().string("Service some_service not in configured list for accessing callback")
        );

        verify(validator).getServiceName(AUTH_HEADER_VALUE);
        verifyNoMoreInteractions(validator);
    }
}
