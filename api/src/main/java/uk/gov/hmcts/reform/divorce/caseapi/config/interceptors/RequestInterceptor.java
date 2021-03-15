package uk.gov.hmcts.reform.divorce.caseapi.config.interceptors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import uk.gov.hmcts.reform.authorisation.validators.AuthTokenValidator;
import uk.gov.hmcts.reform.divorce.caseapi.exceptions.UnAuthorisedServiceException;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static uk.gov.hmcts.reform.divorce.caseapi.enums.NotificationConstants.SERVICE_AUTHORIZATION;

@Slf4j
@Component
public class RequestInterceptor implements HandlerInterceptor {

    @Autowired
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
