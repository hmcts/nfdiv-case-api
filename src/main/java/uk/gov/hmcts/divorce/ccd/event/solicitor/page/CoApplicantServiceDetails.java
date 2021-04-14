package uk.gov.hmcts.divorce.ccd.event.solicitor.page;

import uk.gov.hmcts.ccd.sdk.api.Event.EventBuilder;
import uk.gov.hmcts.ccd.sdk.api.FieldCollection.FieldCollectionBuilder;
import uk.gov.hmcts.ccd.sdk.type.Organisation;
import uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy;
import uk.gov.hmcts.divorce.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.ccd.model.CaseData;
import uk.gov.hmcts.divorce.ccd.model.State;
import uk.gov.hmcts.divorce.ccd.model.UserRole;

import static uk.gov.hmcts.divorce.ccd.model.UserRole.APPLICANT_SOLICITOR;

public class CoApplicantServiceDetails implements CcdPageConfiguration {

    @Override
    public void addTo(final FieldCollectionBuilder<CaseData, EventBuilder<CaseData, UserRole, State>> fieldCollectionBuilder) {

        fieldCollectionBuilder
            .page("CoApplicantServiceDetails")
            .pageLabel("Co-Applicant service details")
            .mandatory(CaseData::getCoApplicantSolicitorRepresented)
            .mandatory(CaseData::getCoApplicantSolicitorName, "coApplicantSolicitorRepresented=\"Yes\"")
            .mandatory(CaseData::getCoApplicantSolicitorReference, "coApplicantSolicitorRepresented=\"Yes\"")
            .mandatory(CaseData::getCoApplicantSolicitorPhone, "coApplicantSolicitorRepresented=\"Yes\"")
            .mandatory(CaseData::getCoApplicantSolicitorEmail, "coApplicantSolicitorRepresented=\"Yes\"")
            .mandatory(CaseData::getDerivedCoApplicantSolicitorAddr, "coApplicantSolicitorRepresented=\"Yes\"")
            .label(
                "LabelCoApplicantServiceDetails-DigitalOrPaper",
                "If the co-applicant solicitor's firm is registered with MyHMCTS, you can assign the case to them. "
                    + "This will allow the co-applicant solicitor to respond digitally. If you cannot find the co-applicant "
                    + "solicitor, a paper AOS pack will be sent to the co-applicant's solicitor's address entered above.",
                "coApplicantSolicitorRepresented=\"Yes\"")
            .mandatory(CaseData::getCoApplicantSolDigital, "coApplicantSolicitorRepresented=\"Yes\"")
            .complex(CaseData::getCoApplicantOrganisationPolicy, "coApplicantSolDigital=\"Yes\"")
                .complex(OrganisationPolicy::getOrganisation)
                    .mandatory(Organisation::getOrganisationId)
                    .done()
                .optional(OrganisationPolicy::getOrgPolicyCaseAssignedRole,
                    "applicantNameChanged=\"NeverShow\"",
                    APPLICANT_SOLICITOR)
                .optional(OrganisationPolicy::getOrgPolicyReference, "applicantNameChanged=\"NeverShow\"")
                .done()
            .optional(CaseData::getDerivedCoApplicantHomeAddress, "coApplicantSolicitorRepresented=\"No\"")
            .mandatory(CaseData::getDerivedCoApplicantCorrespondenceAddr, "coApplicantSolicitorRepresented=\"No\"");
    }
}
