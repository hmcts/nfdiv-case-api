package uk.gov.hmcts.divorce.bulkaction.ccd.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionPageBuilder;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.data.BulkListCaseDetails;
import uk.gov.hmcts.divorce.bulkaction.service.BulkTriggerService;
import uk.gov.hmcts.divorce.bulkaction.service.ScheduleCaseService;
import uk.gov.hmcts.divorce.bulkaction.task.BulkCaseCaseTaskFactory;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.schedule.bulkaction.FailedBulkCaseRemover;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState.Created;
import static uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState.Listed;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPronouncement;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemLinkWithBulkCase.SYSTEM_LINK_WITH_BULK_CASE;

@Component
@Slf4j
@RequiredArgsConstructor
public class CaseworkerScheduleCase implements CCDConfig<BulkActionCaseData, BulkActionState, UserRole> {
    public static final String CASEWORKER_SCHEDULE_CASE = "caseworker-schedule-case";
    public static final String ERROR_HEARING_DATE_IN_PAST = "Please enter a hearing date and time in the future";
    public static final String ERROR_CASE_IDS_DUPLICATED = "Case IDs duplicated in the list: ";
    public static final String ERROR_REMOVE_DUPLICATES = "Please remove duplicates and try again";
    public static final String ERROR_NO_NEW_CASES_ADDED = "Please add at least one new case to schedule for listing";
    public static final String ERROR_CASE_ID = "Case ID ";
    public static final String ERROR_INVALID_STATE = " is in state ";
    public static final String ERROR_ONLY_AWAITING_PRONOUNCEMENT = ". Only cases in Awaiting Pronouncement can be scheduled for listing";
    public static final String ERROR_ALREADY_LINKED_TO_BULK_CASE = " is already linked to bulk case ";
    public static final String ERROR_CASES_NOT_FOUND = "Search returned no results for the following Case IDs: ";
    public static final String ERROR_NO_CASES_FOUND = "Search returned no cases for the provided Case IDs: ";

    private static final String SCHEDULE_CASES_FOR_LISTING = "Schedule cases for listing";

    private final ScheduleCaseService scheduleCaseService;
    private final BulkTriggerService bulkTriggerService;
    private final BulkCaseCaseTaskFactory bulkCaseCaseTaskFactory;
    private final IdamService idamService;
    private final AuthTokenGenerator authTokenGenerator;
    private final HttpServletRequest request;
    private final FailedBulkCaseRemover failedBulkCaseRemover;
    private final CcdSearchService ccdSearchService;
    private final ObjectMapper objectMapper;

