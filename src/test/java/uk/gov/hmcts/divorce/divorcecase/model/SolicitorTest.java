package uk.gov.hmcts.divorce.divorcecase.model;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.type.Organisation;
import uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy;

import static org.assertj.core.api.Assertions.assertThat;

class SolicitorTest {

    @Test
    void shouldReturnTrueIfOrganisationIdIsSet() {

        final Solicitor solicitor = Solicitor.builder()
            .organisationPolicy(OrganisationPolicy.<UserRole>builder()
                .organisation(Organisation.builder()
                    .organisationId("Org ID")
                    .build())
                .build())
            .build();

        assertThat(solicitor.hasOrgId()).isTrue();
    }

    @Test
    void shouldReturnFalseIfOrganisationIdIsEmptyOrNull() {

        final Solicitor solicitor1 = Solicitor.builder()
            .organisationPolicy(OrganisationPolicy.<UserRole>builder()
                .organisation(Organisation.builder()
                    .organisationId("")
                    .build())
                .build())
            .build();
        final Solicitor solicitor2 = Solicitor.builder()
            .organisationPolicy(OrganisationPolicy.<UserRole>builder()
                .organisation(Organisation.builder().build())
                .build())
            .build();

        assertThat(solicitor1.hasOrgId()).isFalse();
        assertThat(solicitor2.hasOrgId()).isFalse();
    }

    @Test
    void shouldReturnFalseIfOrganisationIsNull() {

        final Solicitor solicitor = Solicitor.builder()
            .organisationPolicy(OrganisationPolicy.<UserRole>builder().build())
            .build();

        assertThat(solicitor.hasOrgId()).isFalse();
    }
    
    @Test
    void shouldReturnFalseIfOrganisationPolicyIsNull() {

        final Solicitor solicitor = Solicitor.builder()
            .build();

        assertThat(solicitor.hasOrgId()).isFalse();
    }
}