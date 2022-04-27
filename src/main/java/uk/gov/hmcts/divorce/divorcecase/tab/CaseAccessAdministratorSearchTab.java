package uk.gov.hmcts.divorce.divorcecase.tab;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

@Component
public class CaseAccessAdministratorSearchTab implements CCDConfig<CaseData, State, UserRole> {

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder
            .searchCasesFields()
            .field("[CASE_REFERENCE]", "Case number", null, null, "1:ASC")
            .field("applicationType", "Application type")
            .field("applicant2SolicitorReference", "Respondent solicitor reference")
            .field("applicant2SolicitorName", "Respondents solicitors name")
            .field("applicant1LastName", "Applicants last name")
            .field("applicant2LastName", "Respondents last name")
            .field("dueDate","Due Date")
            .createdDateField()
            .lastModifiedDate()
            .stateField();
    }
}
