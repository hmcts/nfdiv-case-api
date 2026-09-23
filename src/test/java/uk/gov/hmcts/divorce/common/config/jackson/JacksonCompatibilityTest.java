package uk.gov.hmcts.divorce.common.config.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.jackson.UnwrappedPrefixModule;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.ccd.sdk.type.Organisation;
import uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static org.assertj.core.api.Assertions.assertThat;

class JacksonCompatibilityTest {

    @Test
    void serializesSdkAddressPropertiesWithJackson2() throws Exception {
        ObjectMapper mapper = mapper();

        String json = mapper.writeValueAsString(address("line 1", "town", "postcode", "UK"));

        assertThat(json)
            .contains("\"AddressLine1\":\"line 1\"")
            .contains("\"PostTown\":\"town\"")
            .contains("\"PostCode\":\"postcode\"")
            .contains("\"Country\":\"UK\"");
    }

    @Test
    void roundTripsPrefixedApplicantAddressesWithJackson2() throws Exception {
        ObjectMapper mapper = mapper();
        CaseData source = CaseData.builder()
            .applicant1(Applicant.builder()
                .address(address("applicant 1 line", "applicant 1 town", "A1", "UK"))
                .build())
            .applicant2(Applicant.builder()
                .address(address("applicant 2 line", "applicant 2 town", "A2", "UK"))
                .build())
            .build();

        String json = mapper.writeValueAsString(source);
        CaseData restored = mapper.readValue(json, CaseData.class);

        assertThat(json)
            .contains("\"applicant1Address\":{\"AddressLine1\":\"applicant 1 line\"")
            .contains("\"applicant2Address\":{\"AddressLine1\":\"applicant 2 line\"");
        assertThat(restored.getApplicant1().getAddress())
            .extracting(AddressGlobalUK::getAddressLine1, AddressGlobalUK::getPostTown,
                AddressGlobalUK::getPostCode, AddressGlobalUK::getCountry)
            .containsExactly("applicant 1 line", "applicant 1 town", "A1", "UK");
        assertThat(restored.getApplicant2().getAddress())
            .extracting(AddressGlobalUK::getAddressLine1, AddressGlobalUK::getPostTown,
                AddressGlobalUK::getPostCode, AddressGlobalUK::getCountry)
            .containsExactly("applicant 2 line", "applicant 2 town", "A2", "UK");
    }

    @Test
    void roundTripsOrganisationPolicyWithJackson2() throws Exception {
        ObjectMapper mapper = mapper();
        OrganisationPolicy<UserRole> source = OrganisationPolicy.<UserRole>builder()
            .organisation(Organisation.builder().organisationId("Org").organisationName("Organisation").build())
            .orgPolicyReference("reference")
            .prepopulateToUsersOrganisation(YesOrNo.YES)
            .orgPolicyCaseAssignedRole(UserRole.APPLICANT_2_SOLICITOR)
            .build();

        String json = mapper.writeValueAsString(source);
        OrganisationPolicy<?> restored = mapper.readValue(json, OrganisationPolicy.class);

        assertThat(json)
            .contains("\"OrganisationID\":\"Org\"")
            .contains("\"OrganisationName\":\"Organisation\"")
            .contains("\"OrgPolicyReference\":\"reference\"")
            .contains("\"PrepopulateToUsersOrganisation\":\"Yes\"")
            .contains("\"OrgPolicyCaseAssignedRole\":\"[APPTWOSOLICITOR]\"");
        assertThat(restored.getOrganisation())
            .extracting(Organisation::getOrganisationId, Organisation::getOrganisationName)
            .containsExactly("Org", "Organisation");
        assertThat(restored.getOrgPolicyReference()).isEqualTo("reference");
        assertThat(restored.getPrepopulateToUsersOrganisation()).isEqualTo(YesOrNo.YES);
        assertThat(restored.getOrgPolicyCaseAssignedRole()).isEqualTo(UserRole.APPLICANT_2_SOLICITOR);
    }

    private static ObjectMapper mapper() {
        // The SDK registers this module on every ObjectMapper bean; this test builds the mapper outside Spring.
        return new JacksonConfiguration().getMapper().registerModule(new UnwrappedPrefixModule());
    }

    private static AddressGlobalUK address(String line1, String town, String postCode, String country) {
        return AddressGlobalUK.builder()
            .addressLine1(line1)
            .postTown(town)
            .postCode(postCode)
            .country(country)
            .build();
    }
}
