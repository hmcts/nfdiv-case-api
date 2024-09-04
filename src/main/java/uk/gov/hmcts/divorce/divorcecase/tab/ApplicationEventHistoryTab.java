package uk.gov.hmcts.divorce.divorcecase.tab;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;

@Component
public class ApplicationEventHistoryTab implements CCDConfig<CaseData, State, UserRole> {
    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        buildHistoryTab(configBuilder);
        buildHistoryTabApp2Sol(configBuilder);
    }

    private void buildHistoryTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("CaseHistory", "History")
            .forRoles(CASE_WORKER, LEGAL_ADVISOR, JUDGE, SUPER_USER, APPLICANT_1_SOLICITOR)
            .field("caseHistory");
    }

    private void buildHistoryTabApp2Sol(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("CaseHistoryApp2Sol", "History")
            .showCondition("applicationType=\"jointApplication\"")
            .forRoles(APPLICANT_2_SOLICITOR)
            .field("caseHistory");
    }
}
