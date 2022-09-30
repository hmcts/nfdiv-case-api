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
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.cloud.contract.spec.internal.HttpStatus.REQUEST_TIMEOUT;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AosDrafted;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AosOverdue;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAos;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingBailiffReferral;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingDocuments;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingGeneralConsideration;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingGeneralReferralPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingService;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingServiceConsideration;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingServicePayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.GeneralApplicationReceived;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Holding;
import static uk.gov.hmcts.divorce.divorcecase.model.State.IssuedToBailiff;
import static uk.gov.hmcts.divorce.divorcecase.model.State.OfflineDocumentReceived;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemMigrateCase.SYSTEM_MIGRATE_CASE;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.STATE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;

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

    @InjectMocks
    private SetAosIsDraftedToYesMigration setAosIsDraftedToYesMigration;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserDetails.builder().build());
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
                getQuery(),
                user,
                SERVICE_AUTHORIZATION,
                AosDrafted,
                AosOverdue,
                OfflineDocumentReceived,
                AwaitingAos,
                GeneralApplicationReceived,
                AwaitingGeneralReferralPayment,
                Holding,
                AwaitingDocuments,
                AwaitingBailiffReferral,
                AwaitingServicePayment,
                AwaitingServiceConsideration,
                IssuedToBailiff,
                AwaitingService,
                AwaitingGeneralConsideration))
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
    void shouldDoNothingAndLogErrorIfSearchFails() {

        setField(setAosIsDraftedToYesMigration, "migrateAosIsDrafted", true);

        final CcdSearchCaseException exception =
            new CcdSearchCaseException("Failed to search cases", mock(FeignException.class));
        when(ccdSearchService
            .searchForAllCasesWithQuery(
                getQuery(),
                user,
                SERVICE_AUTHORIZATION,
                AosDrafted,
                AosOverdue,
                OfflineDocumentReceived,
                AwaitingAos,
                GeneralApplicationReceived,
                AwaitingGeneralReferralPayment,
                Holding,
                AwaitingDocuments,
                AwaitingBailiffReferral,
                AwaitingServicePayment,
                AwaitingServiceConsideration,
                IssuedToBailiff,
                AwaitingService,
                AwaitingGeneralConsideration))
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
                getQuery(),
                user,
                SERVICE_AUTHORIZATION,
                AosDrafted,
                AosOverdue,
                OfflineDocumentReceived,
                AwaitingAos,
                GeneralApplicationReceived,
                AwaitingGeneralReferralPayment,
                Holding,
                AwaitingDocuments,
                AwaitingBailiffReferral,
                AwaitingServicePayment,
                AwaitingServiceConsideration,
                IssuedToBailiff,
                AwaitingService,
                AwaitingGeneralConsideration))
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
                getQuery(),
                user,
                SERVICE_AUTHORIZATION,
                AosDrafted,
                AosOverdue,
                OfflineDocumentReceived,
                AwaitingAos,
                GeneralApplicationReceived,
                AwaitingGeneralReferralPayment,
                Holding,
                AwaitingDocuments,
                AwaitingBailiffReferral,
                AwaitingServicePayment,
                AwaitingServiceConsideration,
                IssuedToBailiff,
                AwaitingService,
                AwaitingGeneralConsideration))
            .thenReturn(searchResponse);

        when(hasAosDraftedEventPredicate.hasAosDraftedEvent(user, SERVICE_AUTHORIZATION))
            .thenReturn(caseDetail -> predicateValues.get(predicateIndex.getAndAdd(1)));

        doThrow(new CcdManagementException(REQUEST_TIMEOUT, "Failed processing of case", mock(FeignException.class)))
            .when(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_MIGRATE_CASE, user, SERVICE_AUTHORIZATION);
        doNothing()
            .when(ccdUpdateService).submitEvent(caseDetails2, SYSTEM_MIGRATE_CASE, user, SERVICE_AUTHORIZATION);

        setAosIsDraftedToYesMigration.apply(user, SERVICE_AUTHORIZATION);

        verify(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_MIGRATE_CASE, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService).submitEvent(caseDetails2, SYSTEM_MIGRATE_CASE, user, SERVICE_AUTHORIZATION);
        verifyNoMoreInteractions(ccdSearchService);
    }

    @Test
    void shouldSkipProcessingIfEnvironmentVariableIsSetToFalse() {

        setField(setAosIsDraftedToYesMigration, "migrateAosIsDrafted", false);

        setAosIsDraftedToYesMigration.apply(user, SERVICE_AUTHORIZATION);

        verify(logger).info("Skipping SetAosIsDraftedToYesMigration, MIGRATE_AOS_IS_DRAFTED=false");
        verifyNoInteractions(ccdSearchService, ccdUpdateService, hasAosDraftedEventPredicate);
    }

    private BoolQueryBuilder getQuery() {
        final BoolQueryBuilder query =
            boolQuery()
                .must(
                    boolQuery()
                        .should(matchQuery(STATE, AosDrafted))
                        .should(matchQuery(STATE, AosOverdue))
                        .should(matchQuery(STATE, OfflineDocumentReceived))
                        .should(matchQuery(STATE, AwaitingAos))
                        .should(matchQuery(STATE, GeneralApplicationReceived))
                        .should(matchQuery(STATE, AwaitingGeneralReferralPayment))
                        .should(matchQuery(STATE, Holding))
                        .should(matchQuery(STATE, AwaitingDocuments))
                        .should(matchQuery(STATE, AwaitingBailiffReferral))
                        .should(matchQuery(STATE, AwaitingServicePayment))
                        .should(matchQuery(STATE, AwaitingServiceConsideration))
                        .should(matchQuery(STATE, IssuedToBailiff))
                        .should(matchQuery(STATE, AwaitingService))
                        .should(matchQuery(STATE, AwaitingGeneralConsideration)))
                .mustNot(matchQuery("data.aosIsDrafted", YES));
        return query;
    }
}