package uk.gov.hmcts.divorce.common.model;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static uk.gov.hmcts.divorce.common.validation.ValidationUtil.EMPTY;

public class JurisdictionTest {

    public static final String CONNECTION = "Connection ";
    public static final String CANNOT_EXIST = " cannot exist";

    @Test
    public void shouldReturnErrorWhenJurisdictionConnectionIsA() {
        Jurisdiction jurisdiction = new Jurisdiction();

        jurisdiction.setConnections(Set.of(JurisdictionConnections.APP_1_APP_2_RESIDENT));

        List<String> errors = jurisdiction.validate();

        assertThat(errors, contains(CONNECTION + JurisdictionConnections.APP_1_APP_2_RESIDENT + CANNOT_EXIST));
    }

    @Test
    public void shouldReturnErrorWhenJurisdictionConnectionIsB() {
        Jurisdiction jurisdiction = new Jurisdiction();

        jurisdiction.setConnections(Set.of(JurisdictionConnections.APP_1_APP_2_LAST_RESIDENT));

        List<String> errors = jurisdiction.validate();

        assertThat(errors, contains(CONNECTION + JurisdictionConnections.APP_1_APP_2_LAST_RESIDENT + CANNOT_EXIST));
    }

    @Test
    public void shouldReturnErrorWhenJurisdictionConnectionIsC() {
        Jurisdiction jurisdiction = new Jurisdiction();

        jurisdiction.setConnections(Set.of(JurisdictionConnections.APP_2_RESIDENT));

        List<String> errors = jurisdiction.validate();

        assertThat(errors, contains(CONNECTION + JurisdictionConnections.APP_2_RESIDENT + CANNOT_EXIST));
    }

    @Test
    public void shouldReturnErrorWhenJurisdictionConnectionIsD() {
        Jurisdiction jurisdiction = new Jurisdiction();

        jurisdiction.setConnections(Set.of(JurisdictionConnections.APP_1_RESIDENT_TWELVE_MONTHS));

        List<String> errors = jurisdiction.validate();

        assertThat(errors, contains(CONNECTION + JurisdictionConnections.APP_1_RESIDENT_TWELVE_MONTHS + CANNOT_EXIST));
    }

    @Test
    public void shouldReturnErrorWhenJurisdictionConnectionIsE() {
        Jurisdiction jurisdiction = new Jurisdiction();

        jurisdiction.setConnections(Set.of(JurisdictionConnections.APP_1_RESIDENT_SIX_MONTHS));

        List<String> errors = jurisdiction.validate();

        assertThat(errors, contains(CONNECTION + JurisdictionConnections.APP_1_RESIDENT_SIX_MONTHS + CANNOT_EXIST));
    }

    @Test
    public void shouldReturnErrorWhenJurisdictionConnectionIsF() {
        Jurisdiction jurisdiction = new Jurisdiction();

        jurisdiction.setConnections(Set.of(JurisdictionConnections.APP_1_APP_2_DOMICILED));

        List<String> errors = jurisdiction.validate();

        assertThat(errors, contains(CONNECTION + JurisdictionConnections.APP_1_APP_2_DOMICILED + CANNOT_EXIST));
    }

    @Test
    public void shouldReturnErrorWhenJurisdictionConnectionIsG() {
        Jurisdiction jurisdiction = new Jurisdiction();

        jurisdiction.setConnections(Set.of(JurisdictionConnections.RESIDUAL_JURISDICTION));

        List<String> errors = jurisdiction.validate();

        assertThat(errors, contains(CONNECTION + JurisdictionConnections.RESIDUAL_JURISDICTION + CANNOT_EXIST));
    }

    @Test
    public void shouldReturnErrorWhenJurisdictionConnectionIsH() {
        Jurisdiction jurisdiction = new Jurisdiction();

        jurisdiction.setConnections(Set.of(JurisdictionConnections.APP_1_DOMICILED));

        List<String> errors = jurisdiction.validate();

        assertThat(errors, contains(CONNECTION + JurisdictionConnections.APP_1_DOMICILED + CANNOT_EXIST));
    }

    @Test
    public void shouldReturnErrorWhenJurisdictionConnectionIsI() {
        Jurisdiction jurisdiction = new Jurisdiction();

        jurisdiction.setConnections(Set.of(JurisdictionConnections.APP_2_DOMICILED));

        List<String> errors = jurisdiction.validate();

        assertThat(errors, contains(CONNECTION + JurisdictionConnections.APP_2_DOMICILED + CANNOT_EXIST));
    }

    @Test
    public void shouldReturnErrorWhenJurisdictionConnectionIsJ() {
        Jurisdiction jurisdiction = new Jurisdiction();

        jurisdiction.setConnections(Set.of(JurisdictionConnections.APP_1_RESIDENT_JOINT));

        List<String> errors = jurisdiction.validate();

        assertThat(errors, contains(CONNECTION + JurisdictionConnections.APP_1_RESIDENT_JOINT + CANNOT_EXIST));
    }

    @Test
    public void shouldReturnErrorWhenJurisdictionConnectionsIsNull() {
        Jurisdiction jurisdiction = new Jurisdiction();

        List<String> errors = jurisdiction.validate();

        assertThat(errors, contains("JurisdictionConnections" + EMPTY));
    }
}
