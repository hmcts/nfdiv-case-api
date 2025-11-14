package uk.gov.hmcts.divorce.divorcecase.validation;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.CaseLink;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.data.BulkListCaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState.Listed;
import static uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderCourt.BIRMINGHAM;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.MALE;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPronouncement;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.divorcecase.validation.BulkCaseValidationUtil.ERROR_ALREADY_LINKED_TO_BULK_CASE;
import static uk.gov.hmcts.divorce.divorcecase.validation.BulkCaseValidationUtil.ERROR_CASE_IDS_DUPLICATED;
import static uk.gov.hmcts.divorce.divorcecase.validation.BulkCaseValidationUtil.ERROR_DO_NOT_REMOVE_CASES;
import static uk.gov.hmcts.divorce.divorcecase.validation.BulkCaseValidationUtil.ERROR_HEARING_DATE_IN_PAST;
import static uk.gov.hmcts.divorce.divorcecase.validation.BulkCaseValidationUtil.ERROR_NOT_AWAITING_PRONOUNCEMENT;
import static uk.gov.hmcts.divorce.divorcecase.validation.BulkCaseValidationUtil.ERROR_NO_CASES_SCHEDULED;
import static uk.gov.hmcts.divorce.divorcecase.validation.BulkCaseValidationUtil.validateBulkListErroredCases;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APP2_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APP2_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getApplicant2WithAddress;

class BulkCaseValidationUtilTest {

    private static final Long BULK_CASE_REFERENCE = 1234123412341234L;
    private static final Long BULK_CASE_REFERENCE_2 = 2345234523452345L;
    private static final Long TEST_CASE_ID_2 = 2L;
    private static final Long TEST_CASE_ID_3 = 3L;
    private static final LocalDateTime NOW = LocalDateTime.now();

    @Test
    void shouldValidateBulkListForErroredCases() {
        final CaseDetails<BulkActionCaseData, BulkActionState> details = new CaseDetails<>();
        details.setData(BulkActionCaseData.builder().erroredCaseDetails(
            List.of(ListValue.<BulkListCaseDetails>builder().build())).build());
        details.setId(TEST_CASE_ID);

        List<String> response = validateBulkListErroredCases(details);

        assertThat(response.size()).isEqualTo(1);
    }

    @Test
    void shouldValidateAndReturnNoErrorsWhenBulkListHasNoErroredCases() {
        final CaseDetails<BulkActionCaseData, BulkActionState> bulkCaseDetails = new CaseDetails<>();
        bulkCaseDetails.setData(BulkActionCaseData.builder()
            .erroredCaseDetails(null)
            .build());

        List<String> errors = BulkCaseValidationUtil.validateBulkListErroredCases(bulkCaseDetails);

        assertThat(errors).isEmpty();
    }

    @Test
    void shouldPopulateErrorMessageWhenHearingDateIsInThePast() {
        final CaseDetails<BulkActionCaseData, BulkActionState> beforeDetails = getBulkCaseDetails(NOW.minusDays(5));
        List<String> errors = BulkCaseValidationUtil.validateHearingDate(beforeDetails.getData());

        assertThat(errors).containsExactly(ERROR_HEARING_DATE_IN_PAST);
    }

    @Test
    void shouldNotPopulateErrorMessageWhenHearingDateIsInTheFuture() {
        final CaseDetails<BulkActionCaseData, BulkActionState> beforeDetails = getBulkCaseDetails(NOW.plusDays(5));
        List<String> errors = BulkCaseValidationUtil.validateHearingDate(beforeDetails.getData());

        assertThat(errors).isEmpty();
    }

    @Test
    void shouldPopulateErrorMessageWhenCaseListsAreEmpty() {
        final CaseDetails<BulkActionCaseData, BulkActionState> details = getBulkCaseDetails(NOW.minusDays(5));
        List<String> errors = BulkCaseValidationUtil.validateCasesAreScheduled(details.getData(), details.getData());

        assertThat(errors).containsExactly(ERROR_NO_CASES_SCHEDULED);
    }

