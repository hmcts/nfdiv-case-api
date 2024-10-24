package uk.gov.hmcts.divorce.systemupdate.schedule;

import com.google.common.collect.ImmutableList;
import feign.FeignException;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.caseworker.service.notification.StateReportNotification;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.service.CcdConflictException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.service.notify.NotificationClientException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SystemGenerateCurrentStateCountsReportTest {

    @Mock
    private CcdSearchService ccdSearchService;

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private StateReportNotification reportNotificationService;

    @InjectMocks
    private SystemGenerateCurrentStateCountsReport reportTask;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("test-auth-token", null);
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn("service-auth-token");
    }

    @Test
    void shouldRunSuccessfullyAndSendNotification() throws CcdSearchCaseException, NotificationClientException, IOException {
        Map<String, Map<String, Long>> searchResult = new HashMap<>();
        Map<String, Long> dateMap = new HashMap<>();
        dateMap.put("2023-10-24", 10L);
        searchResult.put("Submitted", dateMap);

        when(ccdSearchService.searchWithQueryAndGroupByStateAndLastStateModifiedDate(any(BoolQueryBuilder.class), eq(user), anyString()))
            .thenReturn(searchResult);

        reportTask.run();

        verify(ccdSearchService).searchWithQueryAndGroupByStateAndLastStateModifiedDate(any(BoolQueryBuilder.class), eq(user), anyString());
        verify(reportNotificationService).send(ArgumentMatchers.<ImmutableList.Builder<String>>any(), anyString());
    }

    @Test
    void shouldHandleCcdSearchCaseExceptionGracefully() throws CcdSearchCaseException, NotificationClientException, IOException {
        when(ccdSearchService.searchWithQueryAndGroupByStateAndLastStateModifiedDate(any(BoolQueryBuilder.class), eq(user), anyString()))
            .thenThrow(new CcdSearchCaseException("Failed to search cases", mock(FeignException.class)));

        reportTask.run();

        verify(ccdSearchService).searchWithQueryAndGroupByStateAndLastStateModifiedDate(any(BoolQueryBuilder.class), eq(user), anyString());
        verify(reportNotificationService, never()).send(ArgumentMatchers.<ImmutableList.Builder<String>>any(), anyString());
    }

    @Test
    void shouldHandleCcdConflictExceptionGracefully()
        throws CcdSearchCaseException, CcdConflictException, NotificationClientException, IOException {
        Map<String, Map<String, Long>> searchResult = new HashMap<>();
        Map<String, Long> dateMap = new HashMap<>();
        dateMap.put("2023-10-24", 10L);
        searchResult.put("Submitted", dateMap);

        when(ccdSearchService.searchWithQueryAndGroupByStateAndLastStateModifiedDate(any(BoolQueryBuilder.class), eq(user), anyString()))
            .thenReturn(searchResult);

        doThrow(new CcdConflictException("Conflict with another task", mock(FeignException.class)))
            .when(reportNotificationService).send(ArgumentMatchers.<ImmutableList.Builder<String>>any(), anyString());

        reportTask.run();

        verify(ccdSearchService).searchWithQueryAndGroupByStateAndLastStateModifiedDate(any(BoolQueryBuilder.class), eq(user), anyString());
        verify(reportNotificationService).send(ArgumentMatchers.<ImmutableList.Builder<String>>any(), anyString());
    }

    @Test
    void shouldHandleNotificationClientExceptionGracefully() throws CcdSearchCaseException, NotificationClientException, IOException {
        Map<String, Map<String, Long>> searchResult = new HashMap<>();
        Map<String, Long> dateMap = new HashMap<>();
        dateMap.put("2023-10-24", 10L);
        searchResult.put("Submitted", dateMap);

        when(ccdSearchService.searchWithQueryAndGroupByStateAndLastStateModifiedDate(any(BoolQueryBuilder.class), eq(user), anyString()))
            .thenReturn(searchResult);

        doThrow(new NotificationClientException("Notification failed"))
            .when(reportNotificationService).send(ArgumentMatchers.<ImmutableList.Builder<String>>any(), anyString());

        reportTask.run();

        verify(ccdSearchService).searchWithQueryAndGroupByStateAndLastStateModifiedDate(any(BoolQueryBuilder.class), eq(user), anyString());
        verify(reportNotificationService).send(ArgumentMatchers.<ImmutableList.Builder<String>>any(), anyString());
    }

    @Test
    void shouldPrepareReportDataSuccessfully() {
        Map<String, Map<String, Long>> data = new HashMap<>();
        Map<String, Long> stateData = new HashMap<>();
        stateData.put("2023-10-24", 5L);
        data.put("Submitted", stateData);

        ImmutableList.Builder<String> result = reportTask.prepareReportData(data, "report.csv");
        assertFalse(result.build().isEmpty());
    }
}
