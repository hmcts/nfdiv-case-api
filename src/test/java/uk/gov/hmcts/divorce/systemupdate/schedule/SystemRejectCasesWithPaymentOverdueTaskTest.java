package uk.gov.hmcts.divorce.systemupdate.schedule;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.SupplementaryCaseType;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.service.CcdConflictException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPayment;
import static uk.gov.hmcts.divorce.systemupdate.event.ApplicationRejectedFeeNotPaid.APPLICATION_REJECTED_FEE_NOT_PAID;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.DATA;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.STATE;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.SUPPLEMENTARY_CASE_TYPE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class SystemRejectCasesWithPaymentOverdueTaskTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().findAndRegisterModules();
    private static final String LAST_MODIFIED = "last_modified";
    private static final String NEW_PAPER_CASE = "newPaperCase";

    private BoolQueryBuilder query;
    private User user;

    @Mock
    private CcdUpdateService ccdUpdateService;

    @Mock
    private CcdSearchService ccdSearchService;

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private SystemRejectCasesWithPaymentOverdueTask task;

    @BeforeEach
    void setUp() {
        final BoolQueryBuilder paperOrJudicialSeparationCases = boolQuery()
            .should(matchQuery(String.format(DATA, SUPPLEMENTARY_CASE_TYPE), "judicialSeparation"))
            .should(matchQuery(String.format(DATA, SUPPLEMENTARY_CASE_TYPE), "separation"))
            .should(matchQuery(String.format(DATA, NEW_PAPER_CASE), "Yes"))
            .minimumShouldMatch(1);

        final MatchQueryBuilder awaitingPaymentQuery = matchQuery(STATE, AwaitingPayment);

        query = boolQuery()
            .should(
                boolQuery()
                    .must(awaitingPaymentQuery)
                    .mustNot(paperOrJudicialSeparationCases)
                    .filter(rangeQuery(LAST_MODIFIED).lte(LocalDate.now().minusDays(14)))
            )
            .should(
                boolQuery()
                    .must(awaitingPaymentQuery)
                    .must(paperOrJudicialSeparationCases)
                    .filter(rangeQuery(LAST_MODIFIED).lte(LocalDate.now().minusDays(17)))
            )
            .minimumShouldMatch(1);

        user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserInfo.builder().build());
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldSearchAndFindCasesThatAreAwaitingPaymentAndLastModifiedDateIsMoreThan14DaysInPastForNonJudicialSeparationAndPaperCases() {

        final List<CaseDetails> matchingCases = List.of(
            CaseDetails.builder().id(1L)
                .state("AwaitingPayment")
                .lastModified(LocalDateTime.now().minusDays(18))
                .build(),
            CaseDetails.builder().id(2L)
                .state("AwaitingPayment")
                .lastModified(LocalDateTime.now().minusDays(18))
                .build()
        );

        when(ccdSearchService.searchForAllCasesWithQuery(
            query, user, SERVICE_AUTHORIZATION, AwaitingPayment)).thenReturn(matchingCases);

        task.run();

        verify(ccdUpdateService).submitEvent(1L, APPLICATION_REJECTED_FEE_NOT_PAID, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService).submitEvent(2L, APPLICATION_REJECTED_FEE_NOT_PAID, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService,never()).submitEvent(3L, APPLICATION_REJECTED_FEE_NOT_PAID, user, SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldNotSubmitEventIfSearchFailsWithCCDSearchCaseException() {
        doThrow(CcdSearchCaseException.class).when(ccdSearchService).searchForAllCasesWithQuery(
                query, user, SERVICE_AUTHORIZATION, AwaitingPayment);

        task.run();

        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    void shouldNotSubmitEventIfSearchFailsWithCCDConflictException() {
        doThrow(CcdConflictException.class).when(ccdSearchService).searchForAllCasesWithQuery(
            query, user, SERVICE_AUTHORIZATION, AwaitingPayment);

        task.run();

        verifyNoInteractions(ccdUpdateService);
    }
}
