package uk.gov.hmcts.divorce.divorcecase.model;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.divorce.payment.model.Payment;
import uk.gov.hmcts.divorce.payment.model.PaymentStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.COURT_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.SOLICITOR_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.SolicitorPaymentMethod.FEES_HELP_WITH;
import static uk.gov.hmcts.divorce.document.model.DocumentType.APPLICATION;
import static uk.gov.hmcts.divorce.payment.model.PaymentStatus.DECLINED;
import static uk.gov.hmcts.divorce.payment.model.PaymentStatus.SUCCESS;

class ApplicationTest {

    @Test
    void shouldReturnTrueIfApplicationHasBeenPaidFor() {

        final List<ListValue<Payment>> payments = new ArrayList<>();
        payments.add(paymentValue(payment(55000, SUCCESS)));

        final var application = Application.builder()
            .applicationFeeOrderSummary(OrderSummary.builder().paymentTotal("55000").build())
            .applicationPayments(payments)
            .build();

        assertThat(application.hasBeenPaidFor()).isTrue();
    }

    @Test
    void shouldReturnFalseIfApplicationHasNotBeenPaidFor() {

        final List<ListValue<Payment>> payments = new ArrayList<>();
        payments.add(paymentValue(payment(55000, SUCCESS)));

        final var applicationNullOrderSummary = Application.builder()
            .applicationPayments(payments)
            .build();

        final var applicationOrderSummary = Application.builder()
            .applicationFeeOrderSummary(OrderSummary.builder().paymentTotal("200").build())
            .applicationPayments(payments)
            .build();

        assertThat(applicationNullOrderSummary.hasBeenPaidFor()).isFalse();
        assertThat(applicationOrderSummary.hasBeenPaidFor()).isFalse();
    }

    @Test
    void shouldReturnZeroPaymentTotalForNullApplicationPayments() {
        assertThat(Application.builder().build().getPaymentTotal()).isZero();
    }

    @Test
    void shouldReturnSuccessfulPaymentTotalForApplicationPayments() {

        final List<ListValue<Payment>> payments = new ArrayList<>();
        payments.add(paymentValue(payment(500, SUCCESS)));
        payments.add(paymentValue(payment(50, SUCCESS)));
        payments.add(paymentValue(payment(50, DECLINED)));

        final var application = Application.builder()
            .applicationPayments(payments)
            .build();

        assertThat(application.getPaymentTotal()).isEqualTo(550);
    }

    @Test
    void shouldReturnLastPaymentStatusAndNullIfEmptyOrNull() {

        final List<ListValue<Payment>> payments = new ArrayList<>();
        payments.add(paymentValue(payment(500, SUCCESS)));
        payments.add(paymentValue(payment(50, SUCCESS)));
        payments.add(paymentValue(payment(50, DECLINED)));

        final var applicationMultiple = Application.builder()
            .applicationPayments(payments)
            .build();
        final var applicationEmpty = Application.builder()
            .applicationPayments(emptyList())
            .build();
        final var applicationNull = Application.builder()
            .build();

        assertThat(applicationMultiple.getLastPaymentStatus()).isEqualTo(DECLINED);
        assertThat(applicationEmpty.getLastPaymentStatus()).isNull();
        assertThat(applicationNull.getLastPaymentStatus()).isNull();
    }

    @Test
    void shouldReturnTrueIfStatementOfTruthIsYesForApplicant1() {

        final var application = Application.builder()
            .applicant1StatementOfTruth(YES)
            .build();

        assertThat(application.applicant1HasStatementOfTruth()).isTrue();
    }

    @Test
    void shouldReturnFalseIfStatementOfTruthIsNoForApplicant1() {

        final var application = Application.builder()
            .applicant1StatementOfTruth(NO)
            .build();

        assertThat(application.applicant1HasStatementOfTruth()).isFalse();
    }

    @Test
    void shouldReturnTrueIfStatementOfTruthIsYesForSolicitor() {

        final var application = Application.builder()
            .solSignStatementOfTruth(YES)
            .build();

        assertThat(application.hasSolSignStatementOfTruth()).isTrue();
    }

    @Test
    void shouldReturnFalseIfStatementOfTruthIsNoForSolicitor() {

        final var application = Application.builder()
            .solSignStatementOfTruth(NO)
            .build();

        assertThat(application.hasSolSignStatementOfTruth()).isFalse();
    }

    @Test
    void shouldReturnDateOfSubmissionResponseIfDateSubmittedIsSet() {

        final LocalDateTime dateSubmitted = LocalDateTime.of(2021, 8, 10, 1, 30);
        final var application = Application.builder()
            .dateSubmitted(dateSubmitted)
            .build();

        assertThat(application.getDateOfSubmissionResponse()).isEqualTo(dateSubmitted.toLocalDate().plusDays(14));
    }

    @Test
    void shouldReturnNullIfDateSubmittedIsNotSet() {

        final var application = Application.builder()
            .build();

        assertThat(application.getDateOfSubmissionResponse()).isNull();
    }

    @Test
    void shouldReturnTrueIfApplicant1WantsToHavePapersServedAnotherWayIsYes() {

        final var application = Application.builder()
            .applicant1WantsToHavePapersServedAnotherWay(YES)
            .build();

        assertThat(application.hasAwaitingApplicant1Documents()).isTrue();
    }

    @Test
    void shouldReturnTrueIfApplicant1WantsToHavePapersServedAnotherWayIsNoAndApplicant1CannotUploadSupportingDocument() {

        final var application = Application.builder()
            .applicant1WantsToHavePapersServedAnotherWay(NO)
            .applicant1CannotUploadSupportingDocument(Set.of(APPLICATION))
            .build();

        assertThat(application.hasAwaitingApplicant1Documents()).isTrue();
    }

