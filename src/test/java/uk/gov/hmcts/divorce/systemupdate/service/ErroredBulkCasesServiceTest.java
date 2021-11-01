package uk.gov.hmcts.divorce.systemupdate.service;

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
import uk.gov.hmcts.divorce.bulkaction.service.BulkTriggerService;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.reform.idam.client.models.User;

import java.util.Arrays;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.bulkaction.ccd.event.SystemUpdateCase.SYSTEM_UPDATE_BULK_CASE;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemPronounceCase.SYSTEM_PRONOUNCE_CASE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;

@ExtendWith(MockitoExtension.class)
class ErroredBulkCasesServiceTest {

    @Mock
    private CcdUpdateService ccdUpdateService;

    @Mock
    private BulkTriggerService bulkTriggerService;

    @InjectMocks
    private ErroredBulkCasesService erroredBulkCasesService;

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

        erroredBulkCasesService.processErroredCasesAndUpdateBulkCase(
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

        erroredBulkCasesService.processErroredCasesAndUpdateBulkCase(
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

        erroredBulkCasesService.processErroredCasesAndUpdateBulkCase(
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

        erroredBulkCasesService.processErroredCasesAndUpdateBulkCase(
            caseDetails,
            SYSTEM_PRONOUNCE_CASE,
            bulkCaseDetails -> bulkCaseDetails,
            user,
            SERVICE_AUTHORIZATION);
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