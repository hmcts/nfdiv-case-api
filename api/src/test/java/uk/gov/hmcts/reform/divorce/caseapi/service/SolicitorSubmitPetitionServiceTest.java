package uk.gov.hmcts.reform.divorce.caseapi.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.divorce.caseapi.clients.FeesAndPaymentsClient;
import uk.gov.hmcts.reform.divorce.ccd.model.FeeValue;
import uk.gov.hmcts.reform.divorce.ccd.model.OrderSummary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.reform.divorce.caseapi.TestConstants.FEE_CODE;
import static uk.gov.hmcts.reform.divorce.caseapi.TestConstants.ISSUE_FEE;
import static uk.gov.hmcts.reform.divorce.caseapi.caseapi.util.TestDataHelper.getFeeResponse;

@ExtendWith(MockitoExtension.class)
public class SolicitorSubmitPetitionServiceTest {

    @Mock
    private FeesAndPaymentsClient feesAndPaymentsClient;

    @InjectMocks
    private SolicitorSubmitPetitionService solicitorSubmitPetitionService;

    @Test
    void shouldReturnOrderSummaryWhenFeeEventIsAvailable() {
        doReturn(getFeeResponse())
            .when(feesAndPaymentsClient)
            .getPetitionIssueFee(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                isNull()
            );

        OrderSummary orderSummary = solicitorSubmitPetitionService.getOrderSummary();
        assertThat(orderSummary.getPaymentReference()).isNull();
        assertThat(orderSummary.getPaymentTotal()).isEqualTo(String.valueOf(1000));// in pence
        assertThat(orderSummary.getFees())
            .extracting("value", FeeValue.class)
            .extracting("feeDescription", "feeVersion", "feeCode", "feeAmount")
            .contains(tuple(ISSUE_FEE, "1", FEE_CODE, "1000")
            );

        verify(feesAndPaymentsClient)
            .getPetitionIssueFee(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                isNull()
            );

        verifyNoMoreInteractions(feesAndPaymentsClient);
    }
}
