package uk.gov.hmcts.divorce.systemupdate.schedule.migration;

import feign.FeignException;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import uk.gov.hmcts.divorce.systemupdate.schedule.migration.predicate.HasAosDraftedEventPredicate;
import uk.gov.hmcts.divorce.systemupdate.schedule.migration.task.UpdateAosIsDrafted;
import uk.gov.hmcts.divorce.systemupdate.service.CcdConflictException;
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

import static java.lang.Boolean.TRUE;
import static java.util.Collections.emptyList;
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
    private Logger logger;

    @Mock
    private CcdSearchService ccdSearchService;

    @Mock
    private CcdUpdateService ccdUpdateService;

    @Mock
    private HasAosDraftedEventPredicate hasAosDraftedEventPredicate;

    @Mock
    private UpdateAosIsDrafted updateAosIsDrafted;

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

        verify(logger)
            .error("Case schedule task(SetAosIsDraftedToYesMigration) stopped after search error", exception);
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
            .when(ccdUpdateService).submitEventWithRetry(
                caseDetails1.getId().toString(),
                SYSTEM_MIGRATE_CASE,
                updateAosIsDrafted,
                user,
                SERVICE_AUTHORIZATION
            );

        doNothing()
            .when(ccdUpdateService).submitEventWithRetry(
                caseDetails2.getId().toString(),
                SYSTEM_MIGRATE_CASE,
                updateAosIsDrafted,
                user,
                SERVICE_AUTHORIZATION
            );

        setAosIsDraftedToYesMigration.apply(user, SERVICE_AUTHORIZATION);

        verify(ccdUpdateService).submitEventWithRetry(
            caseDetails1.getId().toString(),
            SYSTEM_MIGRATE_CASE,
            updateAosIsDrafted,
            user,
            SERVICE_AUTHORIZATION
        );

        verify(ccdUpdateService).submitEventWithRetry(
            caseDetails2.getId().toString(),
            SYSTEM_MIGRATE_CASE,
            updateAosIsDrafted,
            user,
            SERVICE_AUTHORIZATION
        );

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

        doThrow(new CcdConflictException("Case is modified by another transaction", mock(FeignException.class)))
            .when(ccdUpdateService).submitEventWithRetry(
                caseDetails1.getId().toString(),
                SYSTEM_MIGRATE_CASE,
                updateAosIsDrafted,
                user,
                SERVICE_AUTHORIZATION
            );

        doNothing()
            .when(ccdUpdateService).submitEventWithRetry(
                caseDetails2.getId().toString(),
                SYSTEM_MIGRATE_CASE,
                updateAosIsDrafted,
                user,
                SERVICE_AUTHORIZATION
            );


        setAosIsDraftedToYesMigration.apply(user, SERVICE_AUTHORIZATION);

        verify(ccdUpdateService).submitEventWithRetry(
            caseDetails1.getId().toString(),
            SYSTEM_MIGRATE_CASE,
            updateAosIsDrafted,
            user,
            SERVICE_AUTHORIZATION
        );

        verify(ccdUpdateService).submitEventWithRetry(
            caseDetails2.getId().toString(),
            SYSTEM_MIGRATE_CASE,
            updateAosIsDrafted,
            user,
            SERVICE_AUTHORIZATION
        );

        verifyNoMoreInteractions(ccdSearchService);
    }

    @Test
    void shouldSkipProcessingIfEnvironmentSwitchIsSetToFalse() {

        setField(setAosIsDraftedToYesMigration, "migrateAosIsDrafted", false);

        setAosIsDraftedToYesMigration.apply(user, SERVICE_AUTHORIZATION);

        verify(logger).info("Skipping SetAosIsDraftedToYesMigration, MIGRATE_AOS_IS_DRAFTED={}, references size: {}", false, 1);
        verifyNoInteractions(ccdSearchService, ccdUpdateService, hasAosDraftedEventPredicate);
    }

    @Test
    void shouldSkipProcessingIfEnvironmentReferencesIsEmpty() {

        final List<Long> references = new ArrayList<>();
        references.add(null);

        setField(setAosIsDraftedToYesMigration, "migrateAosIsDrafted", true);
        setField(setAosIsDraftedToYesMigration, "aosIsDraftedReferences", references);

        setAosIsDraftedToYesMigration.apply(user, SERVICE_AUTHORIZATION);

        verify(logger).info("Skipping SetAosIsDraftedToYesMigration, MIGRATE_AOS_IS_DRAFTED={}, references size: {}", true, 0);
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
