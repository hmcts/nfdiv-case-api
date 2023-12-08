package uk.gov.hmcts.divorce.systemupdate.schedule.migration;

import uk.gov.hmcts.divorce.idam.User;

public interface Migration {

    /**
     * Apply the migration with the given user and service authorization.
     *
     * @param user                 - for authentication with CCD API
     * @param serviceAuthorization - for authorization with CCD API
     */
    void apply(final User user, final String serviceAuthorization);

    /**
     * Returns the priority of the migration task.
     * Priority is ascending, 0 (zero) is highest, then 1,2,3...
     *
     * @return migration priority
     */
    default Integer getPriority() {
        return 100;
    }
}
