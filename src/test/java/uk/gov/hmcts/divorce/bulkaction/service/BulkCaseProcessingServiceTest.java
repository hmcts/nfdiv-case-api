package uk.gov.hmcts.divorce.bulkaction.service;

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
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.divorce.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.idam.client.models.User;

import java.util.Arrays;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.bulkaction.ccd.event.SystemUpdateCase.SYSTEM_UPDATE_BULK_CASE;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemPronounceCase.SYSTEM_PRONOUNCE_CASE;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemRemoveBulkCase.SYSTEM_REMOVE_BULK_CASE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;

@ExtendWith(MockitoExtension.class)
class BulkCaseProcessingServiceTest {

    @Mock
    private CcdUpdateService ccdUpdateService;

    @Mock
    private BulkTriggerService bulkTriggerService;

    @InjectMocks
    private BulkCaseProcessingService bulkCaseProcessingService;

    @Test
    void shouldProcessAllBulkActionCasesAndUpdateWithAnyErrorsAfterCompletion() {

        final User user = mock(User.class);
        final CaseTask caseTask = bulkCaseDetails -> bulkCaseDetails;
        final List<ListValue<BulkListCaseDetails>> fullBulkList = getBulkListCaseDetailsListValueForCaseIds("1", "2", "3", "4");
        final List<ListValue<BulkListCaseDetails>> processedCases = getBulkListCaseDetailsListValueForCaseIds("1", "3", "4");
        final List<ListValue<BulkListCaseDetails>> unprocessedCases = getBulkListCaseDetailsListValueForCaseIds("2");

        final BulkActionCaseData bulkActionCaseData = BulkActionCaseData.builder()
            .bulkListCaseDetails(fullBulkList)
            .build();

        final CaseDetails<BulkActionCaseData, BulkActionState> caseDetails = new CaseDetails<>();
        caseDetails.setId(1L);
        caseDetails.setData(bulkActionCaseData);

        when(bulkTriggerService.bulkTrigger(
            fullBulkList,
            SYSTEM_PRONOUNCE_CASE,
            caseTask,
            user,
            SERVICE_AUTHORIZATION))
            .thenReturn(unprocessedCases);

        bulkCaseProcessingService.updateAllBulkCases(
            caseDetails,
            SYSTEM_PRONOUNCE_CASE,
            caseTask,
            user,
            SERVICE_AUTHORIZATION);

        assertThat(caseDetails.getData().getProcessedCaseDetails()).isEqualTo(processedCases);
        assertThat(caseDetails.getData().getErroredCaseDetails()).isEqualTo(unprocessedCases);

        verify(ccdUpdateService).submitBulkActionEvent(
            caseDetails,
            SYSTEM_UPDATE_BULK_CASE,
            user,
            SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldProcessBulkActionCasesWithErrorsAndUpdateWithAnyErrorsAfterCompletion() {

        final User user = mock(User.class);
        final List<ListValue<BulkListCaseDetails>> fullBulkList = getBulkListCaseDetailsListValueForCaseIds("1", "2", "3", "4");
        final List<ListValue<BulkListCaseDetails>> currentProcessed = getBulkListCaseDetailsListValueForCaseIds("3", "4");
        final List<ListValue<BulkListCaseDetails>> updatedProcessed = getBulkListCaseDetailsListValueForCaseIds("1", "3", "4");
        final List<ListValue<BulkListCaseDetails>> currentErrors = getBulkListCaseDetailsListValueForCaseIds("1", "2");
        final List<ListValue<BulkListCaseDetails>> updatedErrors = getBulkListCaseDetailsListValueForCaseIds("2");

        final BulkActionCaseData bulkActionCaseData = BulkActionCaseData.builder()
            .bulkListCaseDetails(fullBulkList)
            .processedCaseDetails(currentProcessed)
            .erroredCaseDetails(currentErrors)
            .build();

        final CaseDetails<BulkActionCaseData, BulkActionState> caseDetails = new CaseDetails<>();
        caseDetails.setId(1L);
        caseDetails.setData(bulkActionCaseData);

        when(bulkTriggerService.bulkTrigger(
            eq(currentErrors),
            eq(SYSTEM_PRONOUNCE_CASE),
            any(CaseTask.class),
            eq(user),
            eq(SERVICE_AUTHORIZATION)))
            .thenReturn(updatedErrors);

        bulkCaseProcessingService.updateUnprocessedBulkCases(
            caseDetails,
            SYSTEM_PRONOUNCE_CASE,
            bulkCaseDetails -> bulkCaseDetails,
            user,
            SERVICE_AUTHORIZATION);

        assertThat(caseDetails.getData().getProcessedCaseDetails()).isEqualTo(updatedProcessed);
        assertThat(caseDetails.getData().getErroredCaseDetails()).isEqualTo(updatedErrors);

        verify(ccdUpdateService).submitBulkActionEvent(
            caseDetails,
            SYSTEM_UPDATE_BULK_CASE,
            user,
            SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldProcessBulkActionCasesWithNoProcessedCasesAndUpdateWithAnyErrorsAfterCompletion() {

        final User user = mock(User.class);
        final List<ListValue<BulkListCaseDetails>> fullBulkList = getBulkListCaseDetailsListValueForCaseIds("1", "2", "3", "4");
        final List<ListValue<BulkListCaseDetails>> updatedProcessed = getBulkListCaseDetailsListValueForCaseIds("1", "3", "4");
        final List<ListValue<BulkListCaseDetails>> updatedErrors = getBulkListCaseDetailsListValueForCaseIds("2");

        final BulkActionCaseData bulkActionCaseData = BulkActionCaseData.builder()
            .bulkListCaseDetails(fullBulkList)
            .build();

        final CaseDetails<BulkActionCaseData, BulkActionState> caseDetails = new CaseDetails<>();
        caseDetails.setId(1L);
        caseDetails.setData(bulkActionCaseData);

        when(bulkTriggerService.bulkTrigger(
            eq(fullBulkList),
            eq(SYSTEM_PRONOUNCE_CASE),
            any(CaseTask.class),
            eq(user),
            eq(SERVICE_AUTHORIZATION)))
            .thenReturn(updatedErrors);

        bulkCaseProcessingService.updateUnprocessedBulkCases(
            caseDetails,
            SYSTEM_PRONOUNCE_CASE,
            bulkCaseDetails -> bulkCaseDetails,
            user,
            SERVICE_AUTHORIZATION);

        assertThat(caseDetails.getData().getProcessedCaseDetails()).isEqualTo(updatedProcessed);
        assertThat(caseDetails.getData().getErroredCaseDetails()).isEqualTo(updatedErrors);

        verify(ccdUpdateService).submitBulkActionEvent(
            caseDetails,
            SYSTEM_UPDATE_BULK_CASE,
            user,
            SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldProcessBulkActionCasesWithEmptyProcessedCasesAndUpdateWithAnyErrorsAfterCompletion() {

        final User user = mock(User.class);
        final List<ListValue<BulkListCaseDetails>> fullBulkList = getBulkListCaseDetailsListValueForCaseIds("1", "2", "3", "4");
        final List<ListValue<BulkListCaseDetails>> updatedProcessed = getBulkListCaseDetailsListValueForCaseIds("1", "3", "4");
        final List<ListValue<BulkListCaseDetails>> updatedErrors = getBulkListCaseDetailsListValueForCaseIds("2");

        final BulkActionCaseData bulkActionCaseData = BulkActionCaseData.builder()
            .bulkListCaseDetails(fullBulkList)
            .processedCaseDetails(emptyList())
            .build();

        final CaseDetails<BulkActionCaseData, BulkActionState> caseDetails = new CaseDetails<>();
        caseDetails.setId(1L);
        caseDetails.setData(bulkActionCaseData);

        when(bulkTriggerService.bulkTrigger(
            eq(fullBulkList),
            eq(SYSTEM_PRONOUNCE_CASE),
            any(CaseTask.class),
            eq(user),
            eq(SERVICE_AUTHORIZATION)))
            .thenReturn(updatedErrors);

        bulkCaseProcessingService.updateUnprocessedBulkCases(
            caseDetails,
            SYSTEM_PRONOUNCE_CASE,
            bulkCaseDetails -> bulkCaseDetails,
            user,
            SERVICE_AUTHORIZATION);

        assertThat(caseDetails.getData().getProcessedCaseDetails()).isEqualTo(updatedProcessed);
        assertThat(caseDetails.getData().getErroredCaseDetails()).isEqualTo(updatedErrors);

        verify(ccdUpdateService).submitBulkActionEvent(
            caseDetails,
            SYSTEM_UPDATE_BULK_CASE,
            user,
            SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldCatchAndNotRethrowCcdManagementException() {

        final User user = mock(User.class);
        final List<ListValue<BulkListCaseDetails>> fullBulkList = getBulkListCaseDetailsListValueForCaseIds("1", "2", "3", "4");
        final List<ListValue<BulkListCaseDetails>> currentProcessed = getBulkListCaseDetailsListValueForCaseIds("3", "4");
        final List<ListValue<BulkListCaseDetails>> currentErrors = getBulkListCaseDetailsListValueForCaseIds("1", "2");
        final List<ListValue<BulkListCaseDetails>> updatedErrors = getBulkListCaseDetailsListValueForCaseIds("2");

        final BulkActionCaseData bulkActionCaseData = BulkActionCaseData.builder()
            .bulkListCaseDetails(fullBulkList)
            .processedCaseDetails(currentProcessed)
            .erroredCaseDetails(currentErrors)
            .build();

        final CaseDetails<BulkActionCaseData, BulkActionState> caseDetails = new CaseDetails<>();
        caseDetails.setId(1L);
        caseDetails.setData(bulkActionCaseData);

        when(bulkTriggerService.bulkTrigger(
            eq(currentErrors),
            eq(SYSTEM_PRONOUNCE_CASE),
            any(CaseTask.class),
            eq(user),
            eq(SERVICE_AUTHORIZATION)))
            .thenReturn(updatedErrors);

        doThrow(new CcdManagementException("Message", null))
            .when(ccdUpdateService).submitBulkActionEvent(
                caseDetails,
                SYSTEM_UPDATE_BULK_CASE,
                user,
                SERVICE_AUTHORIZATION);

        try {
            bulkCaseProcessingService.updateUnprocessedBulkCases(
                caseDetails,
                SYSTEM_PRONOUNCE_CASE,
                bulkCaseDetails -> bulkCaseDetails,
                user,
                SERVICE_AUTHORIZATION);
        } catch (Exception e) {
            fail("No exception should be thrown");
        }
    }

    @Test
    void shouldRemoveAllCasesAndEmptyCasesToBeRemovedList() {

        final User user = mock(User.class);
        final List<ListValue<BulkListCaseDetails>> casesToBeRemoved = getBulkListCaseDetailsListValueForCaseIds("1", "2", "3", "4");

        final BulkActionCaseData bulkActionCaseData = BulkActionCaseData.builder()
            .casesToBeRemoved(casesToBeRemoved)
            .build();

        final CaseDetails<BulkActionCaseData, BulkActionState> caseDetails = new CaseDetails<>();
        caseDetails.setId(1L);
        caseDetails.setData(bulkActionCaseData);

        when(bulkTriggerService.bulkTrigger(
            eq(casesToBeRemoved),
            eq(SYSTEM_REMOVE_BULK_CASE),
            any(CaseTask.class),
            eq(user),
            eq(SERVICE_AUTHORIZATION)))
            .thenReturn(emptyList());

        bulkCaseProcessingService.updateCasesToBeRemoved(
            caseDetails,
            SYSTEM_REMOVE_BULK_CASE,
            bulkCaseDetails -> bulkCaseDetails,
            user,
            SERVICE_AUTHORIZATION);

        assertThat(caseDetails.getData().getCasesToBeRemoved()).isEmpty();

        verify(ccdUpdateService).submitBulkActionEvent(
            caseDetails,
            SYSTEM_UPDATE_BULK_CASE,
            user,
            SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldUpdateCasesToBeRemovedListIfNotAllCasesCouldBeRemoved() {

        final User user = mock(User.class);
        final List<ListValue<BulkListCaseDetails>> casesToBeRemoved = getBulkListCaseDetailsListValueForCaseIds("1", "2", "3", "4");

        final BulkActionCaseData bulkActionCaseData = BulkActionCaseData.builder()
            .casesToBeRemoved(casesToBeRemoved)
            .build();

        final CaseDetails<BulkActionCaseData, BulkActionState> caseDetails = new CaseDetails<>();
        caseDetails.setId(1L);
        caseDetails.setData(bulkActionCaseData);

        when(bulkTriggerService.bulkTrigger(
            eq(casesToBeRemoved),
            eq(SYSTEM_REMOVE_BULK_CASE),
            any(CaseTask.class),
            eq(user),
            eq(SERVICE_AUTHORIZATION)))
            .thenReturn(List.of(getBulkListCaseDetailsListValue("1")));

        bulkCaseProcessingService.updateCasesToBeRemoved(
            caseDetails,
            SYSTEM_REMOVE_BULK_CASE,
            bulkCaseDetails -> bulkCaseDetails,
            user,
            SERVICE_AUTHORIZATION);

        assertThat(caseDetails.getData().getCasesToBeRemoved()).hasSize(1);
        assertThat(caseDetails.getData().getCasesToBeRemoved()).contains(getBulkListCaseDetailsListValue("1"));

        verify(ccdUpdateService).submitBulkActionEvent(
            caseDetails,
            SYSTEM_UPDATE_BULK_CASE,
            user,
            SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldCatchAndNotRethrowCcdManagementExceptionWhenUpdatingCasesToBeRemoved() {

        final User user = mock(User.class);
        final List<ListValue<BulkListCaseDetails>> casesToBeRemoved = getBulkListCaseDetailsListValueForCaseIds("1", "2", "3", "4");

        final BulkActionCaseData bulkActionCaseData = BulkActionCaseData.builder()
            .casesToBeRemoved(casesToBeRemoved)
            .build();

        final CaseDetails<BulkActionCaseData, BulkActionState> caseDetails = new CaseDetails<>();
        caseDetails.setId(1L);
        caseDetails.setData(bulkActionCaseData);

        when(bulkTriggerService.bulkTrigger(
            eq(casesToBeRemoved),
            eq(SYSTEM_REMOVE_BULK_CASE),
            any(CaseTask.class),
            eq(user),
            eq(SERVICE_AUTHORIZATION)))
            .thenReturn(emptyList());

        doThrow(new CcdManagementException("Message", null))
            .when(ccdUpdateService).submitBulkActionEvent(
                caseDetails,
                SYSTEM_UPDATE_BULK_CASE,
                user,
                SERVICE_AUTHORIZATION);

        try {
            bulkCaseProcessingService.updateCasesToBeRemoved(
                caseDetails,
                SYSTEM_REMOVE_BULK_CASE,
                bulkCaseDetails -> bulkCaseDetails,
                user,
                SERVICE_AUTHORIZATION);
        } catch (Exception e) {
            fail("No exception should be thrown");
        }
    }

    private List<ListValue<BulkListCaseDetails>> getBulkListCaseDetailsListValueForCaseIds(final String... caseIds) {
        return Arrays.stream(caseIds)
            .map(this::getBulkListCaseDetailsListValue).collect(toList());
    }

    private ListValue<BulkListCaseDetails> getBulkListCaseDetailsListValue(final String caseId) {
        var bulkListCaseDetails = BulkListCaseDetails.builder().caseReference(CaseLink.builder().caseReference(caseId).build()).build();
        return ListValue.<BulkListCaseDetails>builder().value(bulkListCaseDetails).build();
    }
}
