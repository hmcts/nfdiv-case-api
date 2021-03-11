package uk.gov.hmcts.reform.divorce.ccd.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum DivorceOrDissolution implements HasLabel {

    DIVORCE("Divorce"),
    DISSOLUTION("Dissolution");

    @JsonValue
    private final String label;

    public static boolean isDivorce(DivorceOrDissolution divorceOrDissolution) {
        return DIVORCE.name().equalsIgnoreCase(divorceOrDissolution.name());
    }
}
