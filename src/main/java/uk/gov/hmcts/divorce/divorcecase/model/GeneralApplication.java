package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.access.CaseworkerDeleteAccess;
import uk.gov.hmcts.divorce.divorcecase.model.access.DefaultAccess;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

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
        label = "General application answers",
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

    @JsonIgnore
    public void recordPayment(String paymentReference, LocalDate dateOfPayment) {

        generalApplicationFee.setPaymentReference(paymentReference);
        generalApplicationFee.setHasCompletedOnlinePayment(YesOrNo.YES);
        generalApplicationFee.setDateOfPayment(dateOfPayment);
    }

    @JsonIgnore
    public String getLabel(int idx, DateTimeFormatter formatter) {
        return String.format(
            "General applications %d, %s, %s",
            idx + 1,
            generalApplicationType == null ? "" : generalApplicationType.getLabel(),
            generalApplicationReceivedDate == null ? "" : generalApplicationReceivedDate.format(formatter)
        );
    }
}
