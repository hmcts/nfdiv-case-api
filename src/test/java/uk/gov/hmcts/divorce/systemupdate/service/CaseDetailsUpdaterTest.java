package uk.gov.hmcts.divorce.systemupdate.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections4.map.HashedMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class CaseDetailsUpdaterTest {

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @InjectMocks
    private CaseDetailsUpdater caseDetailsUpdater;

    @Test
    void shouldReturnUpdatedCaseDetailsFromStartEventResponse() {
        final LocalDate now = LocalDate.now();
        final CaseTask caseTask = caseDetails -> {
            caseDetails.getData().setDueDate(now);
            return caseDetails;
        };
        final StartEventResponse startEventResponse = StartEventResponse.builder()
            .caseDetails(CaseDetails.builder()
                .data(new HashedMap<>())
                .build())
            .build();

        final uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> caseDetails = caseDetailsUpdater.updateCaseData(
            caseTask,
            startEventResponse);

        assertThat(caseDetails.getData().getDueDate()).isEqualTo(now);
    }
}