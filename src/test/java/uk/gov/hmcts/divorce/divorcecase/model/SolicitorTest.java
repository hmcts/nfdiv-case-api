package uk.gov.hmcts.divorce.divorcecase.model;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.type.Organisation;
import uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy;
import uk.gov.hmcts.divorce.solicitor.client.organisation.OrganisationContactInformation;
import uk.gov.hmcts.divorce.solicitor.client.organisation.OrganisationsResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_ADDRESS;

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

    @Test
    void hasOrgNameShouldReturnTrueIfOrganisationNameIsSet() {
        final Solicitor solicitor = Solicitor.builder()
            .organisationPolicy(OrganisationPolicy.<UserRole>builder()
                .organisation(Organisation.builder()
                    .organisationName("Org Name")
                    .organisationId("Org ID")
                    .build())
                .build())
            .build();

        assertThat(solicitor.hasOrgName()).isTrue();
    }

    @Test
    void hasOrgNameShouldReturnFalseIfOrganisationNameIsEmptyOrNull() {

        final Solicitor solicitor1 = Solicitor.builder()
            .organisationPolicy(OrganisationPolicy.<UserRole>builder()
                .organisation(Organisation.builder()
                    .organisationName("")
                    .build())
                .build())
            .build();
        final Solicitor solicitor2 = Solicitor.builder()
            .organisationPolicy(OrganisationPolicy.<UserRole>builder()
                .organisation(Organisation.builder().build())
                .build())
            .build();

        assertThat(solicitor1.hasOrgName()).isFalse();
        assertThat(solicitor2.hasOrgName()).isFalse();
    }

    @Test
    void hasOrgNameShouldReturnFalseIfOrganisationIsNull() {

        final Solicitor solicitor = Solicitor.builder()
            .organisationPolicy(OrganisationPolicy.<UserRole>builder().build())
            .build();

        assertThat(solicitor.hasOrgName()).isFalse();
    }

    @Test
    void shouldReturnOrganisationIdIfPresent() {

        final Solicitor solicitor = Solicitor.builder()
            .organisationPolicy(OrganisationPolicy.<UserRole>builder()
                .organisation(Organisation.builder()
                    .organisationId("Org ID")
                    .build())
                .build())
            .build();

        assertThat(solicitor.hasOrgName()).isFalse();
        assertThat(solicitor.getOrganisationId()).isEqualTo("Org ID");
    }

    @Test
    void shouldReturnNullIfOrganisationIdIsMissing() {

        final Solicitor solicitor1 = Solicitor.builder().build();
        final Solicitor solicitor2 = Solicitor.builder()
            .organisationPolicy(OrganisationPolicy.<UserRole>builder().build()).build();

        assertThat(solicitor1.getOrganisationId()).isEqualTo(null);
        assertThat(solicitor2.getOrganisationId()).isEqualTo(null);
    }

    @Test
    void shouldHandleNullOrganisationResponseWhenSettingAddressToOrganisationDefault() {
        final Solicitor solicitor = Solicitor.builder().build();

        solicitor.setAddressToOrganisationDefault(null);

        assertThat(solicitor.getAddress()).isEqualTo(null);
        assertThat(solicitor.getAddressOverseas()).isEqualTo(null);
    }

    @Test
    void shouldHandleMissingContactInformationWhenSettingAddressToOrganisationDefault() {
        final OrganisationsResponse organisationResponse = OrganisationsResponse.builder()
            .contactInformation(null)
            .build();
        final Solicitor solicitor = Solicitor.builder().build();

        solicitor.setAddressToOrganisationDefault(organisationResponse);

        assertThat(solicitor.getAddress()).isEqualTo(null);
        assertThat(solicitor.getAddressOverseas()).isEqualTo(null);
    }

    @Test
    void shouldSetAddressWhenOrganisationContactInformationIsPresent() {
        final OrganisationsResponse organisationResponse = OrganisationsResponse.builder()
            .contactInformation(List.of(OrganisationContactInformation.builder().addressLine1(TEST_SOLICITOR_ADDRESS).build()))
            .build();
        final Solicitor solicitor = Solicitor.builder().build();

        solicitor.setAddressToOrganisationDefault(organisationResponse);

        assertThat(solicitor.getAddress()).isEqualTo(TEST_SOLICITOR_ADDRESS);
        assertThat(solicitor.getAddressOverseas()).isEqualTo(null);
    }
}
