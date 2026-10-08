package uk.gov.hmcts.divorce.solicitor.service;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.FeeDetails;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplication;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplicationD11JourneyOptions;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplicationHearingNotRequired;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.ServicePaymentMethod;
import uk.gov.hmcts.divorce.divorcecase.model.SolicitorPaymentMethod;
import uk.gov.hmcts.divorce.payment.service.PaymentSetupService;
import uk.gov.hmcts.divorce.solicitor.client.pba.PbaService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralApplicationFee.FEE0227;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralApplicationFee.FEE0228;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
public class GeneralApplicationPaymentPreparationServiceTest {

    @InjectMocks
    private GeneralApplicationPaymentPreparationService generalApplicationPaymentPreparationService;

    @Mock
    private PaymentSetupService paymentSetupService;

    @Mock
    private PbaService pbaService;

    @Test
    void shouldPreparePbaPaymentWhenPaymentMethodIsNotHelpWithFeesAndHearingIsRequired() {
        Applicant applicant = Applicant.builder().firstName("John").lastName("Smith").build();

        OrderSummary orderSummary = OrderSummary.builder().build();
        DynamicList pbaNumbers = DynamicList.builder().build();

        when(paymentSetupService.createGeneralApplicationOrderSummary(TEST_CASE_ID, FEE0227)).thenReturn(orderSummary);
        when(pbaService.populatePbaDynamicList()).thenReturn(pbaNumbers);
        when(paymentSetupService.createGeneralApplicationPaymentServiceRequest(orderSummary, TEST_CASE_ID, applicant.getFullName()))
            .thenReturn("SR-123");

        FeeDetails feeDetails = FeeDetails.builder().build();
        GeneralApplication generalApplication = GeneralApplication.builder().generalApplicationFee(feeDetails).build();

        InterimApplicationOptions options = InterimApplicationOptions.builder()
            .interimAppsPaymentMethod(SolicitorPaymentMethod.FEE_PAY_BY_ACCOUNT)
            .interimApplicationType(InterimApplicationType.DIGITISED_GENERAL_APPLICATION_D11)
            .generalApplicationD11JourneyOptions(GeneralApplicationD11JourneyOptions.builder()
                .hearingNotRequired(GeneralApplicationHearingNotRequired.NO).build())
            .build();

        generalApplicationPaymentPreparationService.prepareDraftGeneralApplicationFee(TEST_CASE_ID, applicant, options, generalApplication);

        assertThat(feeDetails.getPaymentMethod()).isEqualTo(ServicePaymentMethod.FEE_PAY_BY_ACCOUNT);
        assertThat(feeDetails.getServiceRequestReference()).isEqualTo("SR-123");

        verify(paymentSetupService).createGeneralApplicationOrderSummary(TEST_CASE_ID, FEE0227);
        verify(pbaService).populatePbaDynamicList();
        verify(paymentSetupService).createGeneralApplicationPaymentServiceRequest(orderSummary, TEST_CASE_ID, applicant.getFullName());
    }

    @Test
    void shouldPreparePbaPaymentWithFee0228WhenHearingIsNotRequired() {
        Applicant applicant = Applicant.builder().firstName("John").lastName("Smith").build();

        OrderSummary orderSummary = OrderSummary.builder().build();

        when(paymentSetupService.createGeneralApplicationOrderSummary(TEST_CASE_ID, FEE0228))
            .thenReturn(orderSummary);
        when(pbaService.populatePbaDynamicList()).thenReturn(DynamicList.builder().build());
        when(paymentSetupService.createGeneralApplicationPaymentServiceRequest(any(), eq(TEST_CASE_ID), eq(applicant.getFullName())))
            .thenReturn("SR-456");

        InterimApplicationOptions options = InterimApplicationOptions.builder()
            .interimAppsPaymentMethod(SolicitorPaymentMethod.FEE_PAY_BY_ACCOUNT)
            .build();

        FeeDetails feeDetails = FeeDetails.builder().build();
        GeneralApplication generalApplication = GeneralApplication.builder().generalApplicationFee(feeDetails).build();

        generalApplicationPaymentPreparationService.prepareDraftGeneralApplicationFee(
            TEST_CASE_ID, applicant, options, generalApplication
        );

        assertThat(generalApplication.getGeneralApplicationFeeType()).isEqualTo(FEE0228);
        assertThat(feeDetails.getPaymentMethod()).isEqualTo(ServicePaymentMethod.FEE_PAY_BY_ACCOUNT);
        assertThat(feeDetails.getServiceRequestReference()).isEqualTo("SR-456");

        verify(paymentSetupService).createGeneralApplicationOrderSummary(TEST_CASE_ID, FEE0228);
    }

    @Test
    void shouldPrepareHelpWithFeesPaymentAndNotCreateServiceRequest() {
        Applicant applicant = Applicant.builder().firstName("John").lastName("Smith").build();
        InterimApplicationOptions options = InterimApplicationOptions.builder()
            .interimAppsPaymentMethod(SolicitorPaymentMethod.FEES_HELP_WITH)
            .build();

        FeeDetails feeDetails = FeeDetails.builder().serviceRequestReference("old-ref").build();

        GeneralApplication generalApplication = GeneralApplication.builder().generalApplicationFee(feeDetails).build();

        generalApplicationPaymentPreparationService.prepareDraftGeneralApplicationFee(
            TEST_CASE_ID, applicant, options, generalApplication
        );

        assertThat(feeDetails.getPaymentMethod()).isEqualTo(ServicePaymentMethod.FEE_PAY_BY_HWF);
        assertThat(feeDetails.getServiceRequestReference()).isNull();

        verifyNoInteractions(pbaService);
        verify(paymentSetupService, never()).createGeneralApplicationOrderSummary(anyLong(), any());
        verify(paymentSetupService, never()).createGeneralApplicationPaymentServiceRequest(any(), anyLong(), anyString());
    }
}
