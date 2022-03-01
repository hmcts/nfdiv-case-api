package uk.gov.hmcts.divorce.caseworker.event.page;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.LabelContent;
import uk.gov.hmcts.divorce.divorcecase.model.MarriageDetails;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.ccd.sdk.api.Event.EventBuilder;
import static uk.gov.hmcts.ccd.sdk.api.FieldCollection.FieldCollectionBuilder;

@Component
@Slf4j
public class UpdateContactDetails implements CcdPageConfiguration {

    public static final String TITLE = "Update contact details";
    private static final String PAGE_ID = "CaseworkerUpdateContactDetails";
    private static final String ALWAYS_HIDE = "marriageApplicant1Name=\"ALWAYS_HIDE\"";
    private static final String FIRST_NAME_LABEL = "${%s} first name";
    private static final String MIDDLE_NAME_LABEL = "${%s} middle name";
    private static final String LAST_NAME_LABEL = "${%s} last name";
    private static final String WARNING_LABEL = "### WARNING: Changing the ${%s} gender here means you need "
        + "to Re-Issue the case to update all case documents";
    private static final String GENDER_LABEL = "What is ${%s} gender?";
    private static final String GENDER_HINT_LABEL = "${%s} gender is collected for statistical purposes only";
    private static final String CONTACT_TYPE_LABEL = "Keep the ${%s} contact details private from ${%s}?";
    private static final String ADDRESS_LABEL = "${%s} home address";
    private static final String PHONE_LABEL = "${%s} phone number";
    private static final String HORIZONTAL_RULE = "<hr>";
    private static final String MARRIAGE_CERT_NAME_LABEL = "${%s} full name as on marriage certificate";
    private static final String MARRIAGE_CERT_NAME_HINT_LABEL =
        "Enter the ${%s} name exactly as it appears on the certificate. Include any extra text such as 'formally known as'.";
    private static final String APPLICANTS_OR_APPLICANT1S = "labelContentApplicantsOrApplicant1s";
    private static final String RESPONDENTS_OR_APPLICANT2S = "labelContentRespondentsOrApplicant2s";

    @Override
    public void addTo(final PageBuilder pageBuilder) {
        FieldCollectionBuilder<CaseData, State, EventBuilder<CaseData, UserRole, State>> fieldCollectionBuilder
            = pageBuilder
                .page(PAGE_ID)
                .pageLabel(TITLE)
                .complex(CaseData::getLabelContent)
                    .readonlyNoSummary(LabelContent::getApplicantsOrApplicant1s, ALWAYS_HIDE)
                    .readonlyNoSummary(LabelContent::getRespondentsOrApplicant2s, ALWAYS_HIDE)
                .done();

        buildApplicant1Fields(fieldCollectionBuilder);

        buildApplicant2Fields(fieldCollectionBuilder);

        buildMarriageCertFields(fieldCollectionBuilder);
    }

    private void buildApplicant1Fields(final FieldCollectionBuilder<CaseData, State,
        EventBuilder<CaseData, UserRole, State>> fieldCollectionBuilder) {

        fieldCollectionBuilder
            .complex(CaseData::getApplicant1)
                .mandatoryWithLabel(Applicant::getFirstName, getLabel(FIRST_NAME_LABEL, APPLICANTS_OR_APPLICANT1S))
                .optionalWithLabel(Applicant::getMiddleName, getLabel(MIDDLE_NAME_LABEL, APPLICANTS_OR_APPLICANT1S))
                .mandatoryWithLabel(Applicant::getLastName, getLabel(LAST_NAME_LABEL, APPLICANTS_OR_APPLICANT1S))
                .label("LabelPetitionerWarning", getLabel(WARNING_LABEL, APPLICANTS_OR_APPLICANT1S))
                .optional(Applicant::getGender, null, null, getLabel(GENDER_LABEL, APPLICANTS_OR_APPLICANT1S),
                    getLabel(GENDER_HINT_LABEL, APPLICANTS_OR_APPLICANT1S))
                .optionalWithLabel(Applicant::getContactDetailsType,
                    getLabel(CONTACT_TYPE_LABEL, APPLICANTS_OR_APPLICANT1S, RESPONDENTS_OR_APPLICANT2S))
                .optionalWithLabel(Applicant::getHomeAddress, getLabel(ADDRESS_LABEL, APPLICANTS_OR_APPLICANT1S))
                .optionalWithLabel(Applicant::getPhoneNumber, getLabel(PHONE_LABEL, APPLICANTS_OR_APPLICANT1S))
                .label("LabelHorizontalLine1", HORIZONTAL_RULE)
            .done();
    }

    private void buildApplicant2Fields(final FieldCollectionBuilder<CaseData, State,
        EventBuilder<CaseData, UserRole, State>> fieldCollectionBuilder) {

        fieldCollectionBuilder
            .complex(CaseData::getApplicant2)
                .mandatoryWithLabel(Applicant::getFirstName, getLabel(FIRST_NAME_LABEL, RESPONDENTS_OR_APPLICANT2S))
                .optionalWithLabel(Applicant::getMiddleName, getLabel(MIDDLE_NAME_LABEL, RESPONDENTS_OR_APPLICANT2S))
                .mandatoryWithLabel(Applicant::getLastName, getLabel(LAST_NAME_LABEL, RESPONDENTS_OR_APPLICANT2S))
                .label("LabelRespondentWarning", getLabel(WARNING_LABEL, RESPONDENTS_OR_APPLICANT2S))
            .optional(Applicant::getGender, null, null, getLabel(GENDER_LABEL, RESPONDENTS_OR_APPLICANT2S),
                getLabel(GENDER_HINT_LABEL, RESPONDENTS_OR_APPLICANT2S))
                .optionalWithLabel(Applicant::getContactDetailsType,
                    getLabel(CONTACT_TYPE_LABEL, RESPONDENTS_OR_APPLICANT2S, APPLICANTS_OR_APPLICANT1S))
                .optionalWithLabel(Applicant::getHomeAddress, getLabel(ADDRESS_LABEL, RESPONDENTS_OR_APPLICANT2S))
                .optionalWithLabel(Applicant::getPhoneNumber, getLabel(PHONE_LABEL, RESPONDENTS_OR_APPLICANT2S))
                .label("LabelHorizontalLine2", HORIZONTAL_RULE)
            .done();
    }

    private void buildMarriageCertFields(final FieldCollectionBuilder<CaseData, State,
        EventBuilder<CaseData, UserRole, State>> fieldCollectionBuilder) {

        fieldCollectionBuilder
            .label("LabelMarriageCertNames",
                "### Only update Marriage Certificate Names to make them match the marriage certificate exactly")
            .complex(CaseData::getApplication)
                .complex(Application::getMarriageDetails)
                    .optional(MarriageDetails::getApplicant1Name, null, null,
                        getLabel(MARRIAGE_CERT_NAME_LABEL, APPLICANTS_OR_APPLICANT1S),
                        getLabel(MARRIAGE_CERT_NAME_HINT_LABEL, APPLICANTS_OR_APPLICANT1S))
                    .optional(MarriageDetails::getApplicant2Name, null, null,
                        getLabel(MARRIAGE_CERT_NAME_LABEL, RESPONDENTS_OR_APPLICANT2S),
                        getLabel(MARRIAGE_CERT_NAME_HINT_LABEL, RESPONDENTS_OR_APPLICANT2S))
            .done();
    }

    private String getLabel(final String label, final Object... value) {
        return String.format(label, value);
    }
}
