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
import uk.gov.hmcts.divorce.bulkaction.service.CasePronouncementService;
import uk.gov.hmcts.divorce.bulkaction.task.BulkCaseCaseTaskFactory;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.List;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState.Pronounced;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class SystemProcessFailedPronouncedCasesTaskTest {

    @Mock
    private CcdSearchService ccdSearchService;

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private BulkCaseCaseTaskFactory bulkCaseCaseTaskFactory;

    @Mock
    private CasePronouncementService casePronouncementService;

    @InjectMocks
    private SystemProcessFailedPronouncedCasesTask systemCreateBulkCaseListTask;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserInfo.builder().build());
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldProcessPronouncedBulkActionCases() {

        final var bulkActionCaseData = BulkActionCaseData.builder().build();

        final CaseDetails<BulkActionCaseData, BulkActionState> caseDetails1 = new CaseDetails<>();
        caseDetails1.setId(TEST_CASE_ID);
        caseDetails1.setData(bulkActionCaseData);

        final CaseDetails<BulkActionCaseData, BulkActionState> caseDetails2 = new CaseDetails<>();
        caseDetails2.setId(2L);
        caseDetails2.setData(bulkActionCaseData);

        final List<CaseDetails<BulkActionCaseData, BulkActionState>> caseDetailsList = asList(caseDetails1, caseDetails2);

        when(ccdSearchService.searchForUnprocessedOrErroredBulkCases(Pronounced, user, SERVICE_AUTHORIZATION))
            .thenReturn(caseDetailsList);

        systemCreateBulkCaseListTask.run();

        verify(casePronouncementService).systemPronounceCases(caseDetails1);
        verify(casePronouncementService).systemPronounceCases(caseDetails2);
    }

    @Test
    void shouldStopProcessingIfCcdSearchCaseExceptionIsThrown() {

        doThrow(new CcdSearchCaseException("message", null))
            .when(ccdSearchService)
            .searchForUnprocessedOrErroredBulkCases(Pronounced, user, SERVICE_AUTHORIZATION);

        systemCreateBulkCaseListTask.run();

        verifyNoInteractions(casePronouncementService);
    }
}
