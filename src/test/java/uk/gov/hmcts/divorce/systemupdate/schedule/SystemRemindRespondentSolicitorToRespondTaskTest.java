package uk.gov.hmcts.divorce.systemupdate.schedule;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.ccd.sdk.type.Organisation;
import uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.existsQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.GATEWAY_TIMEOUT;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAos;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemRemindRespondentSolicitor.SYSTEM_REMIND_RESPONDENT_SOLICITOR_TO_RESPOND;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.APPLICANT2_REPRESENTED;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.APPLICANT2_SOL_EMAIL;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.APPLICANT2_SOL_ORG_POLICY;
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
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
public class SystemRemindRespondentSolicitorToRespondTaskTest {

    private User user;

    private static final String FLAG = "respondentSolicitorReminderSent";

    private static final BoolQueryBuilder query = boolQuery()
        .must(matchQuery(STATE, AwaitingAos))
        .must(matchQuery(String.format(DATA, APPLICATION_TYPE), SOLE_APPLICATION))
        .must(matchQuery(String.format(DATA, APPLICANT2_REPRESENTED), YesOrNo.YES))
        .must(existsQuery(String.format(DATA, APPLICANT2_SOL_EMAIL)))
        .must(existsQuery(String.format(DATA, APPLICANT2_SOL_ORG_POLICY)))
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

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private SystemRemindRespondentSolicitorToRespondTask remindRespondentSolicitorToRespondTask;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(remindRespondentSolicitorToRespondTask, "responseReminderOffsetDays", 10);
        setMockClock(clock);

        user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserInfo.builder().build());
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
    }

    @Test
    public void shouldSendReminderEmailToRespondentSolicitor() {

        CaseDetails details1 = CaseDetails.builder()
            .data(Map.of("applicant2SolicitorOrganisationPolicy", organisationPolicy(),
                "applicant2SolicitorEmail", "abc@gm.com"))
            .id(TEST_CASE_ID)
            .build();

        List<CaseDetails> caseDetailsList = List.of(details1);

        when(ccdSearchService.searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION, AwaitingAos))
            .thenReturn(caseDetailsList);

        mockObjectMapper();

        remindRespondentSolicitorToRespondTask.run();

        verify(ccdUpdateService, times(1))
            .submitEvent(TEST_CASE_ID, SYSTEM_REMIND_RESPONDENT_SOLICITOR_TO_RESPOND, user, SERVICE_AUTHORIZATION);
    }

    @Test
    public void shouldNotSendReminderEmailToRespondentSolicitorWhenNoCasesFound() {

        when(ccdSearchService.searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION, AwaitingAos))
            .thenReturn(null);

        remindRespondentSolicitorToRespondTask.run();

        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    public void shouldNotSendReminderEmailToRespondentSolicitorWhenCcdSearchCaseExceptionIsThrown() {

        doThrow(new CcdSearchCaseException("Failed elastic search", mock(FeignException.class)))
            .when(ccdSearchService).searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION, AwaitingAos);

        remindRespondentSolicitorToRespondTask.run();

        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    void shouldContinueToNextCaseIfCcdManagementExceptionIsThrownWhileProcessingPreviousCase() {
        CaseDetails details1 = CaseDetails.builder()
            .id(TEST_CASE_ID)
            .data(Map.of("applicant2SolicitorOrganisationPolicy", organisationPolicy(),
                "applicant2SolicitorEmail", "abc@gm.com"))
            .build();

        CaseDetails details2 = CaseDetails.builder()
            .id(2L)
            .data(Map.of("applicant2SolicitorOrganisationPolicy", organisationPolicy(),
                "applicant2SolicitorEmail", "xyz@gm.com"))
            .build();

        List<CaseDetails> caseDetailsList = List.of(details1, details2);

        mockObjectMapper();

        when(ccdSearchService.searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION, AwaitingAos))
            .thenReturn(caseDetailsList);
        doThrow(new CcdManagementException(GATEWAY_TIMEOUT.value(), "Failed processing of case", mock(FeignException.class)))
            .when(ccdUpdateService).submitEvent(TEST_CASE_ID, SYSTEM_REMIND_RESPONDENT_SOLICITOR_TO_RESPOND, user, SERVICE_AUTHORIZATION);

        remindRespondentSolicitorToRespondTask.run();

        verify(ccdUpdateService).submitEvent(TEST_CASE_ID, SYSTEM_REMIND_RESPONDENT_SOLICITOR_TO_RESPOND, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService).submitEvent(2L, SYSTEM_REMIND_RESPONDENT_SOLICITOR_TO_RESPOND, user, SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldContinueToNextCaseIfIllegalArgumentExceptionIsThrownWhileProcessingPreviousCase() {
        CaseDetails details1 = CaseDetails.builder()
            .id(TEST_CASE_ID)
            .data(Map.of("applicant2SolicitorOrganisationPolicy", organisationPolicy(),
                "applicant2SolicitorEmail", "abc@gm.com"))
            .build();

        CaseDetails details2 = CaseDetails.builder()
            .id(2L)
            .data(Map.of("applicant2SolicitorOrganisationPolicy", organisationPolicy(),
                "applicant2SolicitorEmail", "xyz@gm.com"))
            .build();

        List<CaseDetails> caseDetailsList = List.of(details1, details2);

        mockObjectMapper();

        when(ccdSearchService.searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION, AwaitingAos))
            .thenReturn(caseDetailsList);

        doThrow(new IllegalArgumentException())
            .when(ccdUpdateService).submitEvent(TEST_CASE_ID, SYSTEM_REMIND_RESPONDENT_SOLICITOR_TO_RESPOND, user, SERVICE_AUTHORIZATION);

        remindRespondentSolicitorToRespondTask.run();

        verify(ccdUpdateService).submitEvent(TEST_CASE_ID, SYSTEM_REMIND_RESPONDENT_SOLICITOR_TO_RESPOND, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService).submitEvent(2L, SYSTEM_REMIND_RESPONDENT_SOLICITOR_TO_RESPOND, user, SERVICE_AUTHORIZATION);
    }

    private OrganisationPolicy<UserRole> organisationPolicy() {
        return OrganisationPolicy.<UserRole>builder()
            .organisation(Organisation
                .builder()
                .organisationName("Test Organisation")
                .organisationId("HEBFGBR")
                .build())
            .build();
    }

    private void mockObjectMapper() {
        when(objectMapper.convertValue(any(), eq(CaseData.class))).thenReturn(
            CaseData.builder()
                .applicant2(Applicant.builder()
                    .solicitor(Solicitor.builder()
                        .organisationPolicy(organisationPolicy())
                        .email("abc@gm.com")
                        .build())
                    .build())
                .build()
        );
    }
}
