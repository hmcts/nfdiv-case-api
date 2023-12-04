package uk.gov.hmcts.divorce.systemupdate.schedule.bulkaction;

import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.divorce.bulkaction.data.BulkCaseRetiredFields;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.schedule.migration.task.MigrateBulkCaseRetiredFields;
import uk.gov.hmcts.divorce.systemupdate.schedule.migration.task.SetFailedBulkCaseMigrationVersionToZero;
import uk.gov.hmcts.divorce.systemupdate.service.CcdConflictException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.HashMap;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.GATEWAY_TIMEOUT;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemMigrateBulkCase.SYSTEM_MIGRATE_BULK_CASE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class SystemMigrateBulkCasesTaskTest {

    @Mock
    private CcdUpdateService ccdUpdateService;

    @Mock
    private CcdSearchService ccdSearchService;

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private MigrateBulkCaseRetiredFields migrateBulkCaseRetiredFields;

    @Mock
    private SetFailedBulkCaseMigrationVersionToZero setFailedBulkCaseMigrationVersionToZero;

    @InjectMocks
    private SystemMigrateBulkCasesTask systemMigrateBulkCasesTask;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserInfo.builder().build());
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldMigrateBulkCase() {
        final CaseDetails caseDetails = mock(CaseDetails.class);

        when(ccdSearchService.searchForBulkCasesWithVersionLessThan(BulkCaseRetiredFields.getVersion(), user, SERVICE_AUTHORIZATION))
            .thenReturn(singletonList(caseDetails));

        systemMigrateBulkCasesTask.run();

        verify(ccdUpdateService)
            .updateBulkCaseWithRetries(
                migrateBulkCaseRetiredFields,
                SYSTEM_MIGRATE_BULK_CASE,
                user,
                SERVICE_AUTHORIZATION,
                caseDetails.getId()
            );
    }

    @Test
    void shouldNotSubmitEventIfSearchFails() {
        when(ccdSearchService.searchForBulkCasesWithVersionLessThan(3, user, SERVICE_AUTHORIZATION))
            .thenThrow(new CcdSearchCaseException("Failed to search cases", mock(FeignException.class)));

        systemMigrateBulkCasesTask.run();

        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    void shouldContinueProcessingIfThereIsConflictDuringSubmission() {
        final CaseDetails caseDetails1 =
            CaseDetails.builder()
                .id(TEST_CASE_ID)
                .data(new HashMap<>())
                .build();

        final CaseDetails caseDetails2 =
            CaseDetails.builder()
                .id(2L)
                .data(new HashMap<>())
                .build();

        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        when(ccdSearchService.searchForBulkCasesWithVersionLessThan(BulkCaseRetiredFields.getVersion(), user, SERVICE_AUTHORIZATION))
            .thenReturn(caseDetailsList);

        doThrow(new CcdConflictException("Case is modified by another transaction", mock(FeignException.class)))
            .when(ccdUpdateService)
            .updateBulkCaseWithRetries(
                migrateBulkCaseRetiredFields,
                SYSTEM_MIGRATE_BULK_CASE,
                user,
                SERVICE_AUTHORIZATION,
                caseDetails1.getId()
            );

        systemMigrateBulkCasesTask.run();

        verify(ccdUpdateService)
            .updateBulkCaseWithRetries(
                migrateBulkCaseRetiredFields,
                SYSTEM_MIGRATE_BULK_CASE,
                user,
                SERVICE_AUTHORIZATION,
                caseDetails1.getId()
            );
        verify(ccdUpdateService)
            .updateBulkCaseWithRetries(
                migrateBulkCaseRetiredFields,
                SYSTEM_MIGRATE_BULK_CASE,
                user,
                SERVICE_AUTHORIZATION,
                caseDetails2.getId()
            );
    }

    @Test
    void shouldContinueToNextCaseIfExceptionIsThrownWhileProcessingPreviousCase() {
        final CaseDetails caseDetails1 =
            CaseDetails.builder()
                .id(TEST_CASE_ID)
                .data(new HashMap<>())
                .build();

        final CaseDetails caseDetails2 =
            CaseDetails.builder()
                .id(2L)
                .data(new HashMap<>())
                .build();

        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        when(ccdSearchService.searchForBulkCasesWithVersionLessThan(BulkCaseRetiredFields.getVersion(), user, SERVICE_AUTHORIZATION))
            .thenReturn(caseDetailsList);

        doThrow(new CcdManagementException(GATEWAY_TIMEOUT.value(), "Failed processing of case", mock(FeignException.class)))
            .doNothing()
            .when(ccdUpdateService)
            .updateBulkCaseWithRetries(
                migrateBulkCaseRetiredFields,
                SYSTEM_MIGRATE_BULK_CASE,
                user,
                SERVICE_AUTHORIZATION,
                caseDetails1.getId()
            );

        systemMigrateBulkCasesTask.run();

        verify(ccdUpdateService)
            .updateBulkCaseWithRetries(
                setFailedBulkCaseMigrationVersionToZero,
                SYSTEM_MIGRATE_BULK_CASE,
                user,
                SERVICE_AUTHORIZATION,
                caseDetails1.getId()
            );

        verify(ccdUpdateService)
            .updateBulkCaseWithRetries(
                migrateBulkCaseRetiredFields,
                SYSTEM_MIGRATE_BULK_CASE,
                user,
                SERVICE_AUTHORIZATION,
                caseDetails2.getId()
            );
    }

    @Test
    void shouldSetDataVersionToZeroIfExceptionIsThrownWhilstSubmittingCcdUpdateEvent() {
        final CaseDetails caseDetails1 =
            CaseDetails.builder()
                .data(new HashMap<>())
                .build();

        final List<CaseDetails> caseDetailsList = List.of(caseDetails1);

        when(ccdSearchService.searchForBulkCasesWithVersionLessThan(BulkCaseRetiredFields.getVersion(), user, SERVICE_AUTHORIZATION))
            .thenReturn(caseDetailsList);

        doThrow(new CcdManagementException(GATEWAY_TIMEOUT.value(), "Failed processing of case", mock(FeignException.class)))
            .doNothing()
            .when(ccdUpdateService)
            .updateBulkCaseWithRetries(
                migrateBulkCaseRetiredFields,
                SYSTEM_MIGRATE_BULK_CASE,
                user,
                SERVICE_AUTHORIZATION,
                caseDetails1.getId()
            );

        systemMigrateBulkCasesTask.run();

        verify(ccdUpdateService)
            .updateBulkCaseWithRetries(
                setFailedBulkCaseMigrationVersionToZero,
                SYSTEM_MIGRATE_BULK_CASE,
                user,
                SERVICE_AUTHORIZATION,
                caseDetails1.getId()
            );
    }

    @Test
    void shouldNotSetDataVersionToZeroIfExceptionIsThrownWhilstSubmittingCcdUpdateEventAndStatusIsNotFound() {
        final CaseDetails caseDetails1 =
            CaseDetails.builder()
                .id(TEST_CASE_ID)
                .data(new HashMap<>())
                .build();

        final List<CaseDetails> caseDetailsList = List.of(caseDetails1);
        final int latestVersion = BulkCaseRetiredFields.getVersion();

        when(ccdSearchService.searchForBulkCasesWithVersionLessThan(latestVersion, user, SERVICE_AUTHORIZATION))
            .thenReturn(caseDetailsList);

        doThrow(new CcdManagementException(NOT_FOUND.value(), "Failed processing of case", mock(FeignException.class)))
            .doNothing()
            .when(ccdUpdateService)
            .updateBulkCaseWithRetries(
                migrateBulkCaseRetiredFields,
                SYSTEM_MIGRATE_BULK_CASE,
                user,
                SERVICE_AUTHORIZATION,
                caseDetails1.getId()
            );

        systemMigrateBulkCasesTask.run();

        verify(ccdUpdateService, times(0))
            .updateBulkCaseWithRetries(
                setFailedBulkCaseMigrationVersionToZero,
                SYSTEM_MIGRATE_BULK_CASE,
                user,
                SERVICE_AUTHORIZATION,
                caseDetails1.getId()
            );
    }
}
