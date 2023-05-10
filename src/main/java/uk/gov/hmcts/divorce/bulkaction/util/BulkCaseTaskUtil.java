package uk.gov.hmcts.divorce.bulkaction.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.data.BulkListCaseDetails;
import uk.gov.hmcts.divorce.bulkaction.service.BulkTriggerService;
import uk.gov.hmcts.divorce.bulkaction.service.filter.CaseProcessingStateFilter;
import uk.gov.hmcts.divorce.bulkaction.task.BulkCaseCaseTaskFactory;
import uk.gov.hmcts.reform.idam.client.models.User;

import java.util.List;

@Component
@Slf4j
public class BulkCaseTaskUtil {

    @Autowired
    protected BulkTriggerService bulkTriggerService;

    @Autowired
    protected BulkCaseCaseTaskFactory bulkCaseCaseTaskFactory;

    @Autowired
    protected CaseProcessingStateFilter caseProcessingStateFilter;

    public CaseDetails<BulkActionCaseData, BulkActionState> processCases(final CaseDetails<BulkActionCaseData, BulkActionState> details,
                                                                         List<ListValue<BulkListCaseDetails>> bulkListCaseDetails,
                                                                         String eventName, User user, String serviceAuth) {

        final Long bulkCaseId = details.getId();
        final BulkActionCaseData bulkActionCaseData = details.getData();

        final List<ListValue<BulkListCaseDetails>> unprocessedCases = bulkTriggerService.bulkTrigger(
                bulkListCaseDetails,
                eventName,
                bulkCaseCaseTaskFactory.getCaseTask(details, eventName),
                user,
                serviceAuth
        );

        log.info("Error bulk case details list size {} for case id {} ", unprocessedCases.size(), bulkCaseId);

        final List<ListValue<BulkListCaseDetails>> processedCases = bulkActionCaseData.calculateProcessedCases(unprocessedCases);

        log.info("Successfully processed bulk case details list size {} for case id {}", processedCases.size(), bulkCaseId);

        bulkActionCaseData.setErroredCaseDetails(unprocessedCases);
        bulkActionCaseData.setProcessedCaseDetails(processedCases);

        return details;
    }
}

