package uk.gov.hmcts.divorce.testutil;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.ccd.document.am.model.Classification;

import java.util.Date;
import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CaseDocumentAMDocument {

    public Classification classification;

    public long size;

    public String mimeType;

    public String originalDocumentName;

    public Date createdOn;

    public Date modifiedOn;

    public String createdBy;

    public String lastModifiedBy;

    public Date ttl;

    public String hashToken;

    public Map<String, String> metadata;

    @JsonProperty("_links")
    public uk.gov.hmcts.reform.ccd.document.am.model.Document.Links links;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Links {
        public uk.gov.hmcts.reform.ccd.document.am.model.Document.Link self;
        public uk.gov.hmcts.reform.ccd.document.am.model.Document.Link binary;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Link {
        public String href;
    }
}

