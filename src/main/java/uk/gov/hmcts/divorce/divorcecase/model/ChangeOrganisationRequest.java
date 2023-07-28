package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;
import uk.gov.hmcts.ccd.sdk.type.ChangeOrganisationApprovalStatus;
import uk.gov.hmcts.ccd.sdk.type.Organisation;

import java.time.LocalDateTime;

@NoArgsConstructor
@Builder
@Data
@ComplexType(name = "ChangeOrganisationRequest", generate = false)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChangeOrganisationRequest {

    @JsonProperty("OrganisationToAdd")
    private Organisation organisationToAdd;

    @JsonProperty("OrganisationToRemove")
    private Organisation organisationToRemove;

    @JsonProperty("CaseRoleId")
    private NocDynamicList caseRoleId;

    @JsonProperty("Reason")
    private String reason;

    @JsonProperty("NotesReason")
    private String notesReason;

    @JsonProperty("ApprovalStatus")
    private ChangeOrganisationApprovalStatus approvalStatus;

    @JsonProperty("RequestTimestamp")
    private LocalDateTime requestTimestamp;

    @JsonProperty("ApprovalRejectionTimestamp")
    private LocalDateTime approvalRejectionTimestamp;

    @JsonCreator
    public ChangeOrganisationRequest(
        @JsonProperty("OrganisationToAdd") Organisation organisationToAdd,
        @JsonProperty("OrganisationToRemove") Organisation organisationToRemove,
        @JsonProperty("CaseRoleId") NocDynamicList caseRoleId,
        @JsonProperty("Reason") String reason,
        @JsonProperty("NotesReason") String notesReason,
        @JsonProperty("ApprovalStatus") ChangeOrganisationApprovalStatus approvalStatus,
        @JsonProperty("RequestTimestamp") LocalDateTime requestTimestamp,
        @JsonProperty("ApprovalRejectionTimestamp") LocalDateTime approvalRejectionTimestamp
    ) {
        this.organisationToAdd = organisationToAdd;
        this.organisationToRemove = organisationToRemove;
        this.caseRoleId = caseRoleId;
        this.reason = reason;
        this.notesReason = notesReason;
        this.approvalStatus = approvalStatus;
        this.requestTimestamp = requestTimestamp;
        this.approvalRejectionTimestamp = approvalRejectionTimestamp;
    }
}
