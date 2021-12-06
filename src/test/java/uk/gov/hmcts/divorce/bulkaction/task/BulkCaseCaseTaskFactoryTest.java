package uk.gov.hmcts.divorce.bulkaction.task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BulkCaseCaseTaskFactoryTest {

    private static final String EVENT_ID = "event-id";

    @Mock
    private BulkActionCaseTaskProvider bulkActionCaseTaskProvider;

    @Mock
    private CaseTask caseTask;

    @InjectMocks
    private BulkCaseCaseTaskFactory bulkCaseCaseTaskFactory;

    @BeforeEach
    public void setProviderMap() {
        final Map<String, BulkActionCaseTaskProvider> providerMap = Map.of(EVENT_ID, bulkActionCaseTaskProvider);
        ReflectionTestUtils.setField(bulkCaseCaseTaskFactory, "caseTaskProviders", providerMap);
    }

    @Test
    void shouldReturnSystemUpdateCaseCourtHearingCaseTask() {

        final CaseDetails<BulkActionCaseData, BulkActionState> bulkCaseDetails = new CaseDetails<>();

        when(bulkActionCaseTaskProvider.getCaseTask(bulkCaseDetails)).thenReturn(caseTask);

        final CaseTask result = bulkCaseCaseTaskFactory.getCaseTask(bulkCaseDetails, EVENT_ID);

        assertThat(result).isSameAs(caseTask);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionIfUnknownEventId() {

        final CaseDetails<BulkActionCaseData, BulkActionState> bulkCaseDetails = new CaseDetails<>();

        assertThrows(
            IllegalArgumentException.class,
            () -> bulkCaseCaseTaskFactory.getCaseTask(bulkCaseDetails, "unknown-event"),
            "Cannot create CaseTask for Event Id: unknown-event");
    }
}