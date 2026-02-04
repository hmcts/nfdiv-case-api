package uk.gov.hmcts.divorce.systemupdate.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;

@Component
@RequiredArgsConstructor
public class CaseDetailsUpdater {

    private final ObjectMapper objectMapper;

    public CaseDetails<CaseData, State> updateCaseData(final CaseTask caseTask,
                                                       final StartEventResponse startEventResponse) {

        final uk.gov.hmcts.reform.ccd.client.model.CaseDetails initCaseDetails = startEventResponse.getCaseDetails();
        final CaseDetails<CaseData, State> caseDetails = objectMapper
            .convertValue(initCaseDetails, new TypeReference<>() {
            });

        return caseTask.apply(caseDetails);
    }
}
