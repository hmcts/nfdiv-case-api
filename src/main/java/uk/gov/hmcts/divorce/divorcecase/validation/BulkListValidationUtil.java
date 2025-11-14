package uk.gov.hmcts.divorce.divorcecase.validation;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.CaseLink;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPronouncement;

@Slf4j
public final class BulkListValidationUtil {
    public static List<String> validateHearingDate(final BulkActionCaseData bulkData) {
        return bulkData.getDateAndTimeOfHearing().isBefore(LocalDateTime.now()) ?
            List.of("Please do not use a hearing date in the past") :
            Collections.emptyList();
    }

    public static List<String> validateCasesNotRemoved(final List<String> afterCaseRefs, final List<String> beforeCaseRefs) {
        Set<String> removedCases = new HashSet<>(beforeCaseRefs);
        removedCases.removeAll(afterCaseRefs);

        return !removedCases.isEmpty()
            ? List.of("Please do not remove cases from the list.")
            : Collections.emptyList();
    }

    public static List<String> validateDuplicates(List<String> caseReferences) {
        Set<String> uniqueCaseReferences = new HashSet<>(caseReferences);
        boolean hasDuplicates = uniqueCaseReferences.size() < caseReferences.size();

        return hasDuplicates ?
            List.of("Please remove duplicate case references from the bulk list.") :
            Collections.emptyList();
    }

    public static List<String> validateLinkedCaseDetails(CaseDetails<CaseData, State> details, Long bulkCaseId) {
        List<String> errors = new ArrayList<>();

        if (!AwaitingPronouncement.equals(details.getState())) {
            errors.add(String.format("Case %s is not in awaiting pronouncement state.", details.getId()));
        }

        CaseLink bulkCaseLink = details.getData().getBulkListCaseReferenceLink();

        if (bulkCaseLink != null && !bulkCaseLink.getCaseReference().equals(bulkCaseId.toString())) {
            errors.add(String.format("Case %s is already linked to bulk list %s.", details.getId(), bulkCaseLink.getCaseReference()));
        }

        return errors;
    }

}
