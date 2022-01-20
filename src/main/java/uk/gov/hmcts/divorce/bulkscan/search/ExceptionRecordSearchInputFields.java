package uk.gov.hmcts.divorce.bulkscan.search;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.SearchField;
import uk.gov.hmcts.divorce.bulkscan.ccd.ExceptionRecordState;
import uk.gov.hmcts.divorce.bulkscan.data.ExceptionRecord;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.util.List;

import static java.util.List.of;

@Component
public class ExceptionRecordSearchInputFields implements CCDConfig<ExceptionRecord, ExceptionRecordState, UserRole> {

    @Override
    public void configure(final ConfigBuilder<ExceptionRecord, ExceptionRecordState, UserRole> configBuilder) {
        final List<SearchField> searchFieldList = of(
            SearchField.builder().label("Delivery date").id("deliveryDate").build(),
            SearchField.builder().label("Opening date").id("openingDate").build(),
            SearchField.builder().label("PO Box").id("poBox").build(),
            SearchField.builder().label("PO Box jurisdiction").id("poBoxJurisdiction").build(),
            SearchField.builder().label("New case reference").id("caseReference").build(),
            SearchField.builder().label("Attach to case reference").id("attachToCaseReference").build(),
            SearchField.builder().label("Journey classification").id("journeyClassification").build(),
            SearchField.builder().label("Form type").id("formType").build(),
            SearchField.builder().label("Contains payments").id("containsPayments").build()
        );

        configBuilder.searchInputFields().fields(searchFieldList);
    }
}
