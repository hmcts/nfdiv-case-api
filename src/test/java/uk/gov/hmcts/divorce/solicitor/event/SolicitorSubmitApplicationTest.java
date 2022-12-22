package uk.gov.hmcts.divorce.solicitor.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.divorce.common.service.SubmissionService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.payment.PaymentService;
import uk.gov.hmcts.divorce.solicitor.event.page.SolPayment;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.idam.client.models.User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.payment.PaymentService.EVENT_ISSUE;
import static uk.gov.hmcts.divorce.payment.PaymentService.KEYWORD_DIVORCE;
import static uk.gov.hmcts.divorce.payment.PaymentService.SERVICE_DIVORCE;
import static uk.gov.hmcts.divorce.solicitor.event.SolicitorSubmitApplication.SOLICITOR_SUBMIT;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemSolicitorPayment.SYSTEM_SOLICITOR_PAYMENT;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getPbaNumbersForAccount;

@ExtendWith(MockitoExtension.class)
public class SolicitorSubmitApplicationTest {

    @Mock
    private PaymentService paymentService;

    @Mock
    private SubmissionService submissionService;

    @Mock
    private SolPayment solPayment;

    @Mock
    private CcdUpdateService ccdUpdateService;

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private SolicitorSubmitApplication solicitorSubmitApplication;

    @Test
    void shouldSetOrderSummaryAndSolicitorFeesInPoundsAndSolicitorRolesAndPbaNumbersWhenAboutToStartIsInvoked() {

        final long caseId = 1L;
        final OrderSummary orderSummary = mock(OrderSummary.class);
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();
        caseDetails.setData(caseData);
        caseDetails.setId(caseId);

        when(paymentService.getOrderSummaryByServiceEvent(SERVICE_DIVORCE, EVENT_ISSUE, KEYWORD_DIVORCE)).thenReturn(orderSummary);
        when(orderSummary.getPaymentTotal()).thenReturn("55000");

        var midEventCaseData = caseData();
        midEventCaseData.getApplication().setPbaNumbers(getPbaNumbersForAccount("PBA0012345"));

        var pbaResponse
            = AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(midEventCaseData)
            .build();

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            solicitorSubmitApplication.aboutToStart(caseDetails);

        assertThat(response.getData().getApplication().getApplicationFeeOrderSummary()).isEqualTo(orderSummary);
        assertThat(response.getData().getApplication().getSolApplicationFeeInPounds()).isEqualTo("550");
    }

    @Test
    void shouldInvokeCaseUpdateFromSubmittedActions() {
        final long caseId = 1L;
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();
        caseDetails.setData(caseData);
        caseDetails.setId(caseId);
        User user = new User("", null);
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        String serviceAuthorization = "serviceAuth";
        when(authTokenGenerator.generate()).thenReturn(serviceAuthorization);

        final SubmittedCallbackResponse response =
            solicitorSubmitApplication.submitted(caseDetails, null);
        assertThat(response.getConfirmationBody()).isEqualTo(null);
        assertThat(response.getConfirmationHeader()).isEqualTo(null);

        verify(ccdUpdateService).submitEvent(caseDetails, SYSTEM_SOLICITOR_PAYMENT, user, serviceAuthorization);
    }

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        solicitorSubmitApplication.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SOLICITOR_SUBMIT);
    }
}
