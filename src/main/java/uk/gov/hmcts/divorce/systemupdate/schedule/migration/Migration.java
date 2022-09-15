package uk.gov.hmcts.divorce.systemupdate.schedule.migration;

import uk.gov.hmcts.reform.idam.client.models.User;

public interface Migration {

    void apply(final User user, final String serviceAuthorization);

    default Integer getPriority() {
        return 100;
    }
}
