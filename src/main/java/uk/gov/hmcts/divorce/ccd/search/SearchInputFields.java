package uk.gov.hmcts.divorce.ccd.search;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.SearchField;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.State;
import uk.gov.hmcts.divorce.common.model.UserRole;

import java.util.List;

import static java.util.List.of;
import static uk.gov.hmcts.divorce.ccd.search.CaseFieldsConstants.APPLICANT_EMAIL;
import static uk.gov.hmcts.divorce.ccd.search.CaseFieldsConstants.APPLICANT_FIRST_NAME;
import static uk.gov.hmcts.divorce.ccd.search.CaseFieldsConstants.APPLICANT_LAST_NAME;
import static uk.gov.hmcts.divorce.ccd.search.CaseFieldsConstants.EMAIL;
import static uk.gov.hmcts.divorce.ccd.search.CaseFieldsConstants.FIRSTNAME;
import static uk.gov.hmcts.divorce.ccd.search.CaseFieldsConstants.LASTNAME;

@Component
public class SearchInputFields implements CCDConfig<CaseData, State, UserRole> {

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        final List<SearchField> searchFieldList = of(
            SearchField.builder().label(FIRSTNAME).id(
                APPLICANT_FIRST_NAME).build(),
            SearchField.builder().label(LASTNAME).id(
                APPLICANT_LAST_NAME).build(),
            SearchField.builder().label(EMAIL).id(APPLICANT_EMAIL).build());

        configBuilder.searchInputFields().caseReferenceField();
        configBuilder.searchInputFields().fields(searchFieldList);
    }
}
