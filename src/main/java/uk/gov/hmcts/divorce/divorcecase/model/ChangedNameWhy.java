package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum ChangedNameWhy implements HasLabel {

    @JsonProperty("deedPoll")
    DEED_POLL("They changed their name by deed poll"),

    @JsonProperty("partOfNameNotIncluded")
    PART_OF_NAME_NOT_INCLUDED("Part of their legal name was not included on the certificate"),

    @JsonProperty("partOfNameAbbreviated")
    PART_OF_NAME_ABBREVIATED("Part of their legal name is abbreviated on the certificate"),

    @JsonProperty("legalNameSpelledDifferently")
    LEGAL_NAME_SPELLED_DIFFERENTLY("Their legal name is spelled differently on the certificate"),

    @JsonProperty("changedPartsOfName")
    CHANGED_PARTS_OF_NAME("They changed their last name or parts of their name when they got married or formed the civil partnership"),

    @JsonProperty("other")
    OTHER("Other");

    private final String label;
}
