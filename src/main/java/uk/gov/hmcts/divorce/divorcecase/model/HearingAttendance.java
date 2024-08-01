package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum HearingAttendance implements HasLabel {
    @JsonProperty("inPerson")
    IN_PERSON("In person"),

    @JsonProperty("remoteVideo")
    REMOTE_VIDEO_CALL("Remote - video call"),

    @JsonProperty("remotePhone")
    REMOTE_PHONE_CALL("Remote - phone call");

    private final String label;
}
