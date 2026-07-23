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
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.ATTACH_TO_CASE_REFERENCE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.CONTAINS_PAYMENTS;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.DELIVERY_DATE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.FORM_TYPE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.JOURNEY_CLASSIFICATION;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.OPENING_DATE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.PO_BOX;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.PO_BOX_JURISDICTION;

@Component
public class ExceptionRecordSearchInputFields implements CCDConfig<ExceptionRecord, ExceptionRecordState, UserRole> {

    @Override
    public void configure(final ConfigBuilder<ExceptionRecord, ExceptionRecordState, UserRole> configBuilder) {
        final List<SearchField<UserRole>> searchFieldList = of(
            SearchField.<UserRole>builder().label("Delivery date").id(DELIVERY_DATE).build(),
            SearchField.<UserRole>builder().label("Opening date").id(OPENING_DATE).build(),
            SearchField.<UserRole>builder().label("PO Box").id(PO_BOX).build(),
            SearchField.<UserRole>builder().label("PO Box jurisdiction").id(PO_BOX_JURISDICTION).build(),
            SearchField.<UserRole>builder().label("New case reference").id(CASE_REFERENCE).build(),
            SearchField.<UserRole>builder().label("Attach to case reference").id(ATTACH_TO_CASE_REFERENCE).build(),
            SearchField.<UserRole>builder().label("Journey classification").id(JOURNEY_CLASSIFICATION).build(),
            SearchField.<UserRole>builder().label("Form type").id(FORM_TYPE).build(),
            SearchField.<UserRole>builder().label("Contains payments").id(CONTAINS_PAYMENTS).build()
        );

        configBuilder.searchInputFields().fields(searchFieldList);
    }
}
