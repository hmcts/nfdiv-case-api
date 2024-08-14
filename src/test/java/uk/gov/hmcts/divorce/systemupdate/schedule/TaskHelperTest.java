package uk.gov.hmcts.divorce.systemupdate.schedule;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.isNull;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class TaskHelperTest {

    @InjectMocks
    private TaskHelper task;

    @Mock
    private Logger logger;

    @Test
    void testReadIdsFromFile() throws IOException {
        List<Long> idList = task.loadCaseIds("TaskHelperTest.csv");

        assertNotNull(idList);
        assertFalse(idList.isEmpty());
    }

    @Test
    void testLogError() {
        task.logError("Test Error", null, null);

        verify(logger).error(eq("Test Error"), isNull(), isNull());
    }

}

