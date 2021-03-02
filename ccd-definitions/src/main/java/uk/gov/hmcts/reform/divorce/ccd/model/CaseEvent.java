package uk.gov.hmcts.reform.divorce.ccd.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.reform.divorce.ccd.CcdBuilder;
import uk.gov.hmcts.reform.divorce.ccd.event.DraftCreate;
import uk.gov.hmcts.reform.divorce.ccd.event.PatchCase;
import uk.gov.hmcts.reform.divorce.ccd.event.SaveAndClose;

public enum CaseEvent {
    @JsonProperty(DraftCreate.DRAFT_CREATE)
    DRAFT_CREATE(new DraftCreate()),

    @JsonProperty(PatchCase.PATCH_CASE)
    PATCH_CASE(new PatchCase()),

    @JsonProperty(SaveAndClose.SAVE_AND_CLOSE)
    SAVE_AND_CLOSE(new SaveAndClose()),
    ;

    public final CcdBuilder builder;

    CaseEvent(CcdBuilder builder) {
        this.builder = builder;
    }
}
