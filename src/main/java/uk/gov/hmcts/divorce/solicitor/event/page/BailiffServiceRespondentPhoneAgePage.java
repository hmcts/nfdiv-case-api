package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.BailiffServiceJourneyOptions;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;

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

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("bailiffServiceRespondentsPhoneAgePage")
            .complex(CaseData::getApplicant1)
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
}
