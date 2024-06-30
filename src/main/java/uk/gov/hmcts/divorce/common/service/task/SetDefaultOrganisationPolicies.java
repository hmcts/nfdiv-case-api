package uk.gov.hmcts.divorce.common.service.task;

import lombok.RequiredArgsConstructor;
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

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class SetDefaultOrganisationPolicies implements CaseTask {
    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {
        log.info("Setting default organisation policies for case id {} ", caseDetails.getId());
        
        final CaseData caseData = caseDetails.getData();

        initializeSolicitor(caseData.getApplicant1());
        initializeSolicitor(caseData.getApplicant2());

        Solicitor app1Solicitor = caseData.getApplicant1().getSolicitor();
        Solicitor app2Solicitor = caseData.getApplicant2().getSolicitor();

        initializeOrgPolicy(app1Solicitor);
        initializeOrgPolicy(app2Solicitor);

        setDefaultOrgPolicyFields(app1Solicitor.getOrganisationPolicy(), UserRole.APPLICANT_1_SOLICITOR);
        setDefaultOrgPolicyFields(app2Solicitor.getOrganisationPolicy(), UserRole.APPLICANT_2_SOLICITOR);

        return caseDetails;
    }

    private void initializeSolicitor(Applicant applicant) {
        applicant.setSolicitor(
            Optional.ofNullable(applicant.getSolicitor())
                .orElseGet(Solicitor::new)
        );
    }

    private void initializeOrgPolicy(Solicitor solicitor) {
        solicitor.setOrganisationPolicy(
            Optional.ofNullable(solicitor.getOrganisationPolicy())
                .orElseGet(OrganisationPolicy::new)
        );
    }

    private void setDefaultOrgPolicyFields(OrganisationPolicy<UserRole> organisationPolicy, UserRole solicitorRole) {
        organisationPolicy.setOrgPolicyCaseAssignedRole(
            Optional.ofNullable(organisationPolicy.getOrgPolicyCaseAssignedRole())
                .orElse(solicitorRole)
        );

        organisationPolicy.setOrganisation(
            Optional.ofNullable(organisationPolicy.getOrganisation())
                .orElse(new Organisation(null, null))
        );
    }
}
