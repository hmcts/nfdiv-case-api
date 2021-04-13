package uk.gov.hmcts.divorce.api.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static uk.gov.hmcts.divorce.api.ccd.event.solicitor.SolicitorCreate.SOLICITOR_CREATE;

@Slf4j
@RestController
@RequestMapping(SOLICITOR_CREATE)
public class SolicitorCreateController {

//    @Autowired
//    private SolicitorCreatePetitionService solicitorCreatePetitionService;

//    @PostMapping(path = ABOUT_TO_START_WEBHOOK, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
//    @ApiOperation(value = "Set default values for solicitor create petition")
//    @ApiResponses(value = {
//        @ApiResponse(code = 200, message = "Set default values successful"),
//        @ApiResponse(code = 400, message = "Bad Request"),
//        @ApiResponse(code = 401, message = "Invalid service authorization token"),
//        @ApiResponse(code = 403, message = "Service not configured to invoke callback")
//    })
//    public CcdCallbackResponse aboutToStart(@RequestBody final CcdCallbackRequest callbackRequest) {
//
//        log.info("Solicitor create petition about to start callback invoked");
//
//        final CaseData caseData = callbackRequest.getCaseDetails().getCaseData();
//        caseData.setLanguagePreferenceWelsh(YesOrNo.NO);
//
//        return CcdCallbackResponse
//            .builder()
//            .data(convertToCcdFormat(caseData))
//            .build();
//    }
//
//    @PostMapping(path = ABOUT_TO_SUBMIT_WEBHOOK, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
//    @ApiOperation(value = "Callback to populate missing requirement fields when creating solicitor cases.")
//    @ApiResponses(value = {
//        @ApiResponse(code = 200, message = "Populate missing requirement fields successful"),
//        @ApiResponse(code = 400, message = "Bad Request"),
//        @ApiResponse(code = 401, message = "Invalid service authorization token"),
//        @ApiResponse(code = 403, message = "Service not configured to invoke callback")
//    })
//    public CcdCallbackResponse aboutToSubmit(
//        @RequestBody final CcdCallbackRequest callbackRequest,
//        @RequestHeader("ServiceAuthorization") final String serviceAuthToken,
//        @RequestHeader("Authorization") final String idamAuthToken
//    ) {
//
//        log.info("Solicitor create petition about to submit callback invoked");
//
//        final CaseData caseData = callbackRequest.getCaseDetails().getCaseData();
//
//        final CaseData updatedCaseData = solicitorCreatePetitionService.aboutToSubmit(
//            caseData,
//            callbackRequest.getCaseDetails().getCaseId(),
//            idamAuthToken
//        );
//
//        return CcdCallbackResponse
//            .builder()
//            .data(convertToCcdFormat(updatedCaseData))
//            .build();
//    }
}
