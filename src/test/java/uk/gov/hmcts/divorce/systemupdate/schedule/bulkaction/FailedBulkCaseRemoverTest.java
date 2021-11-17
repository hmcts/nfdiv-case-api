package uk.gov.hmcts.divorce.systemupdate.schedule.bulkaction;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.CaseLink;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.data.BulkListCaseDetails;
import uk.gov.hmcts.divorce.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.idam.client.models.User;

import java.util.List;

import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.divorce.bulkaction.ccd.event.SystemEmptyCase.SYSTEM_EMPTY_CASE;
import static uk.gov.hmcts.divorce.bulkaction.ccd.event.SystemRemoveFailedCases.SYSTEM_REMOVE_FAILED_CASES;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;

@ExtendWith(MockitoExtension.class)
class FailedBulkCaseRemoverTest {

    @Mock
    private CcdUpdateService ccdUpdateService;

    @InjectMocks
    private FailedBulkCaseRemover failedBulkCaseRemover;

    @Test
    void shouldRemoveGivenCaseIdsFromBulkCaseListAndUpdateBulkCase() {

        final User user = mock(User.class);
        final List<Long> failedCaseIds = List.of(2L, 4L);
        final List<ListValue<BulkListCaseDetails>> listValues = getBulkListCaseDetailsListValueForCaseIds("1", "2", "3", "4", "5");
        final CaseDetails<BulkActionCaseData, BulkActionState> bulkCaseDetails = new CaseDetails<>();
        bulkCaseDetails.setId(1L);
        bulkCaseDetails.setData(BulkActionCaseData.builder().bulkListCaseDetails(listValues).build());

        failedBulkCaseRemover.removeFailedCasesFromBulkListCaseDetails(
            failedCaseIds,
            bulkCaseDetails,
            user,
            SERVICE_AUTHORIZATION);

        verify(ccdUpdateService).submitBulkActionEvent(
            bulkCaseDetails,
            SYSTEM_REMOVE_FAILED_CASES,
            user,
            SERVICE_AUTHORIZATION);

        verifyNoMoreInteractions(ccdUpdateService);
    }

    @Test
    void shouldRemoveGivenCaseIdsFromBulkCaseListAndSetBulkCaseStateToEmptyIfAllCasesFailed() {

        final User user = mock(User.class);
        final List<Long> failedCaseIds = List.of(1L, 2L, 3L, 4L, 5L);
        final List<ListValue<BulkListCaseDetails>> listValues = getBulkListCaseDetailsListValueForCaseIds("1", "2", "3", "4", "5");
        final CaseDetails<BulkActionCaseData, BulkActionState> bulkCaseDetails = new CaseDetails<>();
        bulkCaseDetails.setId(1L);
        bulkCaseDetails.setData(BulkActionCaseData.builder().bulkListCaseDetails(listValues).build());

        failedBulkCaseRemover.removeFailedCasesFromBulkListCaseDetails(
            failedCaseIds,
            bulkCaseDetails,
            user,
            SERVICE_AUTHORIZATION);

        verify(ccdUpdateService).submitBulkActionEvent(
            bulkCaseDetails,
            SYSTEM_REMOVE_FAILED_CASES,
            user,
            SERVICE_AUTHORIZATION);

        verify(ccdUpdateService).submitBulkActionEvent(
            bulkCaseDetails,
            SYSTEM_EMPTY_CASE,
            user,
            SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldNotDoAnythingIfCaseIdsToRemoveListIsEmpty() {

        final User user = mock(User.class);

        final CaseDetails<BulkActionCaseData, BulkActionState> bulkCaseDetails = new CaseDetails<>();
        bulkCaseDetails.setId(1L);

        failedBulkCaseRemover.removeFailedCasesFromBulkListCaseDetails(
            emptyList(),
            bulkCaseDetails,
            user,
            SERVICE_AUTHORIZATION);

        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    void shouldNotThrowExceptionAndCompleteNormallyIfCcdManagementExceptionIfThrown() {

        final User user = mock(User.class);
        final List<Long> failedCaseIds = List.of(2L, 4L);
        final List<ListValue<BulkListCaseDetails>> listValues = getBulkListCaseDetailsListValueForCaseIds("1", "2", "3", "4", "5");
        final CaseDetails<BulkActionCaseData, BulkActionState> bulkCaseDetails = new CaseDetails<>();
        bulkCaseDetails.setId(1L);
        bulkCaseDetails.setData(BulkActionCaseData.builder().bulkListCaseDetails(listValues).build());

        doThrow(new CcdManagementException("Message", null))
            .when(ccdUpdateService).submitBulkActionEvent(
                bulkCaseDetails,
                SYSTEM_REMOVE_FAILED_CASES,
                user,
                SERVICE_AUTHORIZATION);

        try {
            failedBulkCaseRemover.removeFailedCasesFromBulkListCaseDetails(
                failedCaseIds,
                bulkCaseDetails,
                user,
                SERVICE_AUTHORIZATION);
        } catch (Exception e) {
            fail("No exception should be thrown");
        }
    }

    private List<ListValue<BulkListCaseDetails>> getBulkListCaseDetailsListValueForCaseIds(final String... caseIds) {
        return stream(caseIds)
            .map(this::getBulkListCaseDetailsListValue).collect(toList());
    }

    private ListValue<BulkListCaseDetails> getBulkListCaseDetailsListValue(final String caseId) {
        var bulkListCaseDetails = BulkListCaseDetails.builder()
            .caseReference(CaseLink.builder()
                .caseReference(caseId)
                .build())
            .build();
        return ListValue.<BulkListCaseDetails>builder().value(bulkListCaseDetails).build();
    }
}