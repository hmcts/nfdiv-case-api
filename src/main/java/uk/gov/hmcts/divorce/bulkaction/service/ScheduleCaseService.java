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
import javax.servlet.http.HttpServletRequest;

import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.divorce.bulkaction.ccd.event.SystemUpdateCaseErrors.SYSTEM_BULK_CASE_ERRORS;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemUpdateCaseWithCourtHearing.SYSTEM_UPDATE_CASE_COURT_HEARING;

@Service
@Slf4j
public class ScheduleCaseService {

    @Autowired
    private BulkTriggerService bulkTriggerService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private CcdUpdateService ccdUpdateService;

    @Autowired
    private IdamService idamService;

    @Async
    public void updateCourtHearingDetailsForCasesInBulk(final CaseDetails<BulkActionCaseData, BulkActionState> bulkCaseDetails) {
        final BulkActionCaseData bulkActionCaseData = bulkCaseDetails.getData();

        String requestHeader = request.getHeader(AUTHORIZATION);
        User user = idamService.retrieveUser(requestHeader);
        String serviceAuth = authTokenGenerator.generate();

        final List<ListValue<BulkListCaseDetails>> unprocessedBulkCases = bulkTriggerService.bulkTrigger(
            bulkActionCaseData.getBulkListCaseDetails(),
            SYSTEM_UPDATE_CASE_COURT_HEARING,
            mainCaseDetails -> {
                final var conditionalOrder = mainCaseDetails.getData().getConditionalOrder();
                conditionalOrder.setDateAndTimeOfHearing(
                    bulkCaseDetails.getData().getDateAndTimeOfHearing()
                );
                conditionalOrder.setCourtName(
                    bulkCaseDetails.getData().getCourtName()
                );
                return mainCaseDetails;
            },
            user,
            serviceAuth
        );

        log.info("Unprocessed bulk cases {} ", unprocessedBulkCases);

        if (!isEmpty(unprocessedBulkCases)) {
            log.info("Error bulk case details list is not empty hence updating bulk case with error list");
            bulkActionCaseData.setErroredCaseDetails(unprocessedBulkCases);

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
    }
}
