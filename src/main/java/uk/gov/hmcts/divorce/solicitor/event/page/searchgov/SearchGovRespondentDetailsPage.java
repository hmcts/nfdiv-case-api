package uk.gov.hmcts.divorce.solicitor.event.page.searchgov;

import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;
import uk.gov.hmcts.divorce.divorcecase.model.SearchGovRecordsJourneyOptions;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import java.time.LocalDate;
import java.util.Collections;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class SearchGovRespondentDetailsPage implements CcdPageConfiguration {

    public static final String ERROR_FUTURE_DOB = "The respondent's date of birth must be in the past.";
    public static final String ERROR_NI_FORMAT = "The National Insurance number must be in correct format.";

    @Override
    public void addTo(PageBuilder pageBuilder) {
        addWithShowCondition(pageBuilder, ALWAYS_SHOW);
    }

    @Override
    public void addWithShowCondition(PageBuilder pageBuilder, String pageShowCondition) {
        var page = pageBuilder.page("searchGovPartnerDetails", this::midEvent);

        if (isNotBlank(pageShowCondition)) {
            page.showCondition(pageShowCondition);
        }
        page.complex(CaseData::getApplicant1)
            .complex(Applicant::getInterimApplicationOptions)
            .complex(InterimApplicationOptions::getSearchGovRecordsJourneyOptions)
            .mandatory(SearchGovRecordsJourneyOptions::getPartnerName)
            .mandatory(SearchGovRecordsJourneyOptions::getKnowPartnerDateOfBirth)
            .mandatory(SearchGovRecordsJourneyOptions::getPartnerDateOfBirth,
                "applicant1SearchGovRecordsKnowPartnerDateOfBirth=\"Yes\"")
            .mandatory(SearchGovRecordsJourneyOptions::getPartnerApproximateAge,
                "applicant1SearchGovRecordsKnowPartnerDateOfBirth=\"No\"")
            .mandatory(SearchGovRecordsJourneyOptions::getKnowPartnerNationalInsurance)
            .mandatory(SearchGovRecordsJourneyOptions::getPartnerNationalInsurance,
                "applicant1SearchGovRecordsKnowPartnerNationalInsurance=\"Yes\"")
            .done()
            .done()
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> beforeDetails) {
        final CaseData caseData = details.getData();
        final SearchGovRecordsJourneyOptions options =
            caseData.getApplicant1().getInterimApplicationOptions().getSearchGovRecordsJourneyOptions();

        if (YesOrNo.YES.equals(options.getKnowPartnerDateOfBirth())
            && options.getPartnerDateOfBirth().isAfter(LocalDate.now())) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(details.getData())
                .errors(Collections.singletonList(ERROR_FUTURE_DOB))
                .build();
        }

        if (YesOrNo.YES.equals(options.getKnowPartnerNationalInsurance())
            && !isNationalInsuranceValid(options.getPartnerNationalInsurance())) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(details.getData())
                .errors(Collections.singletonList(ERROR_NI_FORMAT))
                .build();
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .build();
    }

    public static boolean isNationalInsuranceValid(String nationalInsuranceNumber) {
        String upperCase = nationalInsuranceNumber.toUpperCase();
        String regex = "^[A-Z]{2}\\s?\\d{2}\\s?\\d{2}\\s?\\d{2}\\s?[A-Z]{1}$";
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(upperCase).matches();
    }
}
