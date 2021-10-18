package uk.gov.hmcts.divorce.bulkaction.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.divorce.bulkaction.data.BulkListCaseDetails;
import uk.gov.hmcts.divorce.bulkaction.service.CaseTriggerService.TriggerResult;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.reform.idam.client.models.User;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
@Slf4j
public class BulkTriggerService {

    @Autowired
    private CaseTriggerService caseTriggerService;

    public List<BulkListCaseDetails> bulkTrigger(final List<BulkListCaseDetails> bulkListCaseDetails,
                                                 final String eventId,
                                                 final CaseTask caseTask,
                                                 final User user,
                                                 final String serviceAuth) {

        log.info("Processing bulk list case details for Event ID: {}", eventId);

        return bulkListCaseDetails.parallelStream()
            .map(caseDetails -> caseTriggerService.caseTrigger(caseDetails, eventId, caseTask, user, serviceAuth))
            .filter(triggerResult -> !triggerResult.isProcessed())
            .map(TriggerResult::getBulkListCaseDetails)
            .collect(toList());
    }
}
