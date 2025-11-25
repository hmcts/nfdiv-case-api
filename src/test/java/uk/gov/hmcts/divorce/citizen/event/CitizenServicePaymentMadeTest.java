package uk.gov.hmcts.divorce.citizen.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.service.PaymentValidatorService;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Payment;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.time.Clock;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.citizen.event.CitizenServicePaymentMade.CITIZEN_SERVICE_PAYMENT;
import static uk.gov.hmcts.divorce.common.service.PaymentValidatorService.ERROR_PAYMENT_INCOMPLETE;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.PaymentStatus.DECLINED;
import static uk.gov.hmcts.divorce.divorcecase.model.PaymentStatus.SUCCESS;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingDocuments;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingServiceConsideration;
import static uk.gov.hmcts.divorce.divorcecase.model.State.WelshTranslationReview;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_REFERENCE;

@ExtendWith(MockitoExtension.class)
class CitizenServicePaymentMadeTest {
    @Mock
    private PaymentValidatorService paymentValidatorService;

    @Mock
    private Clock clock;


    @InjectMocks
    private CitizenServicePaymentMade citizenServicePaymentMade;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        citizenServicePaymentMade.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CITIZEN_SERVICE_PAYMENT);
    }

    @Test
    void shouldReturnErrorIfPaymentValidationFails() {
        List<ListValue<Payment>> payments = singletonList(new ListValue<>("1", Payment.builder().amount(6000).status(DECLINED).build()));

        CaseData caseData = CaseData.builder()
            .alternativeService(AlternativeService.builder()
                .alternativeServiceType(AlternativeServiceType.ALTERNATIVE_SERVICE)
                .servicePayments(payments)
                .build()
            ).build();

        final var caseDetails = CaseDetails.<CaseData, State>builder().data(caseData).build();
        caseDetails.setId(TEST_CASE_ID);

        when(paymentValidatorService.validatePayments(payments, TEST_CASE_ID))
            .thenReturn(Collections.singletonList(ERROR_PAYMENT_INCOMPLETE));


        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenServicePaymentMade.aboutToSubmit(
            caseDetails, caseDetails
        );

        assertThat(response.getErrors()).isEqualTo(Collections.singletonList(ERROR_PAYMENT_INCOMPLETE));
    }

    @Test
    void givenValidPaymentMadeThenShouldSetPaymentDetails() {
        setMockClock(clock);

        List<ListValue<Payment>> payments = singletonList(new ListValue<>(
            "1", Payment.builder().amount(6000).status(SUCCESS).reference(TEST_REFERENCE).build())
        );

        CaseData caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .alternativeService(AlternativeService.builder()
                .alternativeServiceType(AlternativeServiceType.ALTERNATIVE_SERVICE)
                .serviceApplicationDocsUploadedPreSubmission(YES)
                .servicePayments(payments)
                .build()
            ).build();

        final var details = CaseDetails.<CaseData, State>builder().data(caseData).build();
        details.setId(TEST_CASE_ID);

        when(paymentValidatorService.validatePayments(payments, TEST_CASE_ID)).thenReturn(
            Collections.emptyList()
        );
        when(paymentValidatorService.getLastPayment(payments)).thenReturn(payments.getLast().getValue());

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenServicePaymentMade.aboutToSubmit(details, details);

        AlternativeService alternativeService = response.getData().getAlternativeService();
        assertThat(response.getState()).isEqualTo(AwaitingServiceConsideration);
        assertThat(alternativeService.getServicePaymentFee().getPaymentReference()).isEqualTo(TEST_REFERENCE);
    }

    @Test
    void givenValidPaymentMadeForWelshApplicationThenShouldSetStateToWelshTranslationReview() {
        setMockClock(clock);

        List<ListValue<Payment>> payments = singletonList(new ListValue<>(
            "1", Payment.builder().amount(6000).status(SUCCESS).reference(TEST_REFERENCE).build())
        );

        CaseData caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .alternativeService(AlternativeService.builder()
                .alternativeServiceType(AlternativeServiceType.ALTERNATIVE_SERVICE)
                .serviceApplicationDocsUploadedPreSubmission(YES)
                .servicePayments(payments)
                .build()
            ).build();
        caseData.getApplicant1().setLanguagePreferenceWelsh(YES);

        final var details = CaseDetails.<CaseData, State>builder().data(caseData).build();
        details.setId(TEST_CASE_ID);

        when(paymentValidatorService.validatePayments(payments, TEST_CASE_ID)).thenReturn(
            Collections.emptyList()
        );
        when(paymentValidatorService.getLastPayment(payments)).thenReturn(payments.getLast().getValue());

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenServicePaymentMade.aboutToSubmit(details, details);

        assertThat(response.getState()).isEqualTo(WelshTranslationReview);
        assertThat(response.getData().getApplication().getWelshPreviousState()).isEqualTo(AwaitingServiceConsideration);
    }

    @Test
    void givenDocumentsNotUploadedThenShouldSetStateToAwaitingApplicant() {
        setMockClock(clock);

        List<ListValue<Payment>> payments = singletonList(new ListValue<>(
            "1", Payment.builder().amount(55000).status(DECLINED).reference(TEST_REFERENCE).build())
        );

        CaseData caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .alternativeService(AlternativeService.builder()
                .alternativeServiceType(AlternativeServiceType.ALTERNATIVE_SERVICE)
                .serviceApplicationDocsUploadedPreSubmission(YesOrNo.NO)
                .servicePayments(payments)
                .build()
            ).build();

        final var details = CaseDetails.<CaseData, State>builder().data(caseData).build();
        details.setId(TEST_CASE_ID);

        when(paymentValidatorService.validatePayments(payments, TEST_CASE_ID)).thenReturn(
            Collections.emptyList()
        );
        when(paymentValidatorService.getLastPayment(payments)).thenReturn(payments.getLast().getValue());

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenServicePaymentMade.aboutToSubmit(details, details);

        AlternativeService alternativeService = response.getData().getAlternativeService();
        assertThat(response.getState()).isEqualTo(AwaitingDocuments);
        assertThat(alternativeService.getServicePaymentFee().getPaymentReference()).isEqualTo(TEST_REFERENCE);
    }
}
