package uk.gov.hmcts.divorce.bulkaction.search;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.SearchField;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.util.List;

import static java.util.List.of;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.CASE_STATE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.CCD_REFERENCE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.LAST_MODIFIED_DATE;

@Component
public class BulkActionSearchResultFields implements CCDConfig<BulkActionCaseData, BulkActionState, UserRole> {

    public static final String CREATED_DATE = "[CREATED_DATE]";
    public static final String SCHEDULED_PRONOUNCEMENT_DATE = "dateAndTimeOfHearing";

    public static final List<SearchField<UserRole>> SEARCH_RESULT_FIELD_LIST = of(
        SearchField.<UserRole>builder().id(CCD_REFERENCE).label("Case Reference").build(),
        SearchField.<UserRole>builder().id(CREATED_DATE).label("Case created date").build(),
        SearchField.<UserRole>builder().id(LAST_MODIFIED_DATE).label("Last modified date").build(),
        SearchField.<UserRole>builder().id(CASE_STATE).label("Case Status").build(),
        SearchField.<UserRole>builder().id(SCHEDULED_PRONOUNCEMENT_DATE).label("Pronouncement date").build()
    );

    @Override
    public void configure(final ConfigBuilder<BulkActionCaseData, BulkActionState, UserRole> configBuilder) {
        configBuilder
            .searchResultFields()
            .fields(SEARCH_RESULT_FIELD_LIST);
    }
}
