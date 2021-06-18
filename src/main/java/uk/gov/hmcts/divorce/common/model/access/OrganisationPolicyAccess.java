package uk.gov.hmcts.divorce.common.model.access;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import uk.gov.hmcts.ccd.sdk.api.HasAccessControl;
import uk.gov.hmcts.ccd.sdk.api.HasRole;
import uk.gov.hmcts.ccd.sdk.api.Permission;

import static uk.gov.hmcts.divorce.common.model.UserRole.APPLICANT_2_SOLICITOR;
import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_COURTADMIN_CTSC;
import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_COURTADMIN_RDU;
import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_SUPERUSER;
import static uk.gov.hmcts.divorce.common.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.common.model.UserRole.ORGANISATION_CASE_ACCESS_ADMINISTRATOR;
import static uk.gov.hmcts.divorce.common.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.common.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.common.model.access.Permissions.CREATE_READ_UPDATE_DELETE;
import static uk.gov.hmcts.divorce.common.model.access.Permissions.READ;

public class OrganisationPolicyAccess implements HasAccessControl {
    @Override
    public SetMultimap<HasRole, Permission> getGrants() {
        SetMultimap<HasRole, Permission> grants = HashMultimap.create();
        grants.putAll(ORGANISATION_CASE_ACCESS_ADMINISTRATOR, CREATE_READ_UPDATE);
        grants.putAll(APPLICANT_2_SOLICITOR, READ);
        grants.putAll(CITIZEN, CREATE_READ_UPDATE);
        grants.putAll(SOLICITOR, CREATE_READ_UPDATE_DELETE);

        grants.putAll(CASEWORKER_SUPERUSER, READ);
        grants.putAll(CASEWORKER_COURTADMIN_CTSC, READ);
        grants.putAll(CASEWORKER_COURTADMIN_RDU, READ);
        grants.putAll(CASEWORKER_LEGAL_ADVISOR, READ);

        return grants;
    }
}
