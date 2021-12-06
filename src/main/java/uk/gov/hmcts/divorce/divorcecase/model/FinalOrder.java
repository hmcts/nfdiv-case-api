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
import uk.gov.hmcts.divorce.divorcecase.model.access.DefaultAccess;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FinalOrder {

    @JsonIgnore
    private static final int FINAL_ORDER_OFFSET_WEEKS = 6;

    @JsonIgnore
    private static final int FINAL_ORDER_OFFSET_DAYS = 1;

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
        label = "Final Order granted?",
        hint = "The Final Order made on ${finalOrderDateFinalOrderSubmitted} will be made absolute and the ${divorceOrDissolution} "
            + "between ${applicant1FirstName} ${applicant1LastName} and ${applicant2FirstName} ${applicant2LastName} will be ended.",
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
        label = "Final Order granted date",
        access = {DefaultAccess.class}
    )
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime grantedDate;

    @CCD(
        label = "Does the applicant want to apply for Final Order and ${labelContentFinaliseDivorceOrEndCivilPartnership}?",
        access = {DefaultAccess.class}
    )
    private YesOrNo doesApplicantWantToApplyForFinalOrder;

    @JsonIgnore
    public LocalDate getDateFinalOrderEligibleFrom(LocalDateTime dateTime) {
        return dateTime.toLocalDate().plusWeeks(FINAL_ORDER_OFFSET_WEEKS).plusDays(FINAL_ORDER_OFFSET_DAYS);
    }
}
