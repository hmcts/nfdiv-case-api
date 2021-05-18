package uk.gov.hmcts.divorce.solicitor.service;

import feign.FeignException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.Fee;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.updater.CaseDataContext;
import uk.gov.hmcts.divorce.common.updater.CaseDataUpdaterChain;
import uk.gov.hmcts.divorce.common.updater.CaseDataUpdaterChainFactory;
import uk.gov.hmcts.divorce.payment.FeesAndPaymentsClient;
import uk.gov.hmcts.divorce.solicitor.service.updater.MiniApplicationRemover;
import uk.gov.hmcts.divorce.solicitor.service.updater.SolicitorSubmitNotification;

import java.util.Collections;
import java.util.Map;

import static feign.Request.HttpMethod.GET;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.common.model.State.SolicitorAwaitingPaymentConfirmation;
import static uk.gov.hmcts.divorce.testutil.TestConstants.FEE_CODE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ISSUE_FEE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getFeeResponse;

@ExtendWith(MockitoExtension.class)
public class SolicitorSubmitApplicationServiceTest {

    @Mock
    private FeesAndPaymentsClient feesAndPaymentsClient;

    @Mock
    private CaseDataUpdaterChainFactory caseDataUpdaterChainFactory;

    @Mock
    private MiniApplicationRemover miniApplicationRemover;

    @Mock
    private SolicitorSubmitNotification solicitorSubmitNotification;

    @InjectMocks
    private SolicitorSubmitApplicationService solicitorSubmitApplicationService;

    @Test
    public void shouldReturnOrderSummaryWhenFeeEventIsAvailable() {
        doReturn(getFeeResponse())
            .when(feesAndPaymentsClient)
            .getApplicationIssueFee(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                isNull()
            );

        OrderSummary orderSummary = solicitorSubmitApplicationService.getOrderSummary();
        assertThat(orderSummary.getPaymentReference()).isNull();
        assertThat(orderSummary.getPaymentTotal()).isEqualTo(String.valueOf(1000));// in pence
        assertThat(orderSummary.getFees())
            .extracting("value", Fee.class)
            .extracting("description", "version", "code", "amount")
            .contains(tuple(ISSUE_FEE, "1", FEE_CODE, "1000")
            );

        verify(feesAndPaymentsClient)
            .getApplicationIssueFee(
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
            .getApplicationIssueFee(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                isNull()
            );

        assertThatThrownBy(() -> solicitorSubmitApplicationService.getOrderSummary())
            .hasMessageContaining("404 Fee Not found")
            .isExactlyInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void shouldCompleteStepsToUpdateApplication() {

        final var caseData = mock(CaseData.class);
        final var caseDataUpdaterChain = mock(CaseDataUpdaterChain.class);

        final var caseDataUpdaters = asList(
            miniApplicationRemover,
            solicitorSubmitNotification
        );

        final var caseDataContext = CaseDataContext.builder()
            .caseData(caseData)
            .caseId(TEST_CASE_ID)
            .userAuthToken(TEST_AUTHORIZATION_TOKEN)
            .build();

        when(caseDataUpdaterChainFactory.createWith(caseDataUpdaters)).thenReturn(caseDataUpdaterChain);
        when(caseDataUpdaterChain.processNext(caseDataContext)).thenReturn(caseDataContext);

        final var response = solicitorSubmitApplicationService.aboutToSubmit(
            caseData,
            TEST_CASE_ID,
            TEST_AUTHORIZATION_TOKEN
        );

        assertThat(response.getData()).isEqualTo(caseData);
        assertThat(response.getState()).isEqualTo(SolicitorAwaitingPaymentConfirmation);
    }
}
