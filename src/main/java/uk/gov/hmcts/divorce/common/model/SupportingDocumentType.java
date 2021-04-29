package uk.gov.hmcts.divorce.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum SupportingDocumentType implements HasLabel {

    @JsonProperty("unionCertificate")
    UNION_CERTIFICATE("Union Certificate"),

    @JsonProperty("foreignUnionCertificate")
    FOREIGN_UNION_CERTIFICATE("Foreign union certificate"),

    @JsonProperty("foreignUnionCertificateTranslation")
    FOREIGN_UNION_CERTIFICATE_TRANSLATION("Certified foreign union certificate translation"),

    @JsonProperty("nameChangeProof")
    NAME_CHANGE_PROOF("Change of name proof");

    private final String label;
}
