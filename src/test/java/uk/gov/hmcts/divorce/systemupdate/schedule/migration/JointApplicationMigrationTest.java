package uk.gov.hmcts.divorce.systemupdate.schedule.migration;

import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.systemupdate.schedule.migration.task.RemoveAccessCode;
import uk.gov.hmcts.divorce.systemupdate.service.CcdConflictException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.HashMap;
import java.util.List;

import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.GATEWAY_TIMEOUT;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemMigrateCase.SYSTEM_MIGRATE_CASE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class JointApplicationMigrationTest {

    @Mock
    private CcdUpdateService ccdUpdateService;

    @Mock
    private CcdSearchService ccdSearchService;

    @Mock
    private RemoveAccessCode removeAccessCode;

    @InjectMocks
    private JointApplicationMigration jointApplicationMigration;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserDetails.builder().build());
    }

    @Test
    void shouldNotRemoveAccessCodeWhenSearchFailsForJointAppMigration() throws Exception {

        when(ccdSearchService.searchJointApplicationsWithAccessCodePostIssueApplication(user, SERVICE_AUTHORIZATION))
            .thenThrow(new CcdSearchCaseException("Failed to search cases", mock(FeignException.class)));

        withEnvironmentVariable("ENABLE_JOINT_APPLICATION_MIGRATION", "true")
            .execute(() -> jointApplicationMigration.apply(user, SERVICE_AUTHORIZATION));

        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    void shouldContinueProcessingIfThereIsConflictDuringSubmissionForJointAppMigration() throws Exception {

        final CaseDetails caseDetails1 = CaseDetails.builder().data(new HashMap<>()).id(TEST_CASE_ID).build();
        final CaseDetails caseDetails2 = CaseDetails.builder().data(new HashMap<>()).id(1616591401473379L).build();

        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        when(ccdSearchService.searchJointApplicationsWithAccessCodePostIssueApplication(user, SERVICE_AUTHORIZATION))
            .thenReturn(caseDetailsList);

        doThrow(new CcdConflictException("Case is modified by another transaction", mock(FeignException.class)))
            .when(ccdUpdateService).submitEventWithRetry(
                caseDetails1.getId().toString(),
                SYSTEM_MIGRATE_CASE,
                removeAccessCode,
                user,
                SERVICE_AUTHORIZATION
            );

        doNothing()
            .when(ccdUpdateService).submitEventWithRetry(
                caseDetails2.getId().toString(),
                SYSTEM_MIGRATE_CASE,
                removeAccessCode,
                user,
                SERVICE_AUTHORIZATION
            );

        withEnvironmentVariable("ENABLE_JOINT_APPLICATION_MIGRATION", "true")
            .execute(() -> jointApplicationMigration.apply(user, SERVICE_AUTHORIZATION));

        verify(ccdUpdateService).submitEventWithRetry(
            caseDetails1.getId().toString(),
            SYSTEM_MIGRATE_CASE,
            removeAccessCode,
            user,
            SERVICE_AUTHORIZATION
        );

        verify(ccdUpdateService).submitEventWithRetry(
            caseDetails2.getId().toString(),
            SYSTEM_MIGRATE_CASE,
            removeAccessCode,
            user,
            SERVICE_AUTHORIZATION
        );
    }

    @Test
    void shouldContinueToNextCaseIfExceptionIsThrownWhileProcessingPreviousCaseForJointAppMigration() throws Exception {

        final CaseDetails caseDetails1 = CaseDetails.builder().data(new HashMap<>()).id(TEST_CASE_ID).build();
        final CaseDetails caseDetails2 = CaseDetails.builder().data(new HashMap<>()).id(1616591401473379L).build();

        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        when(ccdSearchService.searchJointApplicationsWithAccessCodePostIssueApplication(user, SERVICE_AUTHORIZATION))
            .thenReturn(caseDetailsList);

        doThrow(new CcdManagementException(GATEWAY_TIMEOUT.value(), "Failed processing of case", mock(FeignException.class)))
            .when(ccdUpdateService).submitEventWithRetry(
                caseDetails1.getId().toString(),
                SYSTEM_MIGRATE_CASE,
                removeAccessCode,
                user,
                SERVICE_AUTHORIZATION
            );

        doNothing()
            .when(ccdUpdateService).submitEventWithRetry(
                caseDetails2.getId().toString(),
                SYSTEM_MIGRATE_CASE,
                removeAccessCode,
                user,
                SERVICE_AUTHORIZATION
            );


        withEnvironmentVariable("ENABLE_JOINT_APPLICATION_MIGRATION", "true")
            .execute(() -> jointApplicationMigration.apply(user, SERVICE_AUTHORIZATION));

        verify(ccdUpdateService).submitEventWithRetry(
            caseDetails2.getId().toString(),
            SYSTEM_MIGRATE_CASE,
            removeAccessCode,
            user,
            SERVICE_AUTHORIZATION
        );
    }

    @Test
    void shouldNotRemoveAccessCodeWhenMigrationDisabled() throws Exception {
        withEnvironmentVariable("ENABLE_JOINT_APPLICATION_MIGRATION", "false")
            .execute(() -> jointApplicationMigration.apply(user, SERVICE_AUTHORIZATION));

        verifyNoInteractions(ccdUpdateService);
    }
}
