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
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.User;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.divorce.bulkaction.ccd.event.SystemUpdateCaseErrors.SYSTEM_BULK_CASE_ERRORS;

@Service
@Slf4j
public class PrintPronounceService {

    @Autowired
    private BulkTriggerService bulkTriggerService;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private CcdUpdateService ccdUpdateService;

    @Autowired
    private IdamService idamService;

    public static final String SYSTEM_UPDATE_CASE_PRONOUNCEMENT_JUDGE = "system-update-case-pronouncement-judge";

    @Async
    public void updatePronouncementJudgeDetailsForCasesInBulk(final CaseDetails<BulkActionCaseData, BulkActionState> bulkCaseDetails,
                                                              final String authorization) {
        final BulkActionCaseData bulkActionCaseData = bulkCaseDetails.getData();

        final User user = idamService.retrieveUser(authorization);
        final String serviceAuth = authTokenGenerator.generate();
        final List<ListValue<BulkListCaseDetails>> bulkListCaseDetails = bulkActionCaseData.getBulkListCaseDetails();

        final List<ListValue<BulkListCaseDetails>> unprocessedBulkCases = bulkTriggerService.bulkTrigger(
            bulkListCaseDetails,
            SYSTEM_UPDATE_CASE_PRONOUNCEMENT_JUDGE,
            mainCaseDetails -> {
                final var conditionalOrder = mainCaseDetails.getData().getConditionalOrder();
                conditionalOrder.setPronouncementJudge(
                    bulkCaseDetails.getData().getPronouncementJudge()
                );
                return mainCaseDetails;
            },
            user,
            serviceAuth
        );

        log.info("Error bulk case details list size{}", unprocessedBulkCases.size());

        List<ListValue<BulkListCaseDetails>> processedBulkCases =
            filterProcessedCases(unprocessedBulkCases, bulkListCaseDetails, bulkCaseDetails.getId());

        log.info("Successfully processed bulk case details list size{}", processedBulkCases.size());

        bulkCaseDetails.getData().setErroredCaseDetails(unprocessedBulkCases);
        bulkCaseDetails.getData().setProcessedCaseDetails(processedBulkCases);

        try {
            ccdUpdateService.submitBulkActionEvent(
                bulkCaseDetails,
                SYSTEM_BULK_CASE_ERRORS,
                user,
                serviceAuth
            );
        } catch (final FeignException e) {
            log.error("Update failed for bulk case id {} ", bulkCaseDetails.getId(), e);
        }
    }

    private List<ListValue<BulkListCaseDetails>> filterProcessedCases(final List<ListValue<BulkListCaseDetails>> unprocessedBulkCases,
                                                                      final List<ListValue<BulkListCaseDetails>> bulkListCaseDetails,
                                                                      final Long bulkCaseId) {

        List<String> unprocessedCaseIds = unprocessedBulkCases
            .stream()
            .map(lv -> lv.getValue().getCaseReference().getCaseReference())
            .collect(toList());

        if (isEmpty(unprocessedCaseIds)) {
            log.info("No unprocessed cases in bulk list for case id {} ", bulkCaseId);
            return bulkListCaseDetails;
        }

        return bulkListCaseDetails
            .stream()
            .filter(lv -> !unprocessedCaseIds.contains(lv.getValue().getCaseReference().getCaseReference()))
            .collect(toList());
    }
}
