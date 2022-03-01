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
public class ExceptionRecordSearchInputFields implements CCDConfig<ExceptionRecord, ExceptionRecordState, UserRole> {

    @Override
    public void configure(final ConfigBuilder<ExceptionRecord, ExceptionRecordState, UserRole> configBuilder) {
        final List<SearchField<UserRole>> searchFieldList = of(
            SearchField.<UserRole>builder().label("Delivery date").id("deliveryDate").build(),
            SearchField.<UserRole>builder().label("Opening date").id("openingDate").build(),
            SearchField.<UserRole>builder().label("PO Box").id("poBox").build(),
            SearchField.<UserRole>builder().label("PO Box jurisdiction").id("poBoxJurisdiction").build(),
            SearchField.<UserRole>builder().label("New case reference").id("caseReference").build(),
            SearchField.<UserRole>builder().label("Attach to case reference").id("attachToCaseReference").build(),
            SearchField.<UserRole>builder().label("Journey classification").id("journeyClassification").build(),
            SearchField.<UserRole>builder().label("Form type").id("formType").build(),
            SearchField.<UserRole>builder().label("Contains payments").id("containsPayments").build()
        );

        configBuilder.searchInputFields().fields(searchFieldList);
    }
}
