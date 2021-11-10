package uk.gov.hmcts.divorce.divorcecase.validation;

import uk.gov.hmcts.ccd.sdk.type.CaseLink;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.MarriageDetails;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static java.time.temporal.ChronoUnit.YEARS;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

public final class ValidationUtil {

    public static final String LESS_THAN_ONE_YEAR_AGO = " can not be less than one year ago.";
    public static final String MORE_THAN_ONE_HUNDRED_YEARS_AGO = " can not be more than 100 years ago.";
    public static final String IN_THE_FUTURE = " can not be in the future.";
    public static final String EMPTY = " cannot be empty or null";
    public static final String MUST_BE_YES = " must be YES";
    public static final String CONNECTION = "Connection ";
    public static final String CANNOT_EXIST = " cannot exist";
    public static final String SOT_REQUIRED = "Statement of truth must be accepted by the person making the application";

    private ValidationUtil() {
    }

    public static List<String> validateBasicCase(CaseData caseData) {
        return flattenLists(
            notNull(caseData.getApplicant1().getFirstName(), "Applicant1FirstName"),
            notNull(caseData.getApplicant1().getLastName(), "Applicant1LastName"),
            notNull(caseData.getApplicant2().getFirstName(), "Applicant2FirstName"),
            notNull(caseData.getApplicant2().getLastName(), "Applicant2LastName"),
            notNull(caseData.getApplicant1().getFinancialOrder(), "Applicant1FinancialOrder"),
            notNull(caseData.getApplicant1().getGender(), "Applicant1Gender"),
            notNull(caseData.getApplicant2().getGender(), "Applicant2Gender"),
            notNull(caseData.getApplication().getMarriageDetails().getApplicant1Name(), "MarriageApplicant1Name"),
            notNull(caseData.getApplicant1().getKeepContactDetailsConfidential(), "Applicant1KeepContactDetailsConfidential"),
            hasStatementOfTruth(caseData.getApplication()),
            validatePrayer(caseData.getApplication().getApplicant1PrayerHasBeenGiven()),
            validateMarriageDate(caseData.getApplication().getMarriageDetails().getDate(), "MarriageDate"),
            validateJurisdictionConnections(caseData.getApplication())
        );
    }

    private static List<String> hasStatementOfTruth(Application application) {
        return application.hasStatementOfTruth() ? emptyList() : List.of(SOT_REQUIRED);
    }

    private static List<String> validatePrayer(Set<Application.ThePrayer> thePrayer) {
        final String field = "Applicant1PrayerHasBeenGiven";

        if (Objects.isNull(thePrayer)) {
            return List.of(field + EMPTY);
        } else if (thePrayer.isEmpty()) {
            return List.of(field + MUST_BE_YES);
        }
        return emptyList();
    }

    public static List<String> validateApplicant1BasicCase(CaseData caseData) {
        return flattenLists(
            notNull(caseData.getApplicant1().getFirstName(), "Applicant1FirstName"),
            notNull(caseData.getApplicant1().getLastName(), "Applicant1LastName"),
            notNull(caseData.getApplicant1().getFinancialOrder(), "Applicant1FinancialOrder"),
            notNull(caseData.getApplicant1().getGender(), "Applicant1Gender"),
            notNull(caseData.getApplicant2().getGender(), "Applicant2Gender"),
            notNull(caseData.getApplication().getMarriageDetails().getApplicant1Name(), "MarriageApplicant1Name"),
            validateMarriageDate(caseData.getApplication().getMarriageDetails().getDate(), "MarriageDate"),
            caseData.getApplication().getJurisdiction().validate()
        );
    }

    public static List<String> validateApplicant2BasicCase(CaseData caseData) {
        return flattenLists(
            notNull(caseData.getApplicant2().getFirstName(), "Applicant2FirstName"),
            notNull(caseData.getApplicant2().getLastName(), "Applicant2LastName"),
            notNull(caseData.getApplication().getApplicant2StatementOfTruth(), "Applicant2StatementOfTruth"),
            notNull(caseData.getApplication().getApplicant2PrayerHasBeenGiven(), "Applicant2PrayerHasBeenGiven"),
            notNull(caseData.getApplication().getMarriageDetails().getApplicant2Name(), "MarriageApplicant2Name")
        );
    }

    public static List<String> validateApplicant2RequestChanges(Application application) {
        return flattenLists(
            notNull(application.getApplicant2ConfirmApplicant1Information(), "Applicant2ConfirmApplicant1Information"),
            notNull(application.getApplicant2ExplainsApplicant1IncorrectInformation(), "Applicant2ExplainsApplicant1IncorrectInformation")
        );
    }

    public static List<String> notNull(Object value, String field) {
        return value == null ? List.of(field + EMPTY) : emptyList();
    }

    public static List<String> validateMarriageDate(LocalDate localDate, String field) {
        if (localDate == null) {
            return List.of(field + EMPTY);
        } else if (isLessThanOneYearAgo(localDate)) {
            return List.of(field + LESS_THAN_ONE_YEAR_AGO);
        } else if (isOverOneHundredYearsAgo(localDate)) {
            return List.of(field + MORE_THAN_ONE_HUNDRED_YEARS_AGO);
        } else if (isInTheFuture(localDate)) {
            return List.of(field + IN_THE_FUTURE);
        }
        return emptyList();
    }

    public static List<String> validateJurisdictionConnections(Application application) {
        if (application.isSolicitorApplication()) {
            if (isEmpty(application.getJurisdiction().getConnections())) {
                return List.of("JurisdictionConnections" + EMPTY);
            }
            return emptyList();
        }
        return application.getJurisdiction().validate();
    }

    public static List<String> validateCasesAcceptedToListForHearing(BulkActionCaseData caseData) {
        final List<ListValue<CaseLink>> casesAcceptedToListForHearing = caseData.getCasesAcceptedToListForHearing();
        final List<String> caseReferences = caseData.getBulkListCaseDetails().stream()
            .map(c -> c.getValue().getCaseReference().getCaseReference())
            .collect(toList());

        final boolean anyDuplicateCases = !casesAcceptedToListForHearing.stream().allMatch(new HashSet<>()::add);
        final boolean anyNewCasesAdded =
            casesAcceptedToListForHearing.stream().anyMatch(caseLink -> !caseReferences.contains(caseLink.getValue().getCaseReference()));

        return anyDuplicateCases || anyNewCasesAdded
            ? singletonList("You can only remove cases from the list of cases accepted to list for hearing.")
            : emptyList();
    }

    private static boolean isLessThanOneYearAgo(LocalDate date) {
        return !date.isAfter(LocalDate.now())
            && date.isAfter(LocalDate.now().minus(1, YEARS));
    }

    private static boolean isOverOneHundredYearsAgo(LocalDate date) {
        return date.isBefore(LocalDate.now().minus(100, YEARS));
    }

    private static boolean isInTheFuture(LocalDate date) {
        return date.isAfter(LocalDate.now());
    }

    public static List<String> validateCaseFieldsForIssueApplication(MarriageDetails marriageDetails) {
        //MarriageApplicant1Name and MarriageDate are validated in validateBasicCase
        return flattenLists(
            notNull(marriageDetails.getApplicant2Name(), "MarriageApplicant2Name"),
            notNull(marriageDetails.getPlaceOfMarriage(), "PlaceOfMarriage")
        );
    }

    @SafeVarargs
    public static <E> List<E> flattenLists(List<E>... lists) {
        return Arrays.stream(lists).flatMap(Collection::stream).collect(toList());
    }
}
