package uk.gov.hmcts.divorce.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.caseworker.service.notification.GeneralEmailNotification;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_COURTADMIN_CTSC;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_COURTADMIN_RDU;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_SUPERUSER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.READ;

@Component
@Slf4j
public class CaseworkerUploadConfidentialDocument implements CCDConfig<CaseData, State, UserRole> {
    public static final String CASEWORKER_UPLOAD_CONFIDENTIAL_DOCUMENT = "caseworker-upload-confidential-document";

    @Autowired
    private GeneralEmailNotification generalEmailNotification;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_UPLOAD_CONFIDENTIAL_DOCUMENT)
            .forAllStates()
            .name("Upload confidential document")
            .description("Upload confidential document")
            .explicitGrants()
            .showSummary()
            .grant(CREATE_READ_UPDATE, CASEWORKER_COURTADMIN_CTSC, CASEWORKER_COURTADMIN_RDU)
            .grant(READ, CASEWORKER_SUPERUSER, CASEWORKER_LEGAL_ADVISOR))
            .page("uploadConfidentialDocuments")
            .pageLabel("Upload Confidential Documents")
            .optional(CaseData::getConfidentialDocumentsUploaded);
        ;
    }
}
