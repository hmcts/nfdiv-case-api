package uk.gov.hmcts.divorce.caseworker.event.page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.FieldCollection;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralReferral;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.payment.service.PaymentService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
class GeneralReferralSelectFeeTest {

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private GeneralReferralSelectFee page;

    @Mock
    private PageBuilder pageBuilder;

    @Mock(answer = Answers.RETURNS_SELF)
    private FieldCollection.FieldCollectionBuilder<CaseData, State, Event.EventBuilder<CaseData, UserRole, State>>
        fieldCollectionBuilder;

    @Mock
    private ComplexType complexType;

    @Test
    void shouldSetGeneralReferralOrderSummaryIfWithNoticeFeeIsChosen() {
        final CaseData caseData = caseData();
        caseData.setGeneralReferral(GeneralReferral.builder()
            .generalReferralFeeType(FEE0227)
            .build());

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        final OrderSummary orderSummary = OrderSummary.builder().build();

        stubOrderSummaryCreation(orderSummary, KEYWORD_NOTICE);

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        verify(paymentService).getOrderSummaryByServiceEvent(SERVICE_OTHER, EVENT_GENERAL, KEYWORD_NOTICE);
        assertEquals(
            response.getData().getGeneralReferral().getGeneralReferralFee().getOrderSummary(),
            orderSummary
        );
    }

    @Test
    void shouldSetGeneralReferralOrderSummaryIfWithoutNoticeFeeIsChosen() {
        final CaseData caseData = caseData();
        caseData.setGeneralReferral(GeneralReferral.builder()
            .generalReferralFeeType(FEE0228)
            .build());

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        final OrderSummary orderSummary = OrderSummary.builder().build();

        stubOrderSummaryCreation(orderSummary, KEYWORD_WITHOUT_NOTICE);

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        verify(paymentService).getOrderSummaryByServiceEvent(SERVICE_OTHER, EVENT_GENERAL, KEYWORD_WITHOUT_NOTICE);
        assertEquals(
            response.getData().getGeneralReferral().getGeneralReferralFee().getOrderSummary(),
            orderSummary
        );
    }

    private void stubOrderSummaryCreation(OrderSummary orderSummary, String keyword) {
        when(paymentService.getOrderSummaryByServiceEvent(SERVICE_OTHER, EVENT_GENERAL, keyword))
            .thenReturn(orderSummary);
    }

    @Test
    void shouldAddGeneralReferralSelectFeeTypePageConfiguration() {
        when(pageBuilder.page(eq("generalReferralSelectFeeType"), any())).thenReturn(fieldCollectionBuilder);

        page.addTo(pageBuilder);

        verify(pageBuilder).page(eq("generalReferralSelectFeeType"), any());
    }
}
