package uk.gov.hmcts.divorce.divorcecase.model.interimapplications;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum NoResponseSendPapersAgainOrTrySomethingElse implements HasLabel {
    @JsonProperty("sendPapersAgain")
    SEND_PAPERS_AGAIN("I want to send the divorce papers again"),

    @JsonProperty("trySomethingElse")
    TRY_SOMETHING_ELSE("I want to try something else"),

    @JsonProperty("papersSent")
    PAPERS_SENT("Papers sent");
    private final String label;
}
