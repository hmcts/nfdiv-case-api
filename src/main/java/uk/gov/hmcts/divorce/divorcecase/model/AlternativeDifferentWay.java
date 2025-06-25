package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum AlternativeDifferentWay implements HasLabel {

    @JsonProperty("textMessage")
    TEXT_MESSAGE("Text Message"),

    @JsonProperty("whatsapp")
    WHATSAPP("WhatsApp"),

    @JsonProperty("socialMedia")
    SOCIAL_MEDIA("Private message on social media"),

    @JsonProperty("other")
    OTHER("Other");

    private final String label;
}
