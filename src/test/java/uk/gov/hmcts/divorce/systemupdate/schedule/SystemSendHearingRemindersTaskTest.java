package uk.gov.hmcts.divorce.systemupdate.schedule;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDate;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.State.PendingHearingOutcome;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemSendHearingReminder.SYSTEM_SEND_HEARING_REMINDER;
import static uk.gov.hmcts.divorce.systemupdate.schedule.SystemSendHearingRemindersTask.DATE_OF_HEARING_CASE_FIELD;
import static uk.gov.hmcts.divorce.systemupdate.schedule.SystemSendHearingRemindersTask.EARLIEST_REMINDER_DAYS_BEFORE_HEARING;
import static uk.gov.hmcts.divorce.systemupdate.schedule.SystemSendHearingRemindersTask.HEARING_REMINDER_NOTIFICATION_FLAG;
import static uk.gov.hmcts.divorce.systemupdate.schedule.SystemSendHearingRemindersTask.LATEST_REMINDER_DAYS_BEFORE_HEARING;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.DATA;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.STATE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class SystemSendHearingRemindersTaskTest {

    @Mock
    private CcdSearchService ccdSearchService;

    @Mock
    private CcdUpdateService ccdUpdateService;

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private SystemSendHearingRemindersTask sendHearingRemindersTask;

    private User user;

    private static final BoolQueryBuilder query =
        boolQuery()
            .must(matchQuery(STATE, PendingHearingOutcome))
            .filter(rangeQuery(DATE_OF_HEARING_CASE_FIELD)
                .gte(LocalDate.now().plusDays(LATEST_REMINDER_DAYS_BEFORE_HEARING))
                .lte(LocalDate.now().plusDays(EARLIEST_REMINDER_DAYS_BEFORE_HEARING))
            )
            .mustNot(matchQuery(String.format(DATA, HEARING_REMINDER_NOTIFICATION_FLAG), YesOrNo.YES));

    @BeforeEach
    void setUp() {
        user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserInfo.builder().build());
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldTriggerHearingReminderForCasesReturnedByElasticSearch() {
        final CaseDetails caseDetails = CaseDetails.builder().id(TEST_CASE_ID).build();

        final List<CaseDetails> caseDetailsList = List.of(caseDetails);

        when(ccdSearchService.searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION, PendingHearingOutcome))
            .thenReturn(caseDetailsList);

        sendHearingRemindersTask.run();

        verify(ccdUpdateService).submitEvent(TEST_CASE_ID, SYSTEM_SEND_HEARING_REMINDER, user, SERVICE_AUTHORIZATION);
    }
}
