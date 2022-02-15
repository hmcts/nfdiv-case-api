package uk.gov.hmcts.divorce.systemupdate.schedule.bulkaction;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.data.BulkCaseRetiredFields;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdConflictException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.cloud.contract.spec.internal.HttpStatus.NOT_FOUND;
import static org.springframework.cloud.contract.spec.internal.HttpStatus.REQUEST_TIMEOUT;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemMigrateBulkCase.SYSTEM_MIGRATE_BULK_CASE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class SystemMigrateBulkCasesTaskTest {

    @Mock
    private CcdUpdateService ccdUpdateService;

    @Mock
    private CcdSearchService ccdSearchService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private SystemMigrateBulkCasesTask systemMigrateBulkCasesTask;

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserDetails.builder().build());
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
            .updateBulkCaseWithRetries(caseDetails, SYSTEM_MIGRATE_BULK_CASE, user, SERVICE_AUTHORIZATION, caseDetails.getId());
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
                .id(1L)
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
            .updateBulkCaseWithRetries(caseDetails1, SYSTEM_MIGRATE_BULK_CASE, user, SERVICE_AUTHORIZATION, caseDetails1.getId());

        systemMigrateBulkCasesTask.run();

        verify(ccdUpdateService)
            .updateBulkCaseWithRetries(caseDetails1, SYSTEM_MIGRATE_BULK_CASE, user, SERVICE_AUTHORIZATION, caseDetails1.getId());
        verify(ccdUpdateService)
            .updateBulkCaseWithRetries(caseDetails2, SYSTEM_MIGRATE_BULK_CASE, user, SERVICE_AUTHORIZATION, caseDetails2.getId());
    }

    @Test
    void shouldContinueToNextCaseIfExceptionIsThrownWhileProcessingPreviousCase() {
        final CaseDetails caseDetails1 =
            CaseDetails.builder()
                .id(1L)
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

        doThrow(new CcdManagementException(REQUEST_TIMEOUT, "Failed processing of case", mock(FeignException.class)))
            .doNothing()
            .when(ccdUpdateService)
            .updateBulkCaseWithRetries(caseDetails1, SYSTEM_MIGRATE_BULK_CASE, user, SERVICE_AUTHORIZATION, caseDetails1.getId());

        systemMigrateBulkCasesTask.run();

        verify(ccdUpdateService, times(2))
            .updateBulkCaseWithRetries(caseDetails1, SYSTEM_MIGRATE_BULK_CASE, user, SERVICE_AUTHORIZATION, caseDetails1.getId());
        verify(ccdUpdateService)
            .updateBulkCaseWithRetries(caseDetails2, SYSTEM_MIGRATE_BULK_CASE, user, SERVICE_AUTHORIZATION, caseDetails2.getId());
    }

    @Test
    void shouldSetDataVersionToZeroIfExceptionIsThrownWhileDeserializingCase() {
        final CaseDetails caseDetails1 =
            CaseDetails.builder()
                .data(new HashMap<>())
                .build();
        final CaseDetails caseDetails2 = mock(CaseDetails.class);

        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        when(ccdSearchService.searchForBulkCasesWithVersionLessThan(BulkCaseRetiredFields.getVersion(), user, SERVICE_AUTHORIZATION))
            .thenReturn(caseDetailsList);

        when(objectMapper.convertValue(eq(caseDetails1.getData()), eq(BulkActionCaseData.class)))
            .thenThrow(new IllegalArgumentException("Failed to deserialize"));

        systemMigrateBulkCasesTask.run();

        assertThat(caseDetails1.getData()).isEqualTo(Map.of("bulkCaseDataVersion", 0));
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

        doThrow(new CcdManagementException(REQUEST_TIMEOUT, "Failed processing of case", mock(FeignException.class)))
            .doNothing()
            .when(ccdUpdateService)
            .updateBulkCaseWithRetries(caseDetails1, SYSTEM_MIGRATE_BULK_CASE, user, SERVICE_AUTHORIZATION, caseDetails1.getId());

        systemMigrateBulkCasesTask.run();

        assertThat(caseDetails1.getData()).isEqualTo(Map.of("bulkCaseDataVersion", 0));
    }

    @Test
    void shouldNotSetDataVersionToZeroIfExceptionIsThrownWhilstSubmittingCcdUpdateEventAndStatusIsNotFound() {
        final CaseDetails caseDetails1 =
            CaseDetails.builder()
                .data(new HashMap<>())
                .build();

        final List<CaseDetails> caseDetailsList = List.of(caseDetails1);
        final int latestVersion = BulkCaseRetiredFields.getVersion();

        when(ccdSearchService.searchForBulkCasesWithVersionLessThan(latestVersion, user, SERVICE_AUTHORIZATION))
            .thenReturn(caseDetailsList);

        doThrow(new CcdManagementException(NOT_FOUND, "Failed processing of case", mock(FeignException.class)))
            .doNothing()
            .when(ccdUpdateService)
            .updateBulkCaseWithRetries(caseDetails1, SYSTEM_MIGRATE_BULK_CASE, user, SERVICE_AUTHORIZATION, caseDetails1.getId());

        systemMigrateBulkCasesTask.run();

        assertThat(caseDetails1.getData()).isEqualTo(Map.of("bulkCaseDataVersion", latestVersion));
    }
}
