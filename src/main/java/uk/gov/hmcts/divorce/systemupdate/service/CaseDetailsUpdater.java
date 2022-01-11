package uk.gov.hmcts.divorce.systemupdate.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;

@Component
@Slf4j
public class CaseDetailsUpdater {

    @Autowired
    private ObjectMapper objectMapper;

    public CaseDetails<CaseData, State> updateCaseData(final CaseTask caseTask,
                                                       final StartEventResponse startEventResponse) {

        final uk.gov.hmcts.reform.ccd.client.model.CaseDetails initCaseDetails = startEventResponse.getCaseDetails();
        final CaseDetails<CaseData, State> caseDetails = objectMapper
            .convertValue(initCaseDetails, new TypeReference<>() {
            });

        //TODO: Remove temp logging for tracking certificate of entitlement
        log.info(
            "****** Converted CaseData for case id: {}, conditional order: {}",
            initCaseDetails.getId(),
            caseDetails.getData().getConditionalOrder());

        return caseTask.apply(caseDetails);
    }
}
