package uk.gov.hmcts.divorce.caseworker.event.page;

import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.FieldCollection;
import uk.gov.hmcts.ccd.sdk.type.Organisation;
import uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.MarriageDetails;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

public class UpdateContactDetails  implements CcdPageConfiguration {
    @Override
    public void addTo(PageBuilder pageBuilder) {
        var fieldCollectionBuilder = pageBuilder.page("UpdateContactDetails");

        addApplicant1Fields(fieldCollectionBuilder);
        addRespondentFields(fieldCollectionBuilder);

        fieldCollectionBuilder
            .label("Label-UpdateMarriageCertWarning",
            "### Only update Marriage Certificate Names to make them match the marriage certificate exactly")
            .complex(CaseData::getApplication)
                .complex(Application::getMarriageDetails)
                    .optional(MarriageDetails::getApplicant1Name)
                    .optional(MarriageDetails::getApplicant2Name)
                .done()
            .done()
            .complex(CaseData::getApplicant1)
                .optional(Applicant::getAgreedToReceiveEmails)
                .label("Label-ApplicantSolicitor",
                    "### Applicant's Solicitor Details")
                .complex(Applicant::getSolicitor)
                    .optional(Solicitor::getReference)
                    .optional(Solicitor::getName)
                    .optional(Solicitor::getAddress)
                    .optional(Solicitor::getPhone)
                    .optional(Solicitor::getEmail)
                    .complex(Solicitor::getOrganisationPolicy)
                        .complex(OrganisationPolicy::getOrganisation)
                            .optional(Organisation::getOrganisationId)
                        .done()
                    .done()
                    .optional(Solicitor::getAgreeToReceiveEmails)
                .done()
            .done()
            .complex(CaseData::getApplicant2)
                .label("Label-RespondentSolicitor",
                    "### Respondent's Solicitor Details")
                .complex(Applicant::getSolicitor)
                    .optional(Solicitor::getReference)
                .done()
                .optionalWithLabel(Applicant::getSolicitorRepresented,
                    "Is respondent represented by a solicitor?")
                .complex(Applicant::getSolicitor)
                    .optional(Solicitor::getName)
                    .optional(Solicitor::getAddress)
                    .optional(Solicitor::getPhone)
                    .optional(Solicitor::getEmail)
                        .complex(Solicitor::getOrganisationPolicy)
                            .complex(OrganisationPolicy::getOrganisation)
                                .optional(Organisation::getOrganisationId)
                            .done()
                        .done()
                .done()
            .done();
    }


    private void addApplicant1Fields(
        FieldCollection.FieldCollectionBuilder<CaseData, State, Event.EventBuilder<CaseData, UserRole, State>> fieldCollectionBuilder) {

        fieldCollectionBuilder
            .complex(CaseData::getApplicant1)
                .optional(Applicant::getFirstName)
                .optional(Applicant::getLastName)
                .label("LabelApp-GenderChangeWarning",
                    "WARNING: Changing the applicant gender here means you need to Re-Issue the case to update all case documents")
                .optional(Applicant::getGender)
                .mandatory(Applicant::getKeepContactDetailsConfidential)
                .label("LabelApp-AddressIsSharable",
                    "### The applicant's contact details may be shared with the respondent",
                    "applicant1KeepContactDetailsConfidential=\"No\"")
                .label("LabelApp-AddressIsConfidential",
                    "### Confidential Address - Take extra care to ensure the applicant's "
                        + "contact details below are not shared with the respondent",
                    "applicant1KeepContactDetailsConfidential=\"Yes\"")
                .mandatory(Applicant::getHomeAddress)
                .optional(Applicant::getCorrespondenceAddress)
                .optional(Applicant::getEmail)
                .optional(Applicant::getPhoneNumber)
            .done();
    }

    private void addRespondentFields(
        FieldCollection.FieldCollectionBuilder<CaseData, State, Event.EventBuilder<CaseData, UserRole, State>> fieldCollectionBuilder) {

        fieldCollectionBuilder
            .complex(CaseData::getApplicant2)
                .optionalWithLabel(Applicant::getFirstName,
                    "Respondent's first name")
                .optionalWithLabel(Applicant::getLastName,
                    "Respondent's last name")
                .label("LabelResp-GenderChangeWarning",
                    "WARNING: Changing the respondent gender here means you need to Re-Issue the case to update all case documents")
                .optionalWithLabel(Applicant::getGender,
                    "Respondent's gender")
                .mandatoryWithLabel(Applicant::getKeepContactDetailsConfidential,
                    "Keep Respondent's contact details private?")
                .label("LabelResp-AddressIsSharable",
                    "### The respondent's contact details may be shared with the applicant",
                    "applicant2KeepContactDetailsConfidential=\"No\"")
                .label("LabelApp-AddressIsConfidential",
                    "### Confidential Address - Take extra care to ensure the respondent's contact "
                        + "details below are not shared with the applicant",
                    "applicant2KeepContactDetailsConfidential=\"Yes\"")
                .mandatoryWithLabel(Applicant::getHomeAddress,
                    "Respondent's home address")
                .optionalWithLabel(Applicant::getCorrespondenceAddress,
                    "Respondent's service address")
                .optionalWithLabel(Applicant::getEmail,
                    "Respondent's email address")
                .optionalWithLabel(Applicant::getPhoneNumber,
                    "Respondent's phone number")
            .done();
    }
}
