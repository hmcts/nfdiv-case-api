package uk.gov.hmcts.reform.divorce.caseapi.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.divorce.caseapi.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.caseapi.notification.handler.SaveAndSignOutNotificationHandler;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;

import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
public class CcdCallbackController {

    @Autowired
    private SaveAndSignOutNotificationHandler saveAndSignOutNotificationHandler;

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping(path = "/notify-applicant", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Notifies applicant by sending email using gov notify")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Applicant was successfully notified", response = CallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<Void> handleSaveAndSignOutCallback(@RequestBody CcdCallbackRequest ccdCallbackRequest) {

        Map<String, Object> ccdCaseData = ccdCallbackRequest.getCaseDetails().getData();
        CaseData caseData = objectMapper.convertValue(ccdCaseData, CaseData.class);

        saveAndSignOutNotificationHandler.notifyApplicant(caseData);

        return ResponseEntity.ok().build();
    }
}
