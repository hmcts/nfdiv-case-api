package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static uk.gov.hmcts.divorce.divorcecase.model.FinancialOrderFor.APPLICANT;
import static uk.gov.hmcts.divorce.divorcecase.model.FinancialOrderFor.CHILDREN;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public class ApplicantPrayer {
    private Set<DissolveDivorce> prayerDissolveDivorce;

    private Set<EndCivilPartnership> prayerEndCivilPartnership;

    private Set<JudicialSeparation> prayerJudicialSeparation;

    private Set<Separation> prayerSeparation;

    private Set<FinancialOrdersThemselves> prayerFinancialOrdersThemselves;

    private Set<FinancialOrdersChild> prayerFinancialOrdersChild;

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
    public enum JudicialSeparation implements HasLabel {

        @JsonProperty("applyJudicialSeparation")
        JUDICIAL_SEPARATION("I confirm the applicant is applying to the court to get a judicial separation order");

        private final String label;
    }

    @Getter
    @AllArgsConstructor
    public enum Separation implements HasLabel {

        @JsonProperty("applySeparation")
        SEPARATION("I confirm the applicant is applying to the court to get a separation order");

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

    @JsonIgnore
    public List<String> validatePrayerApplicant1(CaseData caseData) {
        List<String> warnings = new ArrayList<>();

        if (caseData.isDivorce() && caseData.hasNaOrNullSupplementaryCaseType() && isEmpty(this.getPrayerDissolveDivorce())) {
            warnings.add("Applicant 1 must confirm prayer to dissolve their marriage (get a divorce)");
        } else if (!caseData.isDivorce() && caseData.hasNaOrNullSupplementaryCaseType() && isEmpty(this.getPrayerEndCivilPartnership())) {
            warnings.add("Applicant 1 must confirm prayer to end their civil partnership");
        } else if (caseData.isDivorce() && caseData.isJudicialSeparationCase() && isEmpty(this.getPrayerJudicialSeparation())) {
            warnings.add("Applicant 1 must confirm prayer to get a judicial separation order");
        } else if (!caseData.isDivorce() && caseData.isJudicialSeparationCase() && isEmpty(this.getPrayerSeparation())) {
            warnings.add("Applicant 1 must confirm prayer to get a separation order");
        }

        if (caseData.getApplicant1().appliedForFinancialOrder()
            && caseData.getApplicant1().getFinancialOrdersFor().contains(APPLICANT)
            && isEmpty(this.getPrayerFinancialOrdersThemselves())) {
            warnings.add("Applicant 1 must confirm prayer for financial orders for themselves");
        }

        if (caseData.getApplicant1().appliedForFinancialOrder()
            && caseData.getApplicant1().getFinancialOrdersFor().contains(CHILDREN)
            && isEmpty(this.getPrayerFinancialOrdersChild())) {
            warnings.add("Applicant 1 must confirm prayer for financial orders for the children");
        }
        return warnings;
    }

    public List<String> validatePrayerApplicant2(CaseData caseData) {
        List<String> warnings = new ArrayList<>();
        if (caseData.isDivorce() && caseData.hasNaOrNullSupplementaryCaseType() && isEmpty(this.getPrayerDissolveDivorce())) {
            warnings.add("Applicant 2 must confirm prayer to dissolve their marriage (get a divorce)");
        } else if (!caseData.isDivorce() && caseData.hasNaOrNullSupplementaryCaseType() && isEmpty(this.getPrayerEndCivilPartnership())) {
            warnings.add("Applicant 2 must confirm prayer to end their civil partnership");
        } else if (caseData.isDivorce() && caseData.isJudicialSeparationCase() && isEmpty(this.getPrayerJudicialSeparation())) {
            warnings.add("Applicant 2 must confirm prayer to get a judicial separation order");
        } else if (!caseData.isDivorce() && caseData.isJudicialSeparationCase() && isEmpty(this.getPrayerSeparation())) {
            warnings.add("Applicant 2 must confirm prayer to get a separation order");
        }

        if (caseData.getApplicant2().appliedForFinancialOrder()
            && caseData.getApplicant2().getFinancialOrdersFor().contains(APPLICANT)
            && isEmpty(this.getPrayerFinancialOrdersThemselves())) {
            warnings.add("Applicant 2 must confirm prayer for financial orders for themselves");
        }
        if (caseData.getApplicant2().appliedForFinancialOrder()
            && caseData.getApplicant2().getFinancialOrdersFor().contains(CHILDREN)
            && isEmpty(this.getPrayerFinancialOrdersChild())) {
            warnings.add("Applicant 2 must confirm prayer for financial orders for the children");
        }
        return warnings;
    }
}
