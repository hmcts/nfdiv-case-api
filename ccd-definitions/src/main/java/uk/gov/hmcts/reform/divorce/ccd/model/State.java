package uk.gov.hmcts.reform.divorce.ccd.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.types.CCD;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Getter
public enum State {

    @JsonProperty("DRAFT")
    @CCD(
        label = "Draft",
        name = "Draft"
    )
    DRAFT("Draft");

    private final String value;
    private final String label;

    State(String value) {
        this.value = value;
        this.label = value;
    }

    public static State fromValue(final String value) {
        return tryFromValue(value)
            .orElseThrow(() -> new NoSuchElementException("Unable to map " + value + " to a case state"));
    }

    public static Optional<State> tryFromValue(final String value) {
        return Stream.of(values())
            .filter(state -> state.value.equalsIgnoreCase(value))
            .findFirst();
    }
}

