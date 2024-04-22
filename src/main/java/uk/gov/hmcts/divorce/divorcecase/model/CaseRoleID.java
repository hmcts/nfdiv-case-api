package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasRole;

import java.util.List;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
public class CaseRoleID implements HasRole {

    @JsonProperty("value")
    private DynamicListItem value;

    @JsonProperty("list_items")
    private List<DynamicListItem> listItems;

    public String getRole() {
        return value.getCode().toString();
    }

    public String getCaseTypePermissions() {
        return value.getCode().toString();
    }

    public CaseRoleID(@JsonProperty("value") DynamicListItem value,
                       @JsonProperty("list_items") List<DynamicListItem> listItems) {
        this.value = value;
        this.listItems = listItems;
    }
}
