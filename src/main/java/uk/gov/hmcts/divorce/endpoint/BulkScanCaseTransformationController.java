package uk.gov.hmcts.divorce.endpoint;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.divorce.bulkscan.transformation.BulkScanService;
import uk.gov.hmcts.reform.bsp.common.config.BulkScanEndpoints;
import uk.gov.hmcts.reform.bsp.common.model.shared.in.ExceptionRecord;
import uk.gov.hmcts.reform.bsp.common.model.transformation.output.CaseCreationDetails;
import uk.gov.hmcts.reform.bsp.common.model.transformation.output.SuccessfulTransformationResponse;

import java.util.List;
import java.util.Map;
import javax.validation.Valid;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.ok;
import static uk.gov.hmcts.divorce.bulkscan.transformation.D8FormToCaseTransformer.TRANSFORMATION_AND_OCR_WARNINGS;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerCreatePaperCase.CREATE_PAPER_CASE;
import static uk.gov.hmcts.divorce.common.config.ControllerConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.divorcecase.NoFaultDivorce.CASE_TYPE;

@RestController
@Slf4j
public class BulkScanCaseTransformationController {

    @Autowired
    private BulkScanService bulkScanService;

    @PostMapping(
        path = BulkScanEndpoints.TRANSFORM,
        produces = APPLICATION_JSON_VALUE,
        consumes = "application/json;charset=UTF-8"
    )
    @Operation(summary = "Transform exception record into CCD case data")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Transformation of Exception Record into CCD Case Data has been successful",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = SuccessfulTransformationResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Request failed due to malformed syntax"),
        @ApiResponse(responseCode = "401", description = "Provided S2S token is missing or invalid"),
        @ApiResponse(responseCode = "403", description = "Calling service is not authorised to use the endpoint"),
        @ApiResponse(responseCode = "422", description = "Exception Record is well-formed, but contains invalid data")
    })
    public ResponseEntity<SuccessfulTransformationResponse> transformExceptionRecordIntoCase(
        @RequestHeader(name = SERVICE_AUTHORIZATION) String s2sAuthToken,
        @Valid @RequestBody ExceptionRecord exceptionRecord
    ) {
        String exceptionRecordId = exceptionRecord.getId();
        log.info("Transforming Exception Record to case with Case ID: {}", exceptionRecordId);

        Map<String, Object> transformedCaseData = bulkScanService.transformBulkScanForm(exceptionRecord);

        @SuppressWarnings("unchecked") final var warnings = (List<String>) transformedCaseData.get(TRANSFORMATION_AND_OCR_WARNINGS);

        SuccessfulTransformationResponse callbackResponse = SuccessfulTransformationResponse.builder()
            .caseCreationDetails(
                new CaseCreationDetails(
                    CASE_TYPE,
                    CREATE_PAPER_CASE,
                    transformedCaseData
                )
            )
            .warnings(warnings)
            .build();

        return ok().body(callbackResponse);
    }
}
