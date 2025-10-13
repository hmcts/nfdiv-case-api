package uk.gov.hmcts.divorce.systemupdate.schedule;

import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.service.CcdConflictException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.io.IOException;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.NOT_FOUND;
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
    void run_submitsEventForEachBulk_foundBySearch() throws IOException, CcdConflictException {
        // CSV → two bulks
        when(taskHelper.loadRectifyBatches("rectify-bulk.csv")).thenReturn(List.of(
            new TaskHelper.BulkRectifySpec(1758254429226124L, List.of(1L, 2L)),
            new TaskHelper.BulkRectifySpec(1758261653985127L, List.of(3L))
        ));

        // IDAM + S2S
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(new User("auth", UserInfo.builder().build()));
        when(authTokenGenerator.generate()).thenReturn("s2s");

        // Search returns both bulks
        CaseDetails b1 = CaseDetails.builder().id(1758254429226124L).build();
        CaseDetails b2 = CaseDetails.builder().id(1758261653985127L).build();
        when(ccdSearchService.searchForAllCasesWithQuery(
            any(), any(), any())
        ).thenReturn(List.of(b1, b2));

        // Run
        task.run();

        // Both bulks submitted
        verify(ccdUpdateService,
            times(1)).submitEvent(eq(1758254429226124L), eq(SYSTEM_RECTIFY_BULK_LIST), any(), any());
        verify(ccdUpdateService,
            times(1)).submitEvent(eq(1758261653985127L), eq(SYSTEM_RECTIFY_BULK_LIST), any(), any());
    }

    @Test
    void run_continuesWhenOneSubmitFails() throws IOException, CcdConflictException {
        when(taskHelper.loadRectifyBatches("rectify-bulk.csv")).thenReturn(List.of(
            new TaskHelper.BulkRectifySpec(1L, List.of(10L)),
            new TaskHelper.BulkRectifySpec(2L, List.of(20L))
        ));

        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(new User("auth", UserInfo.builder().build()));
        when(authTokenGenerator.generate()).thenReturn("s2s");

        CaseDetails b1 = CaseDetails.builder().id(1L).build();
        CaseDetails b2 = CaseDetails.builder().id(2L).build();
        when(ccdSearchService.searchForAllCasesWithQuery(
            any(), any(), any())
        ).thenReturn(List.of(b1, b2));

        doThrow(new CcdManagementException(NOT_FOUND.value(), "Failed processing of case", mock(FeignException.class)))
            .when(ccdUpdateService)
            .submitEvent(1L, SYSTEM_RECTIFY_BULK_LIST, any(), any());

        task.run();

        // First failed, second still submitted
        verify(ccdUpdateService, times(1)).submitEvent(1L, SYSTEM_RECTIFY_BULK_LIST, any(), any());
        verify(ccdUpdateService, times(1)).submitEvent(2L, SYSTEM_RECTIFY_BULK_LIST, any(), any());
    }
}
