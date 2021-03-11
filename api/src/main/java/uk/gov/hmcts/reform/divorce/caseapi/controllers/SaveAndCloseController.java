package uk.gov.hmcts.reform.divorce.caseapi.controllers;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.authorisation.validators.AuthTokenValidator;
import uk.gov.hmcts.reform.divorce.caseapi.exceptions.UnAuthorisedServiceException;
import uk.gov.hmcts.reform.divorce.caseapi.model.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.caseapi.notification.handler.SaveAndSignOutNotificationHandler;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.divorce.caseapi.enums.NotificationConstants.SAVE_AND_CLOSE;
import static uk.gov.hmcts.reform.divorce.caseapi.enums.NotificationConstants.SUBMITTED_WEBHOOK;

@Slf4j
@RestController
@RequestMapping(SAVE_AND_CLOSE)
public class SaveAndCloseController {

    @Autowired
    private SaveAndSignOutNotificationHandler saveAndSignOutNotificationHandler;

    @Autowired
    private AuthTokenValidator tokenValidator;

    @Value("#{'${s2s-authorised.services}'.split(',')}")
    private List<String> authorisedServices;

    @PostMapping(path = SUBMITTED_WEBHOOK, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Notifies applicant by sending email using gov notify")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Applicant was successfully notified"),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 401, message = "Invalid service authorization token"),
        @ApiResponse(code = 403, message = "Service not configured to invoke callback")
    })
    public void saveAndClose(
        @RequestHeader("ServiceAuthorization") String serviceAuthToken,
        @RequestBody CcdCallbackRequest callbackRequest
    ) {
        log.info("Save and sign out callback invoked");

        String serviceName = tokenValidator.getServiceName(serviceAuthToken);
        if (!authorisedServices.contains(serviceName)) {
            throw new UnAuthorisedServiceException(serviceName);
        }

        saveAndSignOutNotificationHandler.notifyApplicant(callbackRequest.getCaseDetails().getCaseData());
    }
}
