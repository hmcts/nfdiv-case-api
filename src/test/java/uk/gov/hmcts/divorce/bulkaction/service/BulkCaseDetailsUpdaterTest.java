package uk.gov.hmcts.divorce.bulkaction.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.task.UpdateCourtHearingDetailsTask;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderCourt;
import uk.gov.hmcts.divorce.systemupdate.service.BulkCaseDetailsUpdater;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BulkCaseDetailsUpdaterTest {

    @Spy
    ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    UpdateCourtHearingDetailsTask updateCourtHearingDetailsTask;

    @InjectMocks
    private BulkCaseDetailsUpdater bulkCaseDetailsUpdater;

    @Test
    void shouldRunCaseTaskAgainstGivenStartEventResponseCaseDetails() {

        final uk.gov.hmcts.reform.ccd.client.model.CaseDetails startEventCaseDetails =
            uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().data(Map.of("court", "birmingham")).build();
        StartEventResponse startEventResponse = StartEventResponse
            .builder()
            .caseDetails(startEventCaseDetails)
            .build();

        final CaseDetails<BulkActionCaseData, BulkActionState> mappedCaseDetails = new CaseDetails<>();
        final BulkActionCaseData bulkActionCaseData = BulkActionCaseData.builder()
            .court(ConditionalOrderCourt.BIRMINGHAM)
            .build();
        mappedCaseDetails.setData(bulkActionCaseData);

        when(updateCourtHearingDetailsTask.apply(any())).thenReturn(mappedCaseDetails);

        var result = bulkCaseDetailsUpdater.updateCaseData(updateCourtHearingDetailsTask, startEventResponse);

        verify(updateCourtHearingDetailsTask).apply(any());
        Assertions.assertThat(result.getData().getCourt()).isEqualTo(ConditionalOrderCourt.BIRMINGHAM);
    }
}
