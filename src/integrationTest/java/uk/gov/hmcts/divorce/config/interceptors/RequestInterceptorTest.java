package uk.gov.hmcts.divorce.config.interceptors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.divorce.payment.FeesAndPaymentsClient;
import uk.gov.hmcts.reform.authorisation.exceptions.InvalidTokenException;
import uk.gov.hmcts.reform.authorisation.validators.AuthTokenValidator;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.divorce.TestConstants.AUTH_HEADER_VALUE;
import static uk.gov.hmcts.divorce.TestConstants.INVALID_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.TestConstants.SUBMITTED_URL;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestPropertySource(
    properties = {
        "s2s.stub=false"
    }
)
public class RequestInterceptorTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthTokenValidator validator;

    @MockBean
    private FeesAndPaymentsClient feesAndPaymentsClient;

    @Test
    public void shouldReturn401WhenAuthTokenIsInvalid() throws Exception {
        when(validator.getServiceName(INVALID_AUTH_TOKEN))
            .thenThrow(InvalidTokenException.class);

        mockMvc.perform(
            post(SUBMITTED_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, INVALID_AUTH_TOKEN)
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
            post(SUBMITTED_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
        ).andExpect(
            status().isForbidden()
        ).andExpect(
            content().string("Service some_service not in configured list for accessing callback")
        );

        verify(validator).getServiceName(AUTH_HEADER_VALUE);
        verifyNoMoreInteractions(validator);
    }
}
