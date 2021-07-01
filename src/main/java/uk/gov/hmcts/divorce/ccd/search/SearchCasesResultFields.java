package uk.gov.hmcts.divorce.ccd.search;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.State;
import uk.gov.hmcts.divorce.common.model.UserRole;

@Component
public class SearchCasesResultFields implements CCDConfig<CaseData, State, UserRole> {

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder
            .searchCasesFields()
            .field("[CASE_REFERENCE]", "Case reference", null, null, "1:ASC")
            .createdDateField()
            .field("applicant1LastName", "App. last name")
            .lastModifiedDate()
            .stateField();
    }
}
