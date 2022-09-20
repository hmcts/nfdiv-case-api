package uk.gov.hmcts.divorce.systemupdate.schedule.migration;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
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
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.cloud.contract.spec.internal.HttpStatus.NOT_FOUND;
import static org.springframework.cloud.contract.spec.internal.HttpStatus.REQUEST_TIMEOUT;
import static uk.gov.hmcts.divorce.divorcecase.model.RetiredFields.getVersion;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemMigrateCase.SYSTEM_MIGRATE_CASE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
class BaseMigrationTest {

    @Mock
    private CcdUpdateService ccdUpdateService;

    @Mock
    private CcdSearchService ccdSearchService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private BaseMigration baseMigration;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserDetails.builder().build());
    }

    @Test
    void shouldNotSubmitEventIfSearchFailsForBaseMigration() {
        when(ccdSearchService.searchForCasesWithVersionLessThan(getVersion(), user, SERVICE_AUTHORIZATION))
            .thenThrow(new CcdSearchCaseException("Failed to search cases", mock(FeignException.class)));

        baseMigration.apply(user, SERVICE_AUTHORIZATION);

        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    void shouldContinueProcessingIfThereIsConflictDuringSubmissionForBaseMigration() {
        final CaseDetails caseDetails1 = mock(CaseDetails.class);
        final CaseDetails caseDetails2 = mock(CaseDetails.class);
        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        when(ccdSearchService.searchForCasesWithVersionLessThan(getVersion(), user, SERVICE_AUTHORIZATION))
            .thenReturn(caseDetailsList);

        doThrow(new CcdConflictException("Case is modified by another transaction", mock(FeignException.class)))
            .when(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_MIGRATE_CASE, user, SERVICE_AUTHORIZATION);
        doNothing()
            .when(ccdUpdateService).submitEvent(caseDetails2, SYSTEM_MIGRATE_CASE, user, SERVICE_AUTHORIZATION);

        baseMigration.apply(user, SERVICE_AUTHORIZATION);

        verify(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_MIGRATE_CASE, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService).submitEvent(caseDetails2, SYSTEM_MIGRATE_CASE, user, SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldContinueToNextCaseIfExceptionIsThrownWhileProcessingPreviousCaseForBaseMigration() {
        final CaseDetails caseDetails1 = mock(CaseDetails.class);
        final CaseDetails caseDetails2 = mock(CaseDetails.class);
        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        when(ccdSearchService.searchForCasesWithVersionLessThan(getVersion(), user, SERVICE_AUTHORIZATION))
            .thenReturn(caseDetailsList);

        doThrow(new CcdManagementException(REQUEST_TIMEOUT, "Failed processing of case", mock(FeignException.class)))
            .doNothing()
            .when(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_MIGRATE_CASE, user, SERVICE_AUTHORIZATION);
        doNothing()
            .when(ccdUpdateService).submitEvent(caseDetails2, SYSTEM_MIGRATE_CASE, user, SERVICE_AUTHORIZATION);

        baseMigration.apply(user, SERVICE_AUTHORIZATION);

        verify(ccdUpdateService, times(2)).submitEvent(caseDetails1, SYSTEM_MIGRATE_CASE, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService).submitEvent(caseDetails2, SYSTEM_MIGRATE_CASE, user, SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldSetDataVersionToZeroIfExceptionIsThrownWhileDeserializingCaseForBaseMigration() {
        final CaseDetails caseDetails1 =
            CaseDetails.builder()
                .data(new HashMap<>())
                .build();

        final List<CaseDetails> caseDetailsList = List.of(caseDetails1);

        when(ccdSearchService.searchForCasesWithVersionLessThan(getVersion(), user, SERVICE_AUTHORIZATION))
            .thenReturn(caseDetailsList);

        doNothing()
            .when(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_MIGRATE_CASE, user, SERVICE_AUTHORIZATION);

        when(objectMapper.convertValue(eq(caseDetails1.getData()), eq(CaseData.class)))
            .thenThrow(new IllegalArgumentException("Failed to deserialize"));

        baseMigration.apply(user, SERVICE_AUTHORIZATION);

        assertThat(caseDetails1.getData()).isEqualTo(Map.of("dataVersion", 0));
    }

    @Test
    void shouldSetDataVersionToZeroIfExceptionIsThrownWhilstSubmittingCcdUpdateEventForBaseMigration() {
        final CaseDetails caseDetails1 =
            CaseDetails.builder()
                .data(new HashMap<>())
                .build();

        final List<CaseDetails> caseDetailsList = List.of(caseDetails1);

        when(ccdSearchService.searchForCasesWithVersionLessThan(getVersion(), user, SERVICE_AUTHORIZATION))
            .thenReturn(caseDetailsList);

        doThrow(new CcdManagementException(REQUEST_TIMEOUT, "Failed processing of case", mock(FeignException.class)))
            .doNothing()
            .when(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_MIGRATE_CASE, user, SERVICE_AUTHORIZATION);

        baseMigration.apply(user, SERVICE_AUTHORIZATION);

        assertThat(caseDetails1.getData()).isEqualTo(Map.of("dataVersion", 0));
    }

    @Test
    void shouldNotSetDataVersionToZeroIfExceptionIsThrownWhilstSubmittingCcdUpdateEventAndStatusIsNotFoundForBaseMigration() {
        final CaseDetails caseDetails1 =
            CaseDetails.builder()
                .data(new HashMap<>())
                .build();

        final List<CaseDetails> caseDetailsList = List.of(caseDetails1);
        final int latestVersion = getVersion();

        when(ccdSearchService.searchForCasesWithVersionLessThan(latestVersion, user, SERVICE_AUTHORIZATION))
            .thenReturn(caseDetailsList);

        doThrow(new CcdManagementException(NOT_FOUND, "Failed processing of case", mock(FeignException.class)))
            .doNothing()
            .when(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_MIGRATE_CASE, user, SERVICE_AUTHORIZATION);

        baseMigration.apply(user, SERVICE_AUTHORIZATION);

        assertThat(caseDetails1.getData()).isEqualTo(Map.of("dataVersion", latestVersion));
    }
}