package uk.gov.hmcts.divorce.systemupdate.schedule.migration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.CaseEventsApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.CaseEventDetail;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDateTime;
import java.util.List;

import static java.time.LocalDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerIssueApplication.CASEWORKER_ISSUE_APPLICATION;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerReissueApplication.CASEWORKER_REISSUE_APPLICATION;
import static uk.gov.hmcts.divorce.common.event.DraftAos.DRAFT_AOS;
import static uk.gov.hmcts.divorce.divorcecase.NoFaultDivorce.CASE_TYPE;
import static uk.gov.hmcts.divorce.divorcecase.NoFaultDivorce.JURISDICTION;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemProgressCaseToAosOverdue.SYSTEM_PROGRESS_TO_AOS_OVERDUE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class HasAosDraftedEventPredicateTest {

    @Mock
    private CaseEventsApi caseEventsApi;

    @InjectMocks
    private HasAosDraftedEventPredicate hasAosDraftedEventPredicate;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserDetails.builder().build());
    }

    @Test
    void shouldReturnTrueIfCaseHasAosDraftedEvent() {

        final LocalDateTime localDateTime = now();
        final CaseDetails caseDetails = CaseDetails.builder()
            .id(TEST_CASE_ID)
            .build();

        final CaseEventDetail caseEventDetail1 = CaseEventDetail.builder()
            .id(CASEWORKER_ISSUE_APPLICATION)
            .createdDate(localDateTime.minusDays(2))
            .build();
        final CaseEventDetail caseEventDetail2 = CaseEventDetail.builder()
            .id(DRAFT_AOS)
            .createdDate(localDateTime.minusDays(1))
            .build();
        final CaseEventDetail caseEventDetail3 = CaseEventDetail.builder()
            .id(SYSTEM_PROGRESS_TO_AOS_OVERDUE)
            .createdDate(localDateTime)
            .build();

        final List<CaseEventDetail> caseEventDetails = List.of(caseEventDetail2, caseEventDetail3, caseEventDetail1);

        when(caseEventsApi.findEventDetailsForCase(
            user.getAuthToken(),
            SERVICE_AUTHORIZATION,
            user.getUserDetails().getId(),
            JURISDICTION,
            CASE_TYPE,
            TEST_CASE_ID.toString())).thenReturn(caseEventDetails);

        assertThat(hasAosDraftedEventPredicate.hasAosDraftedEvent(user, SERVICE_AUTHORIZATION)
            .test(caseDetails)).isTrue();
    }

    @Test
    void shouldReturnTrueIfCaseHasReissueEventBeforeLastAosDraftedEvent() {

        final LocalDateTime localDateTime = now();
        final CaseDetails caseDetails = CaseDetails.builder()
            .id(TEST_CASE_ID)
            .build();

        final CaseEventDetail caseEventDetail1 = CaseEventDetail.builder()
            .id(CASEWORKER_ISSUE_APPLICATION)
            .createdDate(localDateTime.minusDays(2))
            .build();
        final CaseEventDetail caseEventDetail2 = CaseEventDetail.builder()
            .id(DRAFT_AOS)
            .createdDate(localDateTime)
            .build();
        final CaseEventDetail caseEventDetail3 = CaseEventDetail.builder()
            .id(CASEWORKER_REISSUE_APPLICATION)
            .createdDate(localDateTime.minusDays(1))
            .build();

        final List<CaseEventDetail> caseEventDetails = List.of(caseEventDetail2, caseEventDetail1, caseEventDetail3);

        when(caseEventsApi.findEventDetailsForCase(
            user.getAuthToken(),
            SERVICE_AUTHORIZATION,
            user.getUserDetails().getId(),
            JURISDICTION,
            CASE_TYPE,
            TEST_CASE_ID.toString())).thenReturn(caseEventDetails);

        assertThat(hasAosDraftedEventPredicate.hasAosDraftedEvent(user, SERVICE_AUTHORIZATION)
            .test(caseDetails)).isTrue();
    }

    @Test
    void shouldReturnFalseIfCaseHasReissueEventAfterLastAosDraftedEvent() {

        final LocalDateTime localDateTime = now();
        final CaseDetails caseDetails = CaseDetails.builder()
            .id(TEST_CASE_ID)
            .build();

        final CaseEventDetail caseEventDetail1 = CaseEventDetail.builder()
            .id(CASEWORKER_ISSUE_APPLICATION)
            .createdDate(localDateTime.minusDays(2))
            .build();
        final CaseEventDetail caseEventDetail2 = CaseEventDetail.builder()
            .id(DRAFT_AOS)
            .createdDate(localDateTime.minusDays(1))
            .build();
        final CaseEventDetail caseEventDetail3 = CaseEventDetail.builder()
            .id(CASEWORKER_REISSUE_APPLICATION)
            .createdDate(localDateTime)
            .build();

        final List<CaseEventDetail> caseEventDetails = List.of(caseEventDetail3, caseEventDetail1, caseEventDetail2);

        when(caseEventsApi.findEventDetailsForCase(
            user.getAuthToken(),
            SERVICE_AUTHORIZATION,
            user.getUserDetails().getId(),
            JURISDICTION,
            CASE_TYPE,
            TEST_CASE_ID.toString())).thenReturn(caseEventDetails);

        assertThat(hasAosDraftedEventPredicate.hasAosDraftedEvent(user, SERVICE_AUTHORIZATION)
            .test(caseDetails)).isFalse();
    }

    @Test
    void shouldReturnFalseIfCaseHasNoAosDraftedEvent() {

        final LocalDateTime localDateTime = now();
        final CaseDetails caseDetails = CaseDetails.builder()
            .id(TEST_CASE_ID)
            .build();

        final CaseEventDetail caseEventDetail1 = CaseEventDetail.builder()
            .id(CASEWORKER_ISSUE_APPLICATION)
            .createdDate(localDateTime.minusDays(1))
            .build();
        final CaseEventDetail caseEventDetail2 = CaseEventDetail.builder()
            .id(SYSTEM_PROGRESS_TO_AOS_OVERDUE)
            .createdDate(localDateTime)
            .build();

        final List<CaseEventDetail> caseEventDetails = List.of(caseEventDetail1, caseEventDetail2);

        when(caseEventsApi.findEventDetailsForCase(
            user.getAuthToken(),
            SERVICE_AUTHORIZATION,
            user.getUserDetails().getId(),
            JURISDICTION,
            CASE_TYPE,
            TEST_CASE_ID.toString())).thenReturn(caseEventDetails);

        assertThat(hasAosDraftedEventPredicate.hasAosDraftedEvent(user, SERVICE_AUTHORIZATION)
            .test(caseDetails)).isFalse();
    }
}