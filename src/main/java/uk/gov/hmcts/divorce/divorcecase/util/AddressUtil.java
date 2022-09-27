package uk.gov.hmcts.divorce.divorcecase.util;

import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.joinWith;
import static org.apache.commons.lang3.StringUtils.removeStart;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringBefore;

public final class AddressUtil {

    private static final String COMMA_SEPARATOR = ",";
    private static final int ADDRESS_LINE_MAX_CHARS = 25;
    private static final List<String> UK_TERMS = Arrays.asList("unitedkingdom", "uk", "england", "wales", "greatbritain");
    private static final List<String> SCOTTISH_POSTCODE_PREFIXES =
        Arrays.asList("ab", "dd", "dg", "eh", "fk", "g", "hs", "iv", "ka", "kw", "ky", "ml", "pa", "ph", "td", "ze");
    private static final String NI_POSTCODE_PREFIX = "bt";
    private static final String OVERSEAS_EXCEPTION_MESSAGE =
        "Cannot assert whether address is overseas or not due to null address or blank/null country";

    private AddressUtil() {
    }

    public static String getPostalAddress(final AddressGlobalUK address) {
        if (null != address) {
            String formattedAddressLine1;
            String formattedAddressLine2;
            // Split the string after 25 characters so that it can fit in the address window of envelope
            if (null != address.getAddressLine1() && address.getAddressLine1().length() > ADDRESS_LINE_MAX_CHARS) {
                formattedAddressLine1 = substringBefore(address.getAddressLine1(), COMMA_SEPARATOR);

                if (StringUtils.isBlank(address.getAddressLine2())) {
                    formattedAddressLine2 = substringAfter(address.getAddressLine1(), COMMA_SEPARATOR);
                } else {
                    formattedAddressLine2 = joinWith(
                        COMMA_SEPARATOR,
                        substringAfter(address.getAddressLine1(), COMMA_SEPARATOR),
                        address.getAddressLine2()
                    );
                }

                // remove any space if present due to split
                formattedAddressLine2 = removeStart(formattedAddressLine2, StringUtils.SPACE);
            } else {
                formattedAddressLine1 = address.getAddressLine1();
                formattedAddressLine2 = address.getAddressLine2();
            }

            return Stream.of(
                    formattedAddressLine1,
                    formattedAddressLine2,
                    address.getAddressLine3(),
                    address.getPostTown(),
                    address.getPostCode()
                )
                .filter(value -> value != null && !value.isEmpty())
                .collect(joining("\n"));
        }

        return null;
    }

    public static boolean isEnglandOrWales(AddressGlobalUK address) {
        if (isNull(address) || StringUtils.isBlank(address.getCountry())) {
            throw new IllegalArgumentException(OVERSEAS_EXCEPTION_MESSAGE);
        }

        final String sanitisedCountry = address.getCountry().replaceAll("[^a-zA-Z0-9]+", "").toLowerCase(Locale.ROOT);
        final String postcode = Optional.ofNullable(address.getPostCode()).orElse("");

        var isScottishOrNorthernIrishPostcode = false;
        if (postcode.matches(".*[a-zA-Z]+.*")) {
            final String sanitisedPostcodePrefix = postcode.split("[0-9]")[0].toLowerCase(Locale.ROOT);
            isScottishOrNorthernIrishPostcode =
                SCOTTISH_POSTCODE_PREFIXES.contains(sanitisedPostcodePrefix) || NI_POSTCODE_PREFIX.equals(sanitisedPostcodePrefix);
        }

        return UK_TERMS.contains(sanitisedCountry) && !isScottishOrNorthernIrishPostcode;
    }
}
