package uk.gov.hmcts.divorce.divorcecase.search;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.SearchField;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.util.List;

import static java.util.List.of;
import static uk.gov.hmcts.ccd.sdk.api.SortOrder.FIRST;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_1_FIRST_NAME;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_1_LAST_NAME;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_2_FIRST_NAME;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_2_LAST_NAME;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_TYPE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.CCD_REFERENCE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.DUE_DATE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.LAST_MODIFIED_DATE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.LAST_STATE_MODIFIED_DATE;

@Component
public class SearchResultFields implements CCDConfig<CaseData, State, UserRole> {

    public static final List<SearchField<UserRole>> SEARCH_RESULT_FIELD_LIST = of(
        SearchField.<UserRole>builder().id(CCD_REFERENCE).label("Case Number").build(),
        SearchField.<UserRole>builder().id(APPLICANT_TYPE).label("Application Type").build(),
        SearchField.<UserRole>builder().id(APPLICANT_1_FIRST_NAME).label("Applicant's First Name").build(),
        SearchField.<UserRole>builder().id(APPLICANT_1_LAST_NAME).label("Applicant's Last Name").build(),
        SearchField.<UserRole>builder().id(APPLICANT_2_FIRST_NAME).label("Respondent's First Name").build(),
        SearchField.<UserRole>builder().id(APPLICANT_2_LAST_NAME).label("Respondent's Last Name").build(),
        SearchField.<UserRole>builder().id(DUE_DATE).label("Due Date").build(),
        SearchField.<UserRole>builder().id(LAST_MODIFIED_DATE).label("Last modified date").build(),
        SearchField.<UserRole>builder().id(LAST_STATE_MODIFIED_DATE).label("Last state modified date").order(FIRST.ASCENDING).build()
    );

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder
            .searchResultFields()
            .fields(SEARCH_RESULT_FIELD_LIST);
    }
}
