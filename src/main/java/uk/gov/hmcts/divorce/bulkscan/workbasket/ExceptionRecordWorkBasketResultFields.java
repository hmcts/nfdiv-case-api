package uk.gov.hmcts.divorce.bulkscan.workbasket;

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
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.CCD_REFERENCE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.CREATED_DATE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.DELIVERY_DATE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.FORM_TYPE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.JOURNEY_CLASSIFICATION;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.OPENING_DATE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.PO_BOX;

@Component
public class ExceptionRecordWorkBasketResultFields implements CCDConfig<ExceptionRecord, ExceptionRecordState, UserRole> {

    @Override
    public void configure(final ConfigBuilder<ExceptionRecord, ExceptionRecordState, UserRole> configBuilder) {
        final List<SearchField<UserRole>> workBasketFieldList = of(
            SearchField.<UserRole>builder().label("Exception Id").id(CCD_REFERENCE).build(),
            SearchField.<UserRole>builder().label("Exception created date").id(CREATED_DATE).build(),
            SearchField.<UserRole>builder().label("Delivery date").id(DELIVERY_DATE).build(),
            SearchField.<UserRole>builder().label("Opening date").id(OPENING_DATE).build(),
            SearchField.<UserRole>builder().label("New case reference").id(CASE_REFERENCE).build(),
            SearchField.<UserRole>builder().label("Attach to case reference").id(ATTACH_TO_CASE_REFERENCE).build(),
            SearchField.<UserRole>builder().label("PO Box").id(PO_BOX).build(),
            SearchField.<UserRole>builder().label("Journey classification").id(JOURNEY_CLASSIFICATION).build(),
            SearchField.<UserRole>builder().label("Form type").id(FORM_TYPE).build()
        );

        configBuilder.workBasketResultFields().fields(workBasketFieldList);
    }
}