    @Test
    void shouldNotPopulateErrorMessageWhenCaseListsAreNotEmpty() {
        final CaseDetails<BulkActionCaseData, BulkActionState> details = getBulkCaseDetails(
            NOW.minusDays(5),
            List.of(bulkListCaseDetailsListValue(TEST_CASE_ID)));
        List<String> errors = BulkCaseValidationUtil.validateCasesAreScheduled(details.getData(), details.getData());

        assertThat(errors).isEmpty();
    }

    @Test
    void shouldPopulateErrorMessageWhenCaseRemovedFromList() {
        final CaseDetails<BulkActionCaseData, BulkActionState> beforeDetails = getBulkCaseDetails(
            NOW.minusDays(5),
            List.of(bulkListCaseDetailsListValue(TEST_CASE_ID))
        );
        final CaseDetails<BulkActionCaseData, BulkActionState> details = getBulkCaseDetails(NOW.minusDays(5));
        List<String> errors = BulkCaseValidationUtil.validateCasesNotRemoved(
            details.getData().getCaseReferences(),
            beforeDetails.getData().getCaseReferences()
        );

        assertThat(errors).containsExactly(ERROR_DO_NOT_REMOVE_CASES);
    }

    @Test
    void shouldNotPopulateErrorMessageWhenCaseNotRemovedFromList() {
        final CaseDetails<BulkActionCaseData, BulkActionState> details = getBulkCaseDetails(
            NOW.minusDays(5),
            List.of(bulkListCaseDetailsListValue(TEST_CASE_ID))
        );
        List<String> errors = BulkCaseValidationUtil.validateCasesNotRemoved(
            details.getData().getCaseReferences(),
            details.getData().getCaseReferences()
        );

        assertThat(errors).isEmpty();
    }

    @Test
    void shouldPopulateErrorMessageWhenCaseDuplicatedInList() {
        final CaseDetails<BulkActionCaseData, BulkActionState> details = getBulkCaseDetails(
            NOW.minusDays(5),
            List.of(bulkListCaseDetailsListValue(TEST_CASE_ID), bulkListCaseDetailsListValue(TEST_CASE_ID))
        );
        List<String> errors = BulkCaseValidationUtil.validateDuplicates(details.getData().getCaseReferences());

        assertThat(errors).containsExactly(ERROR_CASE_IDS_DUPLICATED);
    }

    @Test
    void shouldNotPopulateErrorMessageWhenCaseNotDuplicatedInList() {
        final CaseDetails<BulkActionCaseData, BulkActionState> details = getBulkCaseDetails(
            NOW.minusDays(5),
            List.of(bulkListCaseDetailsListValue(TEST_CASE_ID), bulkListCaseDetailsListValue(TEST_CASE_ID_2))
        );
        List<String> errors = BulkCaseValidationUtil.validateDuplicates(details.getData().getCaseReferences());

        assertThat(errors).isEmpty();
    }

    @Test
    void shouldPopulateErrorMessageWhenCaseInWrongStateAndNotLinked() {
        List<String> errors = BulkCaseValidationUtil.validateLinkToBulkCase(getCaseDetails(TEST_CASE_ID, Submitted, getCaseData()), BULK_CASE_REFERENCE);

        assertThat(errors).containsExactly(String.format(ERROR_NOT_AWAITING_PRONOUNCEMENT, TEST_CASE_ID));
    }

    @Test
    void shouldPopulateErrorMessageWhenCaseInWrongStateAndLinkedToThisList() {
        List<String> errors = BulkCaseValidationUtil.validateLinkToBulkCase(getCaseDetails(TEST_CASE_ID, Submitted, getCaseDataWithCaseLink(BULK_CASE_REFERENCE)), BULK_CASE_REFERENCE);

        assertThat(errors).containsExactly(String.format(ERROR_NOT_AWAITING_PRONOUNCEMENT, TEST_CASE_ID));
    }

    @Test
    void shouldNotPopulateErrorMessageWhenCaseInAwaitingPronouncementAndNotLinked() {
        List<String> errors = BulkCaseValidationUtil.validateLinkToBulkCase(getCaseDetails(TEST_CASE_ID, AwaitingPronouncement, getCaseData()), BULK_CASE_REFERENCE);

        assertThat(errors).isEmpty();
    }

