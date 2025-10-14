package uk.gov.hmcts.divorce.systemupdate.schedule;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

    @Test
    void loadRectifyBatches_parsesMultipleLines() throws IOException {
        List<TaskHelper.BulkRectifySpec> specs = task.loadRectifyBatches("TaskHelperTest2.csv");

        assertEquals(2, specs.size());

        TaskHelper.BulkRectifySpec first = specs.get(0);
        assertEquals(1758254429226124L, first.bulkRef());
        assertEquals(List.of(1743586983353275L, 1743507344866516L), first.caseRefs());

        TaskHelper.BulkRectifySpec second = specs.get(1);
        assertEquals(1758261653985127L, second.bulkRef());
        assertEquals(List.of(1743669242986927L, 1743669242986928L), second.caseRefs());
    }

    @Test
    void loadRectifyBatches_returnsEmptyListForEmptyFile() throws IOException {
        List<TaskHelper.BulkRectifySpec> specs = task.loadRectifyBatches("TaskHelperTest_empty.csv");
        assertNotNull(specs);
        assertTrue(specs.isEmpty());
    }
}

