package uk.gov.hmcts.reform.divorce.caseapi.controllers.event;

import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;

import static uk.gov.hmcts.reform.divorce.ccd.event.SaveAndClose.SAVE_AND_CLOSE;
import static uk.gov.hmcts.reform.divorce.ccd.model.Constants.SUBMITTED_WEBHOOK;


@RestController()
@RequestMapping(SAVE_AND_CLOSE)
@Slf4j
public class SaveAndCloseController {

    @PostMapping(path = SUBMITTED_WEBHOOK)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success"),
        @ApiResponse(code = 401, message = "User Not Authenticated"),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<Void> saveAndClose(@RequestBody @ApiParam("CaseData") CaseData caseData) {
        log.info(caseData.toString());
        return ResponseEntity.ok().build();
    }
}
