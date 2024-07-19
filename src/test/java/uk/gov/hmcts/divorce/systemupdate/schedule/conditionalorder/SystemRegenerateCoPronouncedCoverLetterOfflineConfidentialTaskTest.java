package uk.gov.hmcts.divorce.systemupdate.schedule.conditionalorder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.service.CcdConflictException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.io.IOException;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;


@ExtendWith(MockitoExtension.class)
class SystemRegenerateCoPronouncedCoverLetterOfflineConfidentialTaskTest {

    @Mock
    private CcdSearchService ccdSearchService;

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private SystemRedoPronouncedCoverLettersTask oldTask;

    @InjectMocks
    private SystemRegenerateCoPronouncedCoverLetterOfflineConfidentialTask task;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserInfo.builder().build());
    }

    @Test
    void run_SearchCaseException_LogsError() throws CcdConflictException, IOException {
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn("token");
        when(oldTask.loadCaseIds()).thenReturn(new ArrayList<>());

        doThrow(new CcdSearchCaseException("Search error", null)).when(ccdSearchService)
            .searchForAllCasesWithQuery(any(), any(), any());
        task.run();
        // Verify that the logger's error method is called with the expected message
        verify(oldTask)
            .logError(eq("SystemRegenerateCoPronouncedCoverLetterOfflineConfidentialTask stopped after search error"), isNull(),
                isA(CcdSearchCaseException.class));
    }

    @Test
    void run_IOException_LogsError() throws CcdSearchCaseException, CcdConflictException, IOException {

        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn("token");
        when(oldTask.loadCaseIds()).thenThrow(new IOException("IO error"));
        task.run();

        // Verify that the IO error is logged
        verify(oldTask)
            .logError(eq("SystemRegenerateCoPronouncedCoverLetterOfflineConfidentialTask stopped after file read error"), isNull(),
                isA(IOException.class));
    }
}

