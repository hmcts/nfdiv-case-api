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
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
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
    private static final String GENDER_LABEL = "What is the ${%s} gender?";
    private static final String GENDER_HINT_LABEL = "The ${%s} gender is collected for statistical purposes only";
    private static final String CONTACT_TYPE_LABEL = "Keep the ${%s} contact details private from ${%s}?";
    private static final String ADDRESS_LABEL = "${%s} home address";
    private static final String EMAIL_LABEL = "${%s} email address";
    private static final String SOLICITOR_DETAILS_LABEL = "### ${%s} solicitor's details";
    private static final String SOLICITOR_NAME_LABEL = "${%s} solicitor's name";
    private static final String PHONE_LABEL = "${%s} phone number";
    private static final String HORIZONTAL_RULE = "<hr>";
    private static final String MARRIAGE_CERT_NAME_LABEL = "${%s} full name as on marriage certificate";
    private static final String MARRIAGE_CERT_NAME_HINT_LABEL =
        "Enter the ${%s} name exactly as it appears on the certificate. Include any extra text such as 'formally known as'.";
    private static final String APPLICANTS_OR_APPLICANT1S = "labelContentApplicantsOrApplicant1s";
    private static final String THE_APPLICANT_OR_APPLICANT1 = "labelContentTheApplicantOrApplicant1";
    private static final String RESPONDENTS_OR_APPLICANT2S = "labelContentRespondentsOrApplicant2s";
    private static final String THE_RESPONDENT_OR_APPLICANT2 = "labelContentTheApplicant2";
    public static final String SOLICITOR_FIRM_LABEL = "${%s} solicitor's firm";
    public static final String SOLICITOR_PHONE_NUMBER_LABEL = "${%s} solicitor phone number";
    public static final String SOLICITOR_EMAIL_LABEL = "${%s} solicitor email";
    public static final String SOLICITOR_S_FIRM_ADDRESS_LABEL = "${%s} solicitor's firm address/DX address";
    public static final String SOLICITOR_REFERENCE_LABEL = "${%s} solicitor's reference";
    public static final String RESPONDENT_SOLICITOR_EMAIL_LABEL = "${%s} solicitor's email address they used to link the case";
    @Override
    public void addTo(final PageBuilder pageBuilder) {
        FieldCollectionBuilder<CaseData, State, EventBuilder<CaseData, UserRole, State>> fieldCollectionBuilder
            = pageBuilder
                .page(PAGE_ID)
                .pageLabel(TITLE)
                .complex(CaseData::getLabelContent)
                    .readonlyNoSummary(LabelContent::getApplicantsOrApplicant1s, ALWAYS_HIDE)
                    .readonlyNoSummary(LabelContent::getTheApplicantOrApplicant1, ALWAYS_HIDE)
                    .readonlyNoSummary(LabelContent::getRespondentsOrApplicant2s, ALWAYS_HIDE)
                    .readonlyNoSummary(LabelContent::getTheApplicant2, ALWAYS_HIDE)
                .done();

        buildApplicant1Fields(fieldCollectionBuilder);

        buildApplicant2Fields(fieldCollectionBuilder);

        buildMarriageCertFields(fieldCollectionBuilder);

        buildApplicantSolicitorDetails(fieldCollectionBuilder);

        buildRespondentSolicitorDetails(fieldCollectionBuilder);
    }

    private void buildApplicantSolicitorDetails(final FieldCollectionBuilder<CaseData, State,
        EventBuilder<CaseData, UserRole, State>> fieldCollectionBuilder) {
        fieldCollectionBuilder
            .label("applicantSolicitorDetailsLabel", getLabel(SOLICITOR_DETAILS_LABEL, APPLICANTS_OR_APPLICANT1S))
            .complex(CaseData::getApplicant1)
                .complex(Applicant::getSolicitor)
                    .optionalWithLabel(Solicitor::getReference, "Reference number")
                    .optionalWithLabel(Solicitor::getName, getLabel(SOLICITOR_NAME_LABEL, APPLICANTS_OR_APPLICANT1S))
                    .optionalWithLabel(Solicitor::getFirmName, getLabel(SOLICITOR_FIRM_LABEL, APPLICANTS_OR_APPLICANT1S))
                    .optionalWithLabel(Solicitor::getPhone, getLabel(SOLICITOR_PHONE_NUMBER_LABEL, APPLICANTS_OR_APPLICANT1S))
                    .optionalWithLabel(Solicitor::getEmail, getLabel(SOLICITOR_EMAIL_LABEL, APPLICANTS_OR_APPLICANT1S))
                    .optionalWithLabel(Solicitor::getAddress, getLabel(SOLICITOR_S_FIRM_ADDRESS_LABEL, APPLICANTS_OR_APPLICANT1S))
                    .optional(Solicitor::getAgreeToReceiveEmailsCheckbox)
                .done()
            .done();
    }

    private void buildRespondentSolicitorDetails(final FieldCollectionBuilder<CaseData, State,
        EventBuilder<CaseData, UserRole, State>> fieldCollectionBuilder) {
        fieldCollectionBuilder
            .label("respondentSolicitorDetailsLabel", getLabel(SOLICITOR_DETAILS_LABEL, RESPONDENTS_OR_APPLICANT2S))
            .complex(CaseData::getApplicant2)
                .complex(Applicant::getSolicitor)
                    .optionalWithLabel(Solicitor::getReference,  getLabel(SOLICITOR_REFERENCE_LABEL, RESPONDENTS_OR_APPLICANT2S))
                    .optionalWithLabel(Solicitor::getEmail,  getLabel(RESPONDENT_SOLICITOR_EMAIL_LABEL, RESPONDENTS_OR_APPLICANT2S))
                .done()
                .mandatoryWithLabel(Applicant::getSolicitorRepresented,
                    "Is ${labelContentTheApplicant2} represented by a solicitor?")
                .complex(Applicant::getSolicitor)
                    .optionalWithLabel(Solicitor::getName, getLabel(SOLICITOR_NAME_LABEL, RESPONDENTS_OR_APPLICANT2S))
                    .optionalWithLabel(Solicitor::getFirmName, getLabel(SOLICITOR_FIRM_LABEL, RESPONDENTS_OR_APPLICANT2S))
                    .optionalWithLabel(Solicitor::getPhone, getLabel(SOLICITOR_PHONE_NUMBER_LABEL, RESPONDENTS_OR_APPLICANT2S))
                    .optionalWithLabel(Solicitor::getEmail, getLabel(SOLICITOR_EMAIL_LABEL, RESPONDENTS_OR_APPLICANT2S))
                    .optionalWithLabel(Solicitor::getAddress, getLabel(SOLICITOR_S_FIRM_ADDRESS_LABEL, RESPONDENTS_OR_APPLICANT2S))
                .done()
            .done();
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
                    getLabel(CONTACT_TYPE_LABEL, APPLICANTS_OR_APPLICANT1S, THE_RESPONDENT_OR_APPLICANT2))
                .optionalWithLabel(Applicant::getAddress, getLabel(ADDRESS_LABEL, APPLICANTS_OR_APPLICANT1S))
                .optionalWithLabel(Applicant::getEmail, getLabel(EMAIL_LABEL, APPLICANTS_OR_APPLICANT1S))
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
                    getLabel(CONTACT_TYPE_LABEL, RESPONDENTS_OR_APPLICANT2S, THE_APPLICANT_OR_APPLICANT1))
                .optionalWithLabel(Applicant::getAddress, getLabel(ADDRESS_LABEL, RESPONDENTS_OR_APPLICANT2S))
                .optionalWithLabel(Applicant::getEmail, getLabel(EMAIL_LABEL, RESPONDENTS_OR_APPLICANT2S))
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
