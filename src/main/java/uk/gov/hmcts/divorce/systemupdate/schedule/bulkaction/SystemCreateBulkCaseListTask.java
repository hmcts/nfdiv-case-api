package uk.gov.hmcts.divorce.systemupdate.schedule.bulkaction;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.CaseLink;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionCaseTypeConfig;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.data.BulkListCaseDetails;
import uk.gov.hmcts.divorce.bulkaction.service.BulkTriggerService;
import uk.gov.hmcts.divorce.bulkaction.task.BulkCaseCaseTaskFactory;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.service.BulkCaseValidationService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdCreateService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static uk.gov.hmcts.divorce.systemupdate.event.SystemLinkWithBulkCase.SYSTEM_LINK_WITH_BULK_CASE;

@Component
@Slf4j
@RequiredArgsConstructor
public class SystemCreateBulkCaseListTask implements Runnable {

    @Value("${bulk-action.min-cases}")
    private int minimumCasesToProcess;

    private final CcdCreateService ccdCreateService;

    private final CcdSearchService ccdSearchService;

    private final IdamService idamService;

    private final AuthTokenGenerator authTokenGenerator;

    private final BulkCaseValidationService bulkCaseValidationService;

    private final FailedBulkCaseRemover failedBulkCaseRemover;

    private final BulkTriggerService bulkTriggerService;

    private final BulkCaseCaseTaskFactory bulkCaseCaseTaskFactory;

    @Override
    public void run() {
        log.info("Awaiting pronouncement scheduled task started");

        final User user = idamService.retrieveSystemUpdateUserDetails();
        final String serviceAuth = authTokenGenerator.generate();

        try {
            final Deque<List<CaseDetails<CaseData, State>>> pages =
                ccdSearchService.searchAwaitingPronouncementCasesAllPages(user, serviceAuth);

            while (!pages.isEmpty()) {
                final List<CaseDetails<CaseData, State>> casesAwaitingPronouncement = pages.poll();

                final List<ListValue<BulkListCaseDetails>> bulkListCaseDetails = checkCasesToProcess(casesAwaitingPronouncement);
                if (!bulkListCaseDetails.isEmpty()) {
                    final CaseDetails<BulkActionCaseData, BulkActionState> caseDetailsBulkCase = createBulkCase(
                        user,
                        serviceAuth,
                        bulkListCaseDetails);

                    final List<ListValue<BulkListCaseDetails>> failedAwaitingPronouncementCases = bulkTriggerService.bulkTrigger(
                        caseDetailsBulkCase.getData().getBulkListCaseDetails(),
                        SYSTEM_LINK_WITH_BULK_CASE,
                        bulkCaseCaseTaskFactory.getCaseTask(caseDetailsBulkCase, SYSTEM_LINK_WITH_BULK_CASE),
                        user,
                        serviceAuth);

                    failedBulkCaseRemover.removeFailedCasesFromBulkListCaseDetails(
                        failedAwaitingPronouncementCases,
                        caseDetailsBulkCase,
                        user,
                        serviceAuth
                    );
                }
            }
            log.info("Awaiting pronouncement scheduled task complete.");
        } catch (final CcdSearchCaseException e) {
            log.error("Awaiting pronouncement schedule task stopped after search error", e);
        } catch (final CcdManagementException e) {
            log.error("Bulk case creation failed with exception ", e);
        }
    }

    private CaseDetails<BulkActionCaseData, BulkActionState> createBulkCase(
        final User user,
        final String serviceAuth,
        final List<ListValue<BulkListCaseDetails>> bulkListCaseDetails) {

        final AtomicInteger counter = new AtomicInteger(1);
        final CaseDetails<BulkActionCaseData, BulkActionState> bulkActionCaseDetails = new CaseDetails<>();
        bulkActionCaseDetails.setCaseTypeId(BulkActionCaseTypeConfig.getCaseType());
        bulkActionCaseDetails.setData(BulkActionCaseData.builder()
            .bulkListCaseDetails(bulkListCaseDetails)
            .casesAcceptedToListForHearing(
                new ArrayList<>(
                    bulkListCaseDetails.stream()
                        .map(c ->
                            ListValue.<CaseLink>builder()
                                .id(String.valueOf(counter.getAndIncrement()))
                                .value(c.getValue().getCaseReference())
                                .build()
                        )
                        .toList())
            ).build());

        return ccdCreateService.createBulkCase(bulkActionCaseDetails, user, serviceAuth);
    }

    private List<ListValue<BulkListCaseDetails>> checkCasesToProcess(final List<CaseDetails<CaseData, State>> casesAwaitingPronouncement) {
        final String belowMinimum
            = "Number of cases does not reach the minimum for awaiting pronouncement processing, Minimum size needed %s, Case list size %s";

        if (minimumCasesToProcess > casesAwaitingPronouncement.size()) {
            log.info(String.format(belowMinimum, minimumCasesToProcess, casesAwaitingPronouncement.size()));
            return new ArrayList<>();
        }

        final List<ListValue<BulkListCaseDetails>> bulkListCaseDetails = createBulkCaseListDetails(casesAwaitingPronouncement);

        if (bulkListCaseDetails.isEmpty()) {
            log.error("Potential Elastic Search issue. bulkListCaseDetails not populated.");
            return new ArrayList<>();
        }

        if (minimumCasesToProcess > bulkListCaseDetails.size()) {
            log.error(String.format(belowMinimum, minimumCasesToProcess, bulkListCaseDetails.size()));
            return new ArrayList<>();
        }

        return bulkListCaseDetails;
    }

    private List<ListValue<BulkListCaseDetails>> createBulkCaseListDetails(
        final List<CaseDetails<CaseData, State>> casesAwaitingPronouncement
    ) {
        final List<ListValue<BulkListCaseDetails>> bulkListCaseDetails = new ArrayList<>();

        for (final CaseDetails<CaseData, State> caseDetails : casesAwaitingPronouncement) {
            final List<String> errors = bulkCaseValidationService.validateCaseForListing(caseDetails);
            if (!errors.isEmpty()) {
                log.error(
                    "Potential Elastic Search issue. Case {} skipped due to errors: {}",
                    caseDetails.getId(),
                    String.join("\n ", errors)
                );
            } else {
                bulkListCaseDetails.add(getBulkListCaseDetailsListValue(caseDetails.getData(), caseDetails.getId()));
            }
        }

        if (casesAwaitingPronouncement.size() > bulkListCaseDetails.size()) {
            log.error("{} cases were skipped as they were either not in the correct state or already linked to a bulk case",
                casesAwaitingPronouncement.size() - bulkListCaseDetails.size());
        }

        return bulkListCaseDetails;
    }

    private ListValue<BulkListCaseDetails> getBulkListCaseDetailsListValue(final CaseData caseData, Long id) {
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
                    .caseReference(String.valueOf(id))
                    .build()
            )
            .decisionDate(caseData.getConditionalOrder().getDecisionDate())
            .build();

        return ListValue
                .<BulkListCaseDetails>builder()
                .value(bulkCaseDetails)
                .build();
    }
}
