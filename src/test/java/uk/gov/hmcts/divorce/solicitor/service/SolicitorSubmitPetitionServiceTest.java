package uk.gov.hmcts.divorce.solicitor.service;

import feign.FeignException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Fee;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.State;
import uk.gov.hmcts.divorce.payment.FeesAndPaymentsClient;
import uk.gov.hmcts.divorce.solicitor.service.notification.ApplicantSubmittedNotification;

import java.util.Collections;
import java.util.Map;

import static feign.Request.HttpMethod.GET;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.divorce.common.model.State.SolicitorAwaitingPaymentConfirmation;
import static uk.gov.hmcts.divorce.testutil.TestConstants.FEE_CODE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ISSUE_FEE;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getFeeResponse;

@ExtendWith(MockitoExtension.class)
public class SolicitorSubmitPetitionServiceTest {

    @Mock
    private FeesAndPaymentsClient feesAndPaymentsClient;

    @Mock
    private ApplicantSubmittedNotification applicantSubmittedNotification;

    @InjectMocks
    private SolicitorSubmitPetitionService solicitorSubmitPetitionService;

    @Test
    public void shouldReturnOrderSummaryWhenFeeEventIsAvailable() {
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
            .extracting("value", Fee.class)
            .extracting("description", "version", "code", "amount")
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

    @Test
    public void shouldThrowFeignExceptionWhenFeeEventIsNotAvailable() {
        byte[] emptyBody = {};
        Request request = Request.create(GET, EMPTY, Map.of(), emptyBody, UTF_8, null);

        FeignException feignException = FeignException.errorStatus(
            "feeLookupNotFound",
            Response.builder()
                .request(request)
                .status(404)
                .headers(Collections.emptyMap())
                .reason("Fee Not found")
                .build()
        );

        doThrow(feignException)
            .when(feesAndPaymentsClient)
            .getPetitionIssueFee(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                isNull()
            );

        assertThatThrownBy(() -> solicitorSubmitPetitionService.getOrderSummary())
            .hasMessageContaining("404 Fee Not found")
            .isExactlyInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void shouldNotifyApplicantAndSetStateForAboutToSubmit() {

        final CaseData caseData = CaseData.builder().build();
        final long caseId = 1L;

        final AboutToStartOrSubmitResponse<CaseData, State> aboutToStartOrSubmitResponse =
            solicitorSubmitPetitionService.aboutToSubmit(caseData, caseId);

        assertThat(aboutToStartOrSubmitResponse.getState()).isEqualTo(SolicitorAwaitingPaymentConfirmation);
        verify(applicantSubmittedNotification).send(caseData, caseId);
    }
}
