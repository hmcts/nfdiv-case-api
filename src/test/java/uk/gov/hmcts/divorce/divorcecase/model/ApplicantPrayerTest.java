package uk.gov.hmcts.divorce.divorcecase.model;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.FinancialOrderFor.APPLICANT;
import static uk.gov.hmcts.divorce.divorcecase.model.FinancialOrderFor.CHILDREN;

public class ApplicantPrayerTest {

    @Test
    void shouldReturnWarningsWhenDivorceApplicant1DetailsAreValidatedAndPrayerForChildrenAndApplicantAreNotConfirmed() {

        var applicant1 = Applicant
            .builder()
            .financialOrder(YES)
            .financialOrdersFor(Set.of(APPLICANT, CHILDREN))
            .build();

        var caseData = CaseData
            .builder()
            .divorceOrDissolution(DIVORCE)
            .applicant1(applicant1)
            .build();

        assertThat(applicant1.getApplicantPrayer().validatePrayerApplicant1(caseData))
            .containsExactlyInAnyOrder(
                "Applicant 1 must confirm prayer to dissolve their marriage (get a divorce)",
                "Applicant 1 must confirm prayer for financial orders for themselves",
                "Applicant 1 must confirm prayer for financial orders for the children"
            );
    }

    @Test
    void shouldReturnWarningsWhenDissolutionApplicant1DetailsAreValidatedAndPrayerForChildrenAndApplicantAreNotConfirmed() {

        var applicant1 = Applicant
            .builder()
            .financialOrder(YES)
            .financialOrdersFor(Set.of(APPLICANT, CHILDREN))
            .build();

        var caseData = CaseData
            .builder()
            .divorceOrDissolution(DISSOLUTION)
            .applicant1(applicant1)
            .build();

        assertThat(applicant1.getApplicantPrayer().validatePrayerApplicant1(caseData))
            .containsExactlyInAnyOrder(
                "Applicant 1 must confirm prayer to end their civil partnership",
                "Applicant 1 must confirm prayer for financial orders for themselves",
                "Applicant 1 must confirm prayer for financial orders for the children"
            );
    }

    @Test
    void shouldReturnWarningsWhenDivorceApplicant2DetailsAreValidatedAndPrayerForChildrenAndApplicantAreNotConfirmed() {

        var applicant2 = Applicant
            .builder()
            .financialOrder(YES)
            .financialOrdersFor(Set.of(APPLICANT, CHILDREN))
            .build();

        var caseData = CaseData
            .builder()
            .divorceOrDissolution(DIVORCE)
            .applicant2(applicant2)
            .build();

        assertThat(applicant2.getApplicantPrayer().validatePrayerApplicant2(caseData))
            .containsExactlyInAnyOrder(
                "Applicant 2 must confirm prayer to dissolve their marriage (get a divorce)",
                "Applicant 2 must confirm prayer for financial orders for themselves",
                "Applicant 2 must confirm prayer for financial orders for the children"
            );
    }

    @Test
    void shouldReturnWarningsWhenDissolutionApplicant2DetailsAreValidatedAndPrayerForChildrenAndApplicantAreNotConfirmed() {

        var applicant2 = Applicant
            .builder()
            .financialOrder(YES)
            .financialOrdersFor(Set.of(APPLICANT, CHILDREN))
            .build();

        var caseData = CaseData
            .builder()
            .divorceOrDissolution(DISSOLUTION)
            .applicant2(applicant2)
            .build();

        assertThat(applicant2.getApplicantPrayer().validatePrayerApplicant2(caseData))
            .containsExactlyInAnyOrder(
                "Applicant 2 must confirm prayer to end their civil partnership",
                "Applicant 2 must confirm prayer for financial orders for themselves",
                "Applicant 2 must confirm prayer for financial orders for the children"
            );
    }
}
