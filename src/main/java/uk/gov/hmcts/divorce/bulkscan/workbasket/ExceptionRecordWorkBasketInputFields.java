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
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.CONTAINS_PAYMENTS;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.FORM_TYPE;

@Component
public class ExceptionRecordWorkBasketInputFields implements CCDConfig<ExceptionRecord, ExceptionRecordState, UserRole> {

    @Override
    public void configure(final ConfigBuilder<ExceptionRecord, ExceptionRecordState, UserRole> configBuilder) {
        final List<SearchField<UserRole>> workBasketFieldList = of(
            SearchField.<UserRole>builder().label("Form type").id(FORM_TYPE).build(),
            SearchField.<UserRole>builder().label("Contains payments").id(CONTAINS_PAYMENTS).build()
        );

        configBuilder.workBasketInputFields().fields(workBasketFieldList);
    }
}
