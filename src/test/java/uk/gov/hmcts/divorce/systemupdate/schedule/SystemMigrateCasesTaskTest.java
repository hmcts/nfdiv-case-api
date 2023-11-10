package uk.gov.hmcts.divorce.systemupdate.schedule;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.schedule.migration.Migration;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
class SystemMigrateCasesTaskTest {

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Spy
    private List<Migration> migrations = new ArrayList<>();

    @InjectMocks
    private SystemMigrateCasesTask systemMigrateCasesTask;

    @Test
    void shouldCallEachMigrationInMigrationsList() {

        final User user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserInfo.builder().build());

        final Migration migration1 = mock(Migration.class);
        final Migration migration2 = mock(Migration.class);
        final Migration migration3 = mock(Migration.class);

        migrations.add(migration1);
        migrations.add(migration2);
        migrations.add(migration3);

        when(migration1.getPriority()).thenReturn(100);
        when(migration2.getPriority()).thenReturn(1000);
        when(migration3.getPriority()).thenReturn(0);

        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);

        systemMigrateCasesTask.run();

        InOrder inOrder = inOrder(migration3, migration1, migration2);

        inOrder.verify(migration3).apply(user, SERVICE_AUTHORIZATION);
        inOrder.verify(migration1).apply(user, SERVICE_AUTHORIZATION);
        inOrder.verify(migration2).apply(user, SERVICE_AUTHORIZATION);
    }
}
