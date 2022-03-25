package uk.gov.hmcts.divorce.divorcecase.validation;

import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Prayer;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static uk.gov.hmcts.divorce.divorcecase.model.FinancialOrderFor.APPLICANT;
import static uk.gov.hmcts.divorce.divorcecase.model.FinancialOrderFor.CHILDREN;

public final class PrayerUtil {

    public static final String EMPTY = " cannot be empty or null";

    private PrayerUtil() {
    }

    public static List<String> validatePrayerApplicant1(CaseData caseData) {
        Prayer prayer = caseData.getApplication().getPrayer();

        if (caseData.isDivorce() && isEmpty(prayer.getApplicant1PrayerDissolveDivorce())) {
            return List.of("Applicant 1 must confirm prayer to dissolve their marriage (get a divorce)");
        }
        if (!caseData.isDivorce() && isEmpty(prayer.getApplicant1PrayerEndCivilPartnership())) {
            return List.of("Applicant 1 must confirm prayer to end their civil partnership");
        }
        if (caseData.getApplicant1().appliedForFinancialOrder()
            && caseData.getApplicant1().getFinancialOrdersFor().contains(APPLICANT)
            && isEmpty(prayer.getApplicant1PrayerFinancialOrdersThemselves())) {
            return List.of("Applicant 1 must confirm prayer  for financial orders for themselves");
        }

        if (caseData.getApplicant1().appliedForFinancialOrder()
            && caseData.getApplicant1().getFinancialOrdersFor().contains(CHILDREN)
            && isEmpty(prayer.getApplicant1PrayerFinancialOrdersChild())) {
            return List.of("Applicant 1 must confirm prayer for financial orders for the children");
        }
        return emptyList();
    }

    public static List<String> validatePrayerApplicant2(CaseData caseData) {
        Prayer prayer = caseData.getApplication().getPrayer();

        if (caseData.isDivorce() && isEmpty(prayer.getApplicant2PrayerDissolveDivorce())) {
            return List.of("Applicant 2 must confirm prayer to dissolve their marriage (get a divorce)");
        }
        if (!caseData.isDivorce() && isEmpty(prayer.getApplicant1PrayerEndCivilPartnership())) {
            return List.of("Applicant 2 must confirm prayer to end their civil partnership");
        }
        if (caseData.getApplicant2().appliedForFinancialOrder()
            && caseData.getApplicant2().getFinancialOrdersFor().contains(APPLICANT)
            && isEmpty(prayer.getApplicant2PrayerFinancialOrdersThemselves())) {
            return List.of("Applicant 2 must confirm prayer for financial orders for themselves");
        }
        if (caseData.getApplicant2().appliedForFinancialOrder()
            && caseData.getApplicant2().getFinancialOrdersFor().contains(CHILDREN)
            && isEmpty(prayer.getApplicant2PrayerFinancialOrdersChild())) {
            return List.of("Applicant 2 must confirm prayer for financial orders for the children");
        }
        return emptyList();
    }
}
