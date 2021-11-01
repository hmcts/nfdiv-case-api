package uk.gov.hmcts.divorce.systemupdate.schedule.bulkaction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.service.ScheduleCaseService;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.divorce.systemupdate.service.ErroredBulkCasesService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.List;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState.Pronounced;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemPronounceCase.SYSTEM_PRONOUNCE_CASE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
class SystemRetryBulkCasePronouncedErrorsTest {

    @Mock
    private CcdSearchService ccdSearchService;

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private ScheduleCaseService scheduleCaseService;

    @Mock
    private ErroredBulkCasesService erroredBulkCasesService;

    @InjectMocks
    private SystemRetryBulkCasePronouncedErrors systemCreateBulkCaseListTask;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserDetails.builder().build());
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldProcessPronouncedBulkActionCases() {

        final var bulkActionCaseData = BulkActionCaseData.builder().build();

        final CaseDetails<BulkActionCaseData, BulkActionState> caseDetails1 = new CaseDetails<>();
        caseDetails1.setId(1L);
        caseDetails1.setData(bulkActionCaseData);

        final CaseDetails<BulkActionCaseData, BulkActionState> caseDetails2 = new CaseDetails<>();
        caseDetails2.setId(2L);
        caseDetails2.setData(bulkActionCaseData);

        final List<CaseDetails<BulkActionCaseData, BulkActionState>> caseDetailsList = asList(caseDetails1, caseDetails2);

        when(ccdSearchService.searchForUnprocessedOrErroredBulkCases(Pronounced, user, SERVICE_AUTHORIZATION))
            .thenReturn(caseDetailsList);

        final CaseTask caseTask = mock(CaseTask.class);
        when(scheduleCaseService.getCaseTask(bulkActionCaseData, SYSTEM_PRONOUNCE_CASE)).thenReturn(caseTask);

        systemCreateBulkCaseListTask.run();

        verify(erroredBulkCasesService)
            .processErroredCasesAndUpdateBulkCase(
                caseDetails1,
                SYSTEM_PRONOUNCE_CASE,
                caseTask,
                user,
                SERVICE_AUTHORIZATION);
        verify(erroredBulkCasesService)
            .processErroredCasesAndUpdateBulkCase(
                caseDetails2,
                SYSTEM_PRONOUNCE_CASE,
                caseTask,
                user,
                SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldStopProcessingIfCcdSearchCaseExceptionIsThrown() {

        doThrow(new CcdSearchCaseException("message", null))
            .when(ccdSearchService)
            .searchForUnprocessedOrErroredBulkCases(Pronounced, user, SERVICE_AUTHORIZATION);

        systemCreateBulkCaseListTask.run();

        verifyNoInteractions(erroredBulkCasesService);
    }
}