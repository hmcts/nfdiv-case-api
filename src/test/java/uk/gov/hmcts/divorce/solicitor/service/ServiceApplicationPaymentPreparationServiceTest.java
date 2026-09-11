package uk.gov.hmcts.divorce.solicitor.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;
import uk.gov.hmcts.divorce.divorcecase.model.ServicePaymentMethod;
import uk.gov.hmcts.divorce.divorcecase.model.SolicitorPaymentMethod;
import uk.gov.hmcts.divorce.payment.service.PaymentSetupService;
import uk.gov.hmcts.divorce.solicitor.client.pba.PbaService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_REFERENCE;

@ExtendWith(MockitoExtension.class)
class ServiceApplicationPaymentPreparationServiceTest {

    @InjectMocks
    private ServiceApplicationPaymentPreparationService paymentPreparationService;

    @Mock
    private PaymentSetupService paymentSetupService;

    @Mock
    private PbaService pbaService;

    @Test
    void shouldPrepareDraftFeeWithServiceRequestWhenPaymentMethodIsFeePayByAccount() {
        Applicant applicant = Applicant.builder().firstName("Applicant").lastName("One").build();
        AlternativeService serviceApplication = AlternativeService.builder().build();
        OrderSummary orderSummary = OrderSummary.builder().build();
        DynamicList pbaNumbersDynamicList = DynamicList.builder()
            .value(DynamicListElement.builder().label("PBA0088776").build())
            .build();

        when(paymentSetupService.createServiceApplicationOrderSummary(serviceApplication, TEST_CASE_ID)).thenReturn(orderSummary);
        when(pbaService.populatePbaDynamicList()).thenReturn(pbaNumbersDynamicList);
        when(paymentSetupService.createServiceApplicationPaymentServiceRequest(
            serviceApplication, TEST_CASE_ID, applicant.getFullName())
        ).thenReturn(TEST_SERVICE_REFERENCE);

        InterimApplicationOptions options = InterimApplicationOptions.builder()
            .interimAppsPaymentMethod(SolicitorPaymentMethod.FEE_PAY_BY_ACCOUNT)
            .build();

        paymentPreparationService.prepareDraftServiceApplicationFee(TEST_CASE_ID, applicant, options, serviceApplication);

        assertThat(serviceApplication.getServicePaymentFee().getPaymentMethod()).isEqualTo(ServicePaymentMethod.FEE_PAY_BY_ACCOUNT);
        assertThat(serviceApplication.getServicePaymentFee().getOrderSummary()).isEqualTo(orderSummary);
        assertThat(serviceApplication.getServicePaymentFee().getServiceRequestReference()).isEqualTo(TEST_SERVICE_REFERENCE);

        verify(paymentSetupService).createServiceApplicationOrderSummary(serviceApplication, TEST_CASE_ID);
        verify(paymentSetupService).createServiceApplicationPaymentServiceRequest(
            serviceApplication, TEST_CASE_ID, applicant.getFullName()
        );
    }

    @Test
    void shouldPrepareDraftFeeWithoutServiceRequestWhenPaymentMethodIsHelpWithFees() {
        Applicant applicant = Applicant.builder().firstName("Applicant").lastName("One").build();
        InterimApplicationOptions options = InterimApplicationOptions.builder()
            .interimAppsPaymentMethod(SolicitorPaymentMethod.FEES_HELP_WITH)
            .build();
        AlternativeService serviceApplication = AlternativeService.builder().build();

        paymentPreparationService.prepareDraftServiceApplicationFee(TEST_CASE_ID, applicant, options, serviceApplication);

        assertThat(serviceApplication.getServicePaymentFee().getPaymentMethod()).isEqualTo(ServicePaymentMethod.FEE_PAY_BY_HWF);
        assertThat(serviceApplication.getServicePaymentFee().getServiceRequestReference()).isNull();

        verify(pbaService, never()).populatePbaDynamicList();
        verify(paymentSetupService, never()).createServiceApplicationPaymentServiceRequest(serviceApplication, TEST_CASE_ID,
            applicant.getFullName());
    }
}
