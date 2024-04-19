package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.type.ChangeOrganisationApprovalStatus;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.Organisation;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangeOrganisationRequest {

    @JsonProperty("OrganisationToAdd")
    private Organisation organisationToAdd;

    @JsonProperty("OrganisationToRemove")
    private Organisation organisationToRemove;

    @JsonProperty("CaseRoleId")
    private DynamicList caseRoleId;

    @JsonProperty("RequestTimestamp")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime requestTimestamp;

    @JsonProperty("ApprovalStatus")
    private ChangeOrganisationApprovalStatus approvalStatus;

    @JsonProperty("CreatedBy")
    private String createdBy;
}
