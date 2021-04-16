package uk.gov.hmcts.divorce.config.interceptors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import uk.gov.hmcts.divorce.exceptions.UnAuthorisedServiceException;
import uk.gov.hmcts.reform.authorisation.validators.AuthTokenValidator;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static uk.gov.hmcts.divorce.constants.ControllerConstants.SERVICE_AUTHORIZATION;

@Slf4j
@Component
public class RequestInterceptor implements HandlerInterceptor {

    @Autowired
    @Lazy
    private AuthTokenValidator tokenValidator;

    @Value("#{'${s2s-authorised.services}'.split(',')}")
    private List<String> authorisedServices;

    @Override
    public boolean preHandle(
        HttpServletRequest request,
        HttpServletResponse response,
        Object handler
    ) {

        String serviceAuthToken = request.getHeader(SERVICE_AUTHORIZATION);

        String serviceName = tokenValidator.getServiceName(serviceAuthToken);
        if (!authorisedServices.contains(serviceName)) {
            log.error("Service {} not allowed to trigger save and sign out callback ", serviceName);
            throw new UnAuthorisedServiceException("Service " + serviceName + " not in configured list for accessing callback");
        }
        return true;
    }
}
