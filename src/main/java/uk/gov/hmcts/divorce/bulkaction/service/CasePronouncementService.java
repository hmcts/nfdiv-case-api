package uk.gov.hmcts.divorce.bulkaction.service;

import feign.FeignException;
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
    public void pronounceCases(final CaseDetails<BulkActionCaseData, BulkActionState> details,
                               final String authorization) {
        final BulkActionCaseData bulkActionCaseData = details.getData();

        final User user = idamService.retrieveUser(authorization);
        final String serviceAuth = authTokenGenerator.generate();

        filterCasesNotInCorrectState(bulkActionCaseData, user, serviceAuth);

        final List<ListValue<BulkListCaseDetails>> unprocessedBulkCases =
            bulkTriggerService.bulkTrigger(
                bulkActionCaseData.getBulkListCaseDetails(),
                SYSTEM_PRONOUNCE_CASE,
                bulkCaseCaseTaskFactory.getCaseTask(details, SYSTEM_PRONOUNCE_CASE),
                user,
                serviceAuth);

        log.info("Error bulk case details list size {}", unprocessedBulkCases.size());

        List<ListValue<BulkListCaseDetails>> processedBulkCases = bulkActionCaseData.calculateProcessedCases(unprocessedBulkCases);

        log.info("Successfully processed bulk case details list size {}", processedBulkCases.size());

        bulkActionCaseData.getErroredCaseDetails().addAll(unprocessedBulkCases);
        bulkActionCaseData.getProcessedCaseDetails().addAll(processedBulkCases);

        try {
            ccdUpdateService.submitBulkActionEvent(
                details,
                SYSTEM_UPDATE_BULK_CASE,
                user,
                serviceAuth
            );
        } catch (final FeignException e) {
            log.error("Update failed for bulk case id {} ", details.getId(), e);
        }
    }

    private void filterCasesNotInCorrectState(BulkActionCaseData bulkActionCaseData,
                                              User user,
                                              String serviceAuth) {

        List<String> caseReferences = bulkActionCaseData.getBulkListCaseDetails().stream()
            .map(bulkCase -> bulkCase.getValue().getCaseReference().getCaseReference())
            .collect(toList());

        List<String> casesToBeAddedToProcessedList = new ArrayList<>();
        List<String> casesToBeAddedToErroredList = new ArrayList<>();

        ccdSearchService.searchForCases(caseReferences, user, serviceAuth)
            .forEach(caseDetails -> {
                if (ConditionalOrderPronounced.getName().equals(caseDetails.getState())) {
                    log.info(
                        "Case ID {} will be skipped and moved to processed list as already pronounced",
                        caseDetails.getId());
                    casesToBeAddedToProcessedList.add(String.valueOf(caseDetails.getId()));
                } else if (!AwaitingPronouncement.getName().equals(caseDetails.getState())) {
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

        bulkActionCaseData.setBulkListCaseDetails(updatedBulkList);
    }
}
