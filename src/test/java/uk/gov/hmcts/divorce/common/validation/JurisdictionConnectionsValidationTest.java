package uk.gov.hmcts.divorce.common.validation;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.JurisdictionConnections;

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.hmcts.divorce.common.validation.JurisdictionConnectionsValidation.validateJurisdictionConnectionA;
import static uk.gov.hmcts.divorce.common.validation.JurisdictionConnectionsValidation.validateJurisdictionConnectionB;
import static uk.gov.hmcts.divorce.common.validation.JurisdictionConnectionsValidation.validateJurisdictionConnectionC;
import static uk.gov.hmcts.divorce.common.validation.JurisdictionConnectionsValidation.validateJurisdictionConnectionD;
import static uk.gov.hmcts.divorce.common.validation.JurisdictionConnectionsValidation.validateJurisdictionConnectionE;
import static uk.gov.hmcts.divorce.common.validation.JurisdictionConnectionsValidation.validateJurisdictionConnectionF;
import static uk.gov.hmcts.divorce.common.validation.JurisdictionConnectionsValidation.validateJurisdictionConnectionG;
import static uk.gov.hmcts.divorce.common.validation.JurisdictionConnectionsValidation.validateJurisdictionConnectionH;
import static uk.gov.hmcts.divorce.common.validation.JurisdictionConnectionsValidation.validateJurisdictionConnectionI;

public class JurisdictionConnectionsValidationTest {

    public static final String CONNECTION = "Connection ";
    public static final String CANNOT_EXIST = " cannot exist";

    @Test
    public void whenValidatingJurisdictionConnectionAShouldReturnError() {
        CaseData caseData = new CaseData();
        caseData.setJurisdictionConnections(Set.of(JurisdictionConnections.PET_RESP_RESIDENT));

        String error = validateJurisdictionConnectionA(caseData.getJurisdictionConnections(), caseData);

        assertThat(error, is(CONNECTION + JurisdictionConnections.PET_RESP_RESIDENT + CANNOT_EXIST));
    }

    @Test
    public void whenValidatingJurisdictionConnectionBShouldReturnError() {
        CaseData caseData = new CaseData();
        caseData.setJurisdictionConnections(Set.of(JurisdictionConnections.PET_RESP_LAST_RESIDENT));

        String error = validateJurisdictionConnectionB(caseData.getJurisdictionConnections(), caseData);

        assertThat(error, is(CONNECTION + JurisdictionConnections.PET_RESP_LAST_RESIDENT + CANNOT_EXIST));
    }

    @Test
    public void whenValidatingJurisdictionConnectionCShouldReturnError() {
        CaseData caseData = new CaseData();
        caseData.setJurisdictionConnections(Set.of(JurisdictionConnections.RESP_RESIDENT));

        String error = validateJurisdictionConnectionC(caseData.getJurisdictionConnections(), caseData);

        assertThat(error, is(CONNECTION + JurisdictionConnections.RESP_RESIDENT + CANNOT_EXIST));
    }

    @Test
    public void whenValidatingJurisdictionConnectionDShouldReturnError() {
        CaseData caseData = new CaseData();
        caseData.setJurisdictionConnections(Set.of(JurisdictionConnections.PET_RESIDENT_TWELVE_MONTHS));

        String error = validateJurisdictionConnectionD(caseData.getJurisdictionConnections(), caseData);

        assertThat(error, is(CONNECTION + JurisdictionConnections.PET_RESIDENT_TWELVE_MONTHS + CANNOT_EXIST));
    }

    @Test
    public void whenValidatingJurisdictionConnectionEShouldReturnError() {
        CaseData caseData = new CaseData();
        caseData.setJurisdictionConnections(Set.of(JurisdictionConnections.PET_RESIDENT_SIX_MONTHS));

        String error = validateJurisdictionConnectionE(caseData.getJurisdictionConnections(), caseData);

        assertThat(error, is(CONNECTION + JurisdictionConnections.PET_RESIDENT_SIX_MONTHS + CANNOT_EXIST));
    }

    @Test
    public void whenValidatingJurisdictionConnectionFShouldReturnError() {
        CaseData caseData = new CaseData();
        caseData.setJurisdictionConnections(Set.of(JurisdictionConnections.PET_RESP_DOMICILED));

        String error = validateJurisdictionConnectionF(caseData.getJurisdictionConnections(), caseData);

        assertThat(error, is(CONNECTION + JurisdictionConnections.PET_RESP_DOMICILED + CANNOT_EXIST));
    }

    @Test
    public void whenValidatingJurisdictionConnectionGShouldReturnError() {
        CaseData caseData = new CaseData();
        caseData.setJurisdictionConnections(Set.of(JurisdictionConnections.RESIDUAL_JURISDICTION));

        String error = validateJurisdictionConnectionG(caseData.getJurisdictionConnections(), caseData);

        assertThat(error, is(CONNECTION + JurisdictionConnections.RESIDUAL_JURISDICTION + CANNOT_EXIST));
    }

    @Test
    public void whenValidatingJurisdictionConnectionHShouldReturnError() {
        CaseData caseData = new CaseData();
        caseData.setJurisdictionConnections(Set.of(JurisdictionConnections.PET_DOMICILED));

        String error = validateJurisdictionConnectionH(caseData.getJurisdictionConnections(), caseData);

        assertThat(error, is(CONNECTION + JurisdictionConnections.PET_DOMICILED + CANNOT_EXIST));
    }

    @Test
    public void whenValidatingJurisdictionConnectionIShouldReturnError() {
        CaseData caseData = new CaseData();
        caseData.setJurisdictionConnections(Set.of(JurisdictionConnections.RESP_DOMICILED));

        String error = validateJurisdictionConnectionI(caseData.getJurisdictionConnections(), caseData);

        assertThat(error, is(CONNECTION + JurisdictionConnections.RESP_DOMICILED + CANNOT_EXIST));
    }
}
