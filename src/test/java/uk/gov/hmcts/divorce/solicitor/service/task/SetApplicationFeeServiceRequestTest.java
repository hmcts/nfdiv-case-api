package uk.gov.hmcts.divorce.solicitor.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.payment.service.PaymentSetupService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_REFERENCE;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.applicantRepresentedBySolicitor;

@ExtendWith(MockitoExtension.class)
class SetApplicationFeeServiceRequestTest {

    @Mock
    private PaymentSetupService paymentSetupService;

    @InjectMocks
    private SetApplicationFeeServiceRequest setApplicationFeeServiceRequest;

    @Test
    void shouldCreateOrderSummaryAndServiceRequestByDelegatingToPaymentSetupService() {
        final var caseData = CaseData.builder()
            .applicant1(applicantRepresentedBySolicitor())
            .build();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        final OrderSummary orderSummary = new OrderSummary();

        when(paymentSetupService.createApplicationFeeOrderSummary(caseData, TEST_CASE_ID))
            .thenReturn(orderSummary);
        when(paymentSetupService.createApplicationFeeServiceRequest(caseData, TEST_CASE_ID, null))
            .thenReturn(TEST_SERVICE_REFERENCE);

        final CaseDetails<CaseData, State> result = setApplicationFeeServiceRequest.apply(caseDetails);
        Application resultApplication = result.getData().getApplication();

        assertThat(resultApplication.getApplicationFeeOrderSummary())
            .isEqualTo(orderSummary);
        assertThat(resultApplication.getApplicationFeeServiceRequestReference())
            .isEqualTo(TEST_SERVICE_REFERENCE);
    }
}
