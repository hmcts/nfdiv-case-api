package uk.gov.hmcts.divorce.divorcecase.validation;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.type.CaseLink;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.data.BulkListCaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections;
import uk.gov.hmcts.divorce.divorcecase.model.MarriageDetails;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.YEARS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.JurisdictionTest.CANNOT_EXIST;
import static uk.gov.hmcts.divorce.divorcecase.model.JurisdictionTest.CONNECTION;
import static uk.gov.hmcts.divorce.divorcecase.validation.ValidationUtil.notNull;
import static uk.gov.hmcts.divorce.divorcecase.validation.ValidationUtil.validateBasicCase;
import static uk.gov.hmcts.divorce.divorcecase.validation.ValidationUtil.validateCaseFieldsForIssueApplication;
import static uk.gov.hmcts.divorce.divorcecase.validation.ValidationUtil.validateCasesAcceptedToListForHearing;
import static uk.gov.hmcts.divorce.divorcecase.validation.ValidationUtil.validateJurisdictionConnections;
import static uk.gov.hmcts.divorce.divorcecase.validation.ValidationUtil.validateMarriageDate;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

public class CaseValidationTest {

    private static final String LESS_THAN_ONE_YEAR_AGO = " can not be less than one year ago.";
    private static final String EMPTY = " cannot be empty or null";
    private static final String IN_THE_FUTURE = " can not be in the future.";
    private static final String MORE_THAN_ONE_HUNDRED_YEARS_AGO = " can not be more than 100 years ago.";

    @Test
    public void shouldValidateBasicCase() {
        CaseData caseData = new CaseData();
        List<String> errors = validateBasicCase(caseData);
        assertThat(errors).hasSize(13);
    }

    @Test
    public void shouldReturnErrorWhenStringIsNull() {
        List<String> response = notNull(null, "field");

        assertThat(response).isEqualTo(List.of("field" + EMPTY));
    }

    @Test
    public void shouldReturnErrorWhenDateIsInTheFuture() {
        List<String> response = validateMarriageDate(LocalDate.now().plus(2, YEARS), "field");

        assertThat(response).isEqualTo(List.of("field" + IN_THE_FUTURE));
    }

    @Test
    public void shouldReturnErrorWhenDateIsOverOneHundredYearsAgo() {
        LocalDate oneHundredYearsAndOneDayAgo = LocalDate.now()
            .minus(100, YEARS)
            .minus(1, DAYS);

        List<String> response = validateMarriageDate(oneHundredYearsAndOneDayAgo, "field");

        assertThat(response).isEqualTo(List.of("field" + MORE_THAN_ONE_HUNDRED_YEARS_AGO));
    }

    @Test
    public void shouldReturnErrorWhenDateIsLessThanOneYearAgo() {
        List<String> response = validateMarriageDate(LocalDate.now().minus(360, DAYS), "field");

        assertThat(response).isEqualTo(List.of("field" + LESS_THAN_ONE_YEAR_AGO));
    }

    @Test
    public void shouldReturnTrueWhenCaseHasAwaitingDocuments() {
        CaseData caseData = new CaseData();
        caseData.getApplication().setApplicant1WantsToHavePapersServedAnotherWay(YES);
        assertTrue(caseData.getApplication().hasAwaitingApplicant1Documents());
    }

    @Test
    public void shouldReturnFalseWhenCaseDoesNotHaveAwaitingDocuments() {
        CaseData caseData = new CaseData();
        assertFalse(caseData.getApplication().hasAwaitingApplicant1Documents());
    }

    @Test
    public void shouldReturnErrorWhenApp2MarriageCertNameAndPlaceOfMarriageAreMissing() {
        CaseData caseData = new CaseData();
        List<String> errors = validateCaseFieldsForIssueApplication(caseData.getApplication().getMarriageDetails());

        assertThat(errors).containsExactlyInAnyOrder(
            "MarriageApplicant2Name cannot be empty or null",
            "PlaceOfMarriage cannot be empty or null"
        );
    }

    @Test
    public void shouldReturnErrorWhenApp2MarriageCertNameIsMissing() {
        MarriageDetails marriageDetails = new MarriageDetails();
        marriageDetails.setPlaceOfMarriage("London");
        List<String> errors = validateCaseFieldsForIssueApplication(marriageDetails);

        assertThat(errors).containsExactlyInAnyOrder(
            "MarriageApplicant2Name cannot be empty or null"
        );
    }

    @Test
    public void shouldNotReturnErrorWhenBothWhenApp2MarriageCertNameAndPlaceOfMarriageArePresent() {
        MarriageDetails marriageDetails = new MarriageDetails();
        marriageDetails.setPlaceOfMarriage("London");
        marriageDetails.setApplicant2Name("TestFname TestMname  TestLname");
        List<String> errors = validateCaseFieldsForIssueApplication(marriageDetails);

        assertThat(errors).isEmpty();
    }

