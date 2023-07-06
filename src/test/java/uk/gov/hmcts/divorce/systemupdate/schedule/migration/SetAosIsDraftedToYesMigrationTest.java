package uk.gov.hmcts.divorce.systemupdate.schedule.migration;

import feign.FeignException;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.systemupdate.schedule.migration.predicate.HasAosDraftedEventPredicate;
import uk.gov.hmcts.divorce.systemupdate.service.CcdConflictException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.existsQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.GATEWAY_TIMEOUT;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemMigrateCase.SYSTEM_MIGRATE_CASE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class SetAosIsDraftedToYesMigrationTest {

    @Mock
    private CcdSearchService ccdSearchService;

    @Mock
    private CcdUpdateService ccdUpdateService;

    @Mock
    private HasAosDraftedEventPredicate hasAosDraftedEventPredicate;

    @InjectMocks
    private SetAosIsDraftedToYesMigration setAosIsDraftedToYesMigration;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserDetails.builder().build());
        setField(setAosIsDraftedToYesMigration, "aosIsDraftedReferences", List.of(TEST_CASE_ID));
        setField(setAosIsDraftedToYesMigration, "aosIsDraftedReferences2", emptyList());
        setField(setAosIsDraftedToYesMigration, "aosIsDraftedReferences3", emptyList());
        setField(setAosIsDraftedToYesMigration, "aosIsDraftedReferences4", emptyList());
        setField(setAosIsDraftedToYesMigration, "aosIsDraftedReferences5", emptyList());
    }

    @Test
    void shouldSetAosIsDraftedToYesToSelectedCases() {

        setField(setAosIsDraftedToYesMigration, "migrateAosIsDrafted", true);

        final AtomicInteger predicateIndex = new AtomicInteger(0);
        final List<Boolean> predicateValues = List.of(TRUE, FALSE, TRUE);

        final CaseDetails caseDetails1 = CaseDetails.builder()
            .id(1L)
            .data(new HashMap<>())
            .build();

        final CaseDetails caseDetails2 = CaseDetails.builder()
            .id(2L)
            .data(new HashMap<>())
            .build();

        final CaseDetails caseDetails3 = CaseDetails.builder()
            .id(3L)
            .data(new HashMap<>())
            .build();

        final List<CaseDetails> searchResponse = List.of(caseDetails1, caseDetails2, caseDetails3);

        when(ccdSearchService
            .searchForAllCasesWithQuery(
                getQuery(TEST_CASE_ID),
                user,
                SERVICE_AUTHORIZATION))
            .thenReturn(searchResponse);

        when(hasAosDraftedEventPredicate.hasAosDraftedEvent(user, SERVICE_AUTHORIZATION))
            .thenReturn(caseDetail -> predicateValues.get(predicateIndex.getAndAdd(1)));

        setAosIsDraftedToYesMigration.apply(user, SERVICE_AUTHORIZATION);

        assertThat(caseDetails1.getData().get("aosIsDrafted")).isEqualTo("Yes");
        assertThat(caseDetails2.getData().get("aosIsDrafted")).isNull();
        assertThat(caseDetails3.getData().get("aosIsDrafted")).isEqualTo("Yes");

        verify(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_MIGRATE_CASE, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService).submitEvent(caseDetails3, SYSTEM_MIGRATE_CASE, user, SERVICE_AUTHORIZATION);
        verifyNoMoreInteractions(ccdSearchService);
    }

    @Test
    void shouldSetAosIsDraftedToYesForMultipleCombinedDistinctReferences() {

        final long reference2 = TEST_CASE_ID + 1L;
        final long reference3 = TEST_CASE_ID + 2L;
        final long reference4 = TEST_CASE_ID + 3L;
        setField(setAosIsDraftedToYesMigration, "migrateAosIsDrafted", true);
        setField(setAosIsDraftedToYesMigration, "aosIsDraftedReferences2", List.of(reference2));
        setField(setAosIsDraftedToYesMigration, "aosIsDraftedReferences3", List.of(reference3));
        setField(setAosIsDraftedToYesMigration, "aosIsDraftedReferences4", List.of(reference4));
        setField(setAosIsDraftedToYesMigration, "aosIsDraftedReferences5", List.of(reference4));

        final CaseDetails caseDetails1 = CaseDetails.builder()
            .id(1L)
            .data(new HashMap<>())
            .build();

        final List<CaseDetails> searchResponse1 = List.of(caseDetails1);

        when(ccdSearchService
            .searchForAllCasesWithQuery(
                getQuery(TEST_CASE_ID, reference2, reference3, reference4),
                user,
                SERVICE_AUTHORIZATION))
            .thenReturn(searchResponse1);

        when(hasAosDraftedEventPredicate.hasAosDraftedEvent(user, SERVICE_AUTHORIZATION)).thenReturn(caseDetail -> true);

        setAosIsDraftedToYesMigration.apply(user, SERVICE_AUTHORIZATION);

        assertThat(caseDetails1.getData()).containsEntry("aosIsDrafted", "Yes");

        verify(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_MIGRATE_CASE, user, SERVICE_AUTHORIZATION);
        verifyNoMoreInteractions(ccdSearchService);
    }

    @Test
    void shouldDoNothingAndLogErrorIfSearchFails() {

        setField(setAosIsDraftedToYesMigration, "migrateAosIsDrafted", true);

        final CcdSearchCaseException exception =
            new CcdSearchCaseException("Failed to search cases", mock(FeignException.class));
        when(ccdSearchService
            .searchForAllCasesWithQuery(
                getQuery(TEST_CASE_ID),
                user,
                SERVICE_AUTHORIZATION))
            .thenThrow(exception);

        setAosIsDraftedToYesMigration.apply(user, SERVICE_AUTHORIZATION);

        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    void shouldContinueProcessingIfThereIsConflictDuringSubmission() {

        setField(setAosIsDraftedToYesMigration, "migrateAosIsDrafted", true);

        final AtomicInteger predicateIndex = new AtomicInteger(0);
        final List<Boolean> predicateValues = List.of(TRUE, TRUE);

        final CaseDetails caseDetails1 = CaseDetails.builder()
            .id(1L)
            .data(new HashMap<>())
            .build();

        final CaseDetails caseDetails2 = CaseDetails.builder()
            .id(2L)
            .data(new HashMap<>())
            .build();

        final List<CaseDetails> searchResponse = List.of(caseDetails1, caseDetails2);

        when(ccdSearchService
            .searchForAllCasesWithQuery(
                getQuery(TEST_CASE_ID),
                user,
                SERVICE_AUTHORIZATION))
            .thenReturn(searchResponse);

        when(hasAosDraftedEventPredicate.hasAosDraftedEvent(user, SERVICE_AUTHORIZATION))
            .thenReturn(caseDetail -> predicateValues.get(predicateIndex.getAndAdd(1)));

        doThrow(new CcdConflictException("Case is modified by another transaction", mock(FeignException.class)))
            .when(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_MIGRATE_CASE, user, SERVICE_AUTHORIZATION);
        doNothing()
            .when(ccdUpdateService).submitEvent(caseDetails2, SYSTEM_MIGRATE_CASE, user, SERVICE_AUTHORIZATION);

        setAosIsDraftedToYesMigration.apply(user, SERVICE_AUTHORIZATION);

        verify(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_MIGRATE_CASE, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService).submitEvent(caseDetails2, SYSTEM_MIGRATE_CASE, user, SERVICE_AUTHORIZATION);
        verifyNoMoreInteractions(ccdSearchService);
    }

    @Test
    void shouldContinueToNextCaseIfExceptionIsThrownWhileProcessingPreviousCase() {

        setField(setAosIsDraftedToYesMigration, "migrateAosIsDrafted", true);

        final AtomicInteger predicateIndex = new AtomicInteger(0);
        final List<Boolean> predicateValues = List.of(TRUE, TRUE);

        final CaseDetails caseDetails1 = CaseDetails.builder()
            .id(1L)
            .data(new HashMap<>())
            .build();

        final CaseDetails caseDetails2 = CaseDetails.builder()
            .id(2L)
            .data(new HashMap<>())
            .build();

        final List<CaseDetails> searchResponse = List.of(caseDetails1, caseDetails2);

        when(ccdSearchService
            .searchForAllCasesWithQuery(
                getQuery(TEST_CASE_ID),
                user,
                SERVICE_AUTHORIZATION))
            .thenReturn(searchResponse);

        when(hasAosDraftedEventPredicate.hasAosDraftedEvent(user, SERVICE_AUTHORIZATION))
            .thenReturn(caseDetail -> predicateValues.get(predicateIndex.getAndAdd(1)));

        doThrow(new CcdManagementException(GATEWAY_TIMEOUT.value(), "Failed processing of case", mock(FeignException.class)))
            .when(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_MIGRATE_CASE, user, SERVICE_AUTHORIZATION);
        doNothing()
            .when(ccdUpdateService).submitEvent(caseDetails2, SYSTEM_MIGRATE_CASE, user, SERVICE_AUTHORIZATION);

        setAosIsDraftedToYesMigration.apply(user, SERVICE_AUTHORIZATION);

        verify(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_MIGRATE_CASE, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService).submitEvent(caseDetails2, SYSTEM_MIGRATE_CASE, user, SERVICE_AUTHORIZATION);
        verifyNoMoreInteractions(ccdSearchService);
    }

    @Test
    void shouldSkipProcessingIfEnvironmentSwitchIsSetToFalse() {

        setField(setAosIsDraftedToYesMigration, "migrateAosIsDrafted", false);

        setAosIsDraftedToYesMigration.apply(user, SERVICE_AUTHORIZATION);

        verifyNoInteractions(ccdSearchService, ccdUpdateService, hasAosDraftedEventPredicate);
    }

    @Test
    void shouldSkipProcessingIfEnvironmentReferencesIsEmpty() {

        final List<Long> references = new ArrayList<>();
        references.add(null);

        setField(setAosIsDraftedToYesMigration, "migrateAosIsDrafted", true);
        setField(setAosIsDraftedToYesMigration, "aosIsDraftedReferences", references);

        setAosIsDraftedToYesMigration.apply(user, SERVICE_AUTHORIZATION);

        verifyNoInteractions(ccdSearchService, ccdUpdateService, hasAosDraftedEventPredicate);
    }

    private BoolQueryBuilder getQuery(final Long... references) {

        final BoolQueryBuilder referenceQuery = boolQuery();
        Arrays.stream(references).forEach(reference -> referenceQuery.should(matchQuery("reference", reference)));

        return boolQuery()
            .must(referenceQuery)
            .mustNot(existsQuery("data.dateAosSubmitted"))
            .mustNot(existsQuery("data.aosIsDrafted"));
    }
}
