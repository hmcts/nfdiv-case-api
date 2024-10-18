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
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.ALTERNATIVE_SERVICE_TYPE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_1_ADDRESS;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_1_EMAIL;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_1_FIRM_NAME;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_1_FIRST_NAME;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_1_HWF;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_1_LAST_NAME;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_2_ADDRESS;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_2_APPLIED_FOR_FINAL_ORDER;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_2_EMAIL;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_2_FIRST_NAME;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_2_HWF;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_2_LAST_NAME;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_2_SOL_APPLIED_FOR_FINAL_ORDER;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_TYPE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_WELSH_TRANSLATION;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.CASE_STATE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.CCD_REFERENCE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.EVIDENCE_HANDLED;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.FRAUD_REFERRAL_CASE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.GENERAL_APPLICATION_URGENT_CASE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.GENERAL_REFERRAL_TYPE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.GENERAL_REFERRAL_URGENT_CASE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.MARRIAGE_DATE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.RESPONDENT_WELSH_TRANSLATION;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.SCANNED_SUBTYPE_RECEIVED;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.SOL_PAYMENT_METHOD;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.URGENT_CASE;

@Component
public class SearchInputFields implements CCDConfig<CaseData, State, UserRole> {

    public static final List<SearchField<UserRole>> SEARCH_FIELD_LIST = of(
        SearchField.<UserRole>builder().label("CCD reference").id(CCD_REFERENCE).build(),
        SearchField.<UserRole>builder().label("Case status (state)").id(CASE_STATE).build(),
        SearchField.<UserRole>builder().label("Application Type (sole or joint)").id(APPLICANT_TYPE).build(),
        SearchField.<UserRole>builder().label("Marriage date").id(MARRIAGE_DATE).build(),
        SearchField.<UserRole>builder().label("Payment method").id(SOL_PAYMENT_METHOD).build(),
        SearchField.<UserRole>builder().label("Applicant 1 HWF reference").id(APPLICANT_1_HWF).build(),
        SearchField.<UserRole>builder().label("Applicant 2 HWF reference").id(APPLICANT_2_HWF).build(),
        SearchField.<UserRole>builder().label("Urgent case").id(URGENT_CASE).build(),
        SearchField.<UserRole>builder().label("Urgent general application case").id(GENERAL_APPLICATION_URGENT_CASE).build(),
        SearchField.<UserRole>builder().label("Urgent general referral case").id(GENERAL_REFERRAL_URGENT_CASE).build(),
        SearchField.<UserRole>builder().label("General referral type").id(GENERAL_REFERRAL_TYPE).build(),
        SearchField.<UserRole>builder().label("Fraud referral case").id(FRAUD_REFERRAL_CASE).build(),
        SearchField.<UserRole>builder().label("Solicitor firm name").id(APPLICANT_1_FIRM_NAME).build(),
        SearchField.<UserRole>builder().label("Type of service").id(ALTERNATIVE_SERVICE_TYPE).build(),
        SearchField.<UserRole>builder().label("Applicant first name").id(APPLICANT_1_FIRST_NAME).build(),
        SearchField.<UserRole>builder().label("Applicant last name").id(APPLICANT_1_LAST_NAME).build(),
        SearchField.<UserRole>builder().label("Applicant email").id(APPLICANT_1_EMAIL).build(),
        SearchField.<UserRole>builder().label("Applicant postcode").id(APPLICANT_1_ADDRESS).listElementCode("PostCode").build(),
        SearchField.<UserRole>builder().label("Respondent first name").id(APPLICANT_2_FIRST_NAME).build(),
        SearchField.<UserRole>builder().label("Respondent last name").id(APPLICANT_2_LAST_NAME).build(),
        SearchField.<UserRole>builder().label("Respondent email").id(APPLICANT_2_EMAIL).build(),
        SearchField.<UserRole>builder().label("Respondent postcode").id(APPLICANT_2_ADDRESS).listElementCode("PostCode").build(),
        SearchField.<UserRole>builder().label("Supplementary evidence handled").id(EVIDENCE_HANDLED).build(),
        SearchField.<UserRole>builder().label("Applicant Welsh Translation").id(APPLICANT_WELSH_TRANSLATION).build(),
        SearchField.<UserRole>builder().label("Respondent Welsh Translation").id(RESPONDENT_WELSH_TRANSLATION).build(),
        SearchField.<UserRole>builder().label("Scanned Document Subtype Received").id(SCANNED_SUBTYPE_RECEIVED).build(),
        SearchField.<UserRole>builder()
            .label("Respondent Solicitor Applied For Final Order")
            .id(APPLICANT_2_SOL_APPLIED_FOR_FINAL_ORDER)
            .build(),
        SearchField.<UserRole>builder()
            .label("Respondent Applied For Final Order")
            .id(APPLICANT_2_APPLIED_FOR_FINAL_ORDER)
            .build()
        );

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.searchInputFields().fields(SEARCH_FIELD_LIST);
    }
}
