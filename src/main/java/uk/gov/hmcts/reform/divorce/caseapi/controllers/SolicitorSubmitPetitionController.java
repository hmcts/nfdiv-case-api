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
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.reform.divorce.caseapi.model.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.caseapi.model.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.caseapi.service.CcdAccessService;
import uk.gov.hmcts.reform.divorce.caseapi.service.SolicitorSubmitPetitionService;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.divorce.caseapi.constants.ControllerConstants.ABOUT_TO_START_WEBHOOK;
import static uk.gov.hmcts.reform.divorce.caseapi.model.CcdCallbackResponse.convertToCcdFormat;
import static uk.gov.hmcts.reform.divorce.ccd.event.solicitor.SolicitorStatementOfTruthPaySubmit.SUBMIT_PETITION;

@Slf4j
@RestController
@RequestMapping(SUBMIT_PETITION)
public class SolicitorSubmitPetitionController {

    @Autowired
    private SolicitorSubmitPetitionService solicitorSubmitPetitionService;

    @Autowired
    private CcdAccessService ccdAccessService;

    @PostMapping(path = ABOUT_TO_START_WEBHOOK, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Sets fees for issue petition and roles for solicitor")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Fees was successfully set in Case Data and roles updated in CCD"),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 401, message = "Invalid service authorization token"),
        @ApiResponse(code = 403, message = "Service not configured to invoke callback")
    })
    public CcdCallbackResponse retrieveFeesAndSetRolesForSubmitPetition(
        @RequestHeader("ServiceAuthorization") String serviceAuthToken,
        @RequestHeader("Authorization") String idamAuthToken,
        @RequestBody CcdCallbackRequest callbackRequest
    ) {
        log.info("Submit petition about to start callback invoked");

        log.info("Retrieving order summary");
        OrderSummary orderSummary = solicitorSubmitPetitionService.getOrderSummary();
        CaseData caseData = callbackRequest.getCaseDetails().getCaseData();
        caseData.setSolApplicationFeeOrderSummary(orderSummary);

        log.info("Adding Petitioner solicitor case roles");
        ccdAccessService.addPetitionerSolicitorRole(
            idamAuthToken,
            callbackRequest.getCaseDetails().getCaseId()
        );

        return CcdCallbackResponse
            .builder()
            .data(convertToCcdFormat(caseData))
            .build();
    }
}
