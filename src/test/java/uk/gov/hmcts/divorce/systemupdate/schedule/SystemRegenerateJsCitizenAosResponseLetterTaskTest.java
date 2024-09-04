package uk.gov.hmcts.divorce.systemupdate.schedule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.service.CcdConflictException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.systemupdate.schedule.SystemRegenerateJsCitizenAosResponseLetterTask.CASE_ID_CSV;
import static uk.gov.hmcts.divorce.systemupdate.schedule.SystemRegenerateJsCitizenAosResponseLetterTask.CONFLICT_ERROR;
import static uk.gov.hmcts.divorce.systemupdate.schedule.SystemRegenerateJsCitizenAosResponseLetterTask.DESERIALIZATION_ERROR;
import static uk.gov.hmcts.divorce.systemupdate.schedule.SystemRegenerateJsCitizenAosResponseLetterTask.FILE_READ_ERROR;
import static uk.gov.hmcts.divorce.systemupdate.schedule.SystemRegenerateJsCitizenAosResponseLetterTask.SEARCH_ERROR;
import static uk.gov.hmcts.divorce.systemupdate.schedule.SystemRegenerateJsCitizenAosResponseLetterTask.SUBMIT_EVENT_ERROR;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;


@ExtendWith(MockitoExtension.class)
class SystemRegenerateJsCitizenAosResponseLetterTaskTest {

    @Mock
    private CcdSearchService ccdSearchService;

    @Mock
    private CcdUpdateService ccdUpdateService;

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private TaskHelper taskHelper;

    @InjectMocks
    private SystemRegenerateJsCitizenAosResponseLetterTask systemRegenerateJsCitizenAosResponseLetterTask;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserInfo.builder().build());
    }

    private List<CaseDetails> createCaseDetailsList(final int size) {

        final List<CaseDetails> caseDetails = new ArrayList<>();

        for (int index = 0; index < size; index++) {
            final var mockCaseDetails = mock(CaseDetails.class);
            mockCaseDetails.setId(TEST_CASE_ID);
            caseDetails.add(mockCaseDetails);
        }

        return caseDetails;
    }

    @Test
    void run_ProcessesCases() {
        // Mock data
        List<CaseDetails> casesToBeUpdated = createCaseDetailsList(10);
        when(ccdSearchService.searchForAllCasesWithQuery(any(), any(), any())).thenReturn(casesToBeUpdated);

        // Call the method under test
        systemRegenerateJsCitizenAosResponseLetterTask.run();

        // Verify that all cases are processed
        verify(ccdUpdateService, times(10)).submitEvent(any(), any(), any(), any());
    }

    @Test
    void run_SearchCaseException_LogsError() throws CcdConflictException, IOException {
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn("token");
        when(taskHelper.loadCaseIds(CASE_ID_CSV)).thenReturn(new ArrayList<>());

        doThrow(new CcdSearchCaseException(SEARCH_ERROR, null)).when(ccdSearchService)
            .searchForAllCasesWithQuery(any(), any(), any());
        systemRegenerateJsCitizenAosResponseLetterTask.run();
        // Verify that the logger's error method is called with the expected message
        verify(taskHelper).logError(eq(SEARCH_ERROR), isNull(), isA(CcdSearchCaseException.class));
    }

    @Test
    void run_conflictException_LogsError() throws CcdConflictException, IOException {
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn("token");
        when(taskHelper.loadCaseIds(CASE_ID_CSV)).thenReturn(new ArrayList<>());

        doThrow(new CcdConflictException(CONFLICT_ERROR, null)).when(ccdSearchService)
            .searchForAllCasesWithQuery(any(), any(), any());
        systemRegenerateJsCitizenAosResponseLetterTask.run();
        // Verify that the logger's error method is called with the expected message
        verify(taskHelper).logError(eq(CONFLICT_ERROR), isNull(), isA(CcdConflictException.class));
    }

    @Test
    void run_IOException_LogsError() throws CcdSearchCaseException, CcdConflictException, IOException {

        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn("token");
        when(taskHelper.loadCaseIds(CASE_ID_CSV)).thenThrow(new IOException("IO error"));
        systemRegenerateJsCitizenAosResponseLetterTask.run();

        // Verify that the IO error is logged
        verify(taskHelper).logError(eq(FILE_READ_ERROR), isNull(), isA(IOException.class));
    }

    @Test
    void run_CCDManagementException_LogsError() throws CcdManagementException {
        CaseDetails caseDetails = CaseDetails.builder().id(TEST_CASE_ID).build();

        doThrow(new CcdManagementException(500, null, null)).when(ccdUpdateService)
            .submitEvent(any(), any(), any(), any());
        systemRegenerateJsCitizenAosResponseLetterTask
            .triggerRegenJsCitizenAosResponseCoverLetterForEligibleCases(user, "token", caseDetails);
        // Verify that the logger's error method is called with the expected message
        verify(taskHelper).logError(eq(SUBMIT_EVENT_ERROR), eq(TEST_CASE_ID), isA(CcdManagementException.class));
    }

    @Test
    void run_IllegalArgumentException_LogsError() throws CcdManagementException {
        CaseDetails caseDetails = CaseDetails.builder().id(TEST_CASE_ID).build();

        doThrow(new IllegalArgumentException()).when(ccdUpdateService)
            .submitEvent(any(), any(), any(), any());
        systemRegenerateJsCitizenAosResponseLetterTask
            .triggerRegenJsCitizenAosResponseCoverLetterForEligibleCases(user, "token", caseDetails);
        // Verify that the logger's error method is called with the expected message
        verify(taskHelper).logError(eq(DESERIALIZATION_ERROR), eq(TEST_CASE_ID), isA(IllegalArgumentException.class));
    }
}

