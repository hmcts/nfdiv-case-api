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
import uk.gov.hmcts.divorce.systemupdate.service.CcdFetchCaseService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.User;

import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.hmcts.divorce.bulkaction.ccd.event.SystemUpdateCase.SYSTEM_UPDATE_BULK_CASE;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPronouncement;
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
    private CcdFetchCaseService ccdFetchCaseService;

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
        bulkActionCaseData.setProcessedCaseDetails(processedBulkCases);

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

        List<ListValue<BulkListCaseDetails>> casesNotInCorrectState =
            bulkActionCaseData.getBulkListCaseDetails().stream()
                .filter(bulkCase -> !AwaitingPronouncement.getName().equals(
                    ccdFetchCaseService.fetchCaseById(
                        bulkCase.getValue().getCaseReference().getCaseReference(),
                        user,
                        serviceAuth).getState()
                    )
                )
                .collect(Collectors.toList());

        casesNotInCorrectState
            .forEach(bulkCase -> log.info(
                "Case ID {} will be skipped as not in correct state to be pronounced",
                bulkCase.getValue().getCaseReference().getCaseReference()));

        bulkActionCaseData.setErroredCaseDetails(casesNotInCorrectState);

        List<ListValue<BulkListCaseDetails>> updatedBulkListCaseDetails =
            bulkActionCaseData.getBulkListCaseDetails().stream()
                .filter(bulkCase -> !casesNotInCorrectState.contains(bulkCase))
                .collect(Collectors.toList());
        bulkActionCaseData.setBulkListCaseDetails(updatedBulkListCaseDetails);
    }
}
