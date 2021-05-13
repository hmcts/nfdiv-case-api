package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.ccd.sdk.type.Organisation;
import uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy;
import uk.gov.hmcts.divorce.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.model.CaseData;

import static uk.gov.hmcts.divorce.common.model.UserRole.APPLICANT_1_SOLICITOR;

public class Applicant2ServiceDetails implements CcdPageConfiguration {

    @Override
    public void addTo(final PageBuilder pageBuilder) {

        pageBuilder
            .page("Applicant2ServiceDetails")
            .pageLabel("Applicant 2 service details")
            .mandatory(CaseData::getApplicant2SolicitorRepresented)
            .mandatory(CaseData::getApplicant2SolicitorName, "applicant2SolicitorRepresented=\"Yes\"")
            .mandatory(CaseData::getApplicant2SolicitorReference, "applicant2SolicitorRepresented=\"Yes\"")
            .mandatory(CaseData::getApplicant2SolicitorPhone, "applicant2SolicitorRepresented=\"Yes\"")
            .mandatory(CaseData::getApplicant2SolicitorEmail, "applicant2SolicitorRepresented=\"Yes\"")
            .mandatory(CaseData::getDerivedApplicant2SolicitorAddr, "applicant2SolicitorRepresented=\"Yes\"")
            .label(
                "LabelApplicant2ServiceDetails-DigitalOrPaper",
                "If applicant 2 solicitor's firm is registered with MyHMCTS, you can assign the case to them. "
                    + "This will allow applicant 2 solicitor to respond digitally. If you cannot find applicant 2 "
                    + "solicitor, a paper AOS pack will be sent to applicant 2's solicitor's address entered above.",
                "applicant2SolicitorRepresented=\"Yes\"")
            .mandatory(CaseData::getApp2SolDigital, "applicant2SolicitorRepresented=\"Yes\"")
            .complex(CaseData::getApplicant2OrganisationPolicy, "app2SolDigital=\"Yes\"")
            .complex(OrganisationPolicy::getOrganisation)
            .mandatory(Organisation::getOrganisationId)
            .done()
            .optional(OrganisationPolicy::getOrgPolicyCaseAssignedRole,
                "applicant1NameChanged=\"NeverShow\"",
                APPLICANT_1_SOLICITOR)
            .optional(OrganisationPolicy::getOrgPolicyReference, "applicant1NameChanged=\"NeverShow\"")
            .done()
            .optional(CaseData::getApplicant2HomeAddress, "applicant2SolicitorRepresented=\"No\"")
            .mandatory(CaseData::getApplicant2CorrespondenceAddress, "applicant2SolicitorRepresented=\"No\"");
    }
}
