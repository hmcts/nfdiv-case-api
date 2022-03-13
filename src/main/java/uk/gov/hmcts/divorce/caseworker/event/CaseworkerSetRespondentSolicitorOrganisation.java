package uk.gov.hmcts.divorce.caseworker.event;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.type.Organisation;
import uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
public class CaseworkerSetRespondentSolicitorOrganisation implements CCDConfig<CaseData, State, UserRole> {

    public static final String CASEWORKER_UPDATE_RESPONDENT_SOL_ORG = "caseworker-update-respondent-sol-org";
    public static final  String NEVER_SHOW = "divorceOrDissolution=\"NEVER_SHOW\"";
    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_UPDATE_RESPONDENT_SOL_ORG)
            .forState(Submitted)
            .name("Set solicitor's organisation")
            .description("Set solicitor's organisation")
            .showSummary()
            .showEventNotes()
            .grant(CREATE_READ_UPDATE, CASE_WORKER)
            .grantHistoryOnly(SUPER_USER, LEGAL_ADVISOR))
            .page("setSolicitorOrg")
            .pageLabel("Set solicitor's organisation")
            .complex(CaseData::getApplicant2)
                .complex(Applicant::getSolicitor)
                    .readonlyWithLabel(Solicitor::getName, "Solicitor’s full name")
                    .readonlyWithLabel(Solicitor::getReference, "Solicitor reference")
                    .readonlyWithLabel(Solicitor::getPhone,  "Solicitor’s direct phone number")
                    .readonlyWithLabel(Solicitor::getEmail, "Solicitor’s email address")
                    .readonlyWithLabel(Solicitor::getAddress,"Solicitor’s firm/ DX address")
                        .complex(Solicitor::getOrganisationPolicy, null, "Solicitor’s firm address")
                            .complex(OrganisationPolicy::getOrganisation)
                                .mandatory(Organisation::getOrganisationId)
                            .done()
                            .optional(OrganisationPolicy::getOrgPolicyCaseAssignedRole, NEVER_SHOW, APPLICANT_2_SOLICITOR)
                            .optional(OrganisationPolicy::getOrgPolicyReference, NEVER_SHOW)
                       .done()
                .done()
            .done();
    }
}
