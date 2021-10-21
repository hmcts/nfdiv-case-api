package uk.gov.hmcts.divorce.systemupdate.schedule.bulkaction;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.ccd.sdk.type.CaseLink;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionCaseTypeConfig;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.data.BulkListCaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdCreateService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static uk.gov.hmcts.divorce.bulkaction.ccd.event.SystemRemoveFailedCases.SYSTEM_REMOVE_FAILED_CASES;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemLinkWithBulkCase.SYSTEM_LINK_WITH_BULK_CASE;

@Component
@Slf4j
public class SystemCreateBulkCaseListTask implements Runnable {

    @Value("${bulk-action.min-cases}")
    private int minimumCasesToProcess;

    @Autowired
    private CcdUpdateService ccdUpdateService;

    @Autowired
    private CcdCreateService ccdCreateService;

    @Autowired
    private CcdSearchService ccdSearchService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IdamService idamService;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Override
    public void run() {
        log.info("Awaiting pronouncement scheduled task started");

        final User user = idamService.retrieveSystemUpdateUserDetails();
        final String serviceAuth = authTokenGenerator.generate();

        try {
            final List<CaseDetails> casesAwaitingPronouncement = ccdSearchService.searchAwaitingPronouncementCases(user, serviceAuth);

            if (minimumCasesToProcess <= casesAwaitingPronouncement.size()) {

                List<ListValue<BulkListCaseDetails>> bulkListCaseDetails = createBulkCaseListDetails(casesAwaitingPronouncement);

                var bulkActionCaseDetails =
                    CaseDetails
                        .builder()
                        .caseTypeId(BulkActionCaseTypeConfig.CASE_TYPE)
                        .data(Map.of("bulkListCaseDetails", bulkListCaseDetails))
                        .build();

                CaseDetails caseDetailsBulkCase = ccdCreateService.createBulkCase(bulkActionCaseDetails, user, serviceAuth);

                List<Long> failedAwaitingPronouncementCaseIds = updateCasesWithBulkListingCaseId(
                    casesAwaitingPronouncement,
                    retrieveCaseIds(bulkListCaseDetails),
                    caseDetailsBulkCase.getId(),
                    user,
                    serviceAuth
                );

                removeFailedAwaitingPronouncementCasesFromBulkCase(
                    failedAwaitingPronouncementCaseIds,
                    caseDetailsBulkCase,
                    user,
                    serviceAuth
                );

            } else {
                log.info("Number of cases do not reach the minimum for awaiting pronouncement processing,"
                    + " Case list size {}", casesAwaitingPronouncement.size());
            }

            log.info("Awaiting pronouncement scheduled task complete.");
        } catch (final CcdSearchCaseException e) {
            log.error("Awaiting pronouncement schedule task stopped after search error", e);
        } catch (final CcdManagementException e) {
            log.error("Bulk case creation failed with exception ", e);
        }
    }

    private void removeFailedAwaitingPronouncementCasesFromBulkCase(final List<Long> failedAwaitingPronouncementCaseIds,
                                                                    final CaseDetails caseDetailsBulkCase,
                                                                    final User user,
                                                                    final String serviceAuth) {
        if (!CollectionUtils.isEmpty(failedAwaitingPronouncementCaseIds)) {
            log.info(
                "There are failed awaiting pronouncement cases with ids {} for bulk list case with id {} ",
                failedAwaitingPronouncementCaseIds,
                caseDetailsBulkCase.getId()
            );

            var bulkCaseData = objectMapper.convertValue(caseDetailsBulkCase.getData(), BulkActionCaseData.class);

            List<ListValue<BulkListCaseDetails>> bulkCaseDetailsListValues = bulkCaseData.getBulkListCaseDetails();

            final Predicate<ListValue<BulkListCaseDetails>> listValuePredicate = lv -> {
                Long caseId = Long.valueOf(lv.getValue().getCaseReference().getCaseReference());
                return failedAwaitingPronouncementCaseIds.contains(caseId);
            };

            bulkCaseDetailsListValues.removeIf(listValuePredicate);

            try {
                ccdUpdateService.updateBulkCaseWithRetries(
                    caseDetailsBulkCase,
                    SYSTEM_REMOVE_FAILED_CASES,
                    user,
                    serviceAuth,
                    caseDetailsBulkCase.getId()
                );
            } catch (final CcdManagementException e) {
                log.error("Removing failed awaiting pronouncement cases failed for bulk case id {} ", caseDetailsBulkCase.getId());
            }
        } else {
            log.info("No failed awaiting pronouncement cases to remove from bulk list case with id {} ", caseDetailsBulkCase.getId());
        }
    }

