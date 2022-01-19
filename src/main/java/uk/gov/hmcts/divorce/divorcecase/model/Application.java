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
import uk.gov.hmcts.divorce.divorcecase.model.access.CaseworkerAccess;
import uk.gov.hmcts.divorce.divorcecase.model.access.DefaultAccess;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.divorce.payment.model.Payment;
import uk.gov.hmcts.divorce.payment.model.PaymentStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static java.lang.Integer.parseInt;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.Collection;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedRadioList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.SOLICITOR_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.SolicitorPaymentMethod.FEES_HELP_WITH;
import static uk.gov.hmcts.divorce.divorcecase.model.SolicitorPaymentMethod.FEE_PAY_BY_ACCOUNT;
import static uk.gov.hmcts.divorce.payment.model.PaymentStatus.SUCCESS;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Application {
    @CCD(ignore = true)
    private static final int SUBMISSION_RESPONSE_DAYS = 14;

    @CCD(
        label = "Has the applicant's ${labelContentMarriageOrCivilPartnership} broken down irretrievably?",
        access = {DefaultAccess.class}
    )
    private YesOrNo applicant1ScreenHasMarriageBroken;

    @CCD(
        label = "Has applicant 2's marriage broken down irretrievably?",
        access = {Applicant2Access.class}
    )
    private YesOrNo applicant2ScreenHasMarriageBroken;

    @CCD(
        label = "The applicant has marriage cert?",
        access = {DefaultAccess.class}
    )
    private YesOrNo screenHasMarriageCert;

    @CCD(
        access = {DefaultAccess.class}
    )
    @JsonUnwrapped(prefix = "marriage")
    @Builder.Default
    private MarriageDetails marriageDetails = new MarriageDetails();

    @JsonUnwrapped(prefix = "jurisdiction")
    @Builder.Default
    private Jurisdiction jurisdiction = new Jurisdiction();

    @JsonUnwrapped(prefix = "solService")
    @Builder.Default
    private SolicitorService solicitorService = new SolicitorService();

    @JsonUnwrapped(prefix = "applicant1HWF")
    @CCD(access = {DefaultAccess.class})
    private HelpWithFees applicant1HelpWithFees;

    @JsonUnwrapped(prefix = "applicant2HWF")
    @CCD(access = {Applicant2Access.class})
    private HelpWithFees applicant2HelpWithFees;

    @CCD(
        label = "Who is the applicant divorcing?",
        hint = "Husband or Wife?",
        typeOverride = FixedList,
        typeParameterOverride = "WhoDivorcing",
        access = {DefaultAccess.class}
    )
    private WhoDivorcing divorceWho;

    @CCD(
        label = "Does this case require urgent issue due to jurisdiction or other financial matters?",
        access = {DefaultAccess.class}
    )
    private YesOrNo solUrgentCase;

    @CCD(
        label = "Any supporting information or instructions?",
        typeOverride = TextArea,
        access = {DefaultAccess.class}
    )
    private String solUrgentCaseSupportingInformation;

    @CCD(
        label = "The applicant wants/will to apply to have the papers served to the respondent another way.",
        hint = "For example by email, text message or social media. This is a separate application with "
            + "an additional fee, which will need to be reviewed by a judge.",
        access = {DefaultAccess.class}
    )
    private YesOrNo applicant1WantsToHavePapersServedAnotherWay;

    @CCD(
        label = "How would you like the respondent to be served?",
        access = {DefaultAccess.class}
    )
    private ServiceMethod solServiceMethod;

    @CCD(
        label = "I have discussed the possibility of a reconciliation with the applicant.",
        access = {DefaultAccess.class}
    )
    private YesOrNo solStatementOfReconciliationCertify;

    @CCD(
        label = "I have given the applicant the names and addresses of persons qualified to help effect a reconciliation.",
        access = {DefaultAccess.class}
    )
    private YesOrNo solStatementOfReconciliationDiscussed;

    @CCD(
        label = "This confirms what you are asking the court to do on behalf of the applicant. It’s known as ‘the prayer’.",
        access = {DefaultAccess.class}
    )
    private Set<ThePrayer> applicant1PrayerHasBeenGivenCheckbox;

    @Getter
    @AllArgsConstructor
    public enum ThePrayer implements HasLabel {

        @JsonProperty("Yes")
        I_CONFIRM("I confirm the applicant is applying to the court to:");

        private final String label;
    }

    @CCD(
        label = "The applicant has given their \"prayer\".",
        hint = "\"The prayer\" means they confirm they wish to dissolve the union, pay any fees (if applicable),"
            + " and have decided how money and property will be split (\"financial order\").",
        access = {Applicant2Access.class}
    )
    private Set<ThePrayer> applicant2PrayerHasBeenGivenCheckbox;

    @CCD(
        label = "The applicant believes that the facts stated in this application are true.",
        access = {DefaultAccess.class}
    )
    private YesOrNo applicant1StatementOfTruth;

    @CCD(
        label = "The applicant believes that the facts stated in this application are true.",
        access = {Applicant2Access.class}
    )
    private YesOrNo applicant2StatementOfTruth;

    @CCD(
        label = "I am duly authorised by the applicant to sign this statement.",
        access = {DefaultAccess.class}
    )
    private YesOrNo solSignStatementOfTruth;

    @CCD(
        label = "I am duly authorised by the applicant to sign this statement.",
        access = {DefaultAccess.class}
    )
    private YesOrNo applicant2SolSignStatementOfTruth;

    @CCD(
        label = "Your name",
        access = {DefaultAccess.class}
    )
    private String solStatementOfReconciliationName;

    @CCD(
        label = "Your name",
        access = {DefaultAccess.class}
    )
    private String applicant2SolStatementOfReconciliationName;

    @CCD(
        label = "Name of your firm",
        access = {DefaultAccess.class}
    )
    private String solStatementOfReconciliationFirm;

    @CCD(
        label = "Name of your firm",
        access = {DefaultAccess.class}
    )
    private String applicant2SolStatementOfReconciliationFirm;

    @CCD(
        label = "Additional comments",
        hint = "For the attention of court staff. These comments will not form part of the application",
        typeOverride = TextArea,
        access = {DefaultAccess.class}
    )
    private String statementOfReconciliationComments;

    @CCD(
        label = "Additional comments",
        hint = "For the attention of court staff. These comments will not form part of the application",
        typeOverride = TextArea,
        access = {DefaultAccess.class}
    )
    private String applicant2StatementOfReconciliationComments;

    // TODO move to OrderSummary?
    @CCD(
        label = "Solicitor application fee (in pounds)",
        access = {DefaultAccess.class}
    )
    private String solApplicationFeeInPounds;

    @CCD(
        label = "How is payment being made?",
        typeOverride = FixedRadioList,
        typeParameterOverride = "SolicitorPaymentMethod",
        access = {DefaultAccess.class}
    )
    private SolicitorPaymentMethod solPaymentHowToPay;

    @CCD(
        label = "Account number",
        access = {DefaultAccess.class}
    )
    private DynamicList pbaNumbers;

    @CCD(
        label = "Fee account reference",
        hint = "This will appear on your statement to help you identify this payment",
        access = {DefaultAccess.class}
    )
    private String feeAccountReference;

    @CCD(
        label = "Here are your order details",
        access = {DefaultAccess.class}
    )
    private OrderSummary applicationFeeOrderSummary;

    @CCD(
        label = "The respondent agrees that the divorce service can send notifications by email.",
        access = {DefaultAccess.class}
    )
    private YesOrNo applicant2AgreeToReceiveEmails;

    @CCD(
        label = "Is the respondent's email address known?",
        access = {DefaultAccess.class}
    )
    private YesOrNo applicant1KnowsApplicant2EmailAddress;

    @CCD(
        label = "Is the respondent's home address known?",
        access = {DefaultAccess.class}
    )
    private YesOrNo applicant1KnowsApplicant2Address;

    @CCD(
        label = "Applicant 2 is using digital channel?",
        access = {DefaultAccess.class}
    )
    private YesOrNo app2ContactMethodIsDigital;

    @CCD(
        label = "Applicant 1 cannot upload supporting documents",
        access = {DefaultAccess.class}
    )
    private Set<DocumentType> applicant1CannotUploadSupportingDocument;

    @CCD(
        label = "Applicant 2 cannot upload supporting documents",
        access = {DefaultAccess.class, Applicant2Access.class}
    )
    private Set<DocumentType> applicant2CannotUploadSupportingDocument;

    @CCD(
        label = "All documents uploaded",
        hint = "Select yes to submit the case, if all documents have been uploaded",
        access = {DefaultAccess.class}
    )
    private YesOrNo documentUploadComplete;

    @CCD(
        label = "Link to online application",
        access = {DefaultAccess.class}
    )
    private Document miniApplicationLink;

    @CCD(
        label = "Date submitted to HMCTS",
        access = {DefaultAccess.class}
    )
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime dateSubmitted;

    @CCD(
        label = "Is the information provided on the case correct?",
        access = {Applicant2Access.class}
    )
    private YesOrNo applicant2ConfirmApplicant1Information;

    @CCD(
        label = "Explain what is incorrect or needs changing.",
        access = {Applicant2Access.class},
        typeOverride = TextArea
    )
    private String applicant2ExplainsApplicant1IncorrectInformation;

    @CCD(
        label = "Date when the application was issued",
        access = {CaseworkerAccess.class}
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate issueDate;

    @CCD(
        label = "Date when the application was reissued",
        access = {CaseworkerAccess.class}
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate reissueDate;

    @CCD(
        label = "Date when the application was created",
        access = {DefaultAccess.class}
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate createdDate;

    @CCD(
        label = "Reject reason",
        access = {CaseworkerAccess.class}
    )
    private RejectReason rejectReason;

    @CCD(
        label = "Previous state",
        access = {CaseworkerAccess.class}
    )
    private State previousState;

    @CCD(
        label = "Payments",
        typeOverride = Collection,
        typeParameterOverride = "Payment",
        access = {DefaultAccess.class}
    )
    private List<ListValue<Payment>> applicationPayments;

    @CCD(
        label = "Notification of overdue application sent?",
        access = {DefaultAccess.class}
    )
    private YesOrNo overdueNotificationSent;

    @CCD(
        label = "Reminder that applicant 2 has approved application sent?",
        access = {DefaultAccess.class}
    )
    private YesOrNo applicant1ReminderSent;

    @CCD(
        label = "Reminder that applicant 2 needs to review joint application",
        access = {DefaultAccess.class}
    )
    private YesOrNo applicant2ReminderSent;

    @CCD(
        label = "Reminder sent to Applicants indicating they can apply for Conditional Order",
        access = {DefaultAccess.class}
    )
    private YesOrNo applicantsRemindedCanApplyForConditionalOrder;

    @CCD(
        label = "What would you like to reissue?",
        typeOverride = FixedRadioList,
        typeParameterOverride = "ReissueOption"
    )
    private ReissueOption reissueOption;

    @CCD(
        label = "Does Applicant 2 needs help with fees?",
        access = {Applicant2Access.class}
    )
    private YesOrNo applicant2NeedsHelpWithFees;

    @CCD(
        label = "Link to applicant 1 solicitors answers",
        access = {CaseworkerAccess.class}
    )
    private Document applicant1SolicitorAnswersLink;

    @JsonIgnore
    public boolean hasBeenPaidFor() {
        return null != applicationFeeOrderSummary
            && parseInt(applicationFeeOrderSummary.getPaymentTotal()) == getPaymentTotal();
    }

    @JsonIgnore
    public Integer getPaymentTotal() {
        return applicationPayments == null
            ? 0
            : applicationPayments
            .stream()
            .filter(p -> p.getValue().getStatus().equals(SUCCESS))
            .map(p -> p.getValue().getAmount())
            .reduce(0, Integer::sum);
    }

    @JsonIgnore
    public PaymentStatus getLastPaymentStatus() {
        return applicationPayments == null || applicationPayments.isEmpty()
            ? null
            : applicationPayments.get(applicationPayments.size() - 1).getValue().getStatus();
    }

    @JsonIgnore
    public boolean applicant1HasStatementOfTruth() {
        return YES.equals(applicant1StatementOfTruth);
    }

    @JsonIgnore
    public boolean hasSolSignStatementOfTruth() {
        return YES.equals(solSignStatementOfTruth);
    }

    @JsonIgnore
    public boolean hasStatementOfTruth() {
        return applicant1HasStatementOfTruth() || hasSolSignStatementOfTruth();
    }

    @JsonIgnore
    public LocalDate getDateOfSubmissionResponse() {
        return dateSubmitted == null ? null : dateSubmitted.plusDays(SUBMISSION_RESPONSE_DAYS).toLocalDate();
    }

    @JsonIgnore
    public boolean hasAwaitingApplicant1Documents() {
        return applicant1WantsToHavePapersServedAnotherWay != null
            && applicant1WantsToHavePapersServedAnotherWay.toBoolean()
            || !isEmpty(applicant1CannotUploadSupportingDocument);
    }

    @JsonIgnore
    public boolean hasAwaitingApplicant2Documents() {
        return !isEmpty(applicant2CannotUploadSupportingDocument);
    }

    @JsonIgnore
    public boolean isSolicitorServiceMethod() {
        return SOLICITOR_SERVICE.equals(solServiceMethod);
    }

    @JsonIgnore
    public boolean hasApplicant2ReminderBeenSent() {
        return YES.equals(applicant2ReminderSent);
    }

    @JsonIgnore
    public boolean hasApplicant1ReminderBeenSent() {
        return YES.equals(applicant1ReminderSent);
    }

    @JsonIgnore
    public boolean hasOverdueNotificationBeenSent() {
        return YES.equals(overdueNotificationSent);
    }

    @JsonIgnore
    public boolean isHelpWithFeesApplication() {
        return null != applicant1HelpWithFees
            && null != applicant1HelpWithFees.getNeedHelp()
            && applicant1HelpWithFees.getNeedHelp().toBoolean()
            || FEES_HELP_WITH.equals(solPaymentHowToPay);
    }

    @JsonIgnore
    public boolean isSolicitorApplication() {
        return hasSolSignStatementOfTruth();
    }

    @JsonIgnore
    public boolean isSolicitorPaymentMethodPba() {
        return FEE_PAY_BY_ACCOUNT.equals(this.getSolPaymentHowToPay());
    }

    @JsonIgnore
    public boolean isApplicant1OffLine() {
        return false;
    }

    @JsonIgnore
    public boolean isApplicant2OffLine() {
        return null != applicant1KnowsApplicant2EmailAddress && !applicant1KnowsApplicant2EmailAddress.toBoolean();
    }

}
