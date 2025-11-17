package uk.gov.hmcts.divorce.systemupdate.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.CaseLink;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.data.BulkListCaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState.Listed;
import static uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderCourt.BIRMINGHAM;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPronouncement;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.systemupdate.service.BulkCaseValidationService.BULK_LIST_ERRORED_CASES;
import static uk.gov.hmcts.divorce.systemupdate.service.BulkCaseValidationService.ERROR_ALREADY_LINKED_TO_BULK_CASE;
import static uk.gov.hmcts.divorce.systemupdate.service.BulkCaseValidationService.ERROR_CASES_NOT_FOUND;
import static uk.gov.hmcts.divorce.systemupdate.service.BulkCaseValidationService.ERROR_CASE_IDS_DUPLICATED;
import static uk.gov.hmcts.divorce.systemupdate.service.BulkCaseValidationService.ERROR_DO_NOT_REMOVE_CASES;
import static uk.gov.hmcts.divorce.systemupdate.service.BulkCaseValidationService.ERROR_HEARING_DATE_IN_PAST;
import static uk.gov.hmcts.divorce.systemupdate.service.BulkCaseValidationService.ERROR_NOT_AWAITING_PRONOUNCEMENT;
import static uk.gov.hmcts.divorce.systemupdate.service.BulkCaseValidationService.ERROR_NO_CASES_SCHEDULED;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_USER_USER_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APP2_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APP2_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
public class BulkCaseValidationServiceTest {
    private static final Long BULK_CASE_REFERENCE = 1234123412341234L;
    private static final Long BULK_CASE_REFERENCE_2 = 2345234523452345L;
    private static final Long TEST_CASE_ID_2 = 2L;
    private static final LocalDateTime NOW = LocalDateTime.now();

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private CcdSearchService ccdSearchService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private BulkCaseValidationService bulkCaseValidationService;

    @Test
    void shouldValidateBulkListForErroredCases() {
        final CaseDetails<BulkActionCaseData, BulkActionState> details = new CaseDetails<>();
        details.setData(BulkActionCaseData.builder().erroredCaseDetails(
            List.of(ListValue.<BulkListCaseDetails>builder().build())).build());
        details.setId(TEST_CASE_ID);

        List<String> errors = bulkCaseValidationService.validateBulkListErroredCases(details);

        assertThat(errors).containsExactly(BULK_LIST_ERRORED_CASES);
    }

    @Test
    void shouldValidateAndReturnNoErrorsWhenBulkListHasNoErroredCases() {
        final CaseDetails<BulkActionCaseData, BulkActionState> bulkCaseDetails = new CaseDetails<>();
        bulkCaseDetails.setData(BulkActionCaseData.builder()
            .erroredCaseDetails(null)
            .build());

        List<String> errors = bulkCaseValidationService.validateBulkListErroredCases(bulkCaseDetails);

        assertThat(errors).isEmpty();
    }

    @Test
    void shouldPopulateErrorMessageWhenHearingDateIsInThePast() {
        final CaseDetails<BulkActionCaseData, BulkActionState> details = getBulkCaseDetails(
            NOW.minusDays(5),
            List.of(bulkListCaseDetailsListValue(TEST_CASE_ID))
        );
        List<String> errors = bulkCaseValidationService.validateData(details.getData(), details.getData(), details.getId());

        assertThat(errors).containsExactly(ERROR_HEARING_DATE_IN_PAST);
    }

    @Test
    void shouldNotPopulateErrorMessageWhenHearingDateIsInTheFuture() {
        final CaseDetails<BulkActionCaseData, BulkActionState> details = getBulkCaseDetails(
            NOW.plusDays(5),
            List.of(bulkListCaseDetailsListValue(TEST_CASE_ID))
        );
        List<String> errors = bulkCaseValidationService.validateData(details.getData(), details.getData(), details.getId());

        assertThat(errors).isEmpty();
    }

    @Test
    void shouldPopulateErrorMessageWhenCaseListsAreEmpty() {
        final CaseDetails<BulkActionCaseData, BulkActionState> details = getBulkCaseDetails(NOW.plusDays(5));
        List<String> errors = bulkCaseValidationService.validateData(details.getData(), details.getData(), details.getId());

        assertThat(errors).containsExactly(ERROR_NO_CASES_SCHEDULED);
    }

