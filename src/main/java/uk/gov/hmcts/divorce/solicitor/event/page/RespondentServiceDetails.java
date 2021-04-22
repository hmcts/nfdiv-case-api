package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.ccd.sdk.api.Event.EventBuilder;
import uk.gov.hmcts.ccd.sdk.api.FieldCollection.FieldCollectionBuilder;
import uk.gov.hmcts.ccd.sdk.type.Organisation;
import uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy;
import uk.gov.hmcts.divorce.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.State;
import uk.gov.hmcts.divorce.common.model.UserRole;

import static uk.gov.hmcts.divorce.common.model.UserRole.PETITIONER_SOLICITOR;

public class RespondentServiceDetails implements CcdPageConfiguration {

    @Override
    public void addTo(
        final FieldCollectionBuilder<CaseData, State, EventBuilder<CaseData, UserRole, State>> fieldCollectionBuilder) {

        fieldCollectionBuilder
            .page("RespondentServiceDetails")
            .pageLabel("Respondent service details")
            .mandatory(CaseData::getRespondentSolicitorRepresented)
            .mandatory(CaseData::getRespondentSolicitorName, "respondentSolicitorRepresented=\"Yes\"")
            .mandatory(CaseData::getRespondentSolicitorReference, "respondentSolicitorRepresented=\"Yes\"")
            .mandatory(CaseData::getRespondentSolicitorPhone, "respondentSolicitorRepresented=\"Yes\"")
            .mandatory(CaseData::getRespondentSolicitorEmail, "respondentSolicitorRepresented=\"Yes\"")
            .mandatory(CaseData::getDerivedRespondentSolicitorAddr, "respondentSolicitorRepresented=\"Yes\"")
            .label(
                "LabelRespondentServiceDetails-DigitalOrPaper",
                "If the respondent solicitor's firm is registered with MyHMCTS, you can assign the case to them. "
                    + "This will allow the respondent solicitor to respond digitally. If you cannot find the respondent "
                    + "solicitor, a paper AOS pack will be sent to the respondent's solicitor's address entered above.",
                "respondentSolicitorRepresented=\"Yes\"")
            .mandatory(CaseData::getRespSolDigital, "respondentSolicitorRepresented=\"Yes\"")
            .complex(CaseData::getRespondentOrganisationPolicy, "respSolDigital=\"Yes\"")
            .complex(OrganisationPolicy::getOrganisation)
            .mandatory(Organisation::getOrganisationId)
            .done()
            .optional(OrganisationPolicy::getOrgPolicyCaseAssignedRole,
                "petitionerNameChanged=\"NeverShow\"",
                PETITIONER_SOLICITOR)
            .optional(OrganisationPolicy::getOrgPolicyReference, "petitionerNameChanged=\"NeverShow\"")
            .done()
            .optional(CaseData::getDerivedRespondentHomeAddress, "respondentSolicitorRepresented=\"No\"")
            .mandatory(CaseData::getDerivedRespondentCorrespondenceAddr, "respondentSolicitorRepresented=\"No\"");
    }
}
