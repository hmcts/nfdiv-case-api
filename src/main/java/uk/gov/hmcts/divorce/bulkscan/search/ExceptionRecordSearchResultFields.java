package uk.gov.hmcts.divorce.bulkscan.search;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.SearchField;
import uk.gov.hmcts.ccd.sdk.type.ExceptionRecord;
import uk.gov.hmcts.divorce.bulkscan.ccd.ExceptionRecordState;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.util.List;

import static java.util.List.of;

@Component
public class ExceptionRecordSearchResultFields implements CCDConfig<ExceptionRecord, ExceptionRecordState, UserRole> {

    @Override
    public void configure(final ConfigBuilder<ExceptionRecord, ExceptionRecordState, UserRole> configBuilder) {
        final List<SearchField<UserRole>> searchFieldList = of(
            SearchField.<UserRole>builder().label("Exception created date").id("[CREATED_DATE]").build(),
            SearchField.<UserRole>builder().label("Delivery date").id("deliveryDate").build(),
            SearchField.<UserRole>builder().label("New case reference").id("caseReference").build(),
            SearchField.<UserRole>builder().label("Attach to case reference").id("attachToCaseReference").build(),
            SearchField.<UserRole>builder().label("Form type").id("formType").build()
        );

        configBuilder.searchResultFields().fields(searchFieldList);
    }
}
