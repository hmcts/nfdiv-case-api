package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.io.IOException;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonDeserialize(using = CaseDataOldDivorce.CaseDataOldDivorceDeserializer.class)
@Data
public class CaseDataOldDivorce {

    @JsonProperty("D8caseReference")
    private String d8caseReference;

    @JsonProperty("D8MarriageDate")
    private String d8MarriageDate;

    @JsonProperty("D8MarriagePlaceOfMarriage")
    private String d8MarriagePlaceOfMarriage;

    @JsonProperty("D8MarriagePetitionerName")
    private String d8MarriagePetitionerName;

    @JsonProperty("D8MarriageRespondentName")
    private String d8MarriageRespondentName;

    @JsonProperty("D8RespondentPostCode")
    private String d8RespondentPostCode;

    @JsonProperty("D8RespondentPostTown")
    private String d8RespondentPostTown;

    @JsonProperty("D8RespondentPostCode")
    private String d8PetitionerPostCode;

    @JsonProperty("D8RespondentPostTown")
    private String d8PetitionerPostTown;

    public static class CaseDataOldDivorceDeserializer extends JsonDeserializer<CaseDataOldDivorce> {
        @Override
        public CaseDataOldDivorce deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
            JsonNode rootNode = jsonParser.getCodec().readTree(jsonParser);
            CaseDataOldDivorce caseData = new CaseDataOldDivorce();

            caseData.setD8caseReference(rootNode.path("D8caseReference").asText(null));
            caseData.setD8MarriageDate(rootNode.path("D8MarriageDate").asText(null));
            caseData.setD8MarriagePlaceOfMarriage(rootNode.path("D8MarriagePlaceOfMarriage").asText(null));
            caseData.setD8MarriagePetitionerName(rootNode.path("D8MarriagePetitionerName").asText(null));
            caseData.setD8MarriageRespondentName(rootNode.path("D8MarriageRespondentName").asText(null));

            JsonNode respondentAddressNode = rootNode.path("D8RespondentHomeAddress");
            caseData.setD8RespondentPostCode(respondentAddressNode.path("PostCode").asText(null));
            caseData.setD8RespondentPostTown(respondentAddressNode.path("PostTown").asText(null));
            JsonNode petitionerAddressNode = rootNode.path("D8PetitionerHomeAddress");
            caseData.setD8PetitionerPostCode(petitionerAddressNode.path("PostCode").asText(null));
            caseData.setD8PetitionerPostTown(petitionerAddressNode.path("PostTown").asText(null));

            return caseData;
        }
    }
}
