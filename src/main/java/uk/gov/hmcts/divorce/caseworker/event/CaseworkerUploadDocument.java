package uk.gov.hmcts.divorce.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_COURTADMIN_CTSC;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_COURTADMIN_RDU;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_SUPERUSER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.READ;

@Component
@Slf4j
public class CaseworkerUploadDocument implements CCDConfig<CaseData, State, UserRole> {
    public static final String CASEWORKER_UPLOAD_DOCUMENT = "caseworker-upload-document";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_UPLOAD_DOCUMENT)
            .forAllStates()
            .name("Upload document")
            .description("Upload document")
            .explicitGrants()
            .showSummary(false)
            .grant(CREATE_READ_UPDATE, CASEWORKER_COURTADMIN_CTSC, CASEWORKER_COURTADMIN_RDU)
            .grant(READ, SOLICITOR, CASEWORKER_SUPERUSER, CASEWORKER_LEGAL_ADVISOR))
            .page("uploadDocument")
            .pageLabel("Upload document")
            .optional(CaseData::getDocumentsUploaded);
    }
}