    @Override
    public void configure(final ConfigBuilder<BulkActionCaseData, BulkActionState, UserRole> configBuilder) {
        new BulkActionPageBuilder(configBuilder
            .event(CASEWORKER_SCHEDULE_CASE)
            .forStates(Created, Listed)
            .name(SCHEDULE_CASES_FOR_LISTING)
            .description(SCHEDULE_CASES_FOR_LISTING)
            .showSummary()
            .showEventNotes()
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted)
            .explicitGrants()
            .grant(CREATE_READ_UPDATE, CASE_WORKER, SYSTEMUPDATE))
            .page("scheduleForListing", this::midEvent)
            .pageLabel(SCHEDULE_CASES_FOR_LISTING)
            .mandatory(BulkActionCaseData::getCourt)
            .mandatory(BulkActionCaseData::getDateAndTimeOfHearing)
            .mandatoryNoSummary(BulkActionCaseData::getBulkListCaseDetails);
    }

    public AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> midEvent(
        CaseDetails<BulkActionCaseData, BulkActionState> bulkCaseDetails,
        CaseDetails<BulkActionCaseData, BulkActionState> beforeDetails
    ) {
        log.info("{} mid event callback invoked for Case Id: {}", CASEWORKER_SCHEDULE_CASE, bulkCaseDetails.getId());

        final List<String> errors = validateData(bulkCaseDetails.getData(), beforeDetails.getData());
        if (!errors.isEmpty()) {
            return AboutToStartOrSubmitResponse
                .<BulkActionCaseData, BulkActionState>builder()
                .errors(errors)
                .data(bulkCaseDetails.getData())
                .build();
        }

        return AboutToStartOrSubmitResponse.<BulkActionCaseData, BulkActionState>builder().build();
    }

    public AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> aboutToSubmit(
        final CaseDetails<BulkActionCaseData, BulkActionState> bulkCaseDetails,
        final CaseDetails<BulkActionCaseData, BulkActionState> beforeDetails
    ) {

        log.info("{} about to submit callback invoked for Case Id: {}", CASEWORKER_SCHEDULE_CASE, bulkCaseDetails.getId());

        return AboutToStartOrSubmitResponse
            .<BulkActionCaseData, BulkActionState>builder()
            .data(bulkCaseDetails.getData())
            .state(Listed)
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<BulkActionCaseData, BulkActionState> bulkCaseDetails,
                                               CaseDetails<BulkActionCaseData, BulkActionState> beforeDetails) {

        log.info("{} submitted callback invoked for Case Id: {}", CASEWORKER_SCHEDULE_CASE, bulkCaseDetails.getId());

        User user = idamService.retrieveUser(request.getHeader(AUTHORIZATION));
        String serviceAuth = authTokenGenerator.generate();

        if (user.getUserDetails().getRoles().contains(CASE_WORKER.getRole())) {
            User systemUser = idamService.retrieveSystemUpdateUserDetails();

            final List<ListValue<BulkListCaseDetails>> failedAwaitingPronouncementCases = bulkTriggerService.bulkTrigger(
                    bulkCaseDetails.getData().getBulkListCaseDetails(),
                    SYSTEM_LINK_WITH_BULK_CASE,
                    bulkCaseCaseTaskFactory.getCaseTask(bulkCaseDetails, SYSTEM_LINK_WITH_BULK_CASE),
                    systemUser,
                    serviceAuth);

            failedBulkCaseRemover.removeFailedCasesFromBulkListCaseDetails(
                    failedAwaitingPronouncementCases,
                    bulkCaseDetails,
                    systemUser,
                    serviceAuth
            );
        }

        scheduleCaseService.updateCourtHearingDetailsForCasesInBulk(bulkCaseDetails);
        return SubmittedCallbackResponse.builder().build();
    }

    private class DuplicateCheckResult {
        private final List<String> duplicateIds;
        private final List<String> uniqueIds;

        public DuplicateCheckResult(List<String> duplicateIds, List<String> uniqueIds) {
            this.duplicateIds = duplicateIds;
            this.uniqueIds = uniqueIds;
        }

        public List<String> getDuplicateIds() {
            return duplicateIds;
        }

        public boolean hasDuplicates() {
            return !duplicateIds.isEmpty();
        }

        public List<String> getUniqueIds() {
            return uniqueIds;
        }

        public boolean hasUniqueIds() {
            return !uniqueIds.isEmpty();
        }
    }

    private DuplicateCheckResult checkForDuplicateIds(final List<String> caseIds) {
        final List<String> duplicateCaseIds = new ArrayList<>();
        final List<String> uniqueIds = new ArrayList<>();
        final Map<String, Integer> frequencyMap = new java.util.HashMap<>();

        for (String caseId : caseIds) {
            frequencyMap.put(caseId, frequencyMap.getOrDefault(caseId, 0) + 1);
        }

        for (Map.Entry<String, Integer> entry : frequencyMap.entrySet()) {
            if (entry.getValue() > 1) {
                duplicateCaseIds.add(entry.getKey());
            } else if (entry.getValue() == 1) {
                uniqueIds.add(entry.getKey());
            }
        }

        return new DuplicateCheckResult(duplicateCaseIds, uniqueIds);
    }

    private DuplicateCheckResult checkForDuplicates(final List<ListValue<BulkListCaseDetails>> bulkListCaseDetails) {
        final List<String> caseIds = bulkListCaseDetails.stream()
            .map(caseDetails -> caseDetails.getValue().getCaseReference().getCaseReference())
            .toList();
        return checkForDuplicateIds(caseIds);
    }

    private List<String> getMissingIds(
        final List<uk.gov.hmcts.reform.ccd.client.model.CaseDetails> searchResultsList,
        final List<String> searchedIds
    ) {
        if (searchResultsList.size() < searchedIds.size()) {
            final List<String> foundIds = searchResultsList.stream().map(caseDetails -> caseDetails.getId().toString()).toList();
            return searchedIds.stream().filter(id -> !foundIds.contains(id)).toList();
        }
        return new ArrayList<>();
    }

    private DuplicateCheckResult getNewCaseIds(final List<ListValue<BulkListCaseDetails>> bulkListCaseDetails,
                                       final List<ListValue<BulkListCaseDetails>> beforeBulkListCaseDetails
    ) {
        if (beforeBulkListCaseDetails == null) {
            return checkForDuplicates(bulkListCaseDetails);
        }

        return checkForDuplicateIds(bulkListCaseDetails.stream()
            .filter(caseDetails -> !beforeBulkListCaseDetails.contains(caseDetails))
            .toList()
            .stream()
            .map(caseDetails -> caseDetails.getValue().getCaseReference().getCaseReference())
            .toList()
        );
    }

    private List<String> validateData(final BulkActionCaseData bulkActionCaseData, final BulkActionCaseData beforeBulkActionCaseData) {
        final List<String> errors = new ArrayList<>();

        if (bulkActionCaseData.getDateAndTimeOfHearing().isBefore(LocalDateTime.now())) {
            errors.add(ERROR_HEARING_DATE_IN_PAST);
        }

        final DuplicateCheckResult duplicateCheckResult = checkForDuplicates(bulkActionCaseData.getBulkListCaseDetails());
        if (duplicateCheckResult.hasDuplicates()) {
            errors.add(ERROR_CASE_IDS_DUPLICATED + String.join(", ", duplicateCheckResult.getDuplicateIds()));
            errors.add(ERROR_REMOVE_DUPLICATES);
        }

        final DuplicateCheckResult newCaseIds = getNewCaseIds(
            bulkActionCaseData.getBulkListCaseDetails(),
            beforeBulkActionCaseData.getBulkListCaseDetails()
        );

        if (!newCaseIds.hasUniqueIds()) {
            if (duplicateCheckResult.hasDuplicates()) {
                return errors;
            }
            errors.add(ERROR_NO_NEW_CASES_ADDED);
            return errors;
        }

        final List<uk.gov.hmcts.reform.ccd.client.model.CaseDetails> caseDetailsList = ccdSearchService.searchForCases(
            newCaseIds.getUniqueIds(),
            idamService.retrieveSystemUpdateUserDetails(),
            authTokenGenerator.generate()
        );

        if (!caseDetailsList.isEmpty()) {
            final List<String> missingIds = getMissingIds(caseDetailsList, newCaseIds.getUniqueIds());
            if (!missingIds.isEmpty()) {
                errors.add(ERROR_CASES_NOT_FOUND + String.join(", ", missingIds));
            }
            caseDetailsList.forEach(caseDetails -> {
                if (!AwaitingPronouncement.toString().equals(caseDetails.getState())) {
                    errors.add(ERROR_CASE_ID + caseDetails.getId() + ERROR_INVALID_STATE
                        + caseDetails.getState() + ERROR_ONLY_AWAITING_PRONOUNCEMENT);
                }
                CaseData caseData = objectMapper.convertValue(caseDetails.getData(), CaseData.class);
                if (caseData.getBulkListCaseReferenceLink() != null) {
                    final String bulkCaseRef = caseData.getBulkListCaseReferenceLink().getCaseReference();
                    errors.add(ERROR_CASE_ID + caseDetails.getId() + ERROR_ALREADY_LINKED_TO_BULK_CASE + bulkCaseRef);
                }
            });
        } else {
            errors.add(ERROR_NO_CASES_FOUND + String.join(", ", newCaseIds.getUniqueIds()));
        }

        return errors;
    }
}
