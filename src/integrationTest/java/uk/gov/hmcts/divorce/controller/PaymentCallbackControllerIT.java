package uk.gov.hmcts.divorce.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.divorce.common.config.WebMvcConfig;
import uk.gov.hmcts.divorce.common.config.interceptors.RequestInterceptor;
import uk.gov.hmcts.divorce.divorcecase.model.PaymentStatus;
import uk.gov.hmcts.divorce.notification.NotificationService;
import uk.gov.hmcts.divorce.payment.PaymentCallbackService;
import uk.gov.hmcts.divorce.payment.model.callback.PaymentCallbackDto;
import uk.gov.hmcts.divorce.payment.model.callback.PaymentMethodDto;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.divorce.controller.PaymentCallbackController.PAYMENT_UPDATE_PATH;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class PaymentCallbackControllerIT {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RequestInterceptor requestInterceptor;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private WebMvcConfig webMvcConfig;

    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    @MockBean
    private PaymentCallbackService paymentCallbackService;

    @Test
    public void givenValidServiceAuthTokenThenProcessesPaymentCallback() throws Exception {
        PaymentCallbackDto paymentCallback = cardPaymentCallback();

        mockMvc.perform(put(PAYMENT_UPDATE_PATH)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(paymentCallback))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(paymentCallbackService).handleCallback(paymentCallback);
    }

    @Test
    public void givenMissingServiceAuthTokenThenDoesNotProcessCallback() throws Exception {
        mockMvc.perform(put(PAYMENT_UPDATE_PATH)
                        .contentType(APPLICATION_JSON)
                        .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                        .content(objectMapper.writeValueAsString(cardPaymentCallback()))
                        .accept(APPLICATION_JSON))
                .andExpect(status().is4xxClientError());

        verifyNoInteractions(paymentCallbackService);
    }

    private PaymentCallbackDto cardPaymentCallback() {
        return PaymentCallbackDto.builder()
            .status(PaymentStatus.SUCCESS.toString())
            .method(PaymentMethodDto.CARD)
            .ccdCaseNumber(TEST_CASE_ID.toString())
            .build();
    }
}
