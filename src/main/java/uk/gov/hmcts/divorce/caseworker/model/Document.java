package uk.gov.hmcts.divorce.caseworker.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;
@ComplexType(
        name = "Document",
        generate = false
)
@Builder
@Data
public class Document {
    @JsonProperty("document_url")
    private String url;
    @JsonProperty("document_filename")
    private String filename;
    @JsonProperty("document_binary_url")
    private String binaryUrl;
    @JsonProperty("category_id")
    private String categoryId;
    public long size;

    public Document(@JsonProperty("document_url") String url, @JsonProperty("document_filename") String filename, @JsonProperty(
            "document_binary_url") String binaryUrl, Long size) {
        this.url = url;
        this.filename = filename;
        this.binaryUrl = binaryUrl;
        this.size = size;
    }

    @JsonCreator
    public Document(@JsonProperty("document_url") String url, @JsonProperty("document_filename") String filename, @JsonProperty(
            "document_binary_url") String binaryUrl, @JsonProperty("category_id") String categoryId, long size) {
        this.url = url;
        this.filename = filename;
        this.binaryUrl = binaryUrl;
        this.categoryId = categoryId;
        this.size = size;
    }
}
