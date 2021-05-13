package uk.gov.hmcts.divorce.common.validation;

import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.JurisdictionConnections;

import java.util.Set;

public final class JurisdictionConnectionsValidation {

    public static final String CONNECTION = "Connection ";
    public static final String CANNOT_EXIST = " cannot exist";

    private JurisdictionConnectionsValidation() {
    }

    public static String validateJurisdictionConnectionA(Set<JurisdictionConnections> jurisdictionConnections, CaseData caseData) {
        if (jurisdictionConnections.contains(JurisdictionConnections.APP_1_APP_2_RESIDENT)
            && caseData.getJurisdictionApplicant1Residence() != YesOrNo.YES
            && caseData.getJurisdictionApplicant2Residence() != YesOrNo.YES) {
            return CONNECTION + JurisdictionConnections.APP_1_APP_2_RESIDENT + CANNOT_EXIST;
        }
        return null;
    }

    public static String validateJurisdictionConnectionB(Set<JurisdictionConnections> jurisdictionConnections, CaseData caseData) {
        if (jurisdictionConnections.contains(JurisdictionConnections.APP_1_APP_2_LAST_RESIDENT)
            && caseData.getJurisdictionBothLastHabituallyResident() != YesOrNo.YES) {
            return CONNECTION + JurisdictionConnections.APP_1_APP_2_LAST_RESIDENT + CANNOT_EXIST;
        }
        return null;
    }

    public static String validateJurisdictionConnectionC(Set<JurisdictionConnections> jurisdictionConnections, CaseData caseData) {
        if (jurisdictionConnections.contains(JurisdictionConnections.APP_2_RESIDENT)
            && caseData.getJurisdictionApplicant2Residence() != YesOrNo.YES) {
            return CONNECTION + JurisdictionConnections.APP_2_RESIDENT + CANNOT_EXIST;
        }
        return null;
    }

    public static String validateJurisdictionConnectionD(Set<JurisdictionConnections> jurisdictionConnections, CaseData caseData) {
        if (jurisdictionConnections.contains(JurisdictionConnections.APP_1_RESIDENT_TWELVE_MONTHS)
            && caseData.getJurisdictionApplicant1Residence() != YesOrNo.YES
            && caseData.getJurisdictionApp1HabituallyResLastTwelveMonths() != YesOrNo.YES) {
            return CONNECTION + JurisdictionConnections.APP_1_RESIDENT_TWELVE_MONTHS + CANNOT_EXIST;
        }
        return null;
    }

    public static String validateJurisdictionConnectionE(Set<JurisdictionConnections> jurisdictionConnections, CaseData caseData) {
        if (jurisdictionConnections.contains(JurisdictionConnections.APP_1_RESIDENT_SIX_MONTHS)
            && caseData.getJurisdictionApplicant1Residence() != YesOrNo.YES
            && caseData.getJurisdictionApp1HabituallyResLastSixMonths() != YesOrNo.YES) {
            return CONNECTION + JurisdictionConnections.APP_1_RESIDENT_SIX_MONTHS + CANNOT_EXIST;
        }
        return null;
    }

    public static String validateJurisdictionConnectionF(Set<JurisdictionConnections> jurisdictionConnections, CaseData caseData) {
        if (jurisdictionConnections.contains(JurisdictionConnections.APP_1_APP_2_DOMICILED)
            && caseData.getJurisdictionApplicant1Domicile() != YesOrNo.YES
            && caseData.getJurisdictionApplicant2Domicile() != YesOrNo.YES) {
            return CONNECTION + JurisdictionConnections.APP_1_APP_2_DOMICILED + CANNOT_EXIST;
        }
        return null;
    }

    public static String validateJurisdictionConnectionG(Set<JurisdictionConnections> jurisdictionConnections, CaseData caseData) {
        if (jurisdictionConnections.contains(JurisdictionConnections.RESIDUAL_JURISDICTION)
            && caseData.getJurisdictionResidualEligible() != YesOrNo.YES) {
            return CONNECTION + JurisdictionConnections.RESIDUAL_JURISDICTION + CANNOT_EXIST;
        }
        return null;
    }

    public static String validateJurisdictionConnectionH(Set<JurisdictionConnections> jurisdictionConnections, CaseData caseData) {
        if (jurisdictionConnections.contains(JurisdictionConnections.APP_1_DOMICILED)
            && caseData.getJurisdictionApplicant1Domicile() != YesOrNo.YES) {
            return CONNECTION + JurisdictionConnections.APP_1_DOMICILED + CANNOT_EXIST;
        }
        return null;
    }

    public static String validateJurisdictionConnectionI(Set<JurisdictionConnections> jurisdictionConnections, CaseData caseData) {
        if (jurisdictionConnections.contains(JurisdictionConnections.APP_2_DOMICILED)
            && caseData.getJurisdictionApplicant2Domicile() != YesOrNo.YES) {
            return CONNECTION + JurisdictionConnections.APP_2_DOMICILED + CANNOT_EXIST;
        }
        return null;
    }
}
