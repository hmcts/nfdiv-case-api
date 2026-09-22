package uk.gov.hmcts.divorce.solicitor.event.page.bailiff;

import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.BailiffServiceJourneyOptions;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import java.time.LocalDate;
import java.util.Collections;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class BailiffServiceRespondentPhoneAgePage implements CcdPageConfiguration {

    private static final String RESPONDENTS_PHONE_QUESTION_LABEL = "Do you know the respondent's phone number?";
    private static final String RESPONDENTS_PHONE_KNOWN = "applicant1BailiffKnowPartnersPhone = \"Yes\"";
    private static final String RESPONDENTS_PHONE_NUMBER_LABEL = "Respondent's Phone Number";
    private static final String RESPONDENTS_PHONE_HINT = "For international numbers include the country code";

    private static final String RESPONDENTS_DOB_QUESTION_LABEL = "Do you know the respondent's date of birth?";
    private static final String RESPONDENTS_DOB_KNOWN = "applicant1BailiffKnowPartnersDateOfBirth = \"Yes\"";
    private static final String RESPONDENTS_DOB_LABEL = "Respondent's date of birth";
    private static final String RESPONDENTS_DOB_HINT = "For example, 23 3 2007";
    private static final String RESPONDENTS_DOB_UNKNOWN = "applicant1BailiffKnowPartnersDateOfBirth = \"No\"";
    private static final String RESPONDENTS_APPROX_AGE_LABEL = "Respondent's approximate age";
    private static final String RESPONDENTS_APPROX_AGE_HINT = "For example, 65 years old";

    private static final String ERROR_FUTURE_DOB = "The respondents date of birth must be in the past.";

    @Override
    public void addTo(PageBuilder pageBuilder) {
        addWithShowCondition(pageBuilder, ALWAYS_SHOW);
    }

    @Override
    public void addWithShowCondition(PageBuilder pageBuilder, String pageShowCondition) {

        var page = pageBuilder.page("bailiffServiceRespPhoneAgePage", this::midEvent);
        if (isNotBlank(pageShowCondition)) {
            page.showCondition(pageShowCondition);
        }
        page.complex(CaseData::getApplicant1)
                .complex(Applicant::getInterimApplicationOptions)
                    .complex(InterimApplicationOptions::getBailiffServiceJourneyOptions)
                        .mandatory(
                            BailiffServiceJourneyOptions::getBailiffKnowPartnersPhone,
                            ALWAYS_SHOW,
                            NO_DEFAULT_VALUE,
                            RESPONDENTS_PHONE_QUESTION_LABEL
                        )
                        .mandatory(
                            BailiffServiceJourneyOptions::getBailiffPartnersPhone,
                            RESPONDENTS_PHONE_KNOWN,
                            NO_DEFAULT_VALUE,
                            RESPONDENTS_PHONE_NUMBER_LABEL,
                            RESPONDENTS_PHONE_HINT
                        )
                        .mandatory(BailiffServiceJourneyOptions::getBailiffKnowPartnersDateOfBirth,
                            ALWAYS_SHOW,
                            NO_DEFAULT_VALUE,
                            RESPONDENTS_DOB_QUESTION_LABEL
                        )
                        .mandatory(
                            BailiffServiceJourneyOptions::getBailiffPartnersDateOfBirth,
                            RESPONDENTS_DOB_KNOWN,
                            NO_DEFAULT_VALUE,
                            RESPONDENTS_DOB_LABEL,
                            RESPONDENTS_DOB_HINT
                        )
                        .mandatory(
                            BailiffServiceJourneyOptions::getBailiffPartnersApproximateAge,
                            RESPONDENTS_DOB_UNKNOWN,
                            NO_DEFAULT_VALUE,
                            RESPONDENTS_APPROX_AGE_LABEL,
                            RESPONDENTS_APPROX_AGE_HINT
                        )
                    .done()
                .done()
            .done()
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(
        CaseDetails<CaseData, State> caseDetails,
        CaseDetails<CaseData, State> beforeDetails
    ) {
        final BailiffServiceJourneyOptions bailiffServiceJourneyOptions =
            caseDetails.getData().getApplicant1().getInterimApplicationOptions().getBailiffServiceJourneyOptions();

        if (YesOrNo.NO.equals(bailiffServiceJourneyOptions.getBailiffKnowPartnersDateOfBirth())) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseDetails.getData())
                .build();
        }

        if (bailiffServiceJourneyOptions.getBailiffPartnersDateOfBirth().isAfter(LocalDate.now())) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseDetails.getData())
                .errors(Collections.singletonList(ERROR_FUTURE_DOB))
                .build();
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseDetails.getData())
            .build();
    }

}
