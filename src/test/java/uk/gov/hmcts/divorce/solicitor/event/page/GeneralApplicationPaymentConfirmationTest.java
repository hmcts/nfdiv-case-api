package uk.gov.hmcts.divorce.solicitor.event.page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FeeDetails;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplication;
import uk.gov.hmcts.divorce.divorcecase.model.ServicePaymentMethod;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.payment.service.PaymentService;
import uk.gov.hmcts.divorce.solicitor.client.pba.PbaService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralApplicationFee.FEE0227;
import static uk.gov.hmcts.divorce.solicitor.event.page.GeneralApplicationPaymentConfirmation.CARD_PAYMENT_ERROR;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class GeneralApplicationPaymentConfirmationTest {

    @Mock
    private PaymentService paymentService;

    @Mock
    private PbaService pbaService;

    @InjectMocks
    private GeneralApplicationPaymentConfirmation page;

    @Test
    void shouldPreventTheUserPayingByCard() {
        final CaseData caseData = caseData();
        caseData.setGeneralApplication(GeneralApplication.builder()
            .generalApplicationFeeType(FEE0227)
                .generalApplicationFee(
                    FeeDetails.builder()
                        .paymentMethod(ServicePaymentMethod.FEE_PAY_BY_CARD)
                        .build()
                )
            .build());

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        assertEquals(1, response.getErrors().size());
        assertThat(response.getErrors()).contains(CARD_PAYMENT_ERROR);
    }

    @Test
    void shouldAllowPaymentByAccount() {
        final CaseData caseData = caseData();
        caseData.setGeneralApplication(GeneralApplication.builder()
            .generalApplicationFeeType(FEE0227)
            .generalApplicationFee(
                FeeDetails.builder()
                    .paymentMethod(ServicePaymentMethod.FEE_PAY_BY_ACCOUNT)
                    .build()
            )
            .build());

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        assertNull(response.getErrors());
    }
}
