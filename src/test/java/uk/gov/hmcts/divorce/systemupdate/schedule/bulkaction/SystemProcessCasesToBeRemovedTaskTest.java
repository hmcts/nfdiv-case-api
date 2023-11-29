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
import uk.gov.hmcts.divorce.bulkaction.task.UpdateCasesToBeRemovedTask;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.List;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.bulkaction.ccd.event.SystemUpdateCase.SYSTEM_UPDATE_BULK_CASE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getBulkListCaseDetailsListValue;

@ExtendWith(MockitoExtension.class)
public class SystemProcessCasesToBeRemovedTaskTest {

    @Mock
    private CcdSearchService ccdSearchService;

    @Mock
    private CcdUpdateService ccdUpdateService;

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private UpdateCasesToBeRemovedTask updateCasesToBeRemovedTask;

    @InjectMocks
    private SystemProcessCasesToBeRemovedTask systemProcessCasesToBeRemovedTask;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserInfo.builder().build());
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldBulkActionCasesWithCasesToBeRemoved() {

        final var bulkActionCaseData = BulkActionCaseData.builder()
            .casesToBeRemoved(List.of(getBulkListCaseDetailsListValue("1")))
            .build();

        final CaseDetails<BulkActionCaseData, BulkActionState> caseDetails1 = new CaseDetails<>();
        caseDetails1.setId(TEST_CASE_ID);
        caseDetails1.setData(bulkActionCaseData);

        final CaseDetails<BulkActionCaseData, BulkActionState> caseDetails2 = new CaseDetails<>();
        caseDetails2.setId(2L);
        caseDetails2.setData(bulkActionCaseData);

        final List<CaseDetails<BulkActionCaseData, BulkActionState>> caseDetailsList = asList(caseDetails1, caseDetails2);

        when(ccdSearchService.searchForCreatedOrListedBulkCasesWithCasesToBeRemoved(user, SERVICE_AUTHORIZATION))
            .thenReturn(caseDetailsList);

        systemProcessCasesToBeRemovedTask.run();

        verify(ccdUpdateService).submitBulkActionEvent(
            updateCasesToBeRemovedTask,
            TEST_CASE_ID,
            SYSTEM_UPDATE_BULK_CASE,
            user,
            SERVICE_AUTHORIZATION
        );

        verify(ccdUpdateService).submitBulkActionEvent(
            updateCasesToBeRemovedTask,
            2L,
            SYSTEM_UPDATE_BULK_CASE,
            user,
            SERVICE_AUTHORIZATION
        );
    }

    @Test
    void shouldStopProcessingIfCcdSearchCaseExceptionIsThrown() {

        doThrow(new CcdSearchCaseException("Case search error!", null))
            .when(ccdSearchService)
            .searchForCreatedOrListedBulkCasesWithCasesToBeRemoved(user, SERVICE_AUTHORIZATION);

        systemProcessCasesToBeRemovedTask.run();

        verifyNoInteractions(ccdUpdateService);
    }
}
