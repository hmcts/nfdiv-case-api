package uk.gov.hmcts.divorce.solicitor.event.page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplication;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.payment.service.PaymentService;
import uk.gov.hmcts.divorce.solicitor.client.pba.PbaService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralApplicationFee.FEE0227;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralApplicationFee.FEE0228;
import static uk.gov.hmcts.divorce.payment.service.PaymentService.EVENT_GENERAL;
import static uk.gov.hmcts.divorce.payment.service.PaymentService.KEYWORD_NOTICE;
import static uk.gov.hmcts.divorce.payment.service.PaymentService.KEYWORD_WITHOUT_NOTICE;
import static uk.gov.hmcts.divorce.payment.service.PaymentService.SERVICE_OTHER;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class GeneralApplicationSelectFeeTest {

    @Mock
    private PaymentService paymentService;

    @Mock
    private PbaService pbaService;

    @InjectMocks
    private GeneralApplicationSelectFee page;

    @Test
    void shouldSetGeneralApplicationOrderSummaryIfWithNoticeFeeIsChosen() {
        final CaseData caseData = caseData();
        caseData.setGeneralApplication(GeneralApplication.builder()
            .generalApplicationFeeType(FEE0227)
            .build());
        String applicantName = caseData.getApplicant1().getFullName();

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        final OrderSummary orderSummary = OrderSummary.builder().build();

        stubOrderSummaryCreation(orderSummary, KEYWORD_NOTICE);

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        verify(paymentService).getOrderSummaryByServiceEvent(SERVICE_OTHER, EVENT_GENERAL, KEYWORD_NOTICE);
        assertEquals(
            response.getData().getGeneralApplication().getGeneralApplicationFee().getOrderSummary(),
            orderSummary
        );
    }

    @Test
    void shouldSetGeneralApplicationOrderSummaryIfWithoutNoticeFeeIsChosen() {
        final CaseData caseData = caseData();
        caseData.setGeneralApplication(GeneralApplication.builder()
            .generalApplicationFeeType(FEE0228)
            .build());
        String applicantName = caseData.getApplicant1().getFullName();

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        final OrderSummary orderSummary = OrderSummary.builder().build();

        stubOrderSummaryCreation(orderSummary, KEYWORD_WITHOUT_NOTICE);

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        verify(paymentService).getOrderSummaryByServiceEvent(SERVICE_OTHER, EVENT_GENERAL, KEYWORD_WITHOUT_NOTICE);
        assertEquals(
            response.getData().getGeneralApplication().getGeneralApplicationFee().getOrderSummary(),
            orderSummary
        );
    }

    @Test
    void shouldPopulatePbaNumbersDynamicList() {
        final CaseData caseData = caseData();
        caseData.setGeneralApplication(GeneralApplication.builder()
            .generalApplicationFeeType(FEE0228)
            .build());

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        DynamicList pbaNumbers = mock(DynamicList.class);

        when(pbaService.populatePbaDynamicList()).thenReturn(pbaNumbers);

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        verify(pbaService).populatePbaDynamicList();
        assertEquals(
            response.getData().getGeneralApplication().getGeneralApplicationFee().getPbaNumbers(),
            pbaNumbers
        );
    }

    private void stubOrderSummaryCreation(OrderSummary orderSummary, String keyword) {
        when(paymentService.getOrderSummaryByServiceEvent(SERVICE_OTHER, EVENT_GENERAL, keyword))
            .thenReturn(orderSummary);
    }
}