    @Test
    void shouldReturnFalseIfApplicant1WantsToHavePapersServedAnotherWayIsNoAndEmptyApplicant1CannotUploadSupportingDocument() {

        final var application = Application.builder()
            .applicant1WantsToHavePapersServedAnotherWay(NO)
            .build();

        assertThat(application.hasAwaitingApplicant1Documents()).isFalse();
    }

    @Test
    void shouldReturnFalseIfApplicant1WantsToHavePapersServedAnotherWayIsNull() {

        final var application = Application.builder()
            .build();

        assertThat(application.hasAwaitingApplicant1Documents()).isFalse();
    }

    @Test
    void shouldReturnTrueIfSolicitorService() {

        final var application = Application.builder()
            .solServiceMethod(SOLICITOR_SERVICE)
            .build();

        assertThat(application.isSolicitorServiceMethod()).isTrue();
    }

    @Test
    void shouldReturnFalseIfNotSolicitorService() {

        final var application = Application.builder()
            .solServiceMethod(COURT_SERVICE)
            .build();

        assertThat(application.isSolicitorServiceMethod()).isFalse();
    }

    @Test
    void shouldReturnTrueIfApplicant2ReminderSentIsYes() {

        final var application = Application.builder()
            .applicant2ReminderSent(YES)
            .build();

        assertThat(application.hasApplicant2ReminderBeenSent()).isTrue();
    }

    @Test
    void shouldReturnFalseIfApplicant2ReminderSentIsNoOrNull() {

        final var application1 = Application.builder()
            .applicant2ReminderSent(NO)
            .build();
        final var application2 = Application.builder()
            .build();

        assertThat(application1.hasApplicant2ReminderBeenSent()).isFalse();
        assertThat(application2.hasApplicant2ReminderBeenSent()).isFalse();
    }

    @Test
    void shouldReturnTrueIfApplicant1ReminderSentIsYes() {

        final var application = Application.builder()
            .applicant1ReminderSent(YES)
            .build();

        assertThat(application.hasApplicant1ReminderBeenSent()).isTrue();
    }

    @Test
    void shouldReturnFalseIfApplicant1ReminderSentIsNoOrNull() {

        final var application1 = Application.builder()
            .applicant1ReminderSent(NO)
            .build();
        final var application2 = Application.builder()
            .build();

        assertThat(application1.hasApplicant1ReminderBeenSent()).isFalse();
        assertThat(application2.hasApplicant1ReminderBeenSent()).isFalse();
    }

    @Test
    void shouldReturnTrueIfOverdueNotificationSentIsYes() {

        final var application = Application.builder()
            .overdueNotificationSent(YES)
            .build();

        assertThat(application.hasOverdueNotificationBeenSent()).isTrue();
    }

    @Test
    void shouldReturnFalseIfOverdueNotificationSentIsNoOrNull() {

        final var application1 = Application.builder()
            .overdueNotificationSent(NO)
            .build();
        final var application2 = Application.builder()
            .build();

        assertThat(application1.hasOverdueNotificationBeenSent()).isFalse();
        assertThat(application2.hasOverdueNotificationBeenSent()).isFalse();
    }

    @Test
    void shouldReturnTrueIfApplicant1HelpWithFeesNeedHelpIsSetToYes() {

        final Application application = Application.builder()
            .applicant1HelpWithFees(HelpWithFees.builder()
                .needHelp(YES)
                .build())
            .build();

        assertThat(application.isHelpWithFeesApplication()).isTrue();
    }

    @Test
    void shouldReturnTrueIfSolPaymentHowToPayIsSetToHelpWithFees() {

        final Application application = Application.builder()
            .solPaymentHowToPay(FEES_HELP_WITH)
            .build();

        assertThat(application.isHelpWithFeesApplication()).isTrue();
    }

    @Test
    void shouldReturnFalseIfApplicant1HelpWithFeesNeedHelpIsSetToNo() {

        final Application application = Application.builder()
            .applicant1HelpWithFees(HelpWithFees.builder()
                .needHelp(NO)
                .build())
            .build();

        assertThat(application.isHelpWithFeesApplication()).isFalse();
    }

    @Test
    void shouldReturnFalseIfApplicant1HelpWithFeesNeedHelpIsNull() {

        final Application application = Application.builder()
            .applicant1HelpWithFees(HelpWithFees.builder()
                .build())
            .build();

        assertThat(application.isHelpWithFeesApplication()).isFalse();
    }

    @Test
    void shouldReturnFalseIfApplicant1HelpWithFeesIsNull() {

        final Application application = Application.builder().build();

        assertThat(application.isHelpWithFeesApplication()).isFalse();
    }

    @Test
    void shouldReturnTrueIfSolicitorApplication() {
        final Application application = Application.builder()
            .solSignStatementOfTruth(YES)
            .build();

        assertThat(application.isSolicitorApplication()).isTrue();
    }

    @Test
    void shouldReturnFalseIfNotSolicitorApplication() {

        assertThat(Application.builder().solSignStatementOfTruth(NO).build()
            .isSolicitorApplication()).isFalse();

        assertThat(Application.builder().build()
            .isSolicitorApplication()).isFalse();
    }

    private ListValue<Payment> paymentValue(final Payment payment) {
        return ListValue.<Payment>builder()
            .value(payment)
            .build();
    }

    private Payment payment(final int amount, final PaymentStatus paymentStatus) {
        return Payment.builder()
            .created(LocalDateTime.now())
            .amount(amount)
            .status(paymentStatus)
            .build();
    }
}
