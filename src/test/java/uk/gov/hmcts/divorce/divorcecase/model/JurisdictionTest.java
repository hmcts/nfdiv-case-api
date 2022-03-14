package uk.gov.hmcts.divorce.divorcecase.model;

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
    public void shouldReturnErrorWhenJurisdictionConnectionIsJ() {
        final CaseData caseData = caseData();
        caseData.setApplicationType(ApplicationType.JOINT_APPLICATION);
        Jurisdiction jurisdiction = new Jurisdiction();

        jurisdiction.setConnections(Set.of(JurisdictionConnections.APP_1_RESIDENT_JOINT));

        List<String> errors = jurisdiction.validateJurisdiction(caseData);

        assertThat(errors, contains(CONNECTION + JurisdictionConnections.APP_1_RESIDENT_JOINT + CANNOT_EXIST));
    }

    @Test
    public void shouldReturnErrorWhenJurisdictionConnectionIsI() {
        final CaseData caseData = caseData();
        caseData.setDivorceOrDissolution(DivorceOrDissolution.DIVORCE);
        caseData.getApplication().getMarriageDetails().setFormationType(MarriageFormation.OPPOSITE_SEX_COUPLE);
        Jurisdiction jurisdiction = new Jurisdiction();

        jurisdiction.setConnections(Set.of(JurisdictionConnections.RESIDUAL_JURISDICTION));

        List<String> errors = jurisdiction.validateJurisdiction(caseData);

        assertThat(errors, contains(CONNECTION + JurisdictionConnections.RESIDUAL_JURISDICTION + CANNOT_EXIST));
    }

    @Test
    public void shouldReturnErrorWhenJurisdictionConnectionsIsNull() {
        final CaseData caseData = caseData();
        Jurisdiction jurisdiction = new Jurisdiction();

        List<String> errors = jurisdiction.validateJurisdiction(caseData);

        assertThat(errors, contains("JurisdictionConnections" + EMPTY));
    }
}
