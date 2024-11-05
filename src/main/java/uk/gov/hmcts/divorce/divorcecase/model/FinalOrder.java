package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.access.Applicant2Access;
import uk.gov.hmcts.divorce.divorcecase.model.access.DefaultAccess;
import uk.gov.hmcts.divorce.divorcecase.model.access.SystemUpdateAndSuperUserAccess;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static java.lang.Integer.parseInt;
import static java.util.Objects.nonNull;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.Collection;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedRadioList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.PaymentStatus.SUCCESS;
import static uk.gov.hmcts.divorce.divorcecase.model.SolicitorPaymentMethod.FEE_PAY_BY_ACCOUNT;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FinalOrder {

    @JsonIgnore
    private static final long FINAL_ORDER_OFFSET_WEEKS = 6L;

    @JsonIgnore
    private static final long FINAL_ORDER_OFFSET_DAYS = 1L;

    @JsonIgnore
    private static final long MONTHS_UNTIL_RESPONDENT_CAN_APPLY_FOR_FINAL_ORDER = 3L;

    @JsonIgnore
    private static final long MONTHS_UNTIL_CASE_IS_NO_LONGER_ELIGIBLE_FOR_FINAL_ORDER = 12L;

    @CCD(
        label = "Date Final Order submitted to HMCTS",
        access = {DefaultAccess.class, Applicant2Access.class}
    )
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime dateFinalOrderSubmitted;

    @CCD(
        label = "Date Final Order Eligible From",
        access = {DefaultAccess.class}
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateFinalOrderEligibleFrom;

    @CCD(
        label = "Grant final order?",
        hint = "The final order will be made between "
            + "${applicant1FirstName} ${applicant1LastName} and ${applicant2FirstName} ${applicant2LastName}.",
        access = {DefaultAccess.class}
    )
    private Set<Granted> granted;

    @Getter
    @AllArgsConstructor
    public enum Granted implements HasLabel {

        @JsonProperty("Yes")
        YES("Yes");

        private final String label;
    }

    @CCD(
        label = "Final Order granted date and time",
        access = {DefaultAccess.class}
    )
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime grantedDate;

    @CCD(
        label = "Does the applicant want to apply for a Final Order?",
        access = {DefaultAccess.class}
    )
    private YesOrNo doesApplicant1WantToApplyForFinalOrder;

    @CCD(
        label = "Has applicant1 applied for a final order?",
        access = {DefaultAccess.class, Applicant2Access.class}
    )
    private YesOrNo applicant1AppliedForFinalOrderFirst;

    @CCD(
        label = "Does the ${labelContentApplicant2} want to apply for a Final Order?",
        access = {Applicant2Access.class}
    )
    private YesOrNo doesApplicant2WantToApplyForFinalOrder;

    @CCD(
        label = "Has ${labelContentApplicant2} applied for a final order?",
        access = {DefaultAccess.class, Applicant2Access.class}
    )
    private YesOrNo applicant2AppliedForFinalOrderFirst;

    @CCD(
        label = "${labelContentTheApplicant2UC} final order explanation",
        access = {Applicant2Access.class}
    )
    private String applicant2FinalOrderExplanation;

    @CCD(
        label = "Date from which ${labelContentTheApplicant2} can apply for Final Order",
        access = {DefaultAccess.class}
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateFinalOrderEligibleToRespondent;

    @CCD(
        label = "Is Final Order overdue?",
        access = {DefaultAccess.class}
    )
    private YesOrNo isFinalOrderOverdue;

    @CCD(
        label = "${labelContentApplicantsOrApplicant1s} final order late explanation",
        hint = "You are making this application for a final order over one year from when the conditional order was made. "
            + "Explain to the court why you did not apply for a final order earlier. "
            + "Your answer will be reviewed as part of your application",
        access = {DefaultAccess.class},
        typeOverride = TextArea
    )
    private String applicant1FinalOrderLateExplanation;

    @CCD(
        label = "${labelContentApplicantsOrApplicant1s} final order late explanation (translated)",
        access = {DefaultAccess.class}
    )
    private String applicant1FinalOrderLateExplanationTranslated;

    @CCD(
        label = "${labelContentApplicantsOrApplicant1s} final order late explanation translated to?",
        typeOverride = FixedRadioList,
        typeParameterOverride = "TranslatedToLanguage",
        access = {SystemUpdateAndSuperUserAccess.class}
    )
    private TranslatedToLanguage applicant1FinalOrderLateExplanationTranslatedTo;

    @CCD(
            label = "${labelContentTheApplicant2UC} final order late explanation",
            hint = "You are making this application for a final order over one year from when the conditional order was made. "
                    + "Explain to the court why you did not apply for a final order earlier. "
                    + "Your answer will be reviewed as part of your application",
            access = {Applicant2Access.class},
            typeOverride = TextArea
    )
    private String applicant2FinalOrderLateExplanation;

    @CCD(
        label = "${labelContentRespondentsOrApplicant2s} final order late explanation (translated)",
        access = {Applicant2Access.class}
    )
    private String applicant2FinalOrderLateExplanationTranslated;

    @CCD(
        label = "${labelContentRespondentsOrApplicant2s} final order late explanation translated to?",
        typeOverride = FixedRadioList,
        typeParameterOverride = "TranslatedToLanguage",
        access = {SystemUpdateAndSuperUserAccess.class}
    )
    private TranslatedToLanguage applicant2FinalOrderLateExplanationTranslatedTo;

    @CCD(
        label = "Explain why you need to apply for the final order",
        access = {Applicant2Access.class},
        typeOverride = TextArea
    )
    private String applicant2SolFinalOrderWhyNeedToApply;

    @CCD(
        label = "Respondent solicitor applied for final order?",
        access = {DefaultAccess.class}
    )
    private YesOrNo applicant2SolAppliedForFinalOrder;

    @CCD(
        label = "Date respondent solicitor applied for final order",
        access = {DefaultAccess.class}
    )
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime dateApplicant2SolAppliedForFinalOrder;

    @CCD(
        label = "Respondent applied for final order?",
        access = {DefaultAccess.class, Applicant2Access.class}
    )
    private YesOrNo applicant2AppliedForFinalOrder;

    @CCD(
        label = "Date respondent applied for final order",
        access = {DefaultAccess.class}
    )
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime dateApplicant2AppliedForFinalOrder;

    @CCD(
        label = "Respondent solicitor responsible for final order application",
        access = {DefaultAccess.class}
    )
    private String applicant2SolResponsibleForFinalOrder;

    @CCD(
        label = "Here are your order details",
        access = {DefaultAccess.class}
    )
    private OrderSummary applicant2SolFinalOrderFeeOrderSummary;

    @CCD(
        label = "Respondent solicitor final order fee (in pounds)",
        access = {DefaultAccess.class}
    )
    private String applicant2SolFinalOrderFeeInPounds;

    @CCD(
        label = "Here are your order details",
        access = {DefaultAccess.class, Applicant2Access.class}
    )
    private OrderSummary applicant2FinalOrderFeeOrderSummary;

    @CCD(
        label = "Final Order Fee Service Request Reference",
        access = {DefaultAccess.class, Applicant2Access.class}
    )
    private String applicant2FinalOrderFeeServiceRequestReference;

    @CCD(
        label = "Respondent final order fee (in pounds)",
        access = {DefaultAccess.class, Applicant2Access.class}
    )
    private String applicant2FinalOrderFeeInPounds;

    @CCD(
        label = "How is payment being made?",
        typeOverride = FixedRadioList,
        typeParameterOverride = "SolicitorPaymentMethod",
        access = {DefaultAccess.class}
    )
    private SolicitorPaymentMethod applicant2SolPaymentHowToPay;

    @JsonIgnore
    public boolean isSolicitorPaymentMethodPba() {
        return FEE_PAY_BY_ACCOUNT.equals(this.getApplicant2SolPaymentHowToPay());
    }

    @CCD(
        label = "Account number",
        access = {DefaultAccess.class}
    )
    private DynamicList finalOrderPbaNumbers;

    @CCD(
        label = "Respondent solicitor PBA number for final order",
        access = {DefaultAccess.class}
    )
    private String finalOrderPbaNumber;

    @CCD(
        label = "Fee account reference",
        hint = "This will appear on your statement to help you identify this payment",
        access = {DefaultAccess.class}
    )
    private String applicant2SolFinalOrderFeeAccountReference;

    @CCD(
        label = "Payments",
        typeOverride = Collection,
        typeParameterOverride = "Payment",
        access = {DefaultAccess.class, Applicant2Access.class}
    )
    private List<ListValue<Payment>> finalOrderPayments;

    @JsonIgnore
    public void updateFinalOrderWithApp2SolPaymentDetails(
        OrderSummary finalOrderFeeOrderSummary,
        CaseData caseData,
        String paymentReference,
        String pbaNumber
    ) {
        var payment = Payment
            .builder()
            .amount(parseInt(finalOrderFeeOrderSummary.getPaymentTotal()))
            .channel("online")
            .feeCode(finalOrderFeeOrderSummary.getFees().get(0).getValue().getCode())
            .reference(paymentReference)
            .status(SUCCESS)
            .build();

        var finalOrder = caseData.getFinalOrder();

        if (isEmpty(finalOrder.getFinalOrderPayments())) {
            List<ListValue<Payment>> payments = new ArrayList<>();
            payments.add(new ListValue<>(UUID.randomUUID().toString(), payment));
            finalOrder.setFinalOrderPayments(payments);
        } else {
            finalOrder.getFinalOrderPayments()
                .add(new ListValue<>(UUID.randomUUID().toString(), payment));
        }

        finalOrder.setFinalOrderPbaNumber(pbaNumber);
    }

    @JsonUnwrapped(prefix = "app2SolFoHWF")
    @CCD(access = {Applicant2Access.class})
    private HelpWithFees applicant2SolFinalOrderHelpWithFees;

    @JsonUnwrapped(prefix = "applicant2FoHWF")
    @CCD(access = {Applicant2Access.class})
    private HelpWithFees applicant2FinalOrderHelpWithFees;

    @CCD(
        label = "The applicant believes that the facts stated in this application are true.",
        access = {DefaultAccess.class}
    )
    private YesOrNo applicant1FinalOrderStatementOfTruth;

    @CCD(
        label = "The ${labelContentApplicant2} believes that the facts stated in this application are true.",
        access = {Applicant2Access.class}
    )
    private YesOrNo applicant2FinalOrderStatementOfTruth;

    @CCD(
        label = "Final date to apply for Final Order",
        access = {DefaultAccess.class}
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateFinalOrderNoLongerEligible;

    @CCD(
        label = "Has the applicant been sent a reminder to apply for the Final Order?",
        access = {DefaultAccess.class}
    )
    private YesOrNo finalOrderReminderSentApplicant1;

    @CCD(
        label = "Has the first in time applicant been notified that other applicant has not applied for the Final Order?",
        access = {DefaultAccess.class}
    )
    private YesOrNo finalOrderFirstInTimeNotifiedOtherApplicantNotApplied;

    @CCD(
        label = "Has applicant been notified they can continue and switch to sole for final order?",
        access = {DefaultAccess.class}
    )
    private YesOrNo finalOrderApplicant1NotifiedCanSwitchToSoleAfterIntention;

    @CCD(
        label = "Has applicant been notified they can continue and switch to sole for final order?",
        access = {DefaultAccess.class}
    )
    private YesOrNo finalOrderApplicant2NotifiedCanSwitchToSoleAfterIntention;

    @CCD(
        label = "Has the respondent been sent a reminder to apply for the Final Order?",
        access = {DefaultAccess.class}
    )
    private YesOrNo finalOrderReminderSentApplicant2;

    @CCD(
        label = "Link to scanned d36 form"
    )
    private Document scannedD36Form;

    @CCD(
        label = "Date D36 form was scanned"
    )

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime dateD36FormScanned;

    @CCD(
        label = "What application type is the D36?",
        typeOverride = FixedRadioList,
        typeParameterOverride = "OfflineApplicationType",
        access = {DefaultAccess.class}
    )
    private OfflineApplicationType d36ApplicationType;

    @CCD(
        label = "Who is submitting the D36?",
        typeOverride = FixedRadioList,
        typeParameterOverride = "OfflineWhoApplying",
        access = {DefaultAccess.class}
    )
    private OfflineWhoApplying d36WhoApplying;

    @CCD(
        label = "Switched to sole",
        access = {DefaultAccess.class}
    )
    private YesOrNo finalOrderSwitchedToSole;


    @CCD(
        label = "Applicant 1 can intend to switch to sole at final order stage",
        access = {DefaultAccess.class}
    )
    private YesOrNo applicant1CanIntendToSwitchToSoleFo;

    @CCD(
        label = "Applicant 1 intends to switch to sole?",
        access = {DefaultAccess.class}
    )
    private Set<IntendsToSwitchToSole> applicant1IntendsToSwitchToSole;

    @CCD(
        label = "Applicant 2 can intend to switch to sole at final order stage",
        access = {DefaultAccess.class, Applicant2Access.class}
    )
    private YesOrNo applicant2CanIntendToSwitchToSoleFo;

    @CCD(
        label = "Applicant 2 intends to switch to sole?",
        access = {DefaultAccess.class, Applicant2Access.class}
    )
    private Set<IntendsToSwitchToSole> applicant2IntendsToSwitchToSole;

    @Getter
    @AllArgsConstructor
    public enum IntendsToSwitchToSole implements HasLabel {

        @JsonProperty("Yes")
        I_INTEND_TO_SWITCH_TO_SOLE(
            """
                I intend to apply for a final order as sole applicant on behalf of my client,
                and I want the court to notify the other applicant
                """
        );

        private final String label;
    }

    @CCD(
        label = "Does applicant 1 intend to switch to sole?",
        access = {DefaultAccess.class}
    )
    private YesOrNo doesApplicant1IntendToSwitchToSole;

    @CCD(
        label = "Date applicant 1 declared their intention to switch to sole final order to switch to sole final order",
        access = {DefaultAccess.class}
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateApplicant1DeclaredIntentionToSwitchToSoleFo;

    @CCD(
        label = "Does applicant 2 intend to switch to sole?",
        access = {DefaultAccess.class, Applicant2Access.class}
    )
    private YesOrNo doesApplicant2IntendToSwitchToSole;

    @CCD(
        label = "Date applicant 2 declared their intention to switch to sole final order to switch to sole final order",
        access = {DefaultAccess.class, Applicant2Access.class}
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateApplicant2DeclaredIntentionToSwitchToSoleFo;

    @CCD(
        label = "Expedited Final Order Authorisation",
        access = {DefaultAccess.class}
    )
    private ExpeditedFinalOrderAuthorisation expeditedFinalOrderAuthorisation;

    @CCD(
        label = "Overdue Final Order Authorisation",
        access = {DefaultAccess.class}
    )
    private FinalOrderAuthorisation overdueFinalOrderAuthorisation;

    @JsonIgnore
    public boolean applicant2NeedsHelpWithFees() {
        return Objects.nonNull(applicant2FinalOrderHelpWithFees)
            && Objects.nonNull(applicant2FinalOrderHelpWithFees.getNeedHelp())
            && applicant2FinalOrderHelpWithFees.getNeedHelp().toBoolean();
    }

    @JsonIgnore
    public LocalDate getDateFinalOrderEligibleFrom(LocalDateTime dateTime) {
        return dateTime.toLocalDate().plusWeeks(FINAL_ORDER_OFFSET_WEEKS).plusDays(FINAL_ORDER_OFFSET_DAYS);
    }

    @JsonIgnore
    public LocalDate calculateDateFinalOrderEligibleToRespondent() {
        return dateFinalOrderEligibleFrom.plusMonths(MONTHS_UNTIL_RESPONDENT_CAN_APPLY_FOR_FINAL_ORDER);
    }

    @JsonIgnore
    public LocalDate calculateDateFinalOrderNoLongerEligible(final LocalDate date) {
        return date.plusMonths(MONTHS_UNTIL_CASE_IS_NO_LONGER_ELIGIBLE_FOR_FINAL_ORDER);
    }

    @JsonIgnore
    public boolean hasFinalOrderReminderSentApplicant1() {
        return YES.equals(finalOrderReminderSentApplicant1);
    }

    @JsonIgnore
    public boolean hasFirstInTimeApplicantBeenNotifiedOtherApplicantHasNotApplied() {
        return YES.equals(finalOrderFirstInTimeNotifiedOtherApplicantNotApplied);
    }

    @JsonIgnore
    public boolean hasApplicant1BeenNotifiedTheyCanContinueSwitchToSoleFO() {
        return YES.equals(finalOrderApplicant1NotifiedCanSwitchToSoleAfterIntention);
    }

    @JsonIgnore
    public boolean hasApplicant2BeenNotifiedTheyCanContinueSwitchToSoleFO() {
        return YES.equals(finalOrderApplicant2NotifiedCanSwitchToSoleAfterIntention);
    }

    @JsonIgnore
    public boolean hasFinalOrderLateExplanation() {
        return nonNull(applicant1FinalOrderLateExplanation)
            || nonNull(applicant2FinalOrderLateExplanation)
            || nonNull(applicant1FinalOrderLateExplanationTranslated)
            || nonNull(applicant2FinalOrderLateExplanationTranslated);
    }
}
