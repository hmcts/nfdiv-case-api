package uk.gov.hmcts.divorce.common.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.Organisation;
import uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;

@Component
@Slf4j
public class SetDefaultOrganisationPolicies implements CaseTask {
    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {
        final CaseData caseData = caseDetails.getData();

        setSolicitor(caseData.getApplicant1());
        setSolicitor(caseData.getApplicant2());

        final Solicitor applicant1Solicitor = caseData.getApplicant1().getSolicitor();
        final Solicitor applicant2Solicitor = caseData.getApplicant2().getSolicitor();

        setDefaultOrgPolicy(applicant1Solicitor, UserRole.APPLICANT_1_SOLICITOR);
        setDefaultOrgPolicy(applicant2Solicitor, UserRole.APPLICANT_2_SOLICITOR);

        return caseDetails;
    }

    private void setSolicitor(Applicant applicant) {
        if (applicant.getSolicitor() == null) {
            applicant.setSolicitor(new Solicitor());
        }
    }

    private void setDefaultOrgPolicy(Solicitor solicitor, UserRole solicitorRole) {
        if (solicitor.getOrganisationPolicy() == null) {
            solicitor.setOrganisationPolicy(new OrganisationPolicy<UserRole>());
        }
        OrganisationPolicy<UserRole> organisationPolicy = solicitor.getOrganisationPolicy();

        if (organisationPolicy.getOrgPolicyCaseAssignedRole() == null) {
            organisationPolicy.setOrgPolicyCaseAssignedRole(solicitorRole);
        }

        if (organisationPolicy.getOrganisation() == null) {
            organisationPolicy.setOrganisation(new Organisation(null, null));
        }
    }
}
