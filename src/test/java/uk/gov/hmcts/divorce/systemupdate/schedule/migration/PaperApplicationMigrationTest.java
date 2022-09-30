package uk.gov.hmcts.divorce.systemupdate.schedule.migration;

import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.systemupdate.service.CcdConflictException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.List;

import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.cloud.contract.spec.internal.HttpStatus.REQUEST_TIMEOUT;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemMigrateCase.SYSTEM_MIGRATE_CASE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
class PaperApplicationMigrationTest {

    @Mock
    private CcdUpdateService ccdUpdateService;

    @Mock
    private CcdSearchService ccdSearchService;

    @InjectMocks
    private PaperApplicationMigration paperApplicationMigration;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserDetails.builder().build());
    }

    @Test
    void shouldNotSetApplicant2OfflineFieldWhenSearchFailsForJointPaperAppMigration() throws Exception {
        when(ccdSearchService.searchJointPaperApplicationsWhereApplicant2OfflineFlagShouldBeSet(user, SERVICE_AUTHORIZATION))
            .thenThrow(new CcdSearchCaseException("Failed to search cases", mock(FeignException.class)));

        withEnvironmentVariable("ENABLE_PAPER_APPLICATION_MIGRATION", "true")
            .execute(() -> paperApplicationMigration.apply(user, SERVICE_AUTHORIZATION));

        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    void shouldNotSetApplicant2OfflineFieldWhenSearchFailsForSolePaperAppMigration() throws Exception {

        when(ccdSearchService.searchSolePaperApplicationsWhereApplicant2OfflineFlagShouldBeSet(user, SERVICE_AUTHORIZATION))
            .thenThrow(new CcdSearchCaseException("Failed to search cases", mock(FeignException.class)));

        withEnvironmentVariable("ENABLE_PAPER_APPLICATION_MIGRATION", "true")
            .execute(() -> paperApplicationMigration.apply(user, SERVICE_AUTHORIZATION));

        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    void shouldContinueProcessingIfThereIsConflictDuringSubmissionForJointPaperAppMigration() throws Exception {

        final CaseDetails caseDetails1 = mock(CaseDetails.class);
        final CaseDetails caseDetails2 = mock(CaseDetails.class);

        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        when(ccdSearchService.searchJointPaperApplicationsWhereApplicant2OfflineFlagShouldBeSet(user, SERVICE_AUTHORIZATION))
            .thenReturn(caseDetailsList);

        doThrow(new CcdConflictException("Case is modified by another transaction", mock(FeignException.class)))
            .when(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_MIGRATE_CASE, user, SERVICE_AUTHORIZATION);
        doNothing()
            .when(ccdUpdateService).submitEvent(caseDetails2, SYSTEM_MIGRATE_CASE, user, SERVICE_AUTHORIZATION);

        withEnvironmentVariable("ENABLE_PAPER_APPLICATION_MIGRATION", "true")
            .execute(() -> paperApplicationMigration.apply(user, SERVICE_AUTHORIZATION));

        verify(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_MIGRATE_CASE, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService).submitEvent(caseDetails2, SYSTEM_MIGRATE_CASE, user, SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldContinueProcessingIfThereIsConflictDuringSubmissionForSolePaperAppMigration() throws Exception {

        final CaseDetails caseDetails1 = mock(CaseDetails.class);
        final CaseDetails caseDetails2 = mock(CaseDetails.class);

        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        when(ccdSearchService.searchSolePaperApplicationsWhereApplicant2OfflineFlagShouldBeSet(user, SERVICE_AUTHORIZATION))
            .thenReturn(caseDetailsList);

        doThrow(new CcdConflictException("Case is modified by another transaction", mock(FeignException.class)))
            .when(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_MIGRATE_CASE, user, SERVICE_AUTHORIZATION);
        doNothing()
            .when(ccdUpdateService).submitEvent(caseDetails2, SYSTEM_MIGRATE_CASE, user, SERVICE_AUTHORIZATION);

        withEnvironmentVariable("ENABLE_PAPER_APPLICATION_MIGRATION", "true")
            .execute(() -> paperApplicationMigration.apply(user, SERVICE_AUTHORIZATION));

        verify(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_MIGRATE_CASE, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService).submitEvent(caseDetails2, SYSTEM_MIGRATE_CASE, user, SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldContinueToNextCaseIfExceptionIsThrownWhileProcessingPreviousCaseForJointPaperAppMigration() throws Exception {

        final CaseDetails caseDetails1 = mock(CaseDetails.class);
        final CaseDetails caseDetails2 = mock(CaseDetails.class);

        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        when(ccdSearchService.searchJointPaperApplicationsWhereApplicant2OfflineFlagShouldBeSet(user, SERVICE_AUTHORIZATION))
            .thenReturn(caseDetailsList);

        doThrow(new CcdManagementException(REQUEST_TIMEOUT, "Failed processing of case", mock(FeignException.class)))
            .when(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_MIGRATE_CASE, user, SERVICE_AUTHORIZATION);
        doNothing()
            .when(ccdUpdateService).submitEvent(caseDetails2, SYSTEM_MIGRATE_CASE, user, SERVICE_AUTHORIZATION);

        withEnvironmentVariable("ENABLE_PAPER_APPLICATION_MIGRATION", "true")
            .execute(() -> paperApplicationMigration.apply(user, SERVICE_AUTHORIZATION));

        verify(ccdUpdateService).submitEvent(caseDetails2, SYSTEM_MIGRATE_CASE, user, SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldContinueToNextCaseIfExceptionIsThrownWhileProcessingPreviousCaseForSolePaperAppMigration() throws Exception {

        final CaseDetails caseDetails1 = mock(CaseDetails.class);
        final CaseDetails caseDetails2 = mock(CaseDetails.class);

        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        when(ccdSearchService.searchSolePaperApplicationsWhereApplicant2OfflineFlagShouldBeSet(user, SERVICE_AUTHORIZATION))
            .thenReturn(caseDetailsList);

        doThrow(new CcdManagementException(REQUEST_TIMEOUT, "Failed processing of case", mock(FeignException.class)))
            .when(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_MIGRATE_CASE, user, SERVICE_AUTHORIZATION);
        doNothing()
            .when(ccdUpdateService).submitEvent(caseDetails2, SYSTEM_MIGRATE_CASE, user, SERVICE_AUTHORIZATION);

        withEnvironmentVariable("ENABLE_PAPER_APPLICATION_MIGRATION", "true")
            .execute(() -> paperApplicationMigration.apply(user, SERVICE_AUTHORIZATION));

        verify(ccdUpdateService).submitEvent(caseDetails2, SYSTEM_MIGRATE_CASE, user, SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldNotTriggerCcdUpdateServiceWhenEnvVarIsFalse() throws Exception {
        withEnvironmentVariable("ENABLE_PAPER_APPLICATION_MIGRATION", "false")
            .execute(() -> paperApplicationMigration.apply(user, SERVICE_AUTHORIZATION));

        verifyNoInteractions(ccdUpdateService);
    }
}
