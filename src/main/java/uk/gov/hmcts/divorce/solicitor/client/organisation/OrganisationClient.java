package uk.gov.hmcts.divorce.solicitor.client.organisation;


import jakarta.validation.constraints.NotBlank;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.divorce.common.config.ControllerConstants.SERVICE_AUTHORIZATION;

@FeignClient(name = "organisation-client", url = "${prd.api.url}")
public interface OrganisationClient {

    @GetMapping(
        value = "/refdata/external/v1/organisations",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    OrganisationsResponse getUserOrganisation(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String s2sToken
    );

    @GetMapping("/refdata/external/v1/organisations/users/accountId")
    OrganisationUser findUserByEmail(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(CoreCaseDataApi.SERVICE_AUTHORIZATION) String serviceAuthorization,
        @RequestHeader("UserEmail") final String email
    );

    @GetMapping("/refdata/internal/v1/organisations/{orgId}/users?returnRoles=false")
    FindUsersByOrganisationResponse getOrganisationUsers(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(CoreCaseDataApi.SERVICE_AUTHORIZATION) String serviceAuthorization,
        @PathVariable("orgId") @NotBlank String organisationIdentifier
    );

    @GetMapping("/refdata/internal/v1/organisations/orgDetails/{userId}")
    OrganisationsResponse getOrganisationByUserId(
            @RequestHeader(AUTHORIZATION) String authorisation,
            @RequestHeader(CoreCaseDataApi.SERVICE_AUTHORIZATION) String serviceAuthorization,
            @PathVariable("userId") @NotBlank String userId
    );
}
