package uk.gov.hmcts.divorce.systemupdate.schedule;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.caseworker.service.CaseFlagsService;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.existsQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Applicant2Approved;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Archived;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingApplicant1Response;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingApplicant2Response;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Draft;
import static uk.gov.hmcts.divorce.divorcecase.model.State.FinalOrderComplete;
import static uk.gov.hmcts.divorce.divorcecase.model.State.NewPaperCase;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Rejected;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Withdrawn;
import static uk.gov.hmcts.divorce.systemupdate.schedule.SystemAddHmctsServiceIdTask.SERVICE_ID_FIELD;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.STATE;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.SUPPLEMENTARY_DATA;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
public class SystemAddHmctsServiceIdTaskTest {

    @Mock
    private CaseFlagsService caseFlagsService;

    @Mock
    private CcdSearchService ccdSearchService;

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private ObjectMapper mapper;

    @InjectMocks
    private SystemAddHmctsServiceIdTask systemAddHmctsServiceIdTask;

    private User user;

    private static final BoolQueryBuilder query =
        boolQuery()
            .mustNot(matchQuery(STATE, Draft))
            .mustNot(matchQuery(STATE, AwaitingApplicant1Response))
            .mustNot(matchQuery(STATE, AwaitingApplicant2Response))
            .mustNot(matchQuery(STATE, Applicant2Approved))
            .mustNot(matchQuery(STATE, AwaitingPayment))
            .mustNot(matchQuery(STATE, Withdrawn))
            .mustNot(matchQuery(STATE, Archived))
            .mustNot(matchQuery(STATE, Rejected))
            .mustNot(matchQuery(STATE, NewPaperCase))
            .mustNot(matchQuery(STATE, FinalOrderComplete))
            .mustNot(existsQuery(String.format(SUPPLEMENTARY_DATA, SERVICE_ID_FIELD)));

    @BeforeEach
    void setUp() {
        user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserInfo.builder().build());
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldCallCaseFlagServiceWhenCaseIsInValidState() {
        final CaseDetails caseDetails1 = mock(CaseDetails.class);

        when(caseDetails1.getId()).thenReturn(TEST_CASE_ID);

        final List<CaseDetails> caseDetailsList = List.of(caseDetails1);

        when(ccdSearchService.searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION,
            Draft, AwaitingApplicant1Response, AwaitingApplicant2Response, Applicant2Approved,
            AwaitingPayment, Withdrawn, Archived, Rejected, NewPaperCase, FinalOrderComplete))
            .thenReturn(caseDetailsList);

        systemAddHmctsServiceIdTask.run();

        verify(caseFlagsService).setSupplementaryDataForCaseFlags(TEST_CASE_ID);
    }
}
