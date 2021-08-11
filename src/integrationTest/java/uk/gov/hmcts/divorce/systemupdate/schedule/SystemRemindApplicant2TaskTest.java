package uk.gov.hmcts.divorce.systemupdate.schedule;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.concurrent.TimeUnit;

import static com.jayway.awaitility.Awaitility.await;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("schedule_test")
public class SystemRemindApplicant2TaskTest {

    @SpyBean
    private SystemRemindApplicant2Task systemRemindApplicant2Task;

    @Test
    public void givenApplicationIsRunningApplicationNotReviewedScheduledJobIsExecutedAtLeastOnce() {

        await("Remind applicant 2 task executed at least once")
            .atMost(60, TimeUnit.SECONDS)
            .pollInterval(2, TimeUnit.SECONDS)
            .until(() -> verify(systemRemindApplicant2Task, atLeastOnce()).execute());
    }
}
