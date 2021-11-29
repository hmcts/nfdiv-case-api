package uk.gov.hmcts.divorce.systemupdate.schedule;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDate;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderPronounced;
import static uk.gov.hmcts.divorce.systemupdate.schedule.SystemProgressCasesToAwaitingFinalOrderTask.DATA_DATE_FINAL_ORDER_ELIGIBLE_FROM;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.STATE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
public class SystemProgressCasesToAwaitingFinalOrderTaskTest {

    @Mock
    private CcdUpdateService ccdUpdateService;

    @Mock
    private CcdSearchService ccdSearchService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private SystemProgressCasesToAwaitingFinalOrderTask task;

    private User user;

    private static final BoolQueryBuilder query = boolQuery()
            .must(matchQuery(STATE, ConditionalOrderPronounced))
            .filter(rangeQuery(DATA_DATE_FINAL_ORDER_ELIGIBLE_FROM).lte(LocalDate.now()));

    @BeforeEach
    void setUp() {
        user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserDetails.builder().build());
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
    }
}
