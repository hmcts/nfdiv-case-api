package uk.gov.hmcts.reform.divorce.caseapi.model.docassembly;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Value;

import java.util.Base64;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@Value
public class DocAssemblyRequest {
    private String templateId;

    private String outputType;

    private JsonNode formPayload;

    private String outputFilename;

    public static class DocAssemblyRequestBuilder {
        public DocAssemblyRequestBuilder templateId(String templateId) {
            this.templateId = Base64.getEncoder()
                .encodeToString(templateId.getBytes());
            return this;
        }
    }
}
