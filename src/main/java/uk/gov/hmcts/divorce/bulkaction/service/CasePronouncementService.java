package uk.gov.hmcts.divorce.bulkaction.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.data.BulkListCaseDetails;
import uk.gov.hmcts.divorce.bulkaction.service.filter.CaseFilterProcessingState;
import uk.gov.hmcts.divorce.bulkaction.service.filter.CaseProcessingStateFilter;
import uk.gov.hmcts.divorce.bulkaction.task.BulkCaseCaseTaskFactory;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.User;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static uk.gov.hmcts.divorce.bulkaction.ccd.event.SystemUpdateCase.SYSTEM_UPDATE_BULK_CASE;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPronouncement;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderPronounced;
import static uk.gov.hmcts.divorce.divorcecase.model.State.OfflineDocumentReceived;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemPronounceCase.SYSTEM_PRONOUNCE_CASE;

@Service
@Slf4j
public class CasePronouncementService {

    @Autowired
    private BulkTriggerService bulkTriggerService;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private CcdUpdateService ccdUpdateService;

    @Autowired
    private IdamService idamService;

    @Autowired
    private BulkCaseCaseTaskFactory bulkCaseCaseTaskFactory;

    @Autowired
    private CaseProcessingStateFilter caseProcessingStateFilter;

    @Async
    public void pronounceCases(final CaseDetails<BulkActionCaseData, BulkActionState> details) {
        pronounceCasesWithFilter(
            details,
            EnumSet.of(AwaitingPronouncement, OfflineDocumentReceived),
            EnumSet.of(ConditionalOrderPronounced)
        );
    }

    @Async
    public void retryPronounceCases(final CaseDetails<BulkActionCaseData, BulkActionState> details) {
        pronounceCasesWithFilter(
            details,
            EnumSet.of(AwaitingPronouncement, OfflineDocumentReceived, ConditionalOrderPronounced),
            EnumSet.noneOf(State.class)
        );
    }

    private void pronounceCasesWithFilter(CaseDetails<BulkActionCaseData, BulkActionState> details,
                                          EnumSet<State> awaitingPronouncement,
                                          EnumSet<State> postStates
    ) {
        final BulkActionCaseData bulkActionCaseData = details.getData();
        final User user = idamService.retrieveSystemUpdateUserDetails();
        final String serviceAuth = authTokenGenerator.generate();

        final CaseFilterProcessingState caseFilterProcessingState = caseProcessingStateFilter.filterProcessingState(
            bulkActionCaseData.getBulkListCaseDetails(),
            user,
            serviceAuth,
            awaitingPronouncement,
            postStates);

        final List<ListValue<BulkListCaseDetails>> erroredCaseDetails = caseFilterProcessingState.getErroredCases();
        final List<ListValue<BulkListCaseDetails>> processedCaseDetails = caseFilterProcessingState.getProcessedCases();
        final List<ListValue<BulkListCaseDetails>> unprocessedCases = caseFilterProcessingState.getUnprocessedCases();

        log.info("Unprocessed bulk case details list size {} and bulk case id {}", unprocessedCases.size(), details.getId());

        erroredCaseDetails.addAll(
            bulkTriggerService.bulkTrigger(
                unprocessedCases,
                SYSTEM_PRONOUNCE_CASE,
                bulkCaseCaseTaskFactory.getCaseTask(details, SYSTEM_PRONOUNCE_CASE),
                user,
                serviceAuth));

        bulkActionCaseData.setErroredCaseDetails(erroredCaseDetails);

        log.info("Error bulk case details list size {} and bulk case id {}", erroredCaseDetails.size(), details.getId());

        final Set<ListValue<BulkListCaseDetails>> mergeProcessedCases = new HashSet<>(processedCaseDetails);
        mergeProcessedCases.addAll(bulkActionCaseData.calculateProcessedCases(erroredCaseDetails));
        bulkActionCaseData.setProcessedCaseDetails(new ArrayList<>(mergeProcessedCases));

        log.info("Successfully processed bulk case details list size {} and bulk case id {}", mergeProcessedCases.size(), details.getId());

        try {
            ccdUpdateService.submitBulkActionEvent(
                details,
                SYSTEM_UPDATE_BULK_CASE,
                user,
                serviceAuth
            );
        } catch (final CcdManagementException e) {
            log.error("Update failed for bulk case id {} ", details.getId(), e);
        }
    }
}
