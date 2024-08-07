package uk.gov.hmcts.divorce.divorcecase.util;

import uk.gov.hmcts.divorce.solicitor.client.organisation.OrganisationContactInformation;

import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static org.springframework.util.ObjectUtils.isEmpty;

public final class SolicitorAddressPopulator {

    private static final int ORGANISATION_ADDRESS_INDEX = 0;

    private SolicitorAddressPopulator() {
    }

    public static String parseOrganisationAddress(List<OrganisationContactInformation> organisationContactInformation) {
        OrganisationContactInformation orgAddress = organisationContactInformation.get(ORGANISATION_ADDRESS_INDEX);

        return Stream.of(
            orgAddress.getAddressLine1(),
                orgAddress.getAddressLine2(),
                orgAddress.getAddressLine3(),
                orgAddress.getTownCity(),
                orgAddress.getCounty(),
                orgAddress.getPostCode(),
                orgAddress.getCountry())
            .filter(value -> !isEmpty(value))
            .collect(joining("\n"));
    }
}
