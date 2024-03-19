package uk.gov.hmcts.divorce.systemupdate.schedule.conditionalorder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.service.CcdConflictException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;


@ExtendWith(MockitoExtension.class)
class SystemRedoPronouncedCoverLettersTaskTest {

    @Mock
    private CcdSearchService ccdSearchService;

    @Mock
    private CcdUpdateService ccdUpdateService;

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private SystemRedoPronouncedCoverLettersTask task;

    @Mock
    private Logger logger;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserInfo.builder().build());
    }

    private void injectDependencies(SystemRedoPronouncedCoverLettersTask thisTask) {
        setField(thisTask, "ccdSearchService", ccdSearchService);
        setField(thisTask, "ccdUpdateService", ccdUpdateService);
        setField(thisTask, "idamService", idamService);
        setField(thisTask, "authTokenGenerator", authTokenGenerator);
    }

    private void setField(Object targetObject, String fieldName, Object value) {
        try {
            Field field = targetObject.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(targetObject, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set field: " + fieldName, e);
        }
    }


    private List<CaseDetails> createCaseDetailsList(final int size) {

        final List<CaseDetails> caseDetails = new ArrayList<>();

        for (int index = 0; index < size; index++) {
            caseDetails.add(mock(CaseDetails.class));
        }

        return caseDetails;
    }

    @Test
    void run_ProcessesUpTo50Cases() {
        // Mock data
        List<CaseDetails> casesToBeUpdated = createCaseDetailsList(51);
        when(ccdSearchService.searchForAllCasesWithQuery(any(), any(), any(), any(), any())).thenReturn(casesToBeUpdated);

        // Call the method under test
        task.run();

        // Verify that only up to 50 cases are processed
        verify(ccdUpdateService, times(50)).submitEvent(any(), any(), any(), any()); // Expect 3 calls to submitEvent
    }

    @Test
    void run_SearchCaseException_LogsError() throws CcdConflictException {
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn("token");

        doThrow(new CcdSearchCaseException("Search error", null)).when(ccdSearchService)
            .searchForAllCasesWithQuery(any(), any(), any(), any(), any());
        task.run();
        // Verify that the logger's error method is called with the expected message
        verify(logger)
            .error(eq("SystemRedoPronouncedCoverLettersTask stopped after search error"), isNull(),
                isA(CcdSearchCaseException.class));
    }

    @Test
    void run_IOException_LogsError() throws CcdSearchCaseException, CcdConflictException, IOException {

        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn("token");

        // Create an instance of your class
        SystemRedoPronouncedCoverLettersTask taskInstance = spy(new SystemRedoPronouncedCoverLettersTask());
        //using spy so that we can mock the loadCaseIds() method but need to injectDependencies on taskInstance then
        injectDependencies(taskInstance);
        // Mock the behavior of loadCaseIds()
        doThrow(new IOException("IO error")).when(taskInstance).loadCaseIds();

        // Call the method that uses loadCaseIds()
        taskInstance.run();

        // Verify that the IO error is logged
        verify(logger).error(eq("SystemRedoPronouncedCoverLettersTask stopped after file read error"), isNull(),
            isA(IOException.class));
    }

    @Test
    void testReadIdsFromFile() throws IOException {
        // Create an instance of your class
        SystemRedoPronouncedCoverLettersTask taskInstance = new SystemRedoPronouncedCoverLettersTask();

        // Call the method to read IDs from the file
        List<Long> idList = taskInstance.loadCaseIds();

        // Validate that the list of IDs is not empty
        assertNotNull(idList);
        assertFalse(idList.isEmpty());
    }

}