    @Test
    void shouldNotPopulateErrorMessageWhenCaseListsAreNotEmpty() {
        final CaseDetails<BulkActionCaseData, BulkActionState> details = getBulkCaseDetails(
            NOW.plusDays(5),
            List.of(bulkListCaseDetailsListValue(TEST_CASE_ID)));
        List<String> errors = bulkCaseValidationService.validateData(details.getData(), details.getData(), details.getId());

        assertThat(errors).isEmpty();
    }

    @Test
    void shouldPopulateErrorMessagesWhenHearingDateIsInThePastAndCaseListsAreEmpty() {
        final CaseDetails<BulkActionCaseData, BulkActionState> details = getBulkCaseDetails(NOW.minusDays(5));
        List<String> errors = bulkCaseValidationService.validateData(details.getData(), details.getData(), details.getId());

        assertThat(errors).containsExactly(ERROR_HEARING_DATE_IN_PAST, ERROR_NO_CASES_SCHEDULED);
    }

    @Test
    void shouldPopulateErrorMessageWhenCaseRemovedFromList() {
        final CaseDetails<BulkActionCaseData, BulkActionState> beforeDetails = getBulkCaseDetails(
            NOW.minusDays(5),
            List.of(bulkListCaseDetailsListValue(TEST_CASE_ID))
        );
        final CaseDetails<BulkActionCaseData, BulkActionState> details = getBulkCaseDetails(NOW.plusDays(5));
        List<String> errors = bulkCaseValidationService.validateData(details.getData(), beforeDetails.getData(), details.getId());

        assertThat(errors).containsExactly(ERROR_DO_NOT_REMOVE_CASES);
    }

    @Test
    void shouldNotPopulateErrorMessageWhenCaseNotRemovedFromList() {
        final CaseDetails<BulkActionCaseData, BulkActionState> details = getBulkCaseDetails(
            NOW.plusDays(5),
            List.of(bulkListCaseDetailsListValue(TEST_CASE_ID))
        );
        List<String> errors = bulkCaseValidationService.validateData(details.getData(), details.getData(), details.getId());

        assertThat(errors).isEmpty();
    }

    @Test
    void shouldPopulateErrorMessagesWhenHearingDateIsInThePastAndCaseRemovedFromList() {
        final CaseDetails<BulkActionCaseData, BulkActionState> beforeDetails = getBulkCaseDetails(
            NOW.minusDays(5),
            List.of(bulkListCaseDetailsListValue(TEST_CASE_ID))
        );
        final CaseDetails<BulkActionCaseData, BulkActionState> details = getBulkCaseDetails(NOW.minusDays(5));
        List<String> errors = bulkCaseValidationService.validateData(details.getData(), beforeDetails.getData(), details.getId());

        assertThat(errors).containsExactly(ERROR_HEARING_DATE_IN_PAST, ERROR_DO_NOT_REMOVE_CASES);
    }

    @Test
    void shouldPopulateErrorMessageWhenCaseDuplicatedInList() {
        final CaseDetails<BulkActionCaseData, BulkActionState> details = getBulkCaseDetails(
            NOW.plusDays(5),
            List.of(bulkListCaseDetailsListValue(TEST_CASE_ID), bulkListCaseDetailsListValue(TEST_CASE_ID))
        );
        List<String> errors = bulkCaseValidationService.validateData(details.getData(), details.getData(), details.getId());

        assertThat(errors).containsExactly(ERROR_CASE_IDS_DUPLICATED);
    }

    @Test
    void shouldNotPopulateErrorMessageWhenCaseNotDuplicatedInList() {
        final CaseDetails<BulkActionCaseData, BulkActionState> details = getBulkCaseDetails(
            NOW.plusDays(5),
            List.of(bulkListCaseDetailsListValue(TEST_CASE_ID), bulkListCaseDetailsListValue(TEST_CASE_ID_2))
        );
        List<String> errors = bulkCaseValidationService.validateData(details.getData(), details.getData(), details.getId());

        assertThat(errors).isEmpty();
    }

