package uk.gov.hmcts.divorce.divorcecase.model;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static uk.gov.hmcts.divorce.divorcecase.validation.ValidationUtil.EMPTY;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

public class JurisdictionTest {

    public static final String CONNECTION = "Connection ";
    public static final String CANNOT_EXIST = " cannot exist";

    @Test
    public void shouldNotReturnErrorsWhenJurisdictionConnectionIsCAndIForValidCase() {
        final CaseData caseData = caseData();
        caseData.setDivorceOrDissolution(DivorceOrDissolution.DISSOLUTION);
        caseData.setApplicationType(ApplicationType.SOLE_APPLICATION);
        Jurisdiction jurisdiction = new Jurisdiction();

        jurisdiction.setConnections(Set.of(JurisdictionConnections.APP_2_RESIDENT_SOLE, JurisdictionConnections.RESIDUAL_JURISDICTION_CP));

        List<String> errors = jurisdiction.validateJurisdiction(caseData);

        Assertions.assertThat(errors).isEmpty();
    }

    @Test
    public void shouldNotReturnErrorsWhenJurisdictionConnectionIsC2AndI2AndJForValidCase() {
        final CaseData caseData = caseData();
        caseData.setDivorceOrDissolution(DivorceOrDissolution.DIVORCE);
        caseData.getApplication().getMarriageDetails().setFormationType(MarriageFormation.SAME_SEX_COUPLE);
        caseData.setApplicationType(ApplicationType.JOINT_APPLICATION);
        Jurisdiction jurisdiction = new Jurisdiction();

        jurisdiction.setConnections(Set.of(
            JurisdictionConnections.APP_2_RESIDENT_JOINT,
            JurisdictionConnections.RESIDUAL_JURISDICTION_D,
            JurisdictionConnections.APP_1_RESIDENT_JOINT
        ));

        List<String> errors = jurisdiction.validateJurisdiction(caseData);

        Assertions.assertThat(errors).isEmpty();
    }

    @Test
    public void shouldReturnErrorWhenJurisdictionConnectionIsCForJointCase() {
        final CaseData caseData = caseData();
        caseData.setApplicationType(ApplicationType.JOINT_APPLICATION);
        Jurisdiction jurisdiction = new Jurisdiction();

        jurisdiction.setConnections(Set.of(JurisdictionConnections.APP_2_RESIDENT_SOLE));

        List<String> errors = jurisdiction.validateJurisdiction(caseData);

        assertThat(errors, contains(CONNECTION + JurisdictionConnections.APP_2_RESIDENT_SOLE + CANNOT_EXIST));
    }

    @Test
    public void shouldReturnErrorWhenJurisdictionConnectionIsC2ForSoleCase() {
        final CaseData caseData = caseData();
        caseData.setApplicationType(ApplicationType.SOLE_APPLICATION);
        Jurisdiction jurisdiction = new Jurisdiction();

        jurisdiction.setConnections(Set.of(JurisdictionConnections.APP_2_RESIDENT_JOINT));

        List<String> errors = jurisdiction.validateJurisdiction(caseData);

        assertThat(errors, contains(CONNECTION + JurisdictionConnections.APP_2_RESIDENT_JOINT + CANNOT_EXIST));
    }

    @Test
    public void shouldReturnErrorWhenJurisdictionConnectionIsJForSoleCase() {
        final CaseData caseData = caseData();
        caseData.setApplicationType(ApplicationType.SOLE_APPLICATION);
        Jurisdiction jurisdiction = new Jurisdiction();

        jurisdiction.setConnections(Set.of(JurisdictionConnections.APP_1_RESIDENT_JOINT));

        List<String> errors = jurisdiction.validateJurisdiction(caseData);

        assertThat(errors, contains(CONNECTION + JurisdictionConnections.APP_1_RESIDENT_JOINT + CANNOT_EXIST));
    }

    @Test
    public void shouldReturnErrorWhenJurisdictionConnectionIsIForDivorceCase() {
        final CaseData caseData = caseData();
        caseData.setDivorceOrDissolution(DivorceOrDissolution.DIVORCE);
        Jurisdiction jurisdiction = new Jurisdiction();

        jurisdiction.setConnections(Set.of(JurisdictionConnections.RESIDUAL_JURISDICTION_CP));

        List<String> errors = jurisdiction.validateJurisdiction(caseData);

        assertThat(errors, contains(CONNECTION + JurisdictionConnections.RESIDUAL_JURISDICTION_CP + CANNOT_EXIST));
    }

    @Test
    public void shouldReturnErrorWhenJurisdictionConnectionIsI2ForMixedSexDivorce() {
        final CaseData caseData = caseData();
        caseData.setDivorceOrDissolution(DivorceOrDissolution.DIVORCE);
        caseData.getApplication().getMarriageDetails().setFormationType(MarriageFormation.OPPOSITE_SEX_COUPLE);
        Jurisdiction jurisdiction = new Jurisdiction();

        jurisdiction.setConnections(Set.of(JurisdictionConnections.RESIDUAL_JURISDICTION_D));

        List<String> errors = jurisdiction.validateJurisdiction(caseData);

        assertThat(errors, contains(CONNECTION + JurisdictionConnections.RESIDUAL_JURISDICTION_D + CANNOT_EXIST));
    }

    @Test
    public void shouldReturnErrorWhenJurisdictionConnectionsIsNull() {
        final CaseData caseData = caseData();
        Jurisdiction jurisdiction = new Jurisdiction();

        List<String> errors = jurisdiction.validateJurisdiction(caseData);

        assertThat(errors, contains("JurisdictionConnections" + EMPTY));
    }
}
