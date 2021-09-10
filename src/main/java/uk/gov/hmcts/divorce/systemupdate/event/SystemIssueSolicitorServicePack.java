package uk.gov.hmcts.divorce.systemupdate.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingService;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_COURTADMIN_CTSC;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_COURTADMIN_RDU;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_SUPERUSER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.READ;

@Component
@Slf4j
public class SystemIssueSolicitorServicePack implements CCDConfig<CaseData, State, UserRole> {

    public static final String CASEWORKER_ISSUE_SOLICITOR_SERVICE_PACK = "system-issue-solicitor-service-pack";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        new PageBuilder(configBuilder
            .event(CASEWORKER_ISSUE_SOLICITOR_SERVICE_PACK)
            .forState(AwaitingService)
            .name("Issue Solicitor Service Pack")
            .description("Issue Solicitor Service Pack to applicant's solicitor (if service method = Solicitor Service)")
            .explicitGrants()
            .grant(CREATE_READ_UPDATE, CASEWORKER_SYSTEMUPDATE)
            .grant(READ, SOLICITOR, CASEWORKER_COURTADMIN_CTSC, CASEWORKER_COURTADMIN_RDU, CASEWORKER_SUPERUSER, CASEWORKER_LEGAL_ADVISOR)
            .retries(120, 120));
    }
}
