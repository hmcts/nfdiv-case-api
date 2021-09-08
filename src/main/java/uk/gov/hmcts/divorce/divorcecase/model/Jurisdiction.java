package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.access.DefaultAccess;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections.APP_1_APP_2_DOMICILED;
import static uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections.APP_1_APP_2_LAST_RESIDENT;
import static uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections.APP_1_APP_2_RESIDENT;
import static uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections.APP_1_DOMICILED;
import static uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections.APP_1_RESIDENT_JOINT;
import static uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections.APP_1_RESIDENT_SIX_MONTHS;
import static uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections.APP_1_RESIDENT_TWELVE_MONTHS;
import static uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections.APP_2_DOMICILED;
import static uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections.APP_2_RESIDENT;
import static uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections.RESIDUAL_JURISDICTION;
import static uk.gov.hmcts.divorce.divorcecase.validation.ValidationUtil.CANNOT_EXIST;
import static uk.gov.hmcts.divorce.divorcecase.validation.ValidationUtil.CONNECTION;
import static uk.gov.hmcts.divorce.divorcecase.validation.ValidationUtil.EMPTY;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public class Jurisdiction {

    @CCD(
        label = "Is the applicant resident?",
        access = {DefaultAccess.class}
    )
    private YesOrNo applicant1Residence;

    @CCD(
        label = "Is the respondent resident?",
        access = {DefaultAccess.class}
    )
    private YesOrNo applicant2Residence;

    @CCD(
        label = "Is the applicant domiciled?",
        access = {DefaultAccess.class}
    )
    private YesOrNo applicant1Domicile;

    @CCD(
        label = "Is the respondent domiciled?",
        access = {DefaultAccess.class}
    )
    private YesOrNo applicant2Domicile;

    @CCD(
        label = "Has the applicant been resident for the last twelve months?",
        access = {DefaultAccess.class}
    )
    private YesOrNo app1HabituallyResLastTwelveMonths;

    @CCD(
        label = "Has the applicant been resident for the last six months?",
        access = {DefaultAccess.class}
    )
    private YesOrNo app1HabituallyResLastSixMonths;

    @CCD(
        label = "Is residual jurisdiction eligible?",
        access = {DefaultAccess.class}
    )
    private YesOrNo residualEligible;

    @CCD(
        label = "Were both the applicant and the respondent last habitually resident, and one still resides?",
        access = {DefaultAccess.class}
    )
    private YesOrNo bothLastHabituallyResident;

    @CCD(
        label = "Legal connections",
        hint = "Tick all the reasons that apply:",
        access = {DefaultAccess.class}
    )
    private Set<JurisdictionConnections> connections;

    public List<String> validate() {
        if (isEmpty(connections)) {
            return List.of("JurisdictionConnections" + EMPTY);
        } else {
            return Stream.of(
                validateJurisdictionConnectionA(),
                validateJurisdictionConnectionB(),
                validateJurisdictionConnectionC(),
                validateJurisdictionConnectionD(),
                validateJurisdictionConnectionE(),
                validateJurisdictionConnectionF(),
                validateJurisdictionConnectionG(),
                validateJurisdictionConnectionH(),
                validateJurisdictionConnectionI(),
                validateJurisdictionConnectionJ()
            ).filter(Objects::nonNull).collect(Collectors.toList());
        }
    }

    private String validateJurisdictionConnectionA() {
        if (connections.contains(APP_1_APP_2_RESIDENT)
            && (applicant1Residence != YesOrNo.YES
            || applicant2Residence != YesOrNo.YES)) {
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
            && (applicant1Residence != YesOrNo.YES
            || app1HabituallyResLastTwelveMonths != YesOrNo.YES)) {
            return CONNECTION + APP_1_RESIDENT_TWELVE_MONTHS + CANNOT_EXIST;
        }
        return null;
    }

    private String validateJurisdictionConnectionE() {
        if (connections.contains(APP_1_RESIDENT_SIX_MONTHS)
            && (applicant1Residence != YesOrNo.YES
            || app1HabituallyResLastSixMonths != YesOrNo.YES)) {
            return CONNECTION + APP_1_RESIDENT_SIX_MONTHS + CANNOT_EXIST;
        }
        return null;
    }

    private String validateJurisdictionConnectionF() {
        if (connections.contains(APP_1_APP_2_DOMICILED)
            && (applicant1Domicile != YesOrNo.YES
            || applicant2Domicile != YesOrNo.YES)) {
            return CONNECTION + APP_1_APP_2_DOMICILED + CANNOT_EXIST;
        }
        return null;
    }

    private String validateJurisdictionConnectionG() {
        if (connections.contains(APP_1_DOMICILED) && applicant1Domicile != YesOrNo.YES) {
            return CONNECTION + APP_1_DOMICILED + CANNOT_EXIST;
        }
        return null;
    }

    private String validateJurisdictionConnectionH() {
        if (connections.contains(APP_2_DOMICILED) && applicant2Domicile != YesOrNo.YES) {
            return CONNECTION + APP_2_DOMICILED + CANNOT_EXIST;
        }
        return null;
    }

    private String validateJurisdictionConnectionI() {
        if (connections.contains(RESIDUAL_JURISDICTION) && residualEligible != YesOrNo.YES) {
            return CONNECTION + RESIDUAL_JURISDICTION + CANNOT_EXIST;
        }
        return null;
    }

    private String validateJurisdictionConnectionJ() {
        if (connections.contains(APP_1_RESIDENT_JOINT) && applicant1Residence != YesOrNo.YES) {
            return CONNECTION + APP_1_RESIDENT_JOINT + CANNOT_EXIST;
        }
        return null;
    }
}
