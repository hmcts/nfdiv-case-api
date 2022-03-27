package uk.gov.hmcts.divorce.bulkscan.transformation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.bulkscan.validation.data.OcrDataFields;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import java.util.Set;
import java.util.function.Function;

import static org.apache.commons.lang3.BooleanUtils.toBoolean;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicantPrayer.DissolveDivorce.DISSOLVE_DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicantPrayer.EndCivilPartnership.END_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicantPrayer.FinancialOrdersChild.FINANCIAL_ORDERS_CHILD;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicantPrayer.FinancialOrdersThemselves.FINANCIAL_ORDERS_THEMSELVES;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;

@Component
public class D8PrayerTransformer implements Function<TransformationDetails, TransformationDetails> {

    private static final String FOR_APPLICANT_1 = "theSoleApplicantOrApplicant1";
    private static final String FOR_THE_CHILDREN = "forTheChildren";
    private static final String FOR_APPLICANT1_AND_CHILDREN = "theSoleApplicantOrApplicant1,forTheChildren";
    private static final String FOR_APPLICANT_2 = "applicant2";
    private static final String FOR_APPLICANT2_AND_CHILDREN = "applicant2,forTheChildren";


    @Override
    public TransformationDetails apply(TransformationDetails transformationDetails) {

        CaseData caseData = transformationDetails.getCaseData();
        OcrDataFields ocrDataFields = transformationDetails.getOcrDataFields();

        final var isMarriageDissolved = toBoolean(ocrDataFields.getPrayerMarriageDissolved());
        final var isCivilPartnershipDissolved = toBoolean(ocrDataFields.getPrayerCivilPartnershipDissolved());

        if (DIVORCE.equals(caseData.getDivorceOrDissolution()) && isMarriageDissolved) {
            caseData.getApplicant1().getApplicantPrayer().setPrayerDissolveDivorce(Set.of(DISSOLVE_DIVORCE));
            caseData.getApplicant2().getApplicantPrayer().setPrayerDissolveDivorce(Set.of(DISSOLVE_DIVORCE));
        } else if (DISSOLUTION.equals(caseData.getDivorceOrDissolution()) && isCivilPartnershipDissolved) {
            caseData.getApplicant1().getApplicantPrayer().setPrayerEndCivilPartnership(Set.of(END_CIVIL_PARTNERSHIP));
            caseData.getApplicant2().getApplicantPrayer().setPrayerEndCivilPartnership(Set.of(END_CIVIL_PARTNERSHIP));
        } else {
            transformationDetails.getTransformationWarnings().add("Please review prayer in the scanned form");
        }

        final var applicant1PrayerFinancialOrderFor = ocrDataFields.getSoleOrApplicant1prayerFinancialOrderFor();
        applicant1PrayerFinancialOrders(caseData, applicant1PrayerFinancialOrderFor);

        final var applicant2PrayerFinancialOrderFor = ocrDataFields.getApplicant2PrayerFinancialOrderFor();
        applicant2PrayerFinancialOrders(caseData, applicant2PrayerFinancialOrderFor);

        return transformationDetails;
    }

    private void applicant1PrayerFinancialOrders(CaseData caseData, String applicant1PrayerFinancialOrder) {
        if (FOR_APPLICANT_1.equalsIgnoreCase(applicant1PrayerFinancialOrder)) {
            caseData.getApplicant1().getApplicantPrayer().setPrayerFinancialOrdersThemselves(Set.of(FINANCIAL_ORDERS_THEMSELVES));
        } else if (FOR_THE_CHILDREN.equalsIgnoreCase(applicant1PrayerFinancialOrder)) {
            caseData.getApplicant1().getApplicantPrayer().setPrayerFinancialOrdersChild(Set.of(FINANCIAL_ORDERS_CHILD));
        } else if (FOR_APPLICANT1_AND_CHILDREN.equalsIgnoreCase(applicant1PrayerFinancialOrder)) {
            caseData.getApplicant1().getApplicantPrayer().setPrayerFinancialOrdersThemselves(Set.of(FINANCIAL_ORDERS_THEMSELVES));
            caseData.getApplicant1().getApplicantPrayer().setPrayerFinancialOrdersChild(Set.of(FINANCIAL_ORDERS_CHILD));
        }
    }

    private void applicant2PrayerFinancialOrders(CaseData caseData, String applicant2PrayerFinancialOrder) {
        if (FOR_APPLICANT_2.equalsIgnoreCase(applicant2PrayerFinancialOrder)) {
            caseData.getApplicant2().getApplicantPrayer().setPrayerFinancialOrdersThemselves(Set.of(FINANCIAL_ORDERS_THEMSELVES));
        } else if (FOR_THE_CHILDREN.equalsIgnoreCase(applicant2PrayerFinancialOrder)) {
            caseData.getApplicant2().getApplicantPrayer().setPrayerFinancialOrdersChild(Set.of(FINANCIAL_ORDERS_CHILD));
        } else if (FOR_APPLICANT2_AND_CHILDREN.equalsIgnoreCase(applicant2PrayerFinancialOrder)) {
            caseData.getApplicant2().getApplicantPrayer().setPrayerFinancialOrdersThemselves(Set.of(FINANCIAL_ORDERS_THEMSELVES));
            caseData.getApplicant2().getApplicantPrayer().setPrayerFinancialOrdersChild(Set.of(FINANCIAL_ORDERS_CHILD));
        }
    }
}
