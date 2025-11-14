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
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.util.ObjectUtils.isEmpty;
import static uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState.Created;
import static uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState.Listed;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPronouncement;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.validation.ValidationUtil.validateBulkListErroredCases;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemLinkWithBulkCase.SYSTEM_LINK_WITH_BULK_CASE;

@Component
@Slf4j
@RequiredArgsConstructor
public class CaseworkerScheduleCase implements CCDConfig<BulkActionCaseData, BulkActionState, UserRole> {
    public static final String CASEWORKER_SCHEDULE_CASE = "caseworker-schedule-case";
    public static final String ERROR_HEARING_DATE_IN_PAST = "Please enter a hearing date and time in the future";
    public static final String ERROR_CASE_IDS_DUPLICATED = "Case IDs duplicated in the list: ";
    public static final String ERROR_NO_NEW_CASES_ADDED_OR_HEARING_DETAILS_UPDATED =
        "Please add at least one new case to schedule for listing or update the hearing details";
    public static final String ERROR_NO_CASES_SCHEDULED = "Please add at least one case to schedule for listing";
    public static final String ERROR_DO_NOT_REMOVE_CASES =
        "You cannot remove cases from the bulk list with this event. Use Remove cases from bulk list instead.";
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
            .forStateTransition(EnumSet.of(Created, Listed), Listed)
            .name(SCHEDULE_CASES_FOR_LISTING)
            .description(SCHEDULE_CASES_FOR_LISTING)
            .showSummary()
            .showEventNotes()
            .aboutToStartCallback(this::aboutToStart)
            .submittedCallback(this::submitted)
            .explicitGrants()
            .grant(CREATE_READ_UPDATE, CASE_WORKER, SYSTEMUPDATE))
            .page("scheduleForListing", this::midEvent)
            .pageLabel(SCHEDULE_CASES_FOR_LISTING)
            .mandatory(BulkActionCaseData::getCourt)
            .mandatory(BulkActionCaseData::getDateAndTimeOfHearing)
            .mandatoryNoSummary(BulkActionCaseData::getBulkListCaseDetails);
    }

    public AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> aboutToStart(final CaseDetails<BulkActionCaseData,
        BulkActionState> bulkCaseDetails) {

        log.info("{} aboutToStart-callback invoked for case id: {}", CASEWORKER_SCHEDULE_CASE, bulkCaseDetails.getId());

        List<String> validationErrors = validateBulkListErroredCases(bulkCaseDetails);
        if (!isEmpty(validationErrors)) {
            return AboutToStartOrSubmitResponse
                .<BulkActionCaseData, BulkActionState>builder()
                .errors(validationErrors)
                .data(bulkCaseDetails.getData())
                .build();
        }

        return AboutToStartOrSubmitResponse.<BulkActionCaseData, BulkActionState>builder()
            .data(bulkCaseDetails.getData())
            .build();
    }

    public AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> midEvent(
        CaseDetails<BulkActionCaseData, BulkActionState> bulkCaseDetails,
        CaseDetails<BulkActionCaseData, BulkActionState> beforeDetails
    ) {
        log.info("{} mid event callback invoked for Case Id: {}", CASEWORKER_SCHEDULE_CASE, bulkCaseDetails.getId());

        final List<String> errors = validateData(bulkCaseDetails, beforeDetails);
        if (!errors.isEmpty()) {
            return AboutToStartOrSubmitResponse
                .<BulkActionCaseData, BulkActionState>builder()
                .errors(errors)
                .data(bulkCaseDetails.getData())
                .build();
        }

        return AboutToStartOrSubmitResponse.<BulkActionCaseData, BulkActionState>builder().build();
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

    private record DuplicateCheckResult(Set<String> duplicateIds, Set<String> uniqueIds) {
        public boolean hasDuplicates() {
            return !duplicateIds.isEmpty();
        }

        public boolean hasUniqueIds() {
            return !uniqueIds.isEmpty();
        }
    }

    private DuplicateCheckResult checkForDuplicates(final List<BulkListCaseDetails> caseDetailsList) {
        final Set<String> duplicateCaseIds = new java.util.HashSet<>();
        final List<String> uniqueIds = new ArrayList<>();
        final Map<BulkListCaseDetails, Integer> frequencyMap = new java.util.HashMap<>();
        final Map<String, Integer> idFrequencyMap = new java.util.HashMap<>();

        //Create frequency map of BulkListCaseDetails entries
        for (BulkListCaseDetails caseDetails : caseDetailsList) {
            frequencyMap.put(caseDetails, frequencyMap.getOrDefault(caseDetails, 0) + 1);
        }

        //Create collections of duplicate and unique case IDs based on frequency map
        for (Map.Entry<BulkListCaseDetails, Integer> entry : frequencyMap.entrySet()) {
            if (entry.getValue() > 1) {
                duplicateCaseIds.add(entry.getKey().getCaseReference().getCaseReference());
            } else if (entry.getValue() == 1) {
                //Unique IDs may still contain duplicates at this point - it's possible the same case ID is present in the list multiple times
                //with different BulkListCaseDetails data (e.g. different parties or reasons for listing)
                uniqueIds.add(entry.getKey().getCaseReference().getCaseReference());
            }
        }

        //Create frequency map of unique IDs to identify any remaining duplicates
        for (String caseRef: uniqueIds) {
            idFrequencyMap.put(caseRef, idFrequencyMap.getOrDefault(caseRef, 0) + 1);
        }

        //Add any remaining duplicates to duplicate IDs collection
        for (Map.Entry<String, Integer> entry : idFrequencyMap.entrySet()) {
            if (entry.getValue() > 1) {
                duplicateCaseIds.add(entry.getKey());
            }
        }
        //Remove all duplicates from unique IDs collection
        uniqueIds.removeAll(duplicateCaseIds);

        return new DuplicateCheckResult(duplicateCaseIds, new java.util.HashSet<>(uniqueIds));
    }

    private Set<String> getMissingIds(
        final Set<uk.gov.hmcts.reform.ccd.client.model.CaseDetails> searchResultsList,
        final Set<String> searchedIds
    ) {
        if (searchResultsList.size() < searchedIds.size()) {
            final Set<String> foundIds = searchResultsList.stream().map(caseDetails -> caseDetails.getId().toString()).collect(Collectors.toSet());
            return searchedIds.stream().filter(id -> !foundIds.contains(id)).collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

//    private DuplicateCheckResult getNewCaseIds(final List<BulkListCaseDetails> bulkListCaseDetails,
//                                       final Set<BulkListCaseDetails> beforeBulkListCaseDetails
//    ) {
//        final DuplicateCheckResult duplicateCheckResult = checkForDuplicates(bulkListCaseDetails);
//        if (beforeBulkListCaseDetails.isEmpty()) {
//            return duplicateCheckResult;
//        }
//
//        final DuplicateCheckResult filteredDuplicateCheckResult = checkForDuplicates(bulkListCaseDetails.stream()
//            .filter(caseDetails -> !beforeBulkListCaseDetails.contains(caseDetails))
//            .toList());
//
//        if (filteredDuplicateCheckResult.hasUniqueIds()) {
//            final Set<String> duplicateIdsToRemove = new java.util.HashSet<>();
//            for (String id : filteredDuplicateCheckResult.uniqueIds()) {
//                if (duplicateCheckResult.duplicateIds().contains(id)) {
//                    duplicateIdsToRemove.add(id);
//                }
//            }
//            filteredDuplicateCheckResult.uniqueIds().removeAll(duplicateIdsToRemove);
//        }
//
//        return filteredDuplicateCheckResult;
//    }

    private List<String> checkForRemovedCases(
        final Set<String> caseDetailsIdSet,
        final Set<String> beforeCaseDetailsIdSet
    ) {
        if (beforeCaseDetailsIdSet.isEmpty()) {
            return Collections.emptyList();
        }

        return beforeCaseDetailsIdSet.stream()
            .filter(beforeRef -> !caseDetailsIdSet.contains(beforeRef))
            .toList();
    }

    private List<String> validateData(
        final CaseDetails<BulkActionCaseData, BulkActionState> bulkActionCaseDetails,
        final CaseDetails<BulkActionCaseData, BulkActionState> beforeBulkActionCaseDetails
    ) {
        final List<String> errors = new ArrayList<>();
        final BulkActionCaseData bulkActionCaseData = bulkActionCaseDetails.getData();
        final BulkActionCaseData beforeBulkActionCaseData = beforeBulkActionCaseDetails.getData();
        final String bulkCaseId = bulkActionCaseDetails.getId().toString();

        // Needs to be a list to preserve any duplicate entries for validation
        final List<BulkListCaseDetails> caseDetailsList = bulkActionCaseData.getBulkListCaseDetails() != null
            ? bulkActionCaseData.getBulkListCaseDetails()
                .stream()
                .map(ListValue::getValue)
                .toList()
            : new ArrayList<>();

        // Needs to be a list to preserve any duplicate entries for validation
        final List<String> caseDetailsIdList = caseDetailsList.stream()
            .map(bulkListCaseDetails -> bulkListCaseDetails.getCaseReference().getCaseReference())
            .toList();

        // Create sets for more efficient lookup
        final Set<String> caseDetailsIdSet = new java.util.HashSet<>(caseDetailsIdList);
        final Set<BulkListCaseDetails> beforeCaseDetailsSet = beforeBulkActionCaseData.getBulkListCaseDetails() != null
            ? beforeBulkActionCaseData.getBulkListCaseDetails()
                .stream()
                .map(ListValue::getValue)
                .collect(Collectors.toSet())
            : Collections.emptySet();
        final Set<String> beforeCaseDetailsIdSet = beforeCaseDetailsSet.stream()
            .map(bulkListCaseDetails -> bulkListCaseDetails.getCaseReference().getCaseReference())
            .collect(Collectors.toSet());

        if (bulkActionCaseData.getDateAndTimeOfHearing().isBefore(LocalDateTime.now())) {
            errors.add(ERROR_HEARING_DATE_IN_PAST);
        }

        if (caseDetailsList.isEmpty() && beforeCaseDetailsSet.isEmpty()) {
            errors.add(ERROR_NO_CASES_SCHEDULED);
            return errors;
        }

        final List<String> removedCases = checkForRemovedCases(caseDetailsIdSet, beforeCaseDetailsIdSet);
        if (!removedCases.isEmpty()) {
            errors.add(ERROR_DO_NOT_REMOVE_CASES);
        }

        final DuplicateCheckResult duplicateCheckResult = checkForDuplicates(caseDetailsList);
        if (duplicateCheckResult.hasDuplicates()) {
            errors.add(ERROR_CASE_IDS_DUPLICATED + String.join(", ", duplicateCheckResult.duplicateIds()));
        }

//        final DuplicateCheckResult newCaseIds = getNewCaseIds(caseDetailsList, beforeCaseDetailsSet);
//
//        if (!newCaseIds.hasUniqueIds()) {
//            if (duplicateCheckResult.hasDuplicates()) {
//                return errors;
//            } else if (
//                bulkActionCaseData.getDateAndTimeOfHearing().equals(beforeBulkActionCaseData.getDateAndTimeOfHearing())
//                    && bulkActionCaseData.getCourt().equals(beforeBulkActionCaseData.getCourt())
//            ) {
//                errors.add(ERROR_NO_NEW_CASES_ADDED_OR_HEARING_DETAILS_UPDATED);
//                return errors;
//            }
//            return errors;
//        }

        if (duplicateCheckResult.hasUniqueIds()) {
            final Set<uk.gov.hmcts.reform.ccd.client.model.CaseDetails> caseDetailsSearchResults = new HashSet<>(ccdSearchService.searchForCases(
//            newCaseIds.uniqueIds().stream().toList(),
                duplicateCheckResult.uniqueIds.stream().toList(),
                idamService.retrieveSystemUpdateUserDetails(),
                authTokenGenerator.generate()
            ));

            if (!caseDetailsSearchResults.isEmpty()) {
//                final Set<String> missingIds = getMissingIds(caseDetailsSearchResults, newCaseIds.uniqueIds());
                final Set<String> missingIds = getMissingIds(caseDetailsSearchResults, duplicateCheckResult.uniqueIds());
                if (!missingIds.isEmpty()) {
                    errors.add(ERROR_CASES_NOT_FOUND + String.join(", ", missingIds));
                }
                caseDetailsSearchResults.forEach(caseDetails -> {
                    if (!AwaitingPronouncement.toString().equals(caseDetails.getState())) {
                        errors.add(ERROR_CASE_ID + caseDetails.getId() + ERROR_INVALID_STATE
                            + caseDetails.getState() + ERROR_ONLY_AWAITING_PRONOUNCEMENT);
                    }
                    CaseData caseData = objectMapper.convertValue(caseDetails.getData(), CaseData.class);
                    if (caseData.getBulkListCaseReferenceLink() != null && !caseData.getBulkListCaseReferenceLink().getCaseReference().equals(bulkCaseId)) {
                        final String bulkCaseRef = caseData.getBulkListCaseReferenceLink().getCaseReference();
                        errors.add(ERROR_CASE_ID + caseDetails.getId() + ERROR_ALREADY_LINKED_TO_BULK_CASE + bulkCaseRef);
                    }
                });
            } else {
//                errors.add(ERROR_NO_CASES_FOUND + String.join(", ", newCaseIds.uniqueIds()));
                errors.add(ERROR_NO_CASES_FOUND + String.join(", ", duplicateCheckResult.uniqueIds()));
            }

        }

        return errors;
    }
}
