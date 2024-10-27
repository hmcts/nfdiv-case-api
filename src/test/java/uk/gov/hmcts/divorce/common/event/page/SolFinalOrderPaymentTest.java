package uk.gov.hmcts.divorce.common.event.page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.SolicitorPaymentMethod;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.payment.PaymentService;
import uk.gov.hmcts.divorce.solicitor.client.pba.PbaService;
import uk.gov.hmcts.divorce.solicitor.event.page.SolFinalOrderPayment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.testutil.TestConstants.APPLICANT_2_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_REFERENCE;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.orderSummaryWithFee;

@ExtendWith(MockitoExtension.class)
class SolFinalOrderPaymentTest {

    @Mock
    private PbaService pbaService;

    @Mock
    private PaymentService paymentService;

    @InjectMocks

    private SolFinalOrderPayment page;

    @Test
    void shouldCreateServiceRequestForPbaPayment() {
        final CaseData caseData = caseData();
        var orderSummary = orderSummaryWithFee();
        caseData.getApplicant2().setFirstName(APPLICANT_2_FIRST_NAME);
        caseData.getFinalOrder().setApplicant2SolFinalOrderFeeOrderSummary(orderSummary);
        caseData.getFinalOrder().setApplicant2SolPaymentHowToPay(SolicitorPaymentMethod.FEE_PAY_BY_ACCOUNT);

        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        when(paymentService.createServiceRequestReference(
            null, TEST_CASE_ID, APPLICANT_2_FIRST_NAME, orderSummary
        )).thenReturn(TEST_SERVICE_REFERENCE);

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, beforeDetails);

        assertThat(response.getData().getFinalOrder().getApplicant2FinalOrderFeeServiceRequestReference())
            .isEqualTo(TEST_SERVICE_REFERENCE);
    }
}
