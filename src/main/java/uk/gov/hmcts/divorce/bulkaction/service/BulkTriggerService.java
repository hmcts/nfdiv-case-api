package uk.gov.hmcts.divorce.bulkaction.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.bulkaction.data.BulkListCaseDetails;
import uk.gov.hmcts.divorce.bulkaction.service.CaseTriggerService.TriggerResult;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.divorce.idam.User;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
@Slf4j
@RequiredArgsConstructor
public class BulkTriggerService {

    private final CaseTriggerService caseTriggerService;

    public List<ListValue<BulkListCaseDetails>> bulkTrigger(final List<ListValue<BulkListCaseDetails>> bulkListCaseDetails,
                                                            final String eventId,
                                                            final CaseTask caseTask,
                                                            final User user,
                                                            final String serviceAuth) {

        log.info("Processing bulk list case details for Event ID: {}", eventId);

        return bulkListCaseDetails.parallelStream()
            .map(listValueCaseDetails -> caseTriggerService.caseTrigger(listValueCaseDetails, eventId, caseTask, user, serviceAuth))
            .filter(triggerResult -> !triggerResult.isProcessed())
            .map(TriggerResult::getListValueBulkListCaseDetails)
            .collect(toList());
    }
}