    @Test
    void shouldPopulateErrorMessagesWhenHearingDateIsInThePastAndCaseRemovedFromListAndCaseDuplicatedInList() {
        final CaseDetails<BulkActionCaseData, BulkActionState> beforeDetails = getBulkCaseDetails(
            NOW.minusDays(5),
            List.of(bulkListCaseDetailsListValue(TEST_CASE_ID))
        );
        final CaseDetails<BulkActionCaseData, BulkActionState> details = getBulkCaseDetails(
            NOW.minusDays(5),
            List.of(bulkListCaseDetailsListValue(TEST_CASE_ID_2), bulkListCaseDetailsListValue(TEST_CASE_ID_2))
        );
        List<String> errors = bulkCaseValidationService.validateData(details.getData(), beforeDetails.getData(), details.getId());

        assertThat(errors).containsExactly(ERROR_HEARING_DATE_IN_PAST, ERROR_DO_NOT_REMOVE_CASES, ERROR_CASE_IDS_DUPLICATED);
    }

    @Test
    void shouldPopulateErrorMessageWhenCaseInWrongStateAndNotLinked() {
        final CaseDetails<BulkActionCaseData, BulkActionState> beforeDetails = getBulkCaseDetails(NOW.minusDays(5));
        final CaseDetails<BulkActionCaseData, BulkActionState> details = getBulkCaseDetails(
            NOW.plusDays(5),
            List.of(bulkListCaseDetailsListValue(TEST_CASE_ID))
        );

        setupSearchMock(Submitted, caseData());

        List<String> errors = bulkCaseValidationService.validateData(details.getData(), beforeDetails.getData(), BULK_CASE_REFERENCE);

        assertThat(errors).containsExactly(String.format(ERROR_NOT_AWAITING_PRONOUNCEMENT, TEST_CASE_ID));
    }

    @Test
    void shouldPopulateErrorMessageWhenCaseInWrongStateAndLinkedToThisList() {
        final CaseDetails<BulkActionCaseData, BulkActionState> beforeDetails = getBulkCaseDetails(NOW.minusDays(5));
        final CaseDetails<BulkActionCaseData, BulkActionState> details = getBulkCaseDetails(
            NOW.plusDays(5),
            List.of(bulkListCaseDetailsListValue(TEST_CASE_ID))
        );

        setupSearchMock(Submitted, getCaseDataWithCaseLink(BULK_CASE_REFERENCE));

        List<String> errors = bulkCaseValidationService.validateData(details.getData(), beforeDetails.getData(), BULK_CASE_REFERENCE);

        assertThat(errors).containsExactly(String.format(ERROR_NOT_AWAITING_PRONOUNCEMENT, TEST_CASE_ID));
    }

    @Test
    void shouldNotPopulateErrorMessageWhenCaseInAwaitingPronouncementAndNotLinked() {
        final CaseDetails<BulkActionCaseData, BulkActionState> beforeDetails = getBulkCaseDetails(NOW.minusDays(5));
        final CaseDetails<BulkActionCaseData, BulkActionState> details = getBulkCaseDetails(
            NOW.plusDays(5),
            List.of(bulkListCaseDetailsListValue(TEST_CASE_ID))
        );

        setupSearchMock(AwaitingPronouncement, caseData());

        List<String> errors = bulkCaseValidationService.validateData(details.getData(), beforeDetails.getData(), BULK_CASE_REFERENCE);

        assertThat(errors).isEmpty();
    }

    @Test
    void shouldNotPopulateErrorMessageWhenCaseInAwaitingPronouncementAndLinkedToThisList() {
        final CaseDetails<BulkActionCaseData, BulkActionState> beforeDetails = getBulkCaseDetails(NOW.minusDays(5));
        final CaseDetails<BulkActionCaseData, BulkActionState> details = getBulkCaseDetails(
            NOW.plusDays(5),
            List.of(bulkListCaseDetailsListValue(TEST_CASE_ID))
        );

        setupSearchMock(AwaitingPronouncement, getCaseDataWithCaseLink(BULK_CASE_REFERENCE));

        List<String> errors = bulkCaseValidationService.validateData(details.getData(), beforeDetails.getData(), BULK_CASE_REFERENCE);

        assertThat(errors).isEmpty();
    }

