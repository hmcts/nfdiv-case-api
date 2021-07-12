package uk.gov.hmcts.divorce.document;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.divorce.common.config.ControllerConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.document.DocumentConstants.DOCUMENT_DELETE_URI;
import static uk.gov.hmcts.divorce.document.DocumentConstants.DOCUMENT_DOWNLOAD_URI;
import static uk.gov.hmcts.divorce.document.DocumentConstants.PERMANENT;
import static uk.gov.hmcts.divorce.document.DocumentConstants.USER_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.USER_ROLES;

@FeignClient(name = "document-management-api", url = "${document_management.url}")
public interface DocumentManagementClient {

    @RequestMapping(method = RequestMethod.DELETE, value = "/documents/{document_delete_uri}")
    void deleteDocument(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuth,
        @RequestHeader(USER_ROLES) String userRoles,
        @RequestHeader(USER_ID) String userId,
        @PathVariable(DOCUMENT_DELETE_URI) String documentDeleteUri,
        @RequestParam(PERMANENT) boolean permanent
    );

    @GetMapping(path = "/documents/{document_download_uri}/binary")
    ResponseEntity<Resource> downloadBinary(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuth,
        @RequestHeader(USER_ROLES) String userRoles,
        @RequestHeader(USER_ID) String userId,
        @PathVariable(DOCUMENT_DOWNLOAD_URI) String documentDownloadUri
    );
}
