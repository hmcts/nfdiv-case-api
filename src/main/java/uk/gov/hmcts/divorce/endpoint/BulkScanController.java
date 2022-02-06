package uk.gov.hmcts.divorce.endpoint;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.bsp.common.config.BulkScanEndpoints;
import uk.gov.hmcts.reform.bsp.common.error.UnsupportedFormTypeException;
import uk.gov.hmcts.reform.bsp.common.model.shared.in.ExceptionRecord;
import uk.gov.hmcts.reform.bsp.common.model.transformation.output.CaseCreationDetails;
import uk.gov.hmcts.reform.bsp.common.model.transformation.output.SuccessfulTransformationResponse;

import java.util.Map;
import javax.validation.Valid;

import static java.lang.String.format;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.ok;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerCreatePaperCase.CREATE_PAPER_CASE;
import static uk.gov.hmcts.divorce.common.config.ControllerConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.divorcecase.NoFaultDivorce.CASE_TYPE;

@RestController
@Slf4j
public class BulkScanController {

    @Autowired
    private BulkScanService bulkScanService;

    @PostMapping(path = BulkScanEndpoints.TRANSFORM, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Transform exception record into CCD case data")
    @ApiResponses({
        @ApiResponse(code = 200, response = SuccessfulTransformationResponse.class,
            message = "Transformation of Exception Record into CCD Case Data has been successful"),
        @ApiResponse(code = 400, message = "Request failed due to malformed syntax"),
        @ApiResponse(code = 401, message = "Provided S2S token is missing or invalid"),
        @ApiResponse(code = 403, message = "Calling service is not authorised to use the endpoint"),
        @ApiResponse(code = 422, message = "Exception Record is well-formed, but contains invalid data")
    })
    public ResponseEntity<SuccessfulTransformationResponse> transformExceptionRecordIntoCase(
        @RequestHeader(name = SERVICE_AUTHORIZATION) String s2sAuthToken,
        @Valid @RequestBody ExceptionRecord exceptionRecord
    ) {
        String exceptionRecordId = exceptionRecord.getId();
        log.info("Transforming Exception Record to case with Case ID: {}", exceptionRecordId);

        ResponseEntity<SuccessfulTransformationResponse> controllerResponse;

        try {
            Map<String, Object> transformedCaseData = bulkScanService.transformBulkScanForm(exceptionRecord);

            SuccessfulTransformationResponse callbackResponse = SuccessfulTransformationResponse.builder()
                .caseCreationDetails(
                    new CaseCreationDetails(
                        CASE_TYPE,
                        CREATE_PAPER_CASE,
                        transformedCaseData
                    )
                )
                .build();

            controllerResponse = ok(callbackResponse);
        } catch (UnsupportedFormTypeException exception) {
            log.error(format("Error transforming Exception Record. Exception record ID is: %s", exceptionRecordId), exception);
            controllerResponse = ResponseEntity.unprocessableEntity().build();
        }

        return controllerResponse;
    }

}
