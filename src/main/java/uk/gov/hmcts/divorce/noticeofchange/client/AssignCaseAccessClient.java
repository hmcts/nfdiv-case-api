package uk.gov.hmcts.divorce.noticeofchange.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.gov.hmcts.divorce.noticeofchange.model.CheckNocRequest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.divorce.common.config.ControllerConstants.SERVICE_AUTHORIZATION;

@FeignClient(name = "aac-client", url = "${aac.api.url}")
public interface AssignCaseAccessClient {
    @PostMapping(value = "/noc/check-noc-approval", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    AboutToStartOrSubmitCallbackResponse requestNoticeOfChange(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @RequestBody CheckNocRequest checkNocRequest
    );
}
