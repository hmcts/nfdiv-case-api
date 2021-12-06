package uk.gov.hmcts.divorce.bulkaction.task;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;

import java.util.Map;
import javax.annotation.Resource;

@Component
public class BulkCaseCaseTaskFactory {

    @Resource(name = "bulkActionCaseTaskProviders")
    private Map<String, BulkActionCaseTaskProvider> caseTaskProviders;

    public CaseTask getCaseTask(final CaseDetails<BulkActionCaseData, BulkActionState> bulkCaseDetails, final String eventId) {
        if (caseTaskProviders.containsKey(eventId)) {
            return caseTaskProviders.get(eventId).getCaseTask(bulkCaseDetails);
        }

        throw new IllegalArgumentException(String.format("Cannot create CaseTask for Event Id: %s", eventId));
    }
}
