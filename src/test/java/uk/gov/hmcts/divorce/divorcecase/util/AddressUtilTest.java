package uk.gov.hmcts.divorce.divorcecase.util;

import com.microsoft.applicationinsights.boot.dependencies.apachecommons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

public class AddressUtilTest {

    private static final String ADDRESS_LINE1 = RandomStringUtils.random(20, true, false);
    private static final String ADDRESS_LINE2 = RandomStringUtils.random(10, true, false);

    @Test
    public void shouldReturnNullWhenAddressNotPresent() {
        assertNull(AddressUtil.getPostalAddress(null));
    }

    @Test
    public void shouldReturnPostalAddressWhenAddressPresentAndLine1IsLongerThan25Chars() {

        AddressGlobalUK addressGlobalUK = AddressGlobalUK.builder()
            .addressLine1(String.format("%s,%s", ADDRESS_LINE1, ADDRESS_LINE2))
            .postTown("town")
            .postCode("postcode")
            .build();

        assertThat(AddressUtil.getPostalAddress(addressGlobalUK),
            is(String.format("%s\n%s\ntown\npostcode", ADDRESS_LINE1, ADDRESS_LINE2)));
    }

    @Test
    public void shouldReturnPostalAddressWhenAddressPresentAndLine1IsLongerThan25CharsAndLine2Present() {

        AddressGlobalUK addressGlobalUK = AddressGlobalUK.builder()
            .addressLine1(String.format("%s,%s", ADDRESS_LINE1, ADDRESS_LINE2))
            .addressLine2("line2")
            .postTown("town")
            .postCode("postcode")
            .build();

        assertThat(AddressUtil.getPostalAddress(addressGlobalUK),
            is(String.format("%s\n%s,line2\ntown\npostcode", ADDRESS_LINE1, ADDRESS_LINE2)));
    }

    @Test
    public void shouldReturnPostalAddressWhenAddressPresentAndLine1IsLessThan25Chars() {

        AddressGlobalUK addressGlobalUK = AddressGlobalUK.builder()
            .addressLine1(ADDRESS_LINE1)
            .addressLine2(ADDRESS_LINE2)
            .postTown("town")
            .postCode("postcode")
            .build();

        assertThat(AddressUtil.getPostalAddress(addressGlobalUK),
            is(String.format("%s\n%s\ntown\npostcode", ADDRESS_LINE1, ADDRESS_LINE2)));
    }

    @Test
    public void shouldReturnPostalAddressWhenAddressPresentAndLine1IsNull() {

        AddressGlobalUK addressGlobalUK = AddressGlobalUK.builder()
            .postTown("town")
            .postCode("postcode")
            .build();

        assertThat(AddressUtil.getPostalAddress(addressGlobalUK), is("town\npostcode"));
    }
}
