package uk.gov.hmcts.reform.divorce.ccd.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum CaseEvent {
    @JsonProperty("draftCreate")
    DRAFT_CREATE("draftCreate"),

    @JsonProperty("patchCase")
    PATCH_CASE("patchCase");

    public final String name;

    CaseEvent(String value) {
        this.name = value;
    }

    public String toString() {
        return this.name;
    }
}
