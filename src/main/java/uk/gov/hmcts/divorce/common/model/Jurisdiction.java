package uk.gov.hmcts.divorce.common.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.model.access.DefaultAccess;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static uk.gov.hmcts.divorce.common.model.JurisdictionConnections.APP_1_APP_2_DOMICILED;
import static uk.gov.hmcts.divorce.common.model.JurisdictionConnections.APP_1_APP_2_LAST_RESIDENT;
import static uk.gov.hmcts.divorce.common.model.JurisdictionConnections.APP_1_APP_2_RESIDENT;
import static uk.gov.hmcts.divorce.common.model.JurisdictionConnections.APP_1_DOMICILED;
import static uk.gov.hmcts.divorce.common.model.JurisdictionConnections.APP_1_RESIDENT_SIX_MONTHS;
import static uk.gov.hmcts.divorce.common.model.JurisdictionConnections.APP_1_RESIDENT_TWELVE_MONTHS;
import static uk.gov.hmcts.divorce.common.model.JurisdictionConnections.APP_2_DOMICILED;
import static uk.gov.hmcts.divorce.common.model.JurisdictionConnections.APP_2_RESIDENT;
import static uk.gov.hmcts.divorce.common.model.JurisdictionConnections.RESIDUAL_JURISDICTION;
import static uk.gov.hmcts.divorce.common.validation.ValidationUtil.CANNOT_EXIST;
import static uk.gov.hmcts.divorce.common.validation.ValidationUtil.CONNECTION;
import static uk.gov.hmcts.divorce.common.validation.ValidationUtil.EMPTY;
import static uk.gov.hmcts.divorce.common.validation.ValidationUtil.addToErrorList;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public class Jurisdiction {

    @CCD(
        label = "Is applicant 1 resident?",
        access = {DefaultAccess.class}
    )
    private YesOrNo applicant1Residence;

    @CCD(
        label = "Is applicant 2 resident?",
        access = {DefaultAccess.class}
    )
    private YesOrNo applicant2Residence;

    @CCD(
        label = "Is applicant 1 domiciled?",
        access = {DefaultAccess.class}
    )
    private YesOrNo applicant1Domicile;

    @CCD(
        label = "Is applicant 2 domiciled?",
        access = {DefaultAccess.class}
    )
    private YesOrNo applicant2Domicile;

    @CCD(
        label = "Has Applicant 1 been resident for the last twelve months?",
        access = {DefaultAccess.class}
    )
    private YesOrNo app1HabituallyResLastTwelveMonths;

    @CCD(
        label = "Has Applicant 1 been resident for the last six months?",
        access = {DefaultAccess.class}
    )
    private YesOrNo app1HabituallyResLastSixMonths;

    @CCD(
        label = "Is residual jurisdiction eligible?",
        access = {DefaultAccess.class}
    )
    private YesOrNo residualEligible;

    @CCD(
        label = "Were both applicant 1 and applicant 2 last habitually resident, and one still resides?",
        access = {DefaultAccess.class}
    )
    private YesOrNo bothLastHabituallyResident;

    @CCD(
        label = "Jurisdiction connections",
        hint = "Tick all the reasons that apply:",
        access = {DefaultAccess.class}
    )
    private Set<JurisdictionConnections> connections;

    public List<String> validate() {
        List<String> errorList = new ArrayList<>();

        if (connections == null) {
            errorList.add("JurisdictionConnections" + EMPTY);
        } else {
            addToErrorList(validateJurisdictionConnectionA(), errorList);
            addToErrorList(validateJurisdictionConnectionB(), errorList);
            addToErrorList(validateJurisdictionConnectionC(), errorList);
            addToErrorList(validateJurisdictionConnectionD(), errorList);
            addToErrorList(validateJurisdictionConnectionE(), errorList);
            addToErrorList(validateJurisdictionConnectionF(), errorList);
            addToErrorList(validateJurisdictionConnectionG(), errorList);
            addToErrorList(validateJurisdictionConnectionH(), errorList);
            addToErrorList(validateJurisdictionConnectionI(), errorList);
        }

        return errorList;
    }

    private String validateJurisdictionConnectionA() {
        if (connections.contains(APP_1_APP_2_RESIDENT) && applicant1Residence != YesOrNo.YES && applicant2Residence != YesOrNo.YES) {
            return CONNECTION + APP_1_APP_2_RESIDENT + CANNOT_EXIST;
        }
        return null;
    }

    private String validateJurisdictionConnectionB() {
        if (connections.contains(APP_1_APP_2_LAST_RESIDENT) && bothLastHabituallyResident != YesOrNo.YES) {
            return CONNECTION + APP_1_APP_2_LAST_RESIDENT + CANNOT_EXIST;
        }
        return null;
    }

    private String validateJurisdictionConnectionC() {
        if (connections.contains(APP_2_RESIDENT) && applicant2Residence != YesOrNo.YES) {
            return CONNECTION + APP_2_RESIDENT + CANNOT_EXIST;
        }
        return null;
    }

    private String validateJurisdictionConnectionD() {
        if (connections.contains(APP_1_RESIDENT_TWELVE_MONTHS)
            && applicant1Residence != YesOrNo.YES
            && app1HabituallyResLastTwelveMonths != YesOrNo.YES) {
            return CONNECTION + APP_1_RESIDENT_TWELVE_MONTHS + CANNOT_EXIST;
        }
        return null;
    }

    private String validateJurisdictionConnectionE() {
        if (connections.contains(APP_1_RESIDENT_SIX_MONTHS)
            && applicant1Residence != YesOrNo.YES
            && app1HabituallyResLastSixMonths != YesOrNo.YES) {
            return CONNECTION + APP_1_RESIDENT_SIX_MONTHS + CANNOT_EXIST;
        }
        return null;
    }

    private String validateJurisdictionConnectionF() {
        if (connections.contains(APP_1_APP_2_DOMICILED) && applicant1Domicile != YesOrNo.YES && applicant2Domicile != YesOrNo.YES) {
            return CONNECTION + APP_1_APP_2_DOMICILED + CANNOT_EXIST;
        }
        return null;
    }

    private String validateJurisdictionConnectionG() {
        if (connections.contains(RESIDUAL_JURISDICTION) && residualEligible != YesOrNo.YES) {
            return CONNECTION + RESIDUAL_JURISDICTION + CANNOT_EXIST;
        }
        return null;
    }

    private String validateJurisdictionConnectionH() {
        if (connections.contains(APP_1_DOMICILED) && applicant1Domicile != YesOrNo.YES) {
            return CONNECTION + APP_1_DOMICILED + CANNOT_EXIST;
        }
        return null;
    }

    private String validateJurisdictionConnectionI() {
        if (connections.contains(APP_2_DOMICILED) && applicant2Domicile != YesOrNo.YES) {
            return CONNECTION + APP_2_DOMICILED + CANNOT_EXIST;
        }
        return null;
    }
}
