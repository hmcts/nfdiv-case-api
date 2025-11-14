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
    public static final String ERROR_HEARING_DATE_IN_PAST = "Please enter a hearing date and time in the future.";
    public static final String ERROR_CASE_IDS_DUPLICATED = "Case IDs duplicated in the list: ";
    public static final String ERROR_NO_CASES_SCHEDULED = "Please add at least one case to schedule for listing.";
    public static final String ERROR_DO_NOT_REMOVE_CASES =
        "You cannot remove cases from the bulk list with this event. Use Remove cases from bulk list instead.";
    public static final String ERROR_CASES_NOT_FOUND = "Search returned no results for the following Case IDs: ";
    public static final String ERROR_ONLY_AWAITING_PRONOUNCEMENT = "Only cases in Awaiting Pronouncement can be scheduled for listing. Check Case IDs: ";
    public static final String ERROR_ALREADY_LINKED_TO_BULK_CASE = " already linked to bulk list: ";
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

        final List<String> errors = validateData(bulkCaseDetails.getData(), beforeDetails.getData(), bulkCaseDetails.getId());
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
        boolean hasDuplicates() {
            return !duplicateIds.isEmpty();
        }

        boolean hasUniqueIds() {
            return !uniqueIds.isEmpty();
        }
    }

    private DuplicateCheckResult checkForDuplicates(final BulkActionCaseData bulkActionCaseData) {
        final List<String> caseReferences = bulkActionCaseData.getBulkListCaseDetails() == null
            ? new ArrayList<>()
            : bulkActionCaseData.getBulkListCaseDetails()
                .stream()
                .map(bulkListCaseDetails -> bulkListCaseDetails.getValue().getCaseReference().getCaseReference())
                .toList();

        final Set<String> duplicateCaseIds = new java.util.HashSet<>();
        final Set<String> uniqueIds = new java.util.HashSet<>();
        final Map<String, Integer> frequencyMap = new java.util.HashMap<>();

        for (String caseRef: caseReferences) {
            frequencyMap.put(caseRef, frequencyMap.getOrDefault(caseRef, 0) + 1);
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

    private Set<String> getMissingIds(
        final List<uk.gov.hmcts.reform.ccd.client.model.CaseDetails> searchResultsList,
        final Set<String> searchedIds
    ) {
        if (searchResultsList.size() < searchedIds.size()) {
            final Set<String> foundIds =
                searchResultsList.stream().map(caseDetails -> caseDetails.getId().toString()).collect(Collectors.toSet());
            return searchedIds.stream().filter(id -> !foundIds.contains(id)).collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

    private Boolean haveCasesBeenRemoved(
        final Set<BulkListCaseDetails> caseDetailsSet,
        final Set<BulkListCaseDetails> beforeCaseDetailsSet
    ) {

        final Set<String> caseDetailsIdSet = caseDetailsSet.stream()
            .map(bulkListCaseDetails -> bulkListCaseDetails.getCaseReference().getCaseReference())
            .collect(Collectors.toSet());
        final Set<String> beforeCaseDetailsIdSet = beforeCaseDetailsSet.stream()
            .map(bulkListCaseDetails -> bulkListCaseDetails.getCaseReference().getCaseReference())
            .collect(Collectors.toSet());

        if (beforeCaseDetailsIdSet.isEmpty()) {
            return false;
        }

        return !beforeCaseDetailsIdSet.stream()
            .filter(beforeRef -> !caseDetailsIdSet.contains(beforeRef))
            .collect(Collectors.toSet())
            .isEmpty();
    }

    private Set<BulkListCaseDetails> getCaseDetailsSet(
        final BulkActionCaseData bulkActionCaseData
    ) {
        return bulkActionCaseData.getBulkListCaseDetails() == null
            ? Collections.emptySet()
            : bulkActionCaseData.getBulkListCaseDetails()
                .stream()
                .map(ListValue::getValue)
                .collect(Collectors.toSet());
    }

    private List<String> validateData(final BulkActionCaseData bulkData, final BulkActionCaseData beforeBulkData, final Long bulkCaseId) {
        final List<String> errors = new ArrayList<>();
        final Set<BulkListCaseDetails> caseDetailsSet = getCaseDetailsSet(bulkData);
        final Set<BulkListCaseDetails> beforeCaseDetailsSet = getCaseDetailsSet(beforeBulkData);

        if (bulkData.getDateAndTimeOfHearing().isBefore(LocalDateTime.now())) {
            errors.add(ERROR_HEARING_DATE_IN_PAST);
        }

        if (caseDetailsSet.isEmpty() && beforeCaseDetailsSet.isEmpty()) {
            errors.add(ERROR_NO_CASES_SCHEDULED);
            return errors;
        }

        if (haveCasesBeenRemoved(caseDetailsSet, beforeCaseDetailsSet)) {
            errors.add(ERROR_DO_NOT_REMOVE_CASES);
        }

        final DuplicateCheckResult duplicateCheckResult = checkForDuplicates(bulkData);
        if (duplicateCheckResult.hasDuplicates()) {
            errors.add(ERROR_CASE_IDS_DUPLICATED + String.join(", ", duplicateCheckResult.duplicateIds()));
        }

        if (duplicateCheckResult.hasUniqueIds()) {
            final List<uk.gov.hmcts.reform.ccd.client.model.CaseDetails> caseDetailsSearchResults = ccdSearchService.searchForCases(
                duplicateCheckResult.uniqueIds.stream().toList(),
                idamService.retrieveSystemUpdateUserDetails(),
                authTokenGenerator.generate()
            );

            if (!caseDetailsSearchResults.isEmpty()) {
                final Set<String> missingIds = getMissingIds(caseDetailsSearchResults, duplicateCheckResult.uniqueIds());
                if (!missingIds.isEmpty()) {
                    errors.add(ERROR_CASES_NOT_FOUND + String.join(", ", missingIds));
                }
                final List<String> wrongState = new ArrayList<>();
                caseDetailsSearchResults.forEach(caseDetails -> {
                    if (!AwaitingPronouncement.toString().equals(caseDetails.getState())) {
                        wrongState.add(caseDetails.getId().toString());
                    }
                    CaseData caseData = objectMapper.convertValue(caseDetails.getData(), CaseData.class);
                    if (caseData.getBulkListCaseReferenceLink() != null
                        && !caseData.getBulkListCaseReferenceLink().getCaseReference().equals(bulkCaseId.toString())
                    ) {
                        final String bulkCaseRef = caseData.getBulkListCaseReferenceLink().getCaseReference();
                        errors.add(caseDetails.getId() + ERROR_ALREADY_LINKED_TO_BULK_CASE + bulkCaseRef);
                    }
                });
                if (!wrongState.isEmpty()) {
                    errors.add(ERROR_ONLY_AWAITING_PRONOUNCEMENT + String.join(", ", wrongState));
                }
            } else {
                errors.add(ERROR_CASES_NOT_FOUND + String.join(", ", duplicateCheckResult.uniqueIds()));
            }

        }

        return errors;
    }
}
