package uk.gov.hmcts.divorce.document.model;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;

import java.util.List;

@Data
@NoArgsConstructor
@Builder
public class LetterPack {
    @CCD(
        label = "letters sent"
    )
    private List<ListValue<Document>> letters;

    @CCD(label = "Address")
    private String recipientAddress;

    @JsonCreator
    public LetterPack(@JsonProperty("letters") List<ListValue<Document>> letters,
                      @JsonProperty("recipientAddress") String recipientAddress) {
        this.letters = letters;
        this.recipientAddress = recipientAddress;
    }
}
