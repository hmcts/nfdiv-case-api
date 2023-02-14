package uk.gov.hmcts.divorce.systemupdate.schedule.migration;

import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionCaseTypeConfig;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class CaseCourtHearingMigrationTest {

    @Mock
    private Logger logger;

    @Mock
    private CoreCaseDataApi coreCaseDataApi;
    @Mock
    private SetCaseCourtHearingBulkAction setCaseCourtHearingBulkAction;

    @InjectMocks
    private CaseCourtHearingMigration caseCourtHearingMigration;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserDetails.builder().build());
        setField(caseCourtHearingMigration, "caseCourtHearingReferences", List.of(TEST_CASE_ID));
    }

    @Test
    void shouldMigrateSelectedBulkCases() {

        setField(caseCourtHearingMigration, "migrateCaseCourtHearing", true);

        final CaseDetails caseDetails1 = CaseDetails.builder()
            .id(1L)
            .data(new HashMap<>())
            .build();

        final CaseDetails caseDetails2 = CaseDetails.builder()
            .id(2L)
            .data(new HashMap<>())
            .build();

        final List<CaseDetails> searchResponse = List.of(caseDetails1, caseDetails2);

        final SearchResult searchResult = SearchResult.builder()
            .cases(searchResponse)
            .build();

        when(coreCaseDataApi
            .searchCases(
                user.getAuthToken(),
                SERVICE_AUTHORIZATION,
                BulkActionCaseTypeConfig.CASE_TYPE,
                getSearchSource()))
            .thenReturn(searchResult);

        caseCourtHearingMigration.apply(user, SERVICE_AUTHORIZATION);

        verify(setCaseCourtHearingBulkAction).setCaseCourtHearing(caseDetails1, user, SERVICE_AUTHORIZATION);
        verify(setCaseCourtHearingBulkAction).setCaseCourtHearing(caseDetails2, user, SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldSkipProcessingIfEnvironmentSwitchIsSetToFalse() {

        setField(caseCourtHearingMigration, "migrateCaseCourtHearing", false);

        caseCourtHearingMigration.apply(user, SERVICE_AUTHORIZATION);

        verify(logger).info("Skipping CaseCourtHearingMigration, MIGRATE_CASE_COURT_HEARING={}, references size: {}", false, 1);
        verifyNoInteractions(coreCaseDataApi, setCaseCourtHearingBulkAction);
    }

    @Test
    void shouldSkipProcessingIfEnvironmentReferencesIsEmpty() {

        final List<Long> references = new ArrayList<>();
        references.add(null);

        setField(caseCourtHearingMigration, "migrateCaseCourtHearing", true);
        setField(caseCourtHearingMigration, "caseCourtHearingReferences", references);

        caseCourtHearingMigration.apply(user, SERVICE_AUTHORIZATION);

        verify(logger).info("Skipping CaseCourtHearingMigration, MIGRATE_CASE_COURT_HEARING={}, references size: {}", true, 0);
        verifyNoInteractions(coreCaseDataApi, setCaseCourtHearingBulkAction);
    }

    private String getSearchSource() {
        return SearchSourceBuilder
            .searchSource()
            .query(boolQuery().must(boolQuery().should(matchQuery("reference", TEST_CASE_ID))))
            .from(0)
            .size(100)
            .toString();
    }
}