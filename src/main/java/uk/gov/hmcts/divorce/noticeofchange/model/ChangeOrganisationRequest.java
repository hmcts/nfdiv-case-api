package uk.gov.hmcts.divorce.noticeofchange.model;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import java.time.LocalDateTime;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;
import uk.gov.hmcts.ccd.sdk.api.HasRole;
import uk.gov.hmcts.ccd.sdk.type.ChangeOrganisationApprovalStatus;

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
            pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS"
    )
    @JsonProperty("RequestTimestamp")
    private LocalDateTime requestTimestamp;
    @JsonProperty("CreatedBy")
    private String createdBy;

    @JsonCreator
    public ChangeOrganisationRequest(@JsonProperty("OrganisationToAdd") Organisation organisationToAdd, @JsonProperty("OrganisationToRemove") Organisation organisationToRemove, @JsonProperty("CaseRoleId") R caseRoleId, @JsonProperty("Reason") String reason, @JsonProperty("NotesReason") String notesReason, @JsonProperty("ApprovalStatus") ChangeOrganisationApprovalStatus approvalStatus, @JsonProperty("RequestTimestamp") LocalDateTime requestTimestamp, @JsonProperty("ApprovalRejectionTimestamp") LocalDateTime approvalRejectionTimestamp, @JsonProperty("CreatedBy") String createdBy) {
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

    public Organisation getOrganisationToAdd() {
        return this.organisationToAdd;
    }

    public Organisation getOrganisationToRemove() {
        return this.organisationToRemove;
    }

    public R getCaseRoleId() {
        return this.caseRoleId;
    }

    public ChangeOrganisationApprovalStatus getApprovalStatus() {
        return this.approvalStatus;
    }

    public LocalDateTime getRequestTimestamp() {
        return this.requestTimestamp;
    }

    public String getCreatedBy() {
        return this.createdBy;
    }

    @JsonProperty("OrganisationToAdd")
    public void setOrganisationToAdd(Organisation organisationToAdd) {
        this.organisationToAdd = organisationToAdd;
    }

    @JsonProperty("OrganisationToRemove")
    public void setOrganisationToRemove(Organisation organisationToRemove) {
        this.organisationToRemove = organisationToRemove;
    }

    @JsonProperty("CaseRoleId")
    public void setCaseRoleId(R caseRoleId) {
        this.caseRoleId = caseRoleId;
    }

    @JsonProperty("ApprovalStatus")
    public void setApprovalStatus(ChangeOrganisationApprovalStatus approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    @JsonProperty("RequestTimestamp")
    public void setRequestTimestamp(LocalDateTime requestTimestamp) {
        this.requestTimestamp = requestTimestamp;
    }

    @JsonProperty("CreatedBy")
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof  ChangeOrganisationRequest)) {
            return false;
        } else {
             ChangeOrganisationRequest<?> other = ( ChangeOrganisationRequest)o;
            if (!other.canEqual(this)) {
                return false;
            } else {
                label119: {
                    Object this$organisationToAdd = this.getOrganisationToAdd();
                    Object other$organisationToAdd = other.getOrganisationToAdd();
                    if (this$organisationToAdd == null) {
                        if (other$organisationToAdd == null) {
                            break label119;
                        }
                    } else if (this$organisationToAdd.equals(other$organisationToAdd)) {
                        break label119;
                    }

                    return false;
                }

                Object this$organisationToRemove = this.getOrganisationToRemove();
                Object other$organisationToRemove = other.getOrganisationToRemove();
                if (this$organisationToRemove == null) {
                    if (other$organisationToRemove != null) {
                        return false;
                    }
                } else if (!this$organisationToRemove.equals(other$organisationToRemove)) {
                    return false;
                }

                label105: {
                    Object this$caseRoleId = this.getCaseRoleId();
                    Object other$caseRoleId = other.getCaseRoleId();
                    if (this$caseRoleId == null) {
                        if (other$caseRoleId == null) {
                            break label105;
                        }
                    } else if (this$caseRoleId.equals(other$caseRoleId)) {
                        break label105;
                    }

                    return false;
                }

                Object this$approvalStatus = this.getApprovalStatus();
                Object other$approvalStatus = other.getApprovalStatus();
                if (this$approvalStatus == null) {
                    if (other$approvalStatus != null) {
                        return false;
                    }
                } else if (!this$approvalStatus.equals(other$approvalStatus)) {
                    return false;
                }

                label77: {
                    Object this$requestTimestamp = this.getRequestTimestamp();
                    Object other$requestTimestamp = other.getRequestTimestamp();
                    if (this$requestTimestamp == null) {
                        if (other$requestTimestamp == null) {
                            break label77;
                        }
                    } else if (this$requestTimestamp.equals(other$requestTimestamp)) {
                        break label77;
                    }

                    return false;
                }

                Object this$createdBy = this.getCreatedBy();
                Object other$createdBy = other.getCreatedBy();
                if (this$createdBy == null) {
                    if (other$createdBy != null) {
                        return false;
                    }
                } else if (!this$createdBy.equals(other$createdBy)) {
                    return false;
                }

                return true;
            }
        }
    }

    protected boolean canEqual(Object other) {
        return other instanceof  ChangeOrganisationRequest;
    }

    public int hashCode() {
        int PRIME = 1;
        int result = 1;
        Object $organisationToAdd = this.getOrganisationToAdd();
        result = result * 59 + ($organisationToAdd == null ? 43 : $organisationToAdd.hashCode());
        Object $organisationToRemove = this.getOrganisationToRemove();
        result = result * 59 + ($organisationToRemove == null ? 43 : $organisationToRemove.hashCode());
        Object $caseRoleId = this.getCaseRoleId();
        result = result * 59 + ($caseRoleId == null ? 43 : $caseRoleId.hashCode());
        Object $approvalStatus = this.getApprovalStatus();
        result = result * 59 + ($approvalStatus == null ? 43 : $approvalStatus.hashCode());
        Object $requestTimestamp = this.getRequestTimestamp();
        result = result * 59 + ($requestTimestamp == null ? 43 : $requestTimestamp.hashCode());
        Object $createdBy = this.getCreatedBy();
        result = result * 59 + ($createdBy == null ? 43 : $createdBy.hashCode());
        return result;
    }

    public String toString() {
        Organisation var10000 = this.getOrganisationToAdd();
        return "ChangeOrganisationRequest(organisationToAdd=" + var10000 + ", organisationToRemove=" + this.getOrganisationToRemove() + ", caseRoleId=" + this.getCaseRoleId()  + ", approvalStatus=" + this.getApprovalStatus() + ", requestTimestamp=" + this.getRequestTimestamp()  + ", createdBy=" + this.getCreatedBy() + ")";
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
        public  ChangeOrganisationRequest.ChangeOrganisationRequestBuilder<R> approvalStatus(ChangeOrganisationApprovalStatus approvalStatus) {
            this.approvalStatus = approvalStatus;
            return this;
        }

        @JsonProperty("RequestTimestamp")
        public  ChangeOrganisationRequest.ChangeOrganisationRequestBuilder<R> requestTimestamp(LocalDateTime requestTimestamp) {
            this.requestTimestamp = requestTimestamp;
            return this;
        }

        @JsonProperty("ApprovalRejectionTimestamp")
        public  ChangeOrganisationRequest.ChangeOrganisationRequestBuilder<R> approvalRejectionTimestamp(LocalDateTime approvalRejectionTimestamp) {
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
            return "ChangeOrganisationRequest.ChangeOrganisationRequestBuilder(organisationToAdd=" + this.organisationToAdd + ", organisationToRemove=" + this.organisationToRemove + ", caseRoleId=" + this.caseRoleId + ", reason=" + this.reason + ", notesReason=" + this.notesReason + ", approvalStatus=" + this.approvalStatus + ", requestTimestamp=" + this.requestTimestamp + ", approvalRejectionTimestamp=" + this.approvalRejectionTimestamp + ", createdBy=" + this.createdBy + ")";
        }
    }
}

