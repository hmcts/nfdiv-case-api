package uk.gov.hmcts.divorce.bulkscan.endpoint;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.bulkscan.endpoint.model.CaseCreationDetails;
import uk.gov.hmcts.divorce.bulkscan.endpoint.model.input.TransformationInput;
import uk.gov.hmcts.divorce.bulkscan.endpoint.model.output.SuccessfulTransformationResponse;
import uk.gov.hmcts.divorce.bulkscan.transformation.BulkScanService;

import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerCreatePaperCase.CREATE_PAPER_CASE;
import static uk.gov.hmcts.divorce.common.config.ControllerConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.divorcecase.NoFaultDivorce.getCaseType;

@RestController
@Slf4j
public class BulkScanCaseTransformationController {

    public static final String TRANSFORMATION_AND_OCR_WARNINGS = "warnings";

    @Autowired
    private BulkScanService bulkScanService;

    @PostMapping(path = "/transform-exception-record", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
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
        @Valid @RequestBody TransformationInput transformationInput
    ) {
        String exceptionRecordId = transformationInput.getId();

        log.info("Transforming Exception Record to case with Case ID: {}", exceptionRecordId);

        Map<String, Object> transformedCaseData = bulkScanService.transformBulkScanForm(transformationInput);

        SuccessfulTransformationResponse callbackResponse = SuccessfulTransformationResponse.builder()
            .caseCreationDetails(
                new CaseCreationDetails(
                    getCaseType(),
                    CREATE_PAPER_CASE,
                    transformedCaseData
                )
            )
            .warnings(deriveWarnings(transformedCaseData))
            .build();

        return ok().body(callbackResponse);
    }

    private List<String> deriveWarnings(Map<String, Object> transformedCaseData) {
        @SuppressWarnings("unchecked")
        final var warnings = (List<ListValue<String>>) transformedCaseData.get(TRANSFORMATION_AND_OCR_WARNINGS);

        if (isEmpty(warnings)) {
            return emptyList();
        }

        return warnings
            .stream()
            .map(ListValue::getValue)
            .collect(toList());
    }
}
