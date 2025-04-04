package uk.gov.hmcts.divorce.systemupdate.schedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.schedule.migration.Migration;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.util.List;

import static java.util.Comparator.comparing;

@Component
@Slf4j
@RequiredArgsConstructor
public class SystemMigrateCasesTask implements Runnable {

    private final IdamService idamService;

    private final AuthTokenGenerator authTokenGenerator;

    private final List<Migration> migrations;

    @Override
    public void run() {
        log.info("Migrate cases scheduled task started");

        final User user = idamService.retrieveSystemUpdateUserDetails();
        final String serviceAuthorization = authTokenGenerator.generate();

        migrations.stream()
            .sorted(comparing(Migration::getPriority)) //Ascending priority, 0 (zero) is highest
            .forEach(migration -> migration.apply(user, serviceAuthorization));
    }
}
