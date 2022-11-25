package uk.gov.hmcts.divorce.common.config.interceptors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.validators.AuthTokenValidator;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static uk.gov.hmcts.divorce.common.config.ControllerConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;

@ExtendWith(MockitoExtension.class)
public class RequestInterceptorTest {

    private static final String AUTH_TOKEN_WITH_BEARER_PREFIX = "Bearer " + TEST_AUTHORIZATION_TOKEN;

    @Mock
    private AuthTokenValidator validator;

    @InjectMocks
    private RequestInterceptor requestInterceptor;

    @BeforeEach
    public void setUp() {
        setField(requestInterceptor, "authorisedServices", List.of("ccd_data", "bulk_scan_processor", "bulk_scan_orchestrator"));
    }

    @Test
    public void shouldAppendBearerPrefixWhenServiceAuthDoesNotIncludeBearerPrefix() throws Exception {
        when(validator.getServiceName(AUTH_TOKEN_WITH_BEARER_PREFIX))
            .thenReturn("ccd_data");

        var request = mock(HttpServletRequest.class);
        var response = mock(HttpServletResponse.class);

        when(request.getHeader(SERVICE_AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);

        assertThat(requestInterceptor.preHandle(request, response, null)).isTrue();

        verify(validator).getServiceName(AUTH_TOKEN_WITH_BEARER_PREFIX);
    }

    @Test
    public void shouldNotAppendBearerPrefixWhenServiceAuthIncludesBearerPrefix() throws Exception {
        when(validator.getServiceName(AUTH_TOKEN_WITH_BEARER_PREFIX))
            .thenReturn("ccd_data");

        var request = mock(HttpServletRequest.class);
        var response = mock(HttpServletResponse.class);
        when(request.getHeader(SERVICE_AUTHORIZATION)).thenReturn(AUTH_TOKEN_WITH_BEARER_PREFIX);

        assertThat(requestInterceptor.preHandle(request, response, null)).isTrue();

        verify(validator).getServiceName(AUTH_TOKEN_WITH_BEARER_PREFIX);
    }
}
