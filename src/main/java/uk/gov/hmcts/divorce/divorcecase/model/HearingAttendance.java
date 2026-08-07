package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum HearingAttendance implements HasLabel {
    @JsonProperty("inPerson")
    IN_PERSON("In person", "Wyneb yn wyneb"),

    @JsonProperty("remoteVideo")
    REMOTE_VIDEO_CALL("Remote - video call", "O bell - galwad fideo"),

    @JsonProperty("remotePhone")
    REMOTE_PHONE_CALL("Remote - phone call", "O bell - galwad ffôn");

    private final String label;
    private final String welshLabel;
}
