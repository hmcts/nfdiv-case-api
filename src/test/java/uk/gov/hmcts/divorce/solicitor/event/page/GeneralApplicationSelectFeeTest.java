package uk.gov.hmcts.divorce.solicitor.event.page;

import feign.FeignException;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        final OrderSummary orderSummary = OrderSummary.builder().build();

        DynamicList pbaNumbers = mock(DynamicList.class);

        when(pbaService.populatePbaDynamicList()).thenReturn(pbaNumbers);

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

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        final OrderSummary orderSummary = OrderSummary.builder().build();

        stubOrderSummaryCreation(orderSummary, KEYWORD_WITHOUT_NOTICE);

        DynamicList pbaNumbers = mock(DynamicList.class);

        when(pbaService.populatePbaDynamicList()).thenReturn(pbaNumbers);

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


    @Test
    void shouldReturnValidationErrorWhenSolicitorPbaListIsEmpty() {
        final CaseData caseData = caseData();
        caseData.setGeneralApplication(GeneralApplication.builder()
            .generalApplicationFeeType(FEE0228)
            .build());

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        doThrow(FeignException.class).when(pbaService).populatePbaDynamicList();

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        assertThat(response.getErrors()).hasSize(1);
        verifyNoInteractions(paymentService);
    }

    private void stubOrderSummaryCreation(OrderSummary orderSummary, String keyword) {
        when(paymentService.getOrderSummaryByServiceEvent(SERVICE_OTHER, EVENT_GENERAL, keyword))
            .thenReturn(orderSummary);
    }
}
