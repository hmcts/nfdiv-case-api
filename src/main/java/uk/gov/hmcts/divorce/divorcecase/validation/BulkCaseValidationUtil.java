package uk.gov.hmcts.divorce.divorcecase.validation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.CaseLink;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.data.BulkListCaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPronouncement;

@Slf4j
public final class BulkCaseValidationUtil {

    public static final String ERROR_HEARING_DATE_IN_PAST = "Please enter a hearing date and time in the future.";
    public static final String ERROR_CASE_IDS_DUPLICATED = "Please removed duplicate Case IDs from the bulk list.";
    public static final String ERROR_NO_CASES_SCHEDULED = "Please add at least one case to schedule for listing.";
    public static final String ERROR_DO_NOT_REMOVE_CASES =
        "You cannot remove cases from the bulk list with this event. Use Remove cases from bulk list instead.";
    public static final String ERROR_NOT_AWAITING_PRONOUNCEMENT =
        "Case %s is not in awaiting pronouncement state.";
    public static final String ERROR_ALREADY_LINKED_TO_BULK_CASE = "Case %s is already linked to bulk list %s.";
    public static final String BULK_LIST_ERRORED_CASES = "There are errors on the bulk list. Please resolve errors before continuing";

    public static List<String> validateBulkListErroredCases(CaseDetails<BulkActionCaseData, BulkActionState> bulkCaseDetails) {

        var erroredCaseDetails = bulkCaseDetails.getData().getErroredCaseDetails();

        return !ObjectUtils.isEmpty(erroredCaseDetails)
            ? singletonList(BULK_LIST_ERRORED_CASES)
            : emptyList();
    }

    public static List<String> validateHearingDate(final BulkActionCaseData bulkData) {
        return bulkData.getDateAndTimeOfHearing().isBefore(LocalDateTime.now()) ?
            List.of(ERROR_HEARING_DATE_IN_PAST) :
            Collections.emptyList();
    }

    public static List<String> validateCasesAreScheduled(final BulkActionCaseData bulkData, final BulkActionCaseData beforeBulkData) {
        return getCaseDetailsSet(bulkData).isEmpty() && getCaseDetailsSet(beforeBulkData).isEmpty()
            ? List.of(ERROR_NO_CASES_SCHEDULED)
            : Collections.emptyList();
    }

    public static List<String> validateCasesNotRemoved(final List<String> afterCaseRefs, final List<String> beforeCaseRefs) {
        Set<String> removedCases = new HashSet<>(beforeCaseRefs);
        removedCases.removeAll(new HashSet<>(afterCaseRefs));

        return !removedCases.isEmpty()
            ? List.of(ERROR_DO_NOT_REMOVE_CASES)
            : Collections.emptyList();
    }

    public static List<String> validateDuplicates(List<String> caseReferences) {
        Set<String> uniqueCaseReferences = new HashSet<>(caseReferences);
        boolean hasDuplicates = uniqueCaseReferences.size() < caseReferences.size();

        return hasDuplicates ?
            List.of(ERROR_CASE_IDS_DUPLICATED) :
            Collections.emptyList();
    }

    public static List<String> validateLinkToBulkCase(CaseDetails<CaseData, State> details, Long bulkCaseId) {
        List<String> errors = new ArrayList<>();

        if (!AwaitingPronouncement.equals(details.getState())) {
            errors.add(String.format(ERROR_NOT_AWAITING_PRONOUNCEMENT, details.getId()));
        }

        CaseLink bulkCaseLink = details.getData().getBulkListCaseReferenceLink();

        if (bulkCaseLink != null && !bulkCaseLink.getCaseReference().equals(bulkCaseId.toString())) {
            errors.add(String.format(ERROR_ALREADY_LINKED_TO_BULK_CASE, details.getId(), bulkCaseLink.getCaseReference()));
        }

        return errors;
    }

    private static Set<BulkListCaseDetails> getCaseDetailsSet(
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
