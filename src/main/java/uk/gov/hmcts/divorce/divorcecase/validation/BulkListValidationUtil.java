package uk.gov.hmcts.divorce.divorcecase.validation;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.CaseLink;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.MarriageDetails;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.solicitor.client.pba.PbaService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;
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

    public static List<String> validateLinkedCaseDetails(CaseDetails details, Long bulkCaseId) {
        List<String> errors = new ArrayList<>();
        long caseId = details.getId();

        if (!AwaitingPronouncement.toString().equals(details.getState())) {
            errors.add(String.format("Case %s is not in the correct state for bulk list processing.", caseId));
        }

        CaseLink bulkCaseLink = details.getData().get;

        if (bulkCaseLink != null && !bulkCaseLink.getCaseReference().equals(bulkCaseId.toString())) {
            errors.add(String.format("Case %s is already linked to bulk list %s.", caseId, bulkCaseLink.getCaseReference()));
        }

        return errors;
    }

}