    @Test
    public void shouldOnlyValidateEmptyJurisdictionConnectionsForSolicitorApplication() {
        final CaseData caseData = caseData();
        caseData.setApplication(Application.builder()
            .solSignStatementOfTruth(YES)
            .build());

        caseData.getApplication().getJurisdiction().setConnections(Collections.emptySet());

        List<String> errors = validateJurisdictionConnections(caseData.getApplication());

        assertThat(errors).containsOnly("JurisdictionConnections" + ValidationUtil.EMPTY);
    }

    @Test
    public void shouldReturnEmptyListForNonEmptyJurisdictionConnectionsForSolicitorApplication() {
        final CaseData caseData = caseData();
        caseData.setApplication(Application.builder()
            .solSignStatementOfTruth(YES)
            .build());

        caseData.getApplication().getJurisdiction().setConnections(Set.of(JurisdictionConnections.APP_1_APP_2_RESIDENT));

        List<String> errors = validateJurisdictionConnections(caseData.getApplication());

        assertThat(errors).isEmpty();
    }

    @Test
    public void shouldValidateJurisdictionConnectionsForCitizenApplication() {
        final CaseData caseData = caseData();
        caseData.setApplication(Application.builder()
            .solSignStatementOfTruth(NO)
            .build());

        caseData.getApplication().getJurisdiction().setConnections(Set.of(JurisdictionConnections.APP_1_APP_2_RESIDENT));

        List<String> errors = validateJurisdictionConnections(caseData.getApplication());

        assertThat(errors).contains(CONNECTION + JurisdictionConnections.APP_1_APP_2_RESIDENT + CANNOT_EXIST);
    }

    @Test
    public void shouldValidateNoCasesAdded() {
        final BulkActionCaseData caseData = bulkActionCaseData();
        final CaseLink caseLink1 = CaseLink.builder()
            .caseReference("12345")
            .build();
        final CaseLink caseLink2 = CaseLink.builder()
            .caseReference("23456")
            .build();
        final CaseLink caseLink3 = CaseLink.builder()
            .caseReference("34567")
            .build();
        final ListValue<CaseLink> caseLinkListValue1 =
            ListValue.<CaseLink>builder()
                .value(caseLink1)
                .build();
        final ListValue<CaseLink> caseLinkListValue2 =
            ListValue.<CaseLink>builder()
                .value(caseLink2)
                .build();
        final ListValue<CaseLink> caseLinkListValue3 =
            ListValue.<CaseLink>builder()
                .value(caseLink3)
                .build();
        caseData.setCasesAcceptedToListForHearing(
            List.of(caseLinkListValue1, caseLinkListValue2, caseLinkListValue3));

        List<String> errors = validateCasesAcceptedToListForHearing(caseData);

        assertThat(errors).contains("You can only remove cases from the list of cases accepted to list for hearing.");
    }

    @Test
    public void shouldValidateNoDuplicateCases() {
        final BulkActionCaseData caseData = bulkActionCaseData();
        final CaseLink caseLink1 = CaseLink.builder()
            .caseReference("12345")
            .build();
        final CaseLink caseLink2 = CaseLink.builder()
            .caseReference("12345")
            .build();
        final ListValue<CaseLink> caseLinkListValue1 =
            ListValue.<CaseLink>builder()
                .value(caseLink1)
                .build();
        final ListValue<CaseLink> caseLinkListValue2 =
            ListValue.<CaseLink>builder()
                .value(caseLink2)
                .build();
        caseData.setCasesAcceptedToListForHearing(List.of(caseLinkListValue1, caseLinkListValue2));

        List<String> errors = validateCasesAcceptedToListForHearing(caseData);

        assertThat(errors).contains("You can only remove cases from the list of cases accepted to list for hearing.");
    }

    private BulkActionCaseData bulkActionCaseData() {
        final CaseLink caseLink1 = CaseLink.builder()
            .caseReference("12345")
            .build();
        final CaseLink caseLink2 = CaseLink.builder()
            .caseReference("98765")
            .build();

        final ListValue<BulkListCaseDetails> bulkListCaseDetailsListValue1 =
            ListValue.<BulkListCaseDetails>builder()
                .value(BulkListCaseDetails.builder()
                    .caseReference(caseLink1)
                    .build())
                .build();

        final ListValue<BulkListCaseDetails> bulkListCaseDetailsListValue2 =
            ListValue.<BulkListCaseDetails>builder()
                .value(BulkListCaseDetails.builder()
                    .caseReference(caseLink2)
                    .build())
                .build();

        return BulkActionCaseData.builder()
            .bulkListCaseDetails(List.of(bulkListCaseDetailsListValue1, bulkListCaseDetailsListValue2))
            .build();
    }
}
