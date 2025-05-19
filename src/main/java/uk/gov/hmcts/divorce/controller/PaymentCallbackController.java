package uk.gov.hmcts.divorce.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.divorce.payment.service.PaymentCallbackService;
import uk.gov.hmcts.divorce.payment.model.PaymentCallbackDto;

import static uk.gov.hmcts.divorce.common.config.ControllerConstants.SERVICE_AUTHORIZATION;

@RestController
@Slf4j
@RequiredArgsConstructor
public class PaymentCallbackController {

    public static final String PAYMENT_UPDATE_PATH = "/payment-update";

    private final PaymentCallbackService paymentCallbackService;

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
        @RequestBody PaymentCallbackDto paymentCallbackDto
    ) {
        log.info("Payment Callback Received For Case: {}", paymentCallbackDto.getCcdCaseNumber());

        paymentCallbackService.handleCallback(paymentCallbackDto);

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
