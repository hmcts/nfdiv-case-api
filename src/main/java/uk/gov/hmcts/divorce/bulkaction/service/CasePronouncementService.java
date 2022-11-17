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
import uk.gov.hmcts.divorce.bulkaction.task.BulkCaseCaseTaskFactory;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.User;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;
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
    private CcdSearchService ccdSearchService;

    @Autowired
    private IdamService idamService;

    @Autowired
    private BulkCaseCaseTaskFactory bulkCaseCaseTaskFactory;

    @Async
    public void pronounceCases(final CaseDetails<BulkActionCaseData, BulkActionState> details
    ) {
        final BulkActionCaseData bulkActionCaseData = details.getData();

        final User user = idamService.retrieveSystemUpdateUserDetails();
        final String serviceAuth = authTokenGenerator.generate();

        final List<ListValue<BulkListCaseDetails>> erroredCaseDetails = bulkActionCaseData.getErroredCaseDetails();
        final List<ListValue<BulkListCaseDetails>> processedCaseDetails = bulkActionCaseData.getProcessedCaseDetails();

        erroredCaseDetails.clear();
        processedCaseDetails.clear();

        final List<ListValue<BulkListCaseDetails>> unprocessedBulkCases =
            bulkTriggerService.bulkTrigger(
                filterCasesNotInCorrectState(bulkActionCaseData, user, serviceAuth),
                SYSTEM_PRONOUNCE_CASE,
                bulkCaseCaseTaskFactory.getCaseTask(details, SYSTEM_PRONOUNCE_CASE),
                user,
                serviceAuth);

        log.info("Error bulk case details list size {} and bulk case id {}", unprocessedBulkCases.size(), details.getId());

        List<ListValue<BulkListCaseDetails>> processedBulkCases = bulkActionCaseData.calculateProcessedCases(unprocessedBulkCases);

        log.info("Successfully processed bulk case details list size {} and bulk case id {}", processedBulkCases.size(), details.getId());

        erroredCaseDetails.addAll(unprocessedBulkCases);
        processedBulkCases.stream()
            .filter(listValue -> !processedCaseDetails.contains(listValue))
            .forEach(processedCaseDetails::add);

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

    private List<ListValue<BulkListCaseDetails>> filterCasesNotInCorrectState(final BulkActionCaseData bulkActionCaseData,
                                                                              final User user,
                                                                              final String serviceAuth) {

        List<String> caseReferences = bulkActionCaseData.getBulkListCaseDetails().stream()
            .map(bulkCase -> bulkCase.getValue().getCaseReference().getCaseReference())
            .collect(toList());

        List<String> casesToBeAddedToProcessedList = new ArrayList<>();
        List<String> casesToBeAddedToErroredList = new ArrayList<>();

        ccdSearchService.searchForCases(caseReferences, user, serviceAuth)
            .forEach(caseDetails -> {
                if (ConditionalOrderPronounced.name().equals(caseDetails.getState())) {
                    log.info(
                        "Case ID {} will be skipped and moved to processed list as already pronounced",
                        caseDetails.getId());
                    casesToBeAddedToProcessedList.add(String.valueOf(caseDetails.getId()));
                } else if (!AwaitingPronouncement.name().equals(caseDetails.getState())
                    && !OfflineDocumentReceived.name().equals(caseDetails.getState())) {
                    log.info(
                        "Case ID {} will be skipped and moved to error list as not in correct state to be pronounced",
                        caseDetails.getId());
                    casesToBeAddedToErroredList.add(String.valueOf(caseDetails.getId()));
                }
            });

        List<ListValue<BulkListCaseDetails>> updatedBulkList = new ArrayList<>();
        bulkActionCaseData.getBulkListCaseDetails()
            .forEach(bulkCase -> {
                if (casesToBeAddedToProcessedList.contains(bulkCase.getValue().getCaseReference().getCaseReference())) {
                    bulkActionCaseData.getProcessedCaseDetails().add(bulkCase);
                } else if (casesToBeAddedToErroredList.contains(bulkCase.getValue().getCaseReference().getCaseReference())) {
                    bulkActionCaseData.getErroredCaseDetails().add(bulkCase);
                } else {
                    updatedBulkList.add(bulkCase);
                }
            });

        return updatedBulkList;
    }
}
