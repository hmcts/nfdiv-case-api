package uk.gov.hmcts.divorce.caseworker.event.page;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.MarriageDetails;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.ccd.sdk.api.Event.EventBuilder;
import static uk.gov.hmcts.ccd.sdk.api.FieldCollection.FieldCollectionBuilder;

@Component
@Slf4j
public class UpdateContactDetails implements CcdPageConfiguration {

    @Override
    public void addTo(final PageBuilder pageBuilder) {
        FieldCollectionBuilder<CaseData, State, EventBuilder<CaseData, UserRole, State>> fieldCollectionBuilder
            = pageBuilder
                .page("CaseworkerUpdateContactDetails")
                .pageLabel("CW Update contact details");

        buildApplicant1Fields(fieldCollectionBuilder);

        buildApplicant2Fields(fieldCollectionBuilder);

        buildMarriageCertFields(fieldCollectionBuilder);
    }

    private void buildApplicant1Fields(final FieldCollectionBuilder<CaseData, State,
        EventBuilder<CaseData, UserRole, State>> fieldCollectionBuilder) {

        fieldCollectionBuilder
            .complex(CaseData::getApplicant1)
            .mandatoryWithLabel(Applicant::getFirstName,
                "${labelContentApplicantsOrApplicant1s} first name")
            .optionalWithLabel(Applicant::getMiddleName,
                "${labelContentApplicantsOrApplicant1s} middle name")
            .mandatoryWithLabel(Applicant::getLastName,
                "${labelContentApplicantsOrApplicant1s} last name")
            .label("LabelPetitionerWarning",
                "### WARNING: Changing the ${labelContentApplicantsOrApplicant1s} gender here means you need to Re-Issue "
                    + "the case to update all case documents")
            .optionalWithLabel(Applicant::getGender, "What is ${labelContentApplicantsOrApplicant1s} gender?")
            .optionalWithLabel(Applicant::getContactDetailsType,
                "Keep ${labelContentApplicantsOrApplicant1s} contact details private "
                    + "from ${labelContentRespondentsOrApplicant2s}?")
            .optionalWithLabel(Applicant::getHomeAddress,
                "${labelContentApplicantsOrApplicant1s} home address")
            .optionalWithLabel(Applicant::getPhoneNumber, "${labelContentApplicantsOrApplicant1s} phone number")
            .label("LabelHorizontalLine1", "<hr>")
            .done();
    }

    private void buildApplicant2Fields(final FieldCollectionBuilder<CaseData, State,
        EventBuilder<CaseData, UserRole, State>> fieldCollectionBuilder) {

        fieldCollectionBuilder
            .complex(CaseData::getApplicant2)
            .mandatoryWithLabel(Applicant::getFirstName,
                "${labelContentRespondentsOrApplicant2s} first name")
            .optionalWithLabel(Applicant::getMiddleName,
                "${labelContentRespondentsOrApplicant2s} middle name")
            .mandatoryWithLabel(Applicant::getLastName,
                "${labelContentRespondentsOrApplicant2s} last name")
            .label("LabelRespondentWarning",
                "### WARNING: Changing the ${labelContentRespondentsOrApplicant2s} gender here means you need to Re-Issue "
                    + "the case to update all case documents")
            .optionalWithLabel(Applicant::getGender, "What is ${labelContentRespondentsOrApplicant2s} gender?")
            .optionalWithLabel(Applicant::getContactDetailsType,
                "Keep ${labelContentRespondentsOrApplicant2s} contact details private "
                    + "from ${labelContentApplicantsOrApplicant1s}?")
            .optionalWithLabel(Applicant::getHomeAddress,
                "${labelContentRespondentsOrApplicant2s} home address")
            .optionalWithLabel(Applicant::getPhoneNumber, "${labelContentRespondentsOrApplicant2s} phone number")
            .done();
    }

    private void buildMarriageCertFields(final FieldCollectionBuilder<CaseData, State,
        EventBuilder<CaseData, UserRole, State>> fieldCollectionBuilder) {

        fieldCollectionBuilder
            .label("LabelMarriageCertNames",
                "Only update Marriage Certificate Names to make them match the marriage certificate exactly")
            .complex(CaseData::getApplication)
                .complex(Application::getMarriageDetails)
                    .optionalWithLabel(MarriageDetails::getApplicant1Name,
                "${labelContentApplicantsOrApplicant1s} full name as on marriage certificate")
                    .optionalWithLabel(MarriageDetails::getApplicant2Name,
                "${labelContentRespondentsOrApplicant2s} full name as on marriage certificate")
            .done();
    }
}
