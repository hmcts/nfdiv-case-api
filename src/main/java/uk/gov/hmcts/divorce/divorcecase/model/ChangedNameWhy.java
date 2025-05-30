package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum ChangedNameWhy implements HasLabel {

    @JsonProperty("deedPoll")
    DEED_POLL("I changed my name by deed poll"),

    @JsonProperty("changedPartsOfName")
    CHANGED_PARTS_OF_NAME("I changed my last name or parts of my name when I got married"),

    @JsonProperty("partOfNameNotIncluded")
    PART_OF_NAME_NOT_INCLUDED("Part of my legal name was not included on the certificate"),

    @JsonProperty("partOfNameAbbreviated")
    PART_OF_NAME_ABBREVIATED("Part of my legal name is abbreviated on the certificate"),

    @JsonProperty("legalNameSpelledDifferently")
    LEGAL_NAME_SPELLED_DIFFERENTLY("My legal name is spelled differently on the certificate"),

    @JsonProperty("other")
    OTHER("Other");

    private final String label;
}
