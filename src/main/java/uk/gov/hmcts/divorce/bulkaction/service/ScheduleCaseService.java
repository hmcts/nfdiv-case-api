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
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.User;

import java.util.List;

import static uk.gov.hmcts.divorce.bulkaction.ccd.event.SystemUpdateCase.SYSTEM_UPDATE_BULK_CASE;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemUpdateCaseWithCourtHearing.SYSTEM_UPDATE_CASE_COURT_HEARING;

@Service
@Slf4j
public class ScheduleCaseService {

    @Autowired
    private BulkTriggerService bulkTriggerService;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private CcdUpdateService ccdUpdateService;

    @Autowired
    private IdamService idamService;

    @Async
    public void updateCourtHearingDetailsForCasesInBulk(final CaseDetails<BulkActionCaseData, BulkActionState> bulkCaseDetails,
                                                        final String authorization) {
        final BulkActionCaseData bulkActionCaseData = bulkCaseDetails.getData();

        final User user = idamService.retrieveUser(authorization);
        final String serviceAuth = authTokenGenerator.generate();
        final List<ListValue<BulkListCaseDetails>> bulkListCaseDetails = bulkActionCaseData.getBulkListCaseDetails();

        final List<ListValue<BulkListCaseDetails>> unprocessedBulkCases = bulkTriggerService.bulkTrigger(
            bulkListCaseDetails,
            SYSTEM_UPDATE_CASE_COURT_HEARING,
            getCaseTask(bulkCaseDetails.getData()),
            user,
            serviceAuth
        );

        log.info("Error bulk case details list size {}", unprocessedBulkCases.size());

        List<ListValue<BulkListCaseDetails>> processedBulkCases = bulkActionCaseData.calculateProcessedCases(unprocessedBulkCases);

        log.info("Successfully processed bulk case details list size {}", processedBulkCases.size());

        bulkCaseDetails.getData().setErroredCaseDetails(unprocessedBulkCases);
        bulkCaseDetails.getData().setProcessedCaseDetails(processedBulkCases);

        try {
            ccdUpdateService.submitBulkActionEvent(
                bulkCaseDetails,
                SYSTEM_UPDATE_BULK_CASE,
                user,
                serviceAuth
            );
        } catch (final FeignException e) {
            log.error("Update failed for bulk case id {} ", bulkCaseDetails.getId(), e);
        }
    }

    public CaseTask getCaseTask(final BulkActionCaseData bulkActionCaseData) {
        return mainCaseDetails -> {
            final var conditionalOrder = mainCaseDetails.getData().getConditionalOrder();
            conditionalOrder.setDateAndTimeOfHearing(
                bulkActionCaseData.getDateAndTimeOfHearing()
            );
            conditionalOrder.setCourtName(
                bulkActionCaseData.getCourtName()
            );
            return mainCaseDetails;
        };
    }
}