    private List<Long> retrieveCaseIds(List<ListValue<BulkListCaseDetails>> bulkListCaseDetails) {
        return bulkListCaseDetails
            .stream()
            .map(lv -> {
                CaseLink caseLink = lv.getValue().getCaseReference();
                return Long.valueOf(caseLink.getCaseReference());
            })
            .collect(Collectors.toList());
    }

    private List<Long> updateCasesWithBulkListingCaseId(final List<CaseDetails> casesAwaitingPronouncement,
                                                        final List<Long> bulkListCaseIds,
                                                        final Long bulkListCaseId,
                                                        final User user,
                                                        final String serviceAuth) {

        List<Long> failedToUpdateAwaitingPronouncementIds = new ArrayList<>();

        for (CaseDetails caseDetails : casesAwaitingPronouncement) {
            final Long awaitingPronouncementCaseId = caseDetails.getId();

            try {
                if (bulkListCaseIds.contains(awaitingPronouncementCaseId)) {
                    caseDetails.getData().put("bulkListCaseReference", String.valueOf(bulkListCaseId));
                    ccdUpdateService.submitEventWithRetry(caseDetails, SYSTEM_LINK_WITH_BULK_CASE, user, serviceAuth);
                    log.info("Successfully updated case id {} with bulk case id {} ", awaitingPronouncementCaseId, bulkListCaseId);
                } else {
                    log.info(
                        "Case id {} was not added to bulk list due to some failure hence skipping update ",
                        awaitingPronouncementCaseId
                    );
                }
            } catch (final CcdManagementException e) {
                log.error(
                    "Updating case with bulk action case reference {} failed for case id {} ",
                    bulkListCaseId,
                    awaitingPronouncementCaseId
                );
                failedToUpdateAwaitingPronouncementIds.add(awaitingPronouncementCaseId);
            }
        }
        return failedToUpdateAwaitingPronouncementIds;
    }

    private List<ListValue<BulkListCaseDetails>> createBulkCaseListDetails(final List<CaseDetails> casesAwaitingPronouncement) {
        List<ListValue<BulkListCaseDetails>> bulkListCaseDetails = new ArrayList<>();

        for (final CaseDetails caseDetails : casesAwaitingPronouncement) {
            try {
                final CaseData caseData = objectMapper.convertValue(caseDetails.getData(), CaseData.class);
                String caseParties = String.format("%s %s vs %s %s",
                    caseData.getApplicant1().getFirstName(),
                    caseData.getApplicant1().getLastName(),
                    caseData.getApplicant2().getFirstName(),
                    caseData.getApplicant2().getLastName()
                );

                var bulkCaseDetails = BulkListCaseDetails
                    .builder()
                    .caseParties(caseParties)
                    .caseReference(
                        CaseLink
                            .builder()
                            .caseReference(String.valueOf(caseDetails.getId()))
                            .build()
                    )
                    .decisionDate(caseData.getConditionalOrder().getDecisionDate())
                    .build();

                var bulkListCaseDetailsListValue =
                    ListValue
                        .<BulkListCaseDetails>builder()
                        .value(bulkCaseDetails)
                        .build();

                bulkListCaseDetails.add(bulkListCaseDetailsListValue);

            } catch (final IllegalArgumentException e) {
                log.error("Deserialization failed for case id: {}, continuing to next case", caseDetails.getId());
            }
        }
        return bulkListCaseDetails;
    }
}
