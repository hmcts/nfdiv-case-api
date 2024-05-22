package uk.gov.hmcts.divorce.noticeofchange.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;
import uk.gov.hmcts.ccd.sdk.api.HasRole;
import uk.gov.hmcts.ccd.sdk.type.ChangeOrganisationApprovalStatus;

import java.time.LocalDateTime;

@Data
@ComplexType(
        name = "ChangeOrganisationRequest",
        generate = false
)
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChangeOrganisationRequest<R extends HasRole> {
    @JsonProperty("OrganisationToAdd")
    private Organisation organisationToAdd;
    @JsonProperty("OrganisationToRemove")
    private Organisation organisationToRemove;
    @JsonProperty("CaseRoleId")
    private R caseRoleId;
    @JsonProperty("ApprovalStatus")
    private ChangeOrganisationApprovalStatus approvalStatus;
    @JsonSerialize(
            using = LocalDateTimeSerializer.class
    )
    @JsonDeserialize(
            using = LocalDateTimeDeserializer.class
    )
    @JsonProperty("RequestTimestamp")
    private LocalDateTime requestTimestamp;
    @JsonProperty("CreatedBy")
    private String createdBy;

    @JsonCreator
    public ChangeOrganisationRequest(@JsonProperty("OrganisationToAdd") Organisation organisationToAdd,
                                     @JsonProperty("OrganisationToRemove") Organisation organisationToRemove,
                                     @JsonProperty("CaseRoleId") R caseRoleId,
                                     @JsonProperty("Reason") String reason,
                                     @JsonProperty("NotesReason") String notesReason,
                                     @JsonProperty("ApprovalStatus") ChangeOrganisationApprovalStatus approvalStatus,
                                     @JsonProperty("RequestTimestamp") LocalDateTime requestTimestamp,
                                     @JsonProperty("ApprovalRejectionTimestamp") LocalDateTime approvalRejectionTimestamp,
                                     @JsonProperty("CreatedBy") String createdBy) {
        this.organisationToAdd = organisationToAdd;
        this.organisationToRemove = organisationToRemove;
        this.caseRoleId = caseRoleId;
        this.approvalStatus = approvalStatus;
        this.requestTimestamp = requestTimestamp;
        this.createdBy = createdBy;
    }

    public static <R extends HasRole> ChangeOrganisationRequestBuilder<R> builder() {
        return new ChangeOrganisationRequestBuilder<>();
    }

    public ChangeOrganisationRequest() {
    }

    public static class ChangeOrganisationRequestBuilder<R extends HasRole> {
        private Organisation organisationToAdd;
        private Organisation organisationToRemove;
        private R caseRoleId;
        private String reason;
        private String notesReason;
        private ChangeOrganisationApprovalStatus approvalStatus;
        private LocalDateTime requestTimestamp;
        private LocalDateTime approvalRejectionTimestamp;
        private String createdBy;

        ChangeOrganisationRequestBuilder() {
        }

        public  ChangeOrganisationRequest<R> build() {
            return new  ChangeOrganisationRequest<>(this.organisationToAdd, this.organisationToRemove, this.caseRoleId, this.reason,
                    this.notesReason, this.approvalStatus, this.requestTimestamp, this.approvalRejectionTimestamp, this.createdBy);
        }
    }
}

