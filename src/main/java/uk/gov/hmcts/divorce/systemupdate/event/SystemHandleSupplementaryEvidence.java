package uk.gov.hmcts.divorce.systemupdate.event;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.UPDATE_DELETE;

@Component
public class SystemHandleSupplementaryEvidence implements CCDConfig<CaseData, State, UserRole> {

    public static final String HANDLE_EVIDENCE = "handleEvidence";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(HANDLE_EVIDENCE)
            .forAllStates()
            .name("Handle supplementary evidence")
            .description("Handle supplementary evidence")
            .grant(CREATE_READ_UPDATE, SYSTEMUPDATE)
            .grant(UPDATE_DELETE, CASE_WORKER))
            .page("handleEvidence")
            .pageLabel("Correspondence")
            .mandatory(CaseData::getEvidenceHandled);
    }
}
