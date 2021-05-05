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
        if (jurisdictionConnections.contains(JurisdictionConnections.PET_RESP_RESIDENT)
            && caseData.getJurisdictionPetitionerResidence() != YesOrNo.YES
            && caseData.getJurisdictionRespondentResidence() != YesOrNo.YES) {
            return CONNECTION + JurisdictionConnections.PET_RESP_RESIDENT + CANNOT_EXIST;
        }
        return null;
    }

    public static String validateJurisdictionConnectionB(Set<JurisdictionConnections> jurisdictionConnections, CaseData caseData) {
        if (jurisdictionConnections.contains(JurisdictionConnections.PET_RESP_LAST_RESIDENT)
            && caseData.getJurisdictionBothLastHabituallyResident() != YesOrNo.YES) {
            return CONNECTION + JurisdictionConnections.PET_RESP_LAST_RESIDENT + CANNOT_EXIST;
        }
        return null;
    }

    public static String validateJurisdictionConnectionC(Set<JurisdictionConnections> jurisdictionConnections, CaseData caseData) {
        if (jurisdictionConnections.contains(JurisdictionConnections.RESP_RESIDENT)
            && caseData.getJurisdictionRespondentResidence() != YesOrNo.YES) {
            return CONNECTION + JurisdictionConnections.RESP_RESIDENT + CANNOT_EXIST;
        }
        return null;
    }

    public static String validateJurisdictionConnectionD(Set<JurisdictionConnections> jurisdictionConnections, CaseData caseData) {
        if (jurisdictionConnections.contains(JurisdictionConnections.PET_RESIDENT_TWELVE_MONTHS)
            && caseData.getJurisdictionPetitionerResidence() != YesOrNo.YES
            && caseData.getJurisdictionPetHabituallyResLastTwelveMonths() != YesOrNo.YES) {
            return CONNECTION + JurisdictionConnections.PET_RESIDENT_TWELVE_MONTHS + CANNOT_EXIST;
        }
        return null;
    }

    public static String validateJurisdictionConnectionE(Set<JurisdictionConnections> jurisdictionConnections, CaseData caseData) {
        if (jurisdictionConnections.contains(JurisdictionConnections.PET_RESIDENT_SIX_MONTHS)
            && caseData.getJurisdictionPetitionerResidence() != YesOrNo.YES
            && caseData.getJurisdictionPetHabituallyResLastSixMonths() != YesOrNo.YES) {
            return CONNECTION + JurisdictionConnections.PET_RESIDENT_SIX_MONTHS + CANNOT_EXIST;
        }
        return null;
    }

    public static String validateJurisdictionConnectionF(Set<JurisdictionConnections> jurisdictionConnections, CaseData caseData) {
        if (jurisdictionConnections.contains(JurisdictionConnections.PET_RESP_DOMICILED)
            && caseData.getJurisdictionPetitionerDomicile() != YesOrNo.YES
            && caseData.getJurisdictionRespondentDomicile() != YesOrNo.YES) {
            return CONNECTION + JurisdictionConnections.PET_RESP_DOMICILED + CANNOT_EXIST;
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
        if (jurisdictionConnections.contains(JurisdictionConnections.PET_DOMICILED)
            && caseData.getJurisdictionPetitionerDomicile() != YesOrNo.YES) {
            return CONNECTION + JurisdictionConnections.PET_DOMICILED + CANNOT_EXIST;
        }
        return null;
    }

    public static String validateJurisdictionConnectionI(Set<JurisdictionConnections> jurisdictionConnections, CaseData caseData) {
        if (jurisdictionConnections.contains(JurisdictionConnections.RESP_DOMICILED)
            && caseData.getJurisdictionRespondentDomicile() != YesOrNo.YES) {
            return CONNECTION + JurisdictionConnections.RESP_DOMICILED + CANNOT_EXIST;
        }
        return null;
    }
}
