package uk.gov.hmcts.reform.divorce.caseapi.controllers;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.reform.divorce.caseapi.model.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.caseapi.model.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.divorce.caseapi.enums.NotificationConstants.ABOUT_TO_START_WEBHOOK;
import static uk.gov.hmcts.reform.divorce.caseapi.model.CcdCallbackResponse.convertToCcdFormat;
import static uk.gov.hmcts.reform.divorce.ccd.event.solicitor.SolicitorCreate.SOLICITOR_CREATE;

@Slf4j
@RestController
@RequestMapping(SOLICITOR_CREATE)
public class SolicitorCreateController {

    @PostMapping(path = ABOUT_TO_START_WEBHOOK, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Set default values for solicitor create petition")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Set default values successful"),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 401, message = "Invalid service authorization token"),
        @ApiResponse(code = 403, message = "Service not configured to invoke callback")
    })
    public CcdCallbackResponse aboutToStart(
        @RequestHeader("ServiceAuthorization") final String serviceAuthToken,
        @RequestBody final CcdCallbackRequest callbackRequest
    ) {

        log.info("Solicitor create petition about to start callback invoked");

        final CaseData caseData = callbackRequest.getCaseDetails().getCaseData();
        caseData.setLanguagePreferenceWelsh(YesOrNo.NO);

        return CcdCallbackResponse
            .builder()
            .data(convertToCcdFormat(caseData))
            .build();
    }
}