    @Test
    void shouldPopulateErrorMessageWhenSearchReturnsNoResults() {
        final CaseDetails<BulkActionCaseData, BulkActionState> beforeDetails = getBulkCaseDetails(NOW.minusDays(5));
        final CaseDetails<BulkActionCaseData, BulkActionState> details = getBulkCaseDetails(
            NOW.plusDays(5),
            List.of(bulkListCaseDetailsListValue(TEST_CASE_ID))
        );

        setupSearchMock(List.of(TEST_CASE_ID.toString()), new ArrayList<>());

        List<String> errors = bulkCaseValidationService.validateData(details.getData(), beforeDetails.getData(), BULK_CASE_REFERENCE);

        assertThat(errors).containsExactly(String.format(ERROR_CASES_NOT_FOUND, TEST_CASE_ID));
    }

    @Test
    void shouldPopulateErrorMessageWhenSearchReturnsMissingResults() {
        final CaseDetails<BulkActionCaseData, BulkActionState> beforeDetails = getBulkCaseDetails(NOW.minusDays(5));
        final CaseDetails<BulkActionCaseData, BulkActionState> details = getBulkCaseDetails(
            NOW.plusDays(5),
            List.of(bulkListCaseDetailsListValue(TEST_CASE_ID), bulkListCaseDetailsListValue(TEST_CASE_ID_2))
        );

        final uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetails = getModelCaseDetails(AwaitingPronouncement);

        setupSearchMock(List.of(TEST_CASE_ID.toString(), TEST_CASE_ID_2.toString()), List.of(caseDetails));

        setupObjectMapperMock(AwaitingPronouncement, caseData());

        List<String> errors = bulkCaseValidationService.validateData(details.getData(), beforeDetails.getData(), BULK_CASE_REFERENCE);

        assertThat(errors).containsExactly(String.format(ERROR_CASES_NOT_FOUND, TEST_CASE_ID_2));
    }

    @Test
    void shouldPopulateErrorMessageWhenCaseLinkedToOtherList() {
        final CaseDetails<BulkActionCaseData, BulkActionState> beforeDetails = getBulkCaseDetails(NOW.minusDays(5));
        final CaseDetails<BulkActionCaseData, BulkActionState> details = getBulkCaseDetails(
            NOW.plusDays(5),
            List.of(bulkListCaseDetailsListValue(TEST_CASE_ID))
        );

        setupSearchMock(AwaitingPronouncement, getCaseDataWithCaseLink(BULK_CASE_REFERENCE_2));

        List<String> errors = bulkCaseValidationService.validateData(details.getData(), beforeDetails.getData(), BULK_CASE_REFERENCE);

        assertThat(errors).containsExactly(String.format(ERROR_ALREADY_LINKED_TO_BULK_CASE, TEST_CASE_ID, BULK_CASE_REFERENCE_2));
    }

    @Test
    void shouldPopulateErrorMessagesWhenCaseInWrongStateAndLinkedToOtherList() {
        final CaseDetails<BulkActionCaseData, BulkActionState> beforeDetails = getBulkCaseDetails(NOW.minusDays(5));
        final CaseDetails<BulkActionCaseData, BulkActionState> details = getBulkCaseDetails(
            NOW.plusDays(5),
            List.of(bulkListCaseDetailsListValue(TEST_CASE_ID))
        );

        setupSearchMock(Submitted, getCaseDataWithCaseLink(BULK_CASE_REFERENCE_2));

        List<String> errors = bulkCaseValidationService.validateData(details.getData(), beforeDetails.getData(), BULK_CASE_REFERENCE);

        assertThat(errors).containsExactly(
            String.format(ERROR_NOT_AWAITING_PRONOUNCEMENT, TEST_CASE_ID),
            String.format(ERROR_ALREADY_LINKED_TO_BULK_CASE, TEST_CASE_ID, BULK_CASE_REFERENCE_2)
        );
    }

