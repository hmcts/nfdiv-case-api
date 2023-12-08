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
import uk.gov.hmcts.divorce.bulkaction.service.filter.CaseFilterProcessingState;
import uk.gov.hmcts.divorce.bulkaction.service.filter.CaseProcessingStateFilter;
import uk.gov.hmcts.divorce.bulkaction.task.BulkCaseCaseTaskFactory;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.idam.User;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static uk.gov.hmcts.divorce.systemupdate.event.SystemPronounceCase.SYSTEM_PRONOUNCE_CASE;

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

    public CaseDetails<BulkActionCaseData, BulkActionState> pronounceCases(final CaseDetails<BulkActionCaseData, BulkActionState> details,
                                                                           EnumSet<State> preStates,
                                                                           EnumSet<State> postStates,
                                                                           User user,
                                                                           String serviceAuth) {
        final Long bulkCaseId = details.getId();
        final BulkActionCaseData bulkActionCaseData = details.getData();

        final CaseFilterProcessingState caseFilterProcessingState = caseProcessingStateFilter.filterProcessingState(
                bulkActionCaseData.getBulkListCaseDetails(),
                user,
                serviceAuth,
                preStates,
                postStates);

        final List<ListValue<BulkListCaseDetails>> erroredCaseDetails = caseFilterProcessingState.getErroredCases();
        final List<ListValue<BulkListCaseDetails>> processedCaseDetails = caseFilterProcessingState.getProcessedCases();
        final List<ListValue<BulkListCaseDetails>> unprocessedCases = caseFilterProcessingState.getUnprocessedCases();

        log.info("Unprocessed bulk case details list size {} and bulk case id {}", unprocessedCases.size(), bulkCaseId);

        erroredCaseDetails.addAll(
                bulkTriggerService.bulkTrigger(
                        unprocessedCases,
                        SYSTEM_PRONOUNCE_CASE,
                        bulkCaseCaseTaskFactory.getCaseTask(details, SYSTEM_PRONOUNCE_CASE),
                        user,
                        serviceAuth));

        bulkActionCaseData.setErroredCaseDetails(erroredCaseDetails);

        log.info("Error bulk case details list size {} and bulk case id {}", erroredCaseDetails.size(), bulkCaseId);

        final Set<ListValue<BulkListCaseDetails>> mergeProcessedCases = new HashSet<>(processedCaseDetails);
        mergeProcessedCases.addAll(bulkActionCaseData.calculateProcessedCases(erroredCaseDetails));
        bulkActionCaseData.setProcessedCaseDetails(new ArrayList<>(mergeProcessedCases));

        log.info("Successfully processed bulk case details list size {} and bulk case id {}", mergeProcessedCases.size(), bulkCaseId);

        return details;
    }
}

