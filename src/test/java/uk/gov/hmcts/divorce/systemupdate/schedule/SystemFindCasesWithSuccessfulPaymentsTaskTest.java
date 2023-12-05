package uk.gov.hmcts.divorce.systemupdate.schedule;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.payment.PaymentStatusService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPayment;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.STATE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
class SystemFindCasesWithSuccessfulPaymentsTaskTest {

    private static final String LAST_MODIFIED = "last_modified";

    @Mock
    private CcdSearchService ccdSearchService;

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private PaymentStatusService paymentStatusService;


    @InjectMocks
    private SystemFindCasesWithSuccessfulPaymentsTask systemFindCasesWithSuccessfulPaymentsTask;

    private User user;
    final BoolQueryBuilder query = boolQuery()
        .filter(matchQuery(STATE, AwaitingPayment))
        .filter(rangeQuery(LAST_MODIFIED)
            .gte(LocalDate.now().minusWeeks(2)));

    @BeforeEach
    void setUp() {
        user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserInfo.builder().build());
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldQueryPaymentApi() {
        CaseDetails caseDetails = CaseDetails.builder().data(Map.of("applicationPayments","SomeObject")).build();
        final List<CaseDetails> caseDetailsList = List.of(caseDetails);
        when(ccdSearchService.searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION, AwaitingPayment))
            .thenReturn(caseDetailsList);

        systemFindCasesWithSuccessfulPaymentsTask.run();
        verify(paymentStatusService).hasSuccessFulPayment(same(caseDetailsList));
    }
}
