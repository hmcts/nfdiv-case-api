package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.access.Applicant2Access;
import uk.gov.hmcts.divorce.divorcecase.model.access.DefaultAccess;
import uk.gov.hmcts.divorce.divorcecase.model.access.SystemUpdateAndSuperUserAccess;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedRadioList;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;

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
        access = {DefaultAccess.class}
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
        access = {DefaultAccess.class}
    )
    private YesOrNo applicant1AppliedForFinalOrderFirst;

    @CCD(
        label = "Does ${labelContentTheApplicant2} want to apply for a Final Order?",
        access = {Applicant2Access.class}
    )
    private YesOrNo doesApplicant2WantToApplyForFinalOrder;

    @CCD(
        label = "Has ${labelContentApplicant2} applied for a final order?",
        access = {DefaultAccess.class}
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
        label = "${labelContentApplicantsOrApplicant1s} final order late explanation",
        access = {DefaultAccess.class}
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
        label = "The applicant believes that the facts stated in this application are true.",
        access = {DefaultAccess.class}
    )
    private YesOrNo applicant1FinalOrderStatementOfTruth;

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
        label = "Has applicant 1 been notified they can continue and switch to sole for final order?",
        access = {DefaultAccess.class}
    )
    private YesOrNo finalOrderApplicant1NotifiedCanSwitchToSoleAfterIntention;

    @CCD(
        label = "Has applicant 2 been notified they can continue and switch to sole for final order?",
        access = {DefaultAccess.class}
    )
    private YesOrNo finalOrderApplicant2NotifiedCanSwitchToSoleAfterIntention;

    @CCD(
        label = "Has the respondent been sent a reminder to apply for the Final Order?",
        access = {DefaultAccess.class}
    )
    private YesOrNo finalOrderReminderSentApplicant2;

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
}
