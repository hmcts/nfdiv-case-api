package uk.gov.hmcts.divorce.systemupdate.schedule;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.concurrent.TimeUnit;

import static com.jayway.awaitility.Awaitility.await;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SystemProgressHeldCasesTaskTest {
    @SpyBean
    private SystemProgressHeldCasesTask awaitingConditionalOrderTask;

    @Test
    public void givenValidCaseDataWhenCallbackIsInvokedThenOrderSummaryAndSolicitorRolesAreSet() {
        await(" Awaiting Conditional order task executed atleast once")
            .atMost(60, TimeUnit.SECONDS)
            .pollInterval(2, TimeUnit.SECONDS)
            .until(() -> verify(awaitingConditionalOrderTask, atLeastOnce()).execute());
    }
}
