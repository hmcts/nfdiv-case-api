package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.access.CaseworkerDeleteAccess;
import uk.gov.hmcts.divorce.divorcecase.model.access.DefaultAccess;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.Collection;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedRadioList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class GeneralApplication {

    @CCD(
        label = "Choose General Application Type",
        typeOverride = FixedList,
        typeParameterOverride = "GeneralApplicationType"
    )
    private GeneralApplicationType generalApplicationType;

    @CCD(
        label = "Specify, what other application?",
        typeOverride = TextArea,
        searchable = false
    )
    private String generalApplicationOtherTypeDetails;

    @CCD(
        label = "Which party submitted the general application?",
        searchable = false
    )
    private GeneralParties generalApplicationParty;

    @CCD(
        label = "Application received date",
        searchable = false
    )
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime generalApplicationReceivedDate;

    @CCD(
        label = "General application referred on",
        searchable = false
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate generalApplicationReferredOnDate;

    @CCD(
        label = "Please provide more information about general application type",
        typeOverride = TextArea,
        searchable = false
    )
    private String generalApplicationTypeOtherComments;

    @CCD(
        label = "Choose General Application Fee Type",
        typeOverride = FixedRadioList,
        typeParameterOverride = "GeneralApplicationFee"
    )
    private GeneralApplicationFee generalApplicationFeeType;

    @CCD(
        label = "General Application Document",
        searchable = false
    )
    private DivorceDocument generalApplicationDocument;

    @CCD(
        label = "General Application Documents",
        typeOverride = Collection,
        typeParameterOverride = "DivorceDocument",
        access = {DefaultAccess.class, CaseworkerDeleteAccess.class},
        searchable = false
    )
    private List<ListValue<DivorceDocument>> generalApplicationDocuments;

    @CCD(
        label = "Additional comments about the supporting document",
        typeOverride = TextArea,
        searchable = false
    )
    private String generalApplicationDocumentComments;

    @CCD(
        label = "Were all supporting documents uploaded before submission?",
        searchable = false
    )
    private YesOrNo generalApplicationDocsUploadedPreSubmission;

    @CCD(
        label = "Was the general application submitted online?",
        searchable = false
    )
    private YesOrNo generalApplicationSubmittedOnline;

    @JsonUnwrapped(prefix = "generalApplicationFee")
    @Builder.Default
    private FeeDetails generalApplicationFee = new FeeDetails();

    @CCD(
        label = "Is this an urgent application?"
    )
    private YesOrNo generalApplicationUrgentCase;

    @CCD(
        label = "How does this qualify as an urgent application?",
        typeOverride = TextArea,
        searchable = false
    )
    private String generalApplicationUrgentCaseReason;

    @CCD(
        access = {DefaultAccess.class},
        searchable = false
    )
    private Set<ApplicantStatementOfTruth> generalApplicationStatementOfTruth;

    @CCD(
        access = {DefaultAccess.class},
        searchable = false
    )
    private Set<AuthorisationStatementOfTruth> generalApplicationSignStatementOfTruth;

    @CCD(
        label = "Your name",
        searchable = false
    )
    private String generalApplicationStatementOfTruthSolsName;

    @CCD(
        label = "Name of your firm",
        searchable = false
    )
    private String generalApplicationStatementOfTruthSolsFirm;

    @CCD(
        label = "Additional comments",
        hint = "For the attention of court staff. These comments will not form part of the application",
        typeOverride = TextArea,
        searchable = false
    )
    private String generalApplicationStatementOfTruthComments;

    @JsonIgnore
    public void recordPayment(String paymentReference, LocalDate dateOfPayment) {

        generalApplicationFee.setPaymentReference(paymentReference);
        generalApplicationFee.setHasCompletedOnlinePayment(YesOrNo.YES);
        generalApplicationFee.setDateOfPayment(dateOfPayment);
    }

    @JsonIgnore
    public void recordAlternatePayment(ServicePaymentMethod paymentMethod) {
        generalApplicationFee.setPaymentMethod(paymentMethod);
        recordPayment(null, null);
    }

    @JsonIgnore
    public String getLabel(int idx, DateTimeFormatter formatter) {
        return String.format(
            "General applications %d,%s,%s",
            idx + 1,
            generalApplicationType == null ? "" : " " + generalApplicationType.getLabel(),
            generalApplicationReceivedDate == null ? "" : " " + generalApplicationReceivedDate.format(formatter)
        );
    }

    @Getter
    @AllArgsConstructor
    public enum ApplicantStatementOfTruth implements HasLabel {

        @JsonProperty("Yes")
        CONFIRM("The applicant believes that the facts stated in this application are true.");

        private final String label;
    }

    @Getter
    @AllArgsConstructor
    public enum AuthorisationStatementOfTruth implements HasLabel {

        @JsonProperty("Yes")
        CONFIRM("I am duly authorised by the applicant to sign this statement.");

        private final String label;
    }
}
