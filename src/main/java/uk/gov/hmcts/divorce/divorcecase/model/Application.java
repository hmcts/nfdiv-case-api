package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.access.Applicant2Access;
import uk.gov.hmcts.divorce.divorcecase.model.access.CaseworkerAccess;
import uk.gov.hmcts.divorce.divorcecase.model.access.DefaultAccess;
import uk.gov.hmcts.divorce.divorcecase.model.access.SystemUpdateAndSuperUserAccess;
import uk.gov.hmcts.divorce.document.model.DocumentType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static java.lang.Integer.parseInt;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.Collection;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedRadioList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.PaymentStatus.SUCCESS;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.COURT_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.PERSONAL_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.SOLICITOR_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.SolicitorPaymentMethod.FEES_HELP_WITH;
import static uk.gov.hmcts.divorce.divorcecase.model.SolicitorPaymentMethod.FEE_PAY_BY_ACCOUNT;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Application {
    @CCD(ignore = true)
    private static final int SUBMISSION_RESPONSE_DAYS = 28;

    @CCD(
        label = "Has the applicant's ${labelContentMarriageOrCivilPartnership} broken down irretrievably?",
        access = {DefaultAccess.class}
    )
    private YesOrNo applicant1ScreenHasMarriageBroken;

    @CCD(
        label = "Has applicant 2's ${labelContentMarriageOrCivilPartnership} broken down irretrievably?",
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
    @CCD(access = {SystemUpdateAndSuperUserAccess.class})
    private Jurisdiction jurisdiction = new Jurisdiction();

    @JsonUnwrapped(prefix = "solService")
    @Builder.Default
    @CCD(access = {DefaultAccess.class})
    private SolicitorService solicitorService = new SolicitorService();

    @JsonUnwrapped(prefix = "applicant1HWF")
    @CCD(access = {DefaultAccess.class})
    private HelpWithFees applicant1HelpWithFees;

    @JsonUnwrapped(prefix = "applicant2HWF")
    @CCD(access = {Applicant2Access.class})
    private HelpWithFees applicant2HelpWithFees;

    @CCD(
        access = {DefaultAccess.class}
    )
    @JsonUnwrapped(prefix = "sts")
    @Builder.Default
    private SwitchedToSole switchedToSole = new SwitchedToSole();

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
        label = "Want to apply to have the papers 'served' (sent to respondent) in another way",
        hint = "For example by email, text message or social media. This is a separate application with "
            + "an additional fee, which will need to be reviewed by a judge.",
        access = {DefaultAccess.class}
    )
    private YesOrNo applicant1WantsToHavePapersServedAnotherWay;

    @CCD(
        label = "How would you like the respondent to be served?",
        access = {DefaultAccess.class}
    )
    private ServiceMethod serviceMethod;

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
        label = "Application Fee Service Request Reference",
        access = {DefaultAccess.class}
    )
    private String applicationFeeServiceRequestReference;

    @CCD(
        label = "The respondent agrees that the divorce service can send notifications by email.",
        access = {DefaultAccess.class, Applicant2Access.class}
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
        label = "Supporting documents need posting?",
        access = {DefaultAccess.class}
    )
    private YesOrNo applicant1CannotUpload;

    @CCD(
        label = "Which documents need posting",
        access = {DefaultAccess.class}
    )
    private Set<DocumentType> applicant1CannotUploadSupportingDocument;

    @CCD(
        label = "Supporting documents need posting?",
        access = {DefaultAccess.class, Applicant2Access.class}
    )
    private YesOrNo applicant2CannotUpload;

    @CCD(
        label = "Which documents need posting",
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
        label = "Current state",
        access = {CaseworkerAccess.class}
    )
    private State currentState;

    @CCD(
        label = "Previous state",
        access = {CaseworkerAccess.class}
    )
    private State previousState;

    @CCD(
        label = "Welsh previous state",
        access = {CaseworkerAccess.class}
    )
    private State welshPreviousState;

    @CCD(
        label = "Bulk Scan state to transition to",
        typeOverride = FixedList,
        typeParameterOverride = "State",
        access = {SystemUpdateAndSuperUserAccess.class}
    )
    private State stateToTransitionApplicationTo;

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
        label = "Reminder that respondent solicitor needs to respond to the application",
        access = {DefaultAccess.class}
    )
    private YesOrNo respondentSolicitorReminderSent;

    @CCD(
        label = "Reminder sent to Applicants indicating they can apply for Conditional Order",
        access = {DefaultAccess.class}
    )
    private YesOrNo applicantsRemindedCanApplyForConditionalOrder;

    @CCD(
        label = "Reminder sent to Applicants indicating other party has applied for final order and their final order is awaiting",
        access = {DefaultAccess.class}
    )
    private YesOrNo applicantsRemindedAwaitingJointFinalOrder;

    @CCD(
        label = "Notification sent to Joint Applicant indicating they can switch to sole",
        access = {DefaultAccess.class}
    )
    private YesOrNo jointApplicantNotifiedCanSwitchToSole;

    @CCD(
        label = "CO Pronounced cover letters resent",
        access = {DefaultAccess.class}
    )
    private YesOrNo coPronouncedCoverLetterResent;

    @CCD(
        label = "CO Pronounced confidential cover letters resent",
        access = {DefaultAccess.class}
    )
    private YesOrNo coPronouncedConfidentialCoverLetterResent;

    @CCD(
        label = "CO Pronounced confidential cover letters resent",
        access = {DefaultAccess.class}
    )
    private YesOrNo coPronouncedForceConfidentialCoverLetterResent;

    @CCD(
        label = "CO Pronounced confidential cover letters resent",
        access = {DefaultAccess.class}
    )
    private YesOrNo coPronouncedForceConfidentialCoverLetterResentAgain;

    @CCD(
        label = "JS Citizen AOS Response Letters Resent",
        access = {DefaultAccess.class}
    )
    private YesOrNo jsCitizenAosResponseLettersResent;

    @CCD(
        label = "What would you like to reissue?",
        typeOverride = FixedRadioList,
        typeParameterOverride = "ReissueOption",
        access = {SystemUpdateAndSuperUserAccess.class}
    )
    private ReissueOption reissueOption;

    @CCD(
        label = "What would you like to reissue?",
        typeOverride = FixedRadioList,
        typeParameterOverride = "JudicialSeparationReissueOption",
        access = {SystemUpdateAndSuperUserAccess.class}
    )
    private JudicialSeparationReissueOption judicialSeparationReissueOption;

    @CCD(
        access = {DefaultAccess.class}
    )
    private ReissueOption previousReissueOption;

    @CCD(
        label = "Does Applicant 2 need help with fees?",
        access = {Applicant2Access.class}
    )
    private YesOrNo applicant2NeedsHelpWithFees;

    @CCD(
        label = "Does ${labelContentTheApplicant2} have a solicitor representing them?",
        access = {DefaultAccess.class}
    )
    private Applicant2Represented applicant1IsApplicant2Represented;

    @CCD(
        label = "Link to applicant 1 solicitors answers",
        access = {CaseworkerAccess.class}
    )
    private Document applicant1SolicitorAnswersLink;

    @CCD(
        label = "Link to applicant 2 solicitors answers",
        access = {CaseworkerAccess.class}
    )
    private Document applicant2SolicitorAnswersLink;

    @CCD(
        label = "Progress paper case",
        typeOverride = FixedList,
        typeParameterOverride = "ProgressPaperCase",
        access = {SystemUpdateAndSuperUserAccess.class}
    )
    private ProgressPaperCase progressPaperCase;

    @CCD(
        label = "How is offline payment being made?",
        typeOverride = FixedRadioList,
        typeParameterOverride = "PaperCasePaymentMethod",
        access = {DefaultAccess.class}
    )
    private PaperCasePaymentMethod paperCasePaymentMethod;

    @CCD(
        label = "Is the case a paper case?",
        access = {DefaultAccess.class}
    )
    private YesOrNo newPaperCase;

    @CCD(
        label = "Has the application been switched from JOINT to SOLE?",
        access = {DefaultAccess.class, Applicant2Access.class}
    )
    private YesOrNo switchedToSoleCo;

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
    public boolean applicant2HasStatementOfTruth() {
        return YES.equals(applicant2StatementOfTruth);
    }

    @JsonIgnore
    public boolean hasApplicant2SolSignStatementOfTruth() {
        return YES.equals(applicant2SolSignStatementOfTruth);
    }

    @JsonIgnore
    public boolean hasStatementOfTruth() {
        return applicant1HasStatementOfTruth() || hasSolSignStatementOfTruth();
    }

    @JsonIgnore
    public boolean hasApplicant2StatementOfTruth() {
        return applicant2HasStatementOfTruth() || hasApplicant2SolSignStatementOfTruth();
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
        return SOLICITOR_SERVICE.equals(serviceMethod);
    }

    @JsonIgnore
    public boolean isCourtServiceMethod() {
        return COURT_SERVICE.equals(serviceMethod);
    }

    @JsonIgnore
    public boolean isPersonalServiceMethod() {
        return PERSONAL_SERVICE.equals(serviceMethod);
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
        return Objects.nonNull(applicant1HelpWithFees)
            && Objects.nonNull(applicant1HelpWithFees.getNeedHelp())
            && applicant1HelpWithFees.getNeedHelp().toBoolean()
            || FEES_HELP_WITH.equals(solPaymentHowToPay);
    }

    @JsonIgnore
    public boolean isHelpWithFeesApplicationApplicant2() {
        return Objects.nonNull(applicant2HelpWithFees)
            && Objects.nonNull(applicant2HelpWithFees.getNeedHelp())
            && applicant2HelpWithFees.getNeedHelp().toBoolean()
            || FEES_HELP_WITH.equals(solPaymentHowToPay);
    }

    @JsonIgnore
    public boolean isSolicitorPaymentMethodPba() {
        return FEE_PAY_BY_ACCOUNT.equals(this.getSolPaymentHowToPay());
    }

    @JsonIgnore
    public Set<DocumentType> getMissingDocumentTypes() {
        Set<DocumentType> missingDocumentTypes = new HashSet<>();
        if (Objects.nonNull(applicant1CannotUploadSupportingDocument)) {
            missingDocumentTypes.addAll(applicant1CannotUploadSupportingDocument);
        }
        if (Objects.nonNull(applicant2CannotUploadSupportingDocument)) {
            missingDocumentTypes.addAll(applicant2CannotUploadSupportingDocument);
        }
        return missingDocumentTypes;
    }

    @JsonIgnore
    public boolean isPaperCase() {
        return YES.equals(newPaperCase);
    }

    @JsonIgnore
    public Optional<String> getPbaNumber() {
        return Optional.ofNullable(pbaNumbers)
            .map(dynamicList -> dynamicList.getValue().getLabel());
    }

}
