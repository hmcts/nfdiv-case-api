package uk.gov.hmcts.divorce.divorcecase.tab;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.*;

@Component
public class CaseAccessAdministratorSearchTab implements CCDConfig<CaseData, State, UserRole> {

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder
            .searchCasesFields()
            .field(CCD_REFERENCE, "Case number", null, null, "1:ASC")
            .field(APPLICATION_TYPE, "Application type")
            .field(APPLICANT_2_SOLICITOR_REFERENCE, "Respondent solicitor reference")
            .field(APPLICANT_2_SOLICITOR_NAME, "Respondents solicitors name")
            .field(APPLICANT_1_LAST_NAME, "Applicants last name")
            .field(APPLICANT_2_LAST_NAME, "Respondents last name")
            .field(DUE_DATE, "Due Date")
            .createdDateField()
            .lastModifiedDate()
            .stateField();
    }
}
