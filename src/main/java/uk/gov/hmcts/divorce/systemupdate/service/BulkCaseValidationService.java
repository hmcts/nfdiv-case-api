package uk.gov.hmcts.divorce.systemupdate.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.CaseLink;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.data.BulkListCaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPronouncement;
import static uk.gov.hmcts.divorce.divorcecase.validation.ValidationUtil.flattenLists;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class BulkCaseValidationService {

    public final static String ERROR_HEARING_DATE_IN_PAST = "Please enter a hearing date and time in the future.";
    public final static String ERROR_CASE_IDS_DUPLICATED = "Please removed duplicate Case IDs from the bulk list.";
    public final static String ERROR_NO_CASES_SCHEDULED = "Please add at least one case to schedule for listing.";
    public final static String ERROR_DO_NOT_REMOVE_CASES =
        "You cannot remove cases from the bulk list with this event. Use Remove cases from bulk list instead.";
    public final static String ERROR_NOT_AWAITING_PRONOUNCEMENT =
        "Case %s is not in awaiting pronouncement state.";
    public final static String ERROR_ALREADY_LINKED_TO_BULK_CASE = "Case %s is already linked to bulk list %s.";
    public final static String BULK_LIST_ERRORED_CASES = "There are errors on the bulk list. Please resolve errors before continuing";
    public final static String ERROR_CASES_NOT_FOUND = "Some cases were not found in CCD: %s";

    private final IdamService idamService;
    private final AuthTokenGenerator authTokenGenerator;
    private final CcdSearchService ccdSearchService;
    private final ObjectMapper objectMapper;

    public List<String> validateBulkListErroredCases(CaseDetails<BulkActionCaseData, BulkActionState> bulkDetails) {

        var erroredCaseDetails = bulkDetails.getData().getErroredCaseDetails();

        return !ObjectUtils.isEmpty(erroredCaseDetails)
            ? singletonList(BULK_LIST_ERRORED_CASES)
            : emptyList();
    }

    public List<String> validateData(final BulkActionCaseData bulkData, final BulkActionCaseData beforeBulkData, final Long bulkCaseId) {
        final List<String> beforeCaseReferences = beforeBulkData.getCaseReferences();
        final List<String> afterCaseReferences = bulkData.getCaseReferences();

        final List<String> errors = flattenLists(
            validateHearingDate(bulkData),
            validateCasesAreScheduled(bulkData, beforeBulkData),
            validateCasesNotRemoved(afterCaseReferences, beforeCaseReferences),
            validateDuplicates(afterCaseReferences)
        );
        return errors.isEmpty() ? validateNewlyAddedCases(afterCaseReferences, beforeCaseReferences, bulkCaseId) : errors;
    }

    private List<String> validateHearingDate(final BulkActionCaseData bulkData) {
        return bulkData.getDateAndTimeOfHearing().isBefore(LocalDateTime.now()) ?
            List.of(ERROR_HEARING_DATE_IN_PAST) :
            Collections.emptyList();
    }

    private List<String> validateCasesAreScheduled(final BulkActionCaseData bulkData, final BulkActionCaseData beforeBulkData) {
        return getCaseDetailsSet(bulkData).isEmpty() && getCaseDetailsSet(beforeBulkData).isEmpty()
            ? List.of(ERROR_NO_CASES_SCHEDULED)
            : Collections.emptyList();
    }

    private List<String> validateCasesNotRemoved(final List<String> afterCaseRefs, final List<String> beforeCaseRefs) {
        Set<String> removedCases = new HashSet<>(beforeCaseRefs);
        removedCases.removeAll(new HashSet<>(afterCaseRefs));

        return !removedCases.isEmpty()
            ? List.of(ERROR_DO_NOT_REMOVE_CASES)
            : Collections.emptyList();
    }

    private List<String> validateDuplicates(List<String> caseReferences) {
        Set<String> uniqueCaseReferences = new HashSet<>(caseReferences);
        boolean hasDuplicates = uniqueCaseReferences.size() < caseReferences.size();

        return hasDuplicates ?
            List.of(ERROR_CASE_IDS_DUPLICATED) :
            Collections.emptyList();
    }

    private List<String> validateLinkToBulkCase(CaseDetails<CaseData, State> caseDetails, Long bulkCaseId) {
        List<String> errors = new ArrayList<>();

        if (!AwaitingPronouncement.equals(caseDetails.getState())) {
            errors.add(String.format(ERROR_NOT_AWAITING_PRONOUNCEMENT, caseDetails.getId()));
        }

        CaseLink bulkCaseLink = caseDetails.getData().getBulkListCaseReferenceLink();

        if (bulkCaseLink != null && !bulkCaseLink.getCaseReference().equals(bulkCaseId.toString())) {
            errors.add(String.format(ERROR_ALREADY_LINKED_TO_BULK_CASE, caseDetails.getId(), bulkCaseLink.getCaseReference()));
        }

        return errors;
    }

    private List<String> validateNewlyAddedCases(
        final List<String> afterCaseReferences,
        final List<String> beforeCaseReferences,
        final Long bulkCaseId
    ) {
        Set<String> searchCaseReferences = new HashSet<>(afterCaseReferences);
        searchCaseReferences.removeAll(new HashSet<>(beforeCaseReferences));

        if (searchCaseReferences.isEmpty()) {
            return Collections.emptyList();
        }

        final List<uk.gov.hmcts.reform.ccd.client.model.CaseDetails> caseSearchResults = ccdSearchService.searchForCases(
            searchCaseReferences.stream().toList(),
            idamService.retrieveSystemUpdateUserDetails(),
            authTokenGenerator.generate()
        );

        List<String> errors = new ArrayList<>();

        caseSearchResults.stream().map(caseDetails -> objectMapper.convertValue(
            caseDetails, new TypeReference<CaseDetails<CaseData, State>>() {}
        )).forEach(caseDetails -> {
            errors.addAll(validateLinkToBulkCase(caseDetails, bulkCaseId));
            searchCaseReferences.remove(caseDetails.getId().toString());
        });

        if (!searchCaseReferences.isEmpty()) {
            errors.add(String.format(ERROR_CASES_NOT_FOUND, String.join(", ", searchCaseReferences)));
        }

        return errors;
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
}
