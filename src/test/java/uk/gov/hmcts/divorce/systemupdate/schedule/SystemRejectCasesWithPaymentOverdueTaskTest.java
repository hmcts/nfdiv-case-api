package uk.gov.hmcts.divorce.systemupdate.schedule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.payment.service.PaymentStatusService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdConflictException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPayment;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemRejectCasesWithPaymentOverdue.APPLICATION_REJECTED_FEE_NOT_PAID;
import static uk.gov.hmcts.divorce.systemupdate.schedule.SystemRejectCasesWithPaymentOverdueTask.CASE_ELIGIBLE_FOR_REJECTION_QUERY;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class SystemRejectCasesWithPaymentOverdueTaskTest {
    private User user;

    @Mock
    private CcdUpdateService ccdUpdateService;

    @Mock
    private CcdSearchService ccdSearchService;

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private PaymentStatusService paymentStatusService;

    @InjectMocks
    private SystemRejectCasesWithPaymentOverdueTask task;

    @BeforeEach
    void setUp() {
        user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserInfo.builder().build());
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
        ReflectionTestUtils.setField(task, "totalMaxResults", 25);
    }

    @Test
    void shouldSearchAndFindCasesThatAreAwaitingPaymentAndLastModifiedDateIsMoreThan14DaysInPastForNonJudicialSeparationAndPaperCases() {

        final CaseDetails cd = CaseDetails.builder().data(Map.of("applicationPayments", ""))
            .id(1L).state("AwaitingPayment").build();

        cd.setLastModified(LocalDateTime.now().minusDays(18));

        final List<CaseDetails> matchingCases = List.of(cd);

        when(ccdSearchService
            .searchForAllCasesWithQuery(CASE_ELIGIBLE_FOR_REJECTION_QUERY, user, SERVICE_AUTHORIZATION, AwaitingPayment)
        ).thenReturn(matchingCases);

        task.run();

        verify(ccdUpdateService, never()).submitEvent(2L, APPLICATION_REJECTED_FEE_NOT_PAID, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService,never()).submitEvent(3L, APPLICATION_REJECTED_FEE_NOT_PAID, user, SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldTriggerRejectIfCaseDoesNotHaveRecentPayments() {

        final CaseDetails cd = CaseDetails.builder().data(Map.of("applicationPayments", ""))
            .id(TEST_CASE_ID).state("AwaitingPayment").build();
        cd.setLastModified(LocalDateTime.now().minusDays(13));

        final List<CaseDetails> matchingCases = List.of(cd);

        when(ccdSearchService.searchForAllCasesWithQuery(
            CASE_ELIGIBLE_FOR_REJECTION_QUERY, user, SERVICE_AUTHORIZATION, AwaitingPayment)
        ).thenReturn(matchingCases);
        when(paymentStatusService.hasRecentPayments(TEST_CASE_ID)).thenReturn(false);

        task.run();

        verify(paymentStatusService).hasRecentPayments(TEST_CASE_ID);
        verify(ccdSearchService).searchForAllCasesWithQuery(
            CASE_ELIGIBLE_FOR_REJECTION_QUERY, user, SERVICE_AUTHORIZATION, AwaitingPayment
        );
        verify(ccdUpdateService).submitEvent(TEST_CASE_ID, APPLICATION_REJECTED_FEE_NOT_PAID, user, SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldNotTriggerRejectIfCaseDoesHaveRecentPayments() {

        final CaseDetails cd = CaseDetails.builder().data(Map.of("applicationPayments", ""))
            .id(TEST_CASE_ID).state("AwaitingPayment").build();
        cd.setLastModified(LocalDateTime.now().minusDays(13));

        final List<CaseDetails> matchingCases = List.of(cd);

        when(ccdSearchService.searchForAllCasesWithQuery(
            CASE_ELIGIBLE_FOR_REJECTION_QUERY, user, SERVICE_AUTHORIZATION, AwaitingPayment)
        ).thenReturn(matchingCases);
        when(paymentStatusService.hasRecentPayments(TEST_CASE_ID)).thenReturn(true);

        task.run();

        verify(paymentStatusService).hasRecentPayments(TEST_CASE_ID);
        verify(ccdSearchService).searchForAllCasesWithQuery(
            CASE_ELIGIBLE_FOR_REJECTION_QUERY, user, SERVICE_AUTHORIZATION, AwaitingPayment
        );
        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    void shouldNotSubmitEventIfSearchFailsWithCCDSearchCaseException() {
        doThrow(CcdSearchCaseException.class).when(ccdSearchService).searchForAllCasesWithQuery(
                CASE_ELIGIBLE_FOR_REJECTION_QUERY, user, SERVICE_AUTHORIZATION, AwaitingPayment);

        task.run();

        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    void shouldNotSubmitEventIfSearchFailsWithCCDConflictException() {
        doThrow(CcdConflictException.class).when(ccdSearchService).searchForAllCasesWithQuery(
            CASE_ELIGIBLE_FOR_REJECTION_QUERY, user, SERVICE_AUTHORIZATION, AwaitingPayment);

        task.run();

        verifyNoInteractions(ccdUpdateService);
    }
}
