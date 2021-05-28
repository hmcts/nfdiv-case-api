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
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.SolToPay;
import uk.gov.hmcts.divorce.common.updater.CaseDataContext;
import uk.gov.hmcts.divorce.common.updater.CaseDataUpdaterChain;
import uk.gov.hmcts.divorce.common.updater.CaseDataUpdaterChainFactory;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.payment.FeesAndPaymentsClient;
import uk.gov.hmcts.divorce.payment.model.Payment;
import uk.gov.hmcts.divorce.payment.model.PaymentStatus;
import uk.gov.hmcts.divorce.solicitor.service.updater.MiniApplicationRemover;
import uk.gov.hmcts.divorce.solicitor.service.updater.SolicitorSubmitNotification;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static feign.Request.HttpMethod.GET;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
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
import static uk.gov.hmcts.divorce.common.model.State.AwaitingHWFDecision;
import static uk.gov.hmcts.divorce.common.model.State.AwaitingPayment;
import static uk.gov.hmcts.divorce.common.model.State.Submitted;
import static uk.gov.hmcts.divorce.document.model.DocumentType.DIVORCE_APPLICATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.FEE_CODE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ISSUE_FEE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.documentWithType;
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

    @Mock
    private Clock clock;

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

        List<ListValue<DivorceDocument>> generatedDocuments = singletonList(documentWithType(DIVORCE_APPLICATION));
        final CaseData caseData = CaseData.builder().build();
        caseData.setDocumentsGenerated(generatedDocuments);
        caseData.setStatementOfTruth(null);
        caseData.setSolSignStatementOfTruth(YesOrNo.YES);
        final OrderSummary orderSummary = OrderSummary.builder().paymentTotal("55000").build();
        caseData.setApplicationFeeOrderSummary(orderSummary);

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
        assertThat(response.getState()).isEqualTo(AwaitingPayment);
    }

    @Test
    void shouldSetStateToAwaitingHWfDecisionWhenPaymentMethodIsHwf() {

        List<ListValue<DivorceDocument>> generatedDocuments = singletonList(documentWithType(DIVORCE_APPLICATION));
        final CaseData caseData = CaseData.builder().build();
        caseData.setDocumentsGenerated(generatedDocuments);
        caseData.setStatementOfTruth(null);
        caseData.setSolSignStatementOfTruth(YesOrNo.YES);
        caseData.setSolPaymentHowToPay(SolToPay.FEES_HELP_WITH);

        final OrderSummary orderSummary = OrderSummary.builder().paymentTotal("55000").build();
        caseData.setApplicationFeeOrderSummary(orderSummary);

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
        assertThat(response.getState()).isEqualTo(AwaitingHWFDecision);
    }

    @Test
    void shouldRemoveDraftApplicationAndNotifyApplicantAndSetStateToSubmittedForAboutToSubmit() {

        List<ListValue<DivorceDocument>> generatedDocuments = singletonList(documentWithType(DIVORCE_APPLICATION));
        final CaseData caseData = CaseData.builder().build();
        caseData.setDocumentsGenerated(generatedDocuments);
        caseData.setStatementOfTruth(null);
        caseData.setSolSignStatementOfTruth(YesOrNo.YES);
        final OrderSummary orderSummary = OrderSummary.builder().paymentTotal("55000").build();
        caseData.setApplicationFeeOrderSummary(orderSummary);

        ListValue<Payment> payment = new ListValue<>(null, Payment
            .builder()
            .paymentAmount(55000)
            .paymentChannel("online")
            .paymentFeeId("FEE0001")
            .paymentReference("paymentRef")
            .paymentSiteId("AA04")
            .paymentStatus(PaymentStatus.SUCCESS)
            .paymentTransactionId("ge7po9h5bhbtbd466424src9tk")
            .build());

        caseData.setPayments(singletonList(payment));

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
        when(clock.instant()).thenReturn(Instant.now());
        when(clock.getZone()).thenReturn(ZoneId.of("Etc/UTC"));

        final var response = solicitorSubmitApplicationService.aboutToSubmit(
            caseData,
            TEST_CASE_ID,
            TEST_AUTHORIZATION_TOKEN
        );

        assertThat(response.getState()).isEqualTo(Submitted);
        assertThat(response.getData().getDateSubmitted()).isEqualTo(LocalDateTime.now(clock));
    }
}
