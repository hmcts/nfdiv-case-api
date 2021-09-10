package uk.gov.hmcts.divorce.divorcecase.model.access;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import uk.gov.hmcts.ccd.sdk.api.HasAccessControl;
import uk.gov.hmcts.ccd.sdk.api.HasRole;
import uk.gov.hmcts.ccd.sdk.api.Permission;

import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.ORGANISATION_CASE_ACCESS_ADMINISTRATOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.READ;

public class OrganisationPolicyAccess implements HasAccessControl {
    @Override
    public SetMultimap<HasRole, Permission> getGrants() {
        SetMultimap<HasRole, Permission> grants = HashMultimap.create();
        grants.putAll(ORGANISATION_CASE_ACCESS_ADMINISTRATOR, CREATE_READ_UPDATE);
        grants.putAll(APPLICANT_2_SOLICITOR, READ);
        grants.putAll(CITIZEN, CREATE_READ_UPDATE);
        grants.putAll(SOLICITOR, CREATE_READ_UPDATE_DELETE);

        grants.putAll(SUPER_USER, READ);
        grants.putAll(CASE_WORKER, READ);
        grants.putAll(LEGAL_ADVISOR, READ);

        return grants;
    }
}
