package uk.gov.hmcts.divorce.noticeofchange.model;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@Builder
@Data
@ComplexType(name = "PreviousOrganisationCollectionItem", generate = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PreviousOrganisationCollectionItem {

    @JsonProperty("id")
    private String id;

    @JsonProperty("value")
    private PreviousOrganisation value;
}
