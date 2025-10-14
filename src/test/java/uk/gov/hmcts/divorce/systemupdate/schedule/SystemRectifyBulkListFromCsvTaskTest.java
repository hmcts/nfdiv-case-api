package uk.gov.hmcts.divorce.systemupdate.schedule;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.service.CcdConflictException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.io.IOException;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.bulkaction.ccd.event.SystemRectifyBulkList.SYSTEM_RECTIFY_BULK_LIST;

@ExtendWith(MockitoExtension.class)
class SystemRectifyBulkListFromCsvTaskTest {

    @Mock private CcdUpdateService ccdUpdateService;
    @Mock private CcdSearchService ccdSearchService;
    @Mock private IdamService idamService;
    @Mock private AuthTokenGenerator authTokenGenerator;
    @Mock private TaskHelper taskHelper;

    @InjectMocks
    private SystemRectifyBulkListFromCsvTask task;

    @Test
    @SuppressWarnings("unchecked")
    void run_submitsEventForEachBulk_foundBySearch() throws IOException, CcdConflictException {
        when(taskHelper.loadRectifyBatches("rectify-bulk.csv")).thenReturn(List.of(
            new TaskHelper.BulkRectifySpec(1758254429226124L, List.of(1L, 2L)),
            new TaskHelper.BulkRectifySpec(1758261653985127L, List.of(3L))
        ));

        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(new User("auth", null));
        when(authTokenGenerator.generate()).thenReturn("s2s");

        // Mock SDK CaseDetails with getId()
        CaseDetails<BulkActionCaseData, BulkActionState> b1 = mock(CaseDetails.class);
        when(b1.getId()).thenReturn(1758254429226124L);
        CaseDetails<BulkActionCaseData, BulkActionState> b2 = mock(CaseDetails.class);
        when(b2.getId()).thenReturn(1758261653985127L);

        when(ccdSearchService.searchForBulkCases(any(), any(), any()))
            .thenReturn(List.of(b1, b2));

        task.run();

        verify(ccdUpdateService, times(1))
            .submitEvent(eq(1758254429226124L), eq(SYSTEM_RECTIFY_BULK_LIST), any(User.class), anyString());
        verify(ccdUpdateService, times(1))
            .submitEvent(eq(1758261653985127L), eq(SYSTEM_RECTIFY_BULK_LIST), any(User.class), anyString());
    }

    @Test
    @SuppressWarnings("unchecked")
    void run_continuesWhenOneSubmitFails() throws IOException, CcdConflictException {
        when(taskHelper.loadRectifyBatches("rectify-bulk.csv")).thenReturn(List.of(
            new TaskHelper.BulkRectifySpec(1L, List.of(10L)),
            new TaskHelper.BulkRectifySpec(2L, List.of(20L))
        ));

        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(new User("auth", null));
        when(authTokenGenerator.generate()).thenReturn("s2s");

        CaseDetails<BulkActionCaseData, BulkActionState> b1 = mock(CaseDetails.class);
        when(b1.getId()).thenReturn(1L);
        CaseDetails<BulkActionCaseData, BulkActionState> b2 = mock(CaseDetails.class);
        when(b2.getId()).thenReturn(2L);

        when(ccdSearchService.searchForBulkCases(any(), any(), any()))
            .thenReturn(List.of(b1, b2));

        doThrow(new CcdManagementException(404, "Failed processing of case", null))
            .when(ccdUpdateService)
            .submitEvent(eq(1L), eq(SYSTEM_RECTIFY_BULK_LIST), any(User.class), anyString());

        task.run();

        verify(ccdUpdateService, times(1))
            .submitEvent(eq(1L), eq(SYSTEM_RECTIFY_BULK_LIST), any(User.class), anyString());
        verify(ccdUpdateService, times(1))
            .submitEvent(eq(2L), eq(SYSTEM_RECTIFY_BULK_LIST), any(User.class), anyString());
    }
}
