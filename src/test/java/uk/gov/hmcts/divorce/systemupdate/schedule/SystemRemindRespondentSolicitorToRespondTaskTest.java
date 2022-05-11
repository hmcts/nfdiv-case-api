package uk.gov.hmcts.divorce.systemupdate.schedule;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAos;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemRemindRespondentSolicitor.SYSTEM_REMIND_RESPONDENT_SOLICITOR_TO_RESPOND;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.APPLICANT2_REPRESENTED;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.APPLICATION_TYPE;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.COURT_SERVICE;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.DATA;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.ISSUE_DATE;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.SERVICE_METHOD;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.STATE;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
public class SystemRemindRespondentSolicitorToRespondTaskTest {

    private User user;

    private static final String FLAG = "respondentSolicitorReminderSent";

    private static final BoolQueryBuilder query = boolQuery()
        .must(matchQuery(STATE, AwaitingAos))
        .must(matchQuery(String.format(DATA, APPLICATION_TYPE), SOLE_APPLICATION))
        .must(matchQuery(String.format(DATA, APPLICANT2_REPRESENTED), YesOrNo.YES))
        .must(matchQuery(String.format(DATA, SERVICE_METHOD), COURT_SERVICE))
        .filter(rangeQuery(ISSUE_DATE).lte(LocalDate.now().minusDays(10)))
        .mustNot(matchQuery(String.format(DATA, FLAG), YesOrNo.YES));

    @Mock
    private CcdSearchService ccdSearchService;

    @Mock
    private CcdUpdateService ccdUpdateService;

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private Clock clock;

    @InjectMocks
    private SystemRemindRespondentSolicitorToRespondTask remindRespondentSolicitorToRespondTask;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(remindRespondentSolicitorToRespondTask, "responseReminderOffsetDays", 10);
        setMockClock(clock);

        user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserDetails.builder().build());
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
    }

    @Test
    public void shouldSendReminderEmailToRespondentSolicitor() {
        CaseDetails details1 = CaseDetails.builder()
            .data(Map.of("applicant2SolicitorOrganisationPolicy", OrganisationPolicy.builder().build()))
            .build();

        CaseDetails details2 = CaseDetails.builder()
            .data(Map.of())
            .build();

        List<CaseDetails> caseDetailsList = List.of(details1, details2);

        when(ccdSearchService.searchForAllCasesWithQuery(AwaitingAos, query, user, SERVICE_AUTHORIZATION))
            .thenReturn(caseDetailsList);

        remindRespondentSolicitorToRespondTask.run();

        verify(ccdUpdateService, times(1))
            .submitEvent(details1, SYSTEM_REMIND_RESPONDENT_SOLICITOR_TO_RESPOND, user, SERVICE_AUTHORIZATION);
    }

    @Test
    public void shouldNotSendReminderEmailToRespondentSolicitorWhenNoCasesFound() {

        when(ccdSearchService.searchForAllCasesWithQuery(AwaitingAos, query, user, SERVICE_AUTHORIZATION))
            .thenReturn(null);

        remindRespondentSolicitorToRespondTask.run();

        verifyNoInteractions(ccdUpdateService);
    }
}
