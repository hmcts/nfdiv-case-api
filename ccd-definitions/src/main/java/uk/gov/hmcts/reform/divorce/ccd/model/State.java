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

    @JsonProperty("Draft")
    @CCD(
        label = "Draft",
        name = "Draft"
    )
    Draft("Draft");

    private final String name;

}

