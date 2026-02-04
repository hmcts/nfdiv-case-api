package uk.gov.hmcts.divorce.systemupdate.service;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@FeignClient(
    name = "core-case-data-api2",
    primary = false,
    url = "${core_case_data.api.url}",
    configuration = CoreCaseDataConfiguration.class
)
public interface CoreCaseDataApiWithStateModifiedDate {

    @PostMapping(value = "${ccdDataStoreAPIConfiguration.caseMatchingPath}?ctid={caseType}",
        consumes = MediaType.APPLICATION_JSON_VALUE)
    ReturnedCases runQuery(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                           @RequestHeader("ServiceAuthorization") String serviceAuthorization,
                           @PathVariable("caseType") String caseType,
                           @RequestBody String searchString);

    String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    String EXPERIMENTAL = "experimental=true";

    @RequestMapping(
        method = RequestMethod.POST,
        value = "/searchCases?ctid={caseType}",
        headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE
    )
    ReturnedCases searchCases(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorisation,
        @PathVariable("caseType") String caseType,
        @RequestBody String searchString
    );

}

