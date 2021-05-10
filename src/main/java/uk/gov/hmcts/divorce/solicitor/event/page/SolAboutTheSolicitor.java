package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.ccd.sdk.type.Organisation;
import uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy;
import uk.gov.hmcts.divorce.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.model.CaseData;

import static uk.gov.hmcts.divorce.common.model.UserRole.APPLICANT_1_SOLICITOR;

public class SolAboutTheSolicitor implements CcdPageConfiguration {

    @Override
    public void addTo(final PageBuilder pageBuilder) {

        pageBuilder
            .page("SolAboutTheSolicitor")
            .pageLabel("About the Solicitor")
            .label(
                "LabelSolAboutEditingApplication-AboutSolicitor",
                "You can make changes at the end of your application.")
            .label(
                "LabelSolAboutTheSolPara-1",
                "Please note that the information provided will be used as evidence by the court to decide if "
                    + "Applicant 1 is entitled to legally end their marriage. **A copy of this form is sent to "
                    + "applicant 2**")
            .mandatory(CaseData::getApplicant1SolicitorName)
            .mandatory(CaseData::getSolicitorReference)
            .mandatory(CaseData::getApplicant1SolicitorPhone)
            .mandatory(CaseData::getApplicant1SolicitorEmail)
            .mandatory(CaseData::getSolicitorAgreeToReceiveEmails)
            .mandatory(CaseData::getDerivedApplicant1SolicitorAddress)
            .complex(CaseData::getApplicant1OrganisationPolicy)
            .complex(OrganisationPolicy::getOrganisation)
            .mandatory(Organisation::getOrganisationId)
            .done()
            .optional(OrganisationPolicy::getOrgPolicyCaseAssignedRole,
                "applicant1NameChanged=\"NeverShow\"",
                APPLICANT_1_SOLICITOR)
            .optional(OrganisationPolicy::getOrgPolicyReference, "applicant1NameChanged=\"NeverShow\"");
    }
}
