package uk.gov.hmcts.reform.divorce.caseapi.controllers;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.divorce.caseapi.model.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.caseapi.notification.handler.SaveAndSignOutNotificationHandler;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.divorce.caseapi.constants.ControllerConstants.SUBMITTED_WEBHOOK;
import static uk.gov.hmcts.reform.divorce.ccd.event.SaveAndClose.SAVE_AND_CLOSE;

@Slf4j
@RestController
@RequestMapping(SAVE_AND_CLOSE)
public class SaveAndCloseController {

    @Autowired
    private SaveAndSignOutNotificationHandler saveAndSignOutNotificationHandler;

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

        saveAndSignOutNotificationHandler.notifyApplicant(callbackRequest.getCaseDetails().getCaseData());
    }
}