    @Test
    void shouldNotPopulateErrorMessageWhenCaseInAwaitingPronouncementAndLinkedToThisList() {
        List<String> errors = BulkCaseValidationUtil.validateLinkToBulkCase(getCaseDetails(TEST_CASE_ID, AwaitingPronouncement, getCaseDataWithCaseLink(BULK_CASE_REFERENCE)), BULK_CASE_REFERENCE);

        assertThat(errors).isEmpty();
    }

    @Test
    void shouldPopulateErrorMessageWhenCaseLinkedToOtherList() {
        List<String> errors = BulkCaseValidationUtil.validateLinkToBulkCase(getCaseDetails(TEST_CASE_ID, AwaitingPronouncement, getCaseDataWithCaseLink(BULK_CASE_REFERENCE_2)), BULK_CASE_REFERENCE);

        assertThat(errors).containsExactly(String.format(ERROR_ALREADY_LINKED_TO_BULK_CASE, TEST_CASE_ID, BULK_CASE_REFERENCE_2));
    }

    @Test
    void shouldPopulateErrorMessagesWhenCaseInWrongStateAndLinkedToOtherList() {
        List<String> errors = BulkCaseValidationUtil.validateLinkToBulkCase(getCaseDetails(TEST_CASE_ID, Submitted, getCaseDataWithCaseLink(BULK_CASE_REFERENCE_2)), BULK_CASE_REFERENCE);

        assertThat(errors).containsExactly(String.format(ERROR_NOT_AWAITING_PRONOUNCEMENT, TEST_CASE_ID), String.format(ERROR_ALREADY_LINKED_TO_BULK_CASE, TEST_CASE_ID, BULK_CASE_REFERENCE_2));
    }

    private ListValue<BulkListCaseDetails> bulkListCaseDetailsListValue(Long caseId) {
        final BulkListCaseDetails bulkCaseDetails = BulkListCaseDetails
            .builder()
            .caseParties(TEST_FIRST_NAME + " " + TEST_LAST_NAME + " vs " + TEST_APP2_FIRST_NAME + " " + TEST_APP2_LAST_NAME)
            .caseReference(CaseLink.builder().caseReference(String.valueOf(caseId)).build())
            .decisionDate(LocalDate.now().minusDays(10))
            .build();
        return ListValue.<BulkListCaseDetails>builder().value(bulkCaseDetails).build();
    }

    private CaseDetails<BulkActionCaseData, BulkActionState> getBulkCaseDetails(LocalDateTime dateAndTimeOfHearing) {
        final CaseDetails<BulkActionCaseData, BulkActionState> details = new CaseDetails<>();
        details.setData(BulkActionCaseData.builder().dateAndTimeOfHearing(dateAndTimeOfHearing).court(BIRMINGHAM).build());
        details.setState(Listed);
        details.setId(BULK_CASE_REFERENCE);
        return details;
    }

    private CaseDetails<BulkActionCaseData, BulkActionState> getBulkCaseDetails(
        LocalDateTime dateAndTimeOfHearing,
        List<ListValue<BulkListCaseDetails>> bulkListCaseDetails
    ) {
        final CaseDetails<BulkActionCaseData, BulkActionState> details = getBulkCaseDetails(dateAndTimeOfHearing);
        details.getData().setBulkListCaseDetails(bulkListCaseDetails);
        return details;
    }

    private CaseDetails<CaseData, State> getCaseDetails(Long id, State state, CaseData caseData) {
        return CaseDetails.<CaseData, State>builder()
            .id(id)
            .data(caseData)
            .state(state)
            .build();
    }

    private CaseData getCaseData() {
        final CaseData mappedCaseData = caseData();
        mappedCaseData.setApplicant2(getApplicant2WithAddress(MALE));
        return mappedCaseData;
    }

    private CaseData getCaseDataWithCaseLink(Long bulkCaseReference) {
        final CaseData caseData = getCaseData();
        caseData.setBulkListCaseReferenceLink(CaseLink.builder().caseReference(bulkCaseReference.toString()).build());
        return caseData;
    }

}
