package uk.gov.hmcts.divorce.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import static uk.gov.hmcts.divorce.common.config.ControllerConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemUpdatePaymentStatus.SYSTEM_UPDATE_PAYMENT_STATUS;

@RestController
@Slf4j
@RequiredArgsConstructor
public class PaymentCallbackController {

    public static final String PAYMENT_UPDATE_PATH = "/payment-update";

    private final CcdUpdateService ccdUpdateService;

    private final IdamService idamService;

    private final AuthTokenGenerator authTokenGenerator;

    @Operation(summary = "Update payment", description = "Update payment")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Updated payment successfully"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "401", description = "Provided S2S token is missing or invalid"),
        @ApiResponse(responseCode = "403", description = "Calling service is not authorised to use the endpoint"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    @PutMapping(
        path = PAYMENT_UPDATE_PATH,
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    ) public ResponseEntity<HttpStatus> updatePayment(
        @RequestHeader(value = SERVICE_AUTHORIZATION) String s2sAuthToken,
        @RequestBody uk.gov.hmcts.divorce.controller.dto.PaymentDto paymentDto
    ) {
        log.info("Payment Callback Received For Case: {}", paymentDto.getCcdCaseNumber());

        User systemUpdateUser = idamService.retrieveSystemUpdateUserDetails();

        final String serviceAuthorization = authTokenGenerator.generate();

        ccdUpdateService.submitEvent(
            Long.parseLong(paymentDto.getCcdCaseNumber()),
            SYSTEM_UPDATE_PAYMENT_STATUS,
            systemUpdateUser,
            serviceAuthorization
        );

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/payment-test")
    public ResponseEntity<HttpStatus> updatePayment() {
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
