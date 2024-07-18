package uk.gov.hmcts.divorce.caseworker.event.page;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Gender;
import uk.gov.hmcts.divorce.divorcecase.model.LabelContent;
import uk.gov.hmcts.divorce.divorcecase.model.MarriageDetails;
import uk.gov.hmcts.divorce.divorcecase.model.MarriageFormation;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.divorcecase.model.WhoDivorcing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static uk.gov.hmcts.ccd.sdk.api.Event.EventBuilder;
import static uk.gov.hmcts.ccd.sdk.api.FieldCollection.FieldCollectionBuilder;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.FEMALE;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.MALE;
import static uk.gov.hmcts.divorce.divorcecase.model.MarriageFormation.OPPOSITE_SEX_COUPLE;
import static uk.gov.hmcts.divorce.divorcecase.model.MarriageFormation.SAME_SEX_COUPLE;
import static uk.gov.hmcts.divorce.divorcecase.model.WhoDivorcing.HUSBAND;
import static uk.gov.hmcts.divorce.divorcecase.model.WhoDivorcing.WIFE;

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
    private static final String CHANGE_REPRESENTATION_WARNING_LABEL = "### WARNING: Use this event for only minor amendments to the "
        + "solicitor's details. DO NOT change the firm. Use Notice of change to change the firm.";
    private static final String GENDER_LABEL = "What is the ${%s} gender?";
    private static final String GENDER_HINT_LABEL = "The ${%s} gender is collected for statistical purposes only";
    private static final String CONTACT_TYPE_LABEL = "Keep the ${%s} contact details private from ${%s}?";
    private static final String ADDRESS_OVERSEAS_LABEL = "is ${%s} address international?";
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
    public static final String SOLICITOR_S_FIRM_ADDRESS_OVERSEAS_LABEL = "Is ${%s} solicitor's firm address/DX address international?";
    public static final String SOLICITOR_REFERENCE_LABEL = "${%s} solicitor's reference";
    public static final String RESPONDENT_SOLICITOR_EMAIL_LABEL = "${%s} solicitor's email address they used to link the case";
    public static final String SOLICITOR_DETAILS_REMOVED_ERROR = """
        You cannot remove the solicitor %s with this event. Please use Notice of Change if you would like to remove representation.
        """;

    @Override
    public void addTo(final PageBuilder pageBuilder) {
        FieldCollectionBuilder<CaseData, State, EventBuilder<CaseData, UserRole, State>> fieldCollectionBuilder
            = pageBuilder
                .page(PAGE_ID, this::midEvent)
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
            .label("LabelApp1ChangeOfRepresentationWarning", getLabel(CHANGE_REPRESENTATION_WARNING_LABEL))
            .complex(CaseData::getApplicant1)
                .complex(Applicant::getSolicitor)
                    .optionalWithLabel(Solicitor::getReference, "Reference number")
                    .optionalWithLabel(Solicitor::getName, getLabel(SOLICITOR_NAME_LABEL, APPLICANTS_OR_APPLICANT1S))
                    .optionalWithLabel(Solicitor::getFirmName, getLabel(SOLICITOR_FIRM_LABEL, APPLICANTS_OR_APPLICANT1S))
                    .optionalWithLabel(Solicitor::getPhone, getLabel(SOLICITOR_PHONE_NUMBER_LABEL, APPLICANTS_OR_APPLICANT1S))
                    .optionalWithLabel(Solicitor::getEmail, getLabel(SOLICITOR_EMAIL_LABEL, APPLICANTS_OR_APPLICANT1S))
                    .optionalWithLabel(Solicitor::getAddress, getLabel(SOLICITOR_S_FIRM_ADDRESS_LABEL, APPLICANTS_OR_APPLICANT1S))
                    .optionalWithLabel(Solicitor::getAddressOverseas,
                        getLabel(SOLICITOR_S_FIRM_ADDRESS_OVERSEAS_LABEL, APPLICANTS_OR_APPLICANT1S))
                    .optional(Solicitor::getAgreeToReceiveEmailsCheckbox)
                .done()
            .done();
    }

    private void buildRespondentSolicitorDetails(final FieldCollectionBuilder<CaseData, State,
        EventBuilder<CaseData, UserRole, State>> fieldCollectionBuilder) {
        fieldCollectionBuilder
            .label("respondentSolicitorDetailsLabel", getLabel(SOLICITOR_DETAILS_LABEL, RESPONDENTS_OR_APPLICANT2S))
            .label("LabelApp2ChangeOfRepresentationWarning", getLabel(CHANGE_REPRESENTATION_WARNING_LABEL))
            .complex(CaseData::getApplicant2)
                .complex(Applicant::getSolicitor)
                    .optionalWithLabel(Solicitor::getReference,  getLabel(SOLICITOR_REFERENCE_LABEL, RESPONDENTS_OR_APPLICANT2S))
                    .optionalWithLabel(Solicitor::getEmail,  getLabel(RESPONDENT_SOLICITOR_EMAIL_LABEL, RESPONDENTS_OR_APPLICANT2S))
                .done()
                .complex(Applicant::getSolicitor)
                    .optionalWithLabel(Solicitor::getName, getLabel(SOLICITOR_NAME_LABEL, RESPONDENTS_OR_APPLICANT2S))
                    .optionalWithLabel(Solicitor::getFirmName, getLabel(SOLICITOR_FIRM_LABEL, RESPONDENTS_OR_APPLICANT2S))
                    .optionalWithLabel(Solicitor::getPhone, getLabel(SOLICITOR_PHONE_NUMBER_LABEL, RESPONDENTS_OR_APPLICANT2S))
                    .optionalWithLabel(Solicitor::getEmail, getLabel(SOLICITOR_EMAIL_LABEL, RESPONDENTS_OR_APPLICANT2S))
                    .optionalWithLabel(Solicitor::getAddress, getLabel(SOLICITOR_S_FIRM_ADDRESS_LABEL, RESPONDENTS_OR_APPLICANT2S))
                    .optionalWithLabel(Solicitor::getAddressOverseas,
                        getLabel(SOLICITOR_S_FIRM_ADDRESS_OVERSEAS_LABEL, RESPONDENTS_OR_APPLICANT2S))
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
            .done()
            .complex(CaseData::getApplication)
                .complex(Application::getMarriageDetails)
                    .optional(MarriageDetails::getFormationType)
                .done()
                .optional(Application::getDivorceWho)
            .done()
            .complex(CaseData::getApplicant1)
                .optionalWithLabel(Applicant::getAddressOverseas, getLabel(ADDRESS_OVERSEAS_LABEL, APPLICANTS_OR_APPLICANT1S))
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
                .optionalWithLabel(Applicant::getAddressOverseas, getLabel(ADDRESS_OVERSEAS_LABEL, RESPONDENTS_OR_APPLICANT2S))
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

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(final CaseDetails<CaseData, State> details,
                                                                  final CaseDetails<CaseData, State> detailsBefore) {
        CaseData caseData = details.getData();
        CaseData caseDataBefore = detailsBefore.getData();

        List<String> solicitorValidationErrors = validateSolicitorDetails(caseDataBefore, caseData);
        if (!solicitorValidationErrors.isEmpty()) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .errors(solicitorValidationErrors)
                .build();
        }

        if (!validApplicantContactDetails(caseDataBefore, caseData)) {

            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .errors(singletonList("Please use the 'Update offline status' event before removing the email address."))
                .build();
        }

        if (!isValidCombination(caseData)) {
            final String errorMessage = String.format(
                """
                You have selected the applicant gender as %s and they are divorcing their %s and they are an %s.
                Please ensure this is correct before submitting.""",
                caseData.getApplicant1().getGender().getLabel(),
                caseData.getApplication().getDivorceWho().getLabel(),
                caseData.getApplication().getMarriageDetails().getFormationType().getLabel()
            );

            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .errors(singletonList(errorMessage))
                .build();
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    private List<String> validateSolicitorDetails(CaseData caseDataBefore, CaseData caseData) {
        List<String> solicitorValidationErrors = new ArrayList<>();

        solicitorValidationErrors.addAll(
            validateSolicitorDetailsNotRemoved(
                caseDataBefore.getApplicant1().getSolicitor(),
                caseData.getApplicant1().getSolicitor()
            )
        );

        solicitorValidationErrors.addAll(
            validateSolicitorDetailsNotRemoved(
                caseDataBefore.getApplicant2().getSolicitor(),
                caseData.getApplicant2().getSolicitor()
            )
        );

        return solicitorValidationErrors;
    }

    private List<String> validateSolicitorDetailsNotRemoved(Solicitor solicitorBefore, Solicitor solicitorAfter) {
        List<String> solicitorDetailRemovedErrors = new ArrayList<>();

        if (solicitorBefore == null) {
            return solicitorDetailRemovedErrors;
        }

        Map<Function<Solicitor, String>, String> getterToFieldNameMap = Map.of(
            Solicitor::getName, "name",
            Solicitor::getEmail, "email address",
            Solicitor::getPhone, "phone number",
            Solicitor::getAddress, "postal address"
        );

        getterToFieldNameMap.forEach((getter, fieldName) -> {
            String valueBefore = getter.apply(solicitorBefore);
            String valueAfter = getter.apply(solicitorAfter);

            if (isNotEmpty(valueBefore) && isEmpty(valueAfter)) {
                solicitorDetailRemovedErrors.add(String.format(SOLICITOR_DETAILS_REMOVED_ERROR, fieldName));
            }
        });

        return solicitorDetailRemovedErrors;
    }

    private boolean validApplicantContactDetails(CaseData caseDataBefore, CaseData caseData) {

        if (caseDataBefore.getApplicant1().getEmail() != null && !caseDataBefore.getApplicant1().getEmail().isBlank()) {
            if (!caseDataBefore.getApplicant1().isRepresented()
                && !caseData.getApplicant1().isApplicantOffline()
                && (caseData.getApplicant1().getEmail() == null || caseData.getApplicant1().getEmail().isBlank())) {
                return false;
            }
        }

        if (caseDataBefore.getApplicant2().getEmail() != null && !caseDataBefore.getApplicant2().getEmail().isBlank()) {
            if (!caseDataBefore.getApplicant2().isRepresented()
                && !caseData.getApplicant2().isApplicantOffline()
                && (caseData.getApplicant2().getEmail() == null || caseData.getApplicant2().getEmail().isBlank())) {
                return false;
            }
        }

        return true;
    }

    private boolean isValidCombination(final CaseData caseData) {

        final Gender applicant1Gender = caseData.getApplicant1().getGender();
        final Gender applicant2Gender = caseData.getApplicant2().getGender();
        final WhoDivorcing divorceWho = caseData.getApplication().getDivorceWho();
        final MarriageFormation formationType = caseData.getApplication().getMarriageDetails().getFormationType();

        if (isEmpty(applicant1Gender) || isEmpty(applicant2Gender) || isEmpty(divorceWho) || isEmpty(formationType)) {
            return true;
        } else if (FEMALE.equals(applicant1Gender)
            && MALE.equals(applicant2Gender)
            && HUSBAND.equals(divorceWho)
            && OPPOSITE_SEX_COUPLE.equals(formationType)
        ) {
            return true;
        } else if (MALE.equals(applicant1Gender)
            && FEMALE.equals(applicant2Gender)
            && WIFE.equals(divorceWho)
            && OPPOSITE_SEX_COUPLE.equals(formationType)
        ) {
            return true;
        } else if (FEMALE.equals(applicant1Gender)
            && FEMALE.equals(applicant2Gender)
            && WIFE.equals(divorceWho)
            && SAME_SEX_COUPLE.equals(formationType)
        ) {
            return true;
        } else {
            return MALE.equals(applicant1Gender)
                && MALE.equals(applicant2Gender)
                && HUSBAND.equals(divorceWho)
                && SAME_SEX_COUPLE.equals(formationType);
        }
    }
}
