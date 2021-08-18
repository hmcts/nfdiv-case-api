package uk.gov.hmcts.divorce.divorcecase.search;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

@Component
public class SearchCasesResultFields implements CCDConfig<CaseData, State, UserRole> {

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder
            .searchCasesFields()
            .field("[CASE_REFERENCE]", "Case Number", null, null, "1:ASC")
            .createdDateField()
            .field("applicant1LastName", "App. last name")
            .lastModifiedDate()
            .stateField();
    }
}
