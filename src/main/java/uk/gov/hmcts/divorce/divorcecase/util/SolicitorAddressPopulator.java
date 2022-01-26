package uk.gov.hmcts.divorce.divorcecase.util;

import uk.gov.hmcts.divorce.solicitor.client.organisation.OrganisationContactInformation;

import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static org.springframework.util.ObjectUtils.isEmpty;

public final class SolicitorAddressPopulator {

    private SolicitorAddressPopulator() {
    }

    public static String populateSolicitorAddress(
        OrganisationContactInformation organisationContactInformation) {

        return Stream.of(
            organisationContactInformation.getAddressLine1(),
            organisationContactInformation.getAddressLine2(),
            organisationContactInformation.getAddressLine3(),
            organisationContactInformation.getTownCity(),
            organisationContactInformation.getCounty(),
            organisationContactInformation.getPostCode(),
            organisationContactInformation.getCountry())
            .filter(value -> !isEmpty(value))
            .collect(joining("\n"));
    }
}
