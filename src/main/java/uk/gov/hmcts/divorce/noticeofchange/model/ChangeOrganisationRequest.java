package uk.gov.hmcts.divorce.noticeofchange.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
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
    @JsonFormat(
            pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS"
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

        @JsonProperty("OrganisationToAdd")
        public  ChangeOrganisationRequest.ChangeOrganisationRequestBuilder<R> organisationToAdd(Organisation organisationToAdd) {
            this.organisationToAdd = organisationToAdd;
            return this;
        }

        @JsonProperty("OrganisationToRemove")
        public  ChangeOrganisationRequest.ChangeOrganisationRequestBuilder<R> organisationToRemove(Organisation organisationToRemove) {
            this.organisationToRemove = organisationToRemove;
            return this;
        }

        @JsonProperty("CaseRoleId")
        public  ChangeOrganisationRequest.ChangeOrganisationRequestBuilder<R> caseRoleId(R caseRoleId) {
            this.caseRoleId = caseRoleId;
            return this;
        }

        @JsonProperty("Reason")
        public  ChangeOrganisationRequest.ChangeOrganisationRequestBuilder<R> reason(String reason) {
            this.reason = reason;
            return this;
        }

        @JsonProperty("NotesReason")
        public  ChangeOrganisationRequest.ChangeOrganisationRequestBuilder<R> notesReason(String notesReason) {
            this.notesReason = notesReason;
            return this;
        }

        @JsonProperty("ApprovalStatus")
        public  ChangeOrganisationRequest.ChangeOrganisationRequestBuilder<R>
            approvalStatus(ChangeOrganisationApprovalStatus approvalStatus) {
            this.approvalStatus = approvalStatus;
            return this;
        }

        @JsonProperty("RequestTimestamp")
        public  ChangeOrganisationRequest.ChangeOrganisationRequestBuilder<R> requestTimestamp(LocalDateTime requestTimestamp) {
            this.requestTimestamp = requestTimestamp;
            return this;
        }

        @JsonProperty("ApprovalRejectionTimestamp")
        public ChangeOrganisationRequest.ChangeOrganisationRequestBuilder<R>
            approvalRejectionTimestamp(LocalDateTime approvalRejectionTimestamp) {
            this.approvalRejectionTimestamp = approvalRejectionTimestamp;
            return this;
        }

        @JsonProperty("CreatedBy")
        public  ChangeOrganisationRequest.ChangeOrganisationRequestBuilder<R> createdBy(String createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public  ChangeOrganisationRequest<R> build() {
            return new  ChangeOrganisationRequest<>(this.organisationToAdd, this.organisationToRemove, this.caseRoleId, this.reason,
                    this.notesReason, this.approvalStatus, this.requestTimestamp, this.approvalRejectionTimestamp, this.createdBy);
        }

        public String toString() {
            return "ChangeOrganisationRequest.ChangeOrganisationRequestBuilder(organisationToAdd="
                    + this.organisationToAdd + ", organisationToRemove=" + this.organisationToRemove
                    + ", caseRoleId=" + this.caseRoleId + ", reason=" + this.reason + ", notesReason="
                    + this.notesReason + ", approvalStatus=" + this.approvalStatus + ", requestTimestamp="
                    + this.requestTimestamp + ", approvalRejectionTimestamp="
                    + this.approvalRejectionTimestamp + ", createdBy=" + this.createdBy + ")";
        }
    }
}

