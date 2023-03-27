package uk.gov.hmcts.divorce.bulkaction.task;

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
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.User;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPronouncement;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderPronounced;
import static uk.gov.hmcts.divorce.divorcecase.model.State.OfflineDocumentReceived;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemPronounceCase.SYSTEM_PRONOUNCE_CASE;

@Slf4j
@Component
public class PronounceCasesTask implements BulkCaseTask {

    final EnumSet<State> awaitingPronouncement = EnumSet.of(AwaitingPronouncement, OfflineDocumentReceived);
    final EnumSet<State> postStates = EnumSet.of(ConditionalOrderPronounced);

    @Autowired
    private BulkTriggerService bulkTriggerService;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private IdamService idamService;

    @Autowired
    private BulkCaseCaseTaskFactory bulkCaseCaseTaskFactory;

    @Autowired
    private CaseProcessingStateFilter caseProcessingStateFilter;

    @Override
    public CaseDetails<BulkActionCaseData, BulkActionState> apply(final CaseDetails<BulkActionCaseData, BulkActionState> details) {

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

        return details;
    }
}
