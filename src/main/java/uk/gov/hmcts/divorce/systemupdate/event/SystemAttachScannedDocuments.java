package uk.gov.hmcts.divorce.systemupdate.event;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER_BSP_SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.UPDATE_DELETE;

@Component
public class SystemAttachScannedDocuments implements CCDConfig<CaseData, State, UserRole> {

    public static final String ATTACH_SCANNED_DOCS = "attachScannedDocs";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(ATTACH_SCANNED_DOCS)
            .forAllStates()
            .name("Attach scanned docs")
            .description("Attach scanned docs")
            .grant(CREATE_READ_UPDATE, CASE_WORKER_BSP_SYSTEMUPDATE)
            .grant(UPDATE_DELETE, CASE_WORKER))
            .page("attachScannedDocs")
            .pageLabel("Correspondence")
            .mandatory(CaseData::getScannedDocuments)
            .mandatory(CaseData::getEvidenceHandled);
    }
}
