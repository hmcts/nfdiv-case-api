package uk.gov.hmcts.divorce.divorcecase.workbasket;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.CASE_STATE;
import static uk.gov.hmcts.divorce.divorcecase.search.SearchInputFields.SEARCH_FIELD_LIST;

@Component
public class WorkBasketInputFields implements CCDConfig<CaseData, State, UserRole> {

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.workBasketInputFields().fields(SEARCH_FIELD_LIST.stream().filter(i -> !i.getId().equals(CASE_STATE)).toList());
    }
}
