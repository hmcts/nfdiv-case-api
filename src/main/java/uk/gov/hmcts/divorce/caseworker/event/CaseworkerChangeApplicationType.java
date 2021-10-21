package uk.gov.hmcts.divorce.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.READ;

@Component
@Slf4j
public class CaseworkerChangeApplicationType implements CCDConfig<CaseData, State, UserRole> {

    public static final String CASEWORKER_CHANGE_APPLICATION_TYPE = "caseworker-change-application-type";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder<>(configBuilder
            .event(CASEWORKER_CHANGE_APPLICATION_TYPE)
            .forAllStates()
            .name("Change application type")
            .description("Change application type from joint to sole application")
            .explicitGrants()
            .grant(CREATE_READ_UPDATE,
                CASE_WORKER,
                LEGAL_ADVISOR)
            .grant(READ, SOLICITOR, SUPER_USER))
            .page("changeApplicationTypePage")
            .mandatory(CaseData::getApplicationType);
    }
}
