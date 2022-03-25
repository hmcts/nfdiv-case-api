package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;
import uk.gov.hmcts.divorce.divorcecase.model.access.DefaultAccess;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Prayer {
    @CCD(
        label = " ",
        access = {DefaultAccess.class}
    )
    private Set<DissolveDivorce> applicant1PrayerDissolveDivorce;

    @CCD(
        label = " ",
        access = {DefaultAccess.class}
    )
    private Set<EndCivilPartnership> applicant1PrayerEndCivilPartnership;

    @CCD(
        label = " ",
        access = {DefaultAccess.class}
    )
    private Set<FinancialOrdersThemselves> applicant1PrayerFinancialOrdersThemselves;

    @CCD(
        label = " ",
        access = {DefaultAccess.class}
    )
    private Set<FinancialOrdersChild> applicant1PrayerFinancialOrdersChild;

    @CCD(
        label = "The prayer",
        access = {DefaultAccess.class}
    )
    private Set<DissolveDivorce> applicant2PrayerDissolveDivorce;

    @CCD(
        label = " ",
        access = {DefaultAccess.class}
    )
    private Set<EndCivilPartnership> applicant2PrayerEndCivilPartnership;

    @CCD(
        label = " ",
        access = {DefaultAccess.class}
    )
    private Set<FinancialOrdersThemselves> applicant2PrayerFinancialOrdersThemselves;

    @CCD(
        label = " ",
        access = {DefaultAccess.class}
    )
    private Set<FinancialOrdersChild> applicant2PrayerFinancialOrdersChild;

    @Getter
    @AllArgsConstructor
    public enum DissolveDivorce implements HasLabel {

        @JsonProperty("dissolveDivorce")
        DISSOLVE_DIVORCE("I confirm the applicant is applying to the court to dissolve their marriage (get a divorce)");

        private final String label;
    }

    @Getter
    @AllArgsConstructor
    public enum EndCivilPartnership implements HasLabel {

        @JsonProperty("endCivilPartnership")
        END_CIVIL_PARTNERSHIP("I confirm the applicant is applying to the court to end their civil partnership");

        private final String label;
    }

    @Getter
    @AllArgsConstructor
    public enum FinancialOrdersThemselves implements HasLabel {

        @JsonProperty("financialOrdersThemselves")
        FINANCIAL_ORDERS_THEMSELVES("I confirm the applicant is applying to the court for financial orders for themselves");

        private final String label;
    }

    @Getter
    @AllArgsConstructor
    public enum FinancialOrdersChild implements HasLabel {

        @JsonProperty("financialOrdersChild")
        FINANCIAL_ORDERS_CHILD("I confirm the applicant is applying to the court for financial orders for the children");

        private final String label;
    }

}
