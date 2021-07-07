package uk.gov.hmcts.divorce.solicitor.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.SolicitorPaymentMethod;
import uk.gov.hmcts.divorce.common.updater.CaseDataContext;
import uk.gov.hmcts.divorce.common.updater.CaseDataUpdaterChain;
import uk.gov.hmcts.divorce.common.updater.CaseDataUpdaterChainFactory;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.payment.model.Payment;
import uk.gov.hmcts.divorce.payment.model.PaymentStatus;
import uk.gov.hmcts.divorce.solicitor.service.updater.MiniApplicationRemover;
import uk.gov.hmcts.divorce.solicitor.service.updater.SolicitorSubmitNotification;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.common.model.State.AwaitingHWFDecision;
import static uk.gov.hmcts.divorce.common.model.State.AwaitingPayment;
import static uk.gov.hmcts.divorce.common.model.State.Submitted;
import static uk.gov.hmcts.divorce.document.model.DocumentType.DIVORCE_APPLICATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.documentWithType;

@ExtendWith(MockitoExtension.class)
public class SolicitorSubmitApplicationServiceTest {

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
    void shouldCompleteStepsToUpdateApplication() {

        List<ListValue<DivorceDocument>> generatedDocuments = singletonList(documentWithType(DIVORCE_APPLICATION));
        final CaseData caseData = CaseData.builder().build();
        caseData.setDocumentsGenerated(generatedDocuments);
        caseData.getApplication().setStatementOfTruth(null);
        caseData.getApplication().setSolSignStatementOfTruth(YesOrNo.YES);
        final OrderSummary orderSummary = OrderSummary.builder().paymentTotal("55000").build();
        caseData.getApplication().setApplicationFeeOrderSummary(orderSummary);

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

        assertThat(response.getCaseData()).isEqualTo(caseData);
        assertThat(response.getState()).isEqualTo(AwaitingPayment);
    }

    @Test
    void shouldSetStateToAwaitingHWfDecisionWhenPaymentMethodIsHwf() {

        List<ListValue<DivorceDocument>> generatedDocuments = singletonList(documentWithType(DIVORCE_APPLICATION));
        final CaseData caseData = CaseData.builder().build();
        caseData.setDocumentsGenerated(generatedDocuments);
        caseData.getApplication().setStatementOfTruth(null);
        caseData.getApplication().setSolSignStatementOfTruth(YesOrNo.YES);
        caseData.getApplication().setSolPaymentHowToPay(SolicitorPaymentMethod.FEES_HELP_WITH);

        final OrderSummary orderSummary = OrderSummary.builder().paymentTotal("55000").build();
        caseData.getApplication().setApplicationFeeOrderSummary(orderSummary);

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

        assertThat(response.getCaseData()).isEqualTo(caseData);
        assertThat(response.getState()).isEqualTo(AwaitingHWFDecision);
    }

    @Test
    void shouldRemoveDraftApplicationAndNotifyApplicantAndSetStateToSubmittedForAboutToSubmit() {

        List<ListValue<DivorceDocument>> generatedDocuments = singletonList(documentWithType(DIVORCE_APPLICATION));
        final CaseData caseData = CaseData.builder().build();
        caseData.setDocumentsGenerated(generatedDocuments);
        caseData.getApplication().setStatementOfTruth(null);
        caseData.getApplication().setSolSignStatementOfTruth(YesOrNo.YES);
        final OrderSummary orderSummary = OrderSummary.builder().paymentTotal("55000").build();
        caseData.getApplication().setApplicationFeeOrderSummary(orderSummary);

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
        assertThat(response.getCaseData().getApplication().getDateSubmitted()).isEqualTo(LocalDateTime.now(clock));
    }
}