    @Test
    void shouldPopulateErrorMessageWhenCaseInWrongStateAndNotLinkedWhenValidatingForListing() {
        List<String> errors = bulkCaseValidationService.validateCaseForListing(getCaseDetails(Submitted, caseData()));

        assertThat(errors).containsExactly(String.format(ERROR_NOT_AWAITING_PRONOUNCEMENT, TEST_CASE_ID));
    }

    @Test
    void shouldPopulateErrorMessageWhenCaseLinkedWhenValidatingForListing() {
        List<String> errors = bulkCaseValidationService.validateCaseForListing(
            getCaseDetails(AwaitingPronouncement, getCaseDataWithCaseLink(BULK_CASE_REFERENCE))
        );

        assertThat(errors).containsExactly(String.format(ERROR_ALREADY_LINKED_TO_BULK_CASE, TEST_CASE_ID, BULK_CASE_REFERENCE));
    }

    @Test
    void shouldPopulateErrorMessageWhenCaseInWrongStateAndLinkedWhenValidatingForListing() {
        List<String> errors = bulkCaseValidationService.validateCaseForListing(
            getCaseDetails(Submitted, getCaseDataWithCaseLink(BULK_CASE_REFERENCE))
        );

        assertThat(errors).containsExactly(
            String.format(ERROR_NOT_AWAITING_PRONOUNCEMENT, TEST_CASE_ID),
            String.format(ERROR_ALREADY_LINKED_TO_BULK_CASE, TEST_CASE_ID, BULK_CASE_REFERENCE)
        );
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
        return CaseDetails.<BulkActionCaseData, BulkActionState>builder()
            .data(BulkActionCaseData.builder().dateAndTimeOfHearing(dateAndTimeOfHearing).court(BIRMINGHAM).build())
            .state(Listed)
            .id(BULK_CASE_REFERENCE)
            .build();
    }

    private CaseDetails<BulkActionCaseData, BulkActionState> getBulkCaseDetails(
        LocalDateTime dateAndTimeOfHearing,
        List<ListValue<BulkListCaseDetails>> bulkListCaseDetails
    ) {
        final CaseDetails<BulkActionCaseData, BulkActionState> details = getBulkCaseDetails(dateAndTimeOfHearing);
        details.getData().setBulkListCaseDetails(bulkListCaseDetails);
        return details;
    }

    private uk.gov.hmcts.reform.ccd.client.model.CaseDetails getModelCaseDetails(State state) {
        return uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
            .id(TEST_CASE_ID)
            .state(state.toString())
            .build();
    }

    private CaseDetails<CaseData, State> getCaseDetails(State state, CaseData caseData) {
        return CaseDetails.<CaseData, State>builder()
            .id(TEST_CASE_ID)
            .state(state)
            .data(caseData)
            .build();
    }

    private CaseData getCaseDataWithCaseLink(Long bulkCaseReference) {
        final CaseData caseData = caseData();
        caseData.setBulkListCaseReferenceLink(CaseLink.builder().caseReference(bulkCaseReference.toString()).build());
        return caseData;
    }

    private void setupSearchMock(List<String> caseIds, List<uk.gov.hmcts.reform.ccd.client.model.CaseDetails> searchResults) {
        final User systemUser = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserInfo.builder().uid(SYSTEM_USER_USER_ID).build());
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(systemUser);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(ccdSearchService.searchForCases(caseIds, systemUser, TEST_SERVICE_AUTH_TOKEN))
            .thenReturn(searchResults);
    }

    private void setupSearchMock(State state, CaseData caseData) {
        setupSearchMock(List.of(String.valueOf(TEST_CASE_ID)), List.of(getModelCaseDetails(state)));
        setupObjectMapperMock(state, caseData);
    }

    private void setupObjectMapperMock(State state, CaseData caseData) {
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .id(TEST_CASE_ID)
            .data(caseData)
            .state(state)
            .build();
        when(objectMapper.convertValue(any(), ArgumentMatchers.<TypeReference<CaseDetails<CaseData, State>>>any()))
            .thenReturn(caseDetails);
    }

}
