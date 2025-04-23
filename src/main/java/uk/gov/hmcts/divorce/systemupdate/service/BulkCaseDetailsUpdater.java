package uk.gov.hmcts.divorce.systemupdate.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.task.BulkCaseTask;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;

@Component
@RequiredArgsConstructor
public class BulkCaseDetailsUpdater {

    private final ObjectMapper objectMapper;

    public CaseDetails<BulkActionCaseData, BulkActionState> updateCaseData(final BulkCaseTask bulkCaseTask,
                                                                           final StartEventResponse startEventResponse) {

        final uk.gov.hmcts.reform.ccd.client.model.CaseDetails initCaseDetails = startEventResponse.getCaseDetails();
        final CaseDetails<BulkActionCaseData, BulkActionState> caseDetails = objectMapper
            .convertValue(initCaseDetails, new TypeReference<>() {
            });

        return bulkCaseTask.apply(caseDetails);
    }
}
