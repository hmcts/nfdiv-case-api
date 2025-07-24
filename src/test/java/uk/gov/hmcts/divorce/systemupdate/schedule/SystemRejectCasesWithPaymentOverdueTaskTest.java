package uk.gov.hmcts.divorce.systemupdate.schedule;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Payment;
import uk.gov.hmcts.divorce.divorcecase.model.PaymentStatus;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.payment.service.PaymentStatusService;
import uk.gov.hmcts.divorce.systemupdate.convert.CaseDetailsConverter;
import uk.gov.hmcts.divorce.systemupdate.service.CcdConflictException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.existsQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPayment;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemRejectCasesWithPaymentOverdue.APPLICATION_REJECTED_FEE_NOT_PAID;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.DATA;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.ISSUE_DATE;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.STATE;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.SUPPLEMENTARY_CASE_TYPE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
class SystemRejectCasesWithPaymentOverdueTaskTest {
    private static final String LAST_STATE_MODIFIED_DATE = "last_state_modified_date";
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

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @Mock
    private PaymentStatusService paymentStatusService;


    @InjectMocks
    private SystemRejectCasesWithPaymentOverdueTask task;


    private List<uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State>> caseDetails;
    private String reference;

    @BeforeEach
    void setUp() {
        final BoolQueryBuilder paperOrJudicialSeparationCases = boolQuery()
            .should(matchQuery(String.format(DATA, SUPPLEMENTARY_CASE_TYPE), "judicialSeparation"))
            .should(matchQuery(String.format(DATA, SUPPLEMENTARY_CASE_TYPE), "separation"))
            .should(matchQuery(String.format(DATA, NEW_PAPER_CASE), "Yes"))
            .mustNot(existsQuery(ISSUE_DATE))
            .minimumShouldMatch(1);

        final MatchQueryBuilder awaitingPaymentQuery = matchQuery(STATE, AwaitingPayment);

        query = boolQuery()
            .should(
                boolQuery()
                    .must(awaitingPaymentQuery)
                    .mustNot(paperOrJudicialSeparationCases)
                    .filter(rangeQuery(LAST_STATE_MODIFIED_DATE).lte(LocalDate.now().minusDays(14)))
            )
            .should(
                boolQuery()
                    .must(awaitingPaymentQuery)
                    .must(paperOrJudicialSeparationCases)
                    .filter(rangeQuery(LAST_STATE_MODIFIED_DATE).lte(LocalDate.now().minusDays(17)))
            )
            .minimumShouldMatch(1);

        user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserInfo.builder().build());
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);

        reference = UUID.randomUUID().toString();

        caseDetails = List.of(
            uk.gov.hmcts.ccd.sdk.api.CaseDetails.<CaseData, State>builder().id(1L)
                .state(AwaitingPayment)
                .data(CaseData.builder().application(
                    Application.builder().applicationPayments(getPayments()).build()).build())
                .lastModified(LocalDateTime.now().minusDays(18))
                .build());
    }

    @Test
    void shouldSearchAndFindCasesThatAreAwaitingPaymentAndLastModifiedDateIsMoreThan14DaysInPastForNonJudicialSeparationAndPaperCases() {

        final CaseDetails cd = CaseDetails.builder().data(Map.of("applicationPayments", ""))
            .id(1L).state("AwaitingPayment").build();

        cd.setLastModified(LocalDateTime.now().minusDays(18));

        final List<CaseDetails> matchingCases = List.of(cd);

        when(ccdSearchService.searchForAllCasesWithQuery(
            query, user, SERVICE_AUTHORIZATION, AwaitingPayment)).thenReturn(matchingCases);

        when(caseDetailsConverter.convertToCaseDetailsFromReformModel(same(cd))).thenReturn(caseDetails.getFirst());
        when(paymentStatusService.hasSuccessfulPayment(caseDetails.getFirst(), SYSTEM_UPDATE_AUTH_TOKEN, SERVICE_AUTHORIZATION))
            .thenReturn(false);

        task.run();

        verify(ccdUpdateService).submitEvent(1L, APPLICATION_REJECTED_FEE_NOT_PAID, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService, never()).submitEvent(2L, APPLICATION_REJECTED_FEE_NOT_PAID, user, SERVICE_AUTHORIZATION);
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

    private List<ListValue<Payment>> getPayments() {

        final Payment payment = Payment
            .builder()
            .status(PaymentStatus.IN_PROGRESS)
            .reference(reference)
            .build();

        final ListValue<Payment> paymentListValue = ListValue
            .<Payment>builder()
            .value(payment)
            .build();
        final List<ListValue<Payment>> payments = new ArrayList<>();
        payments.add(paymentListValue);

        return payments;
    }
}
