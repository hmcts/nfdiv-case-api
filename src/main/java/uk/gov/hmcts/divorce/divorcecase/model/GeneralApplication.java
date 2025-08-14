package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonFormat;
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
        label = "Application date",
        searchable = false
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate receivedGeneralApplicationDate;

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
        label = "All documents uploaded before submission?",
        searchable = false
    )
    private YesOrNo generalApplicationDocsUploadedPreSubmission;

    @CCD(
        label = "Was the general application submitted digitally?",
        searchable = false
    )
    private YesOrNo generalApplicationSubmittedOnline;

    @CCD(
        label = "Which party submitted the general application?",
        searchable = false
    )
    private GeneralParties generalParties;

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
        label = "Has the citizen or their representative completed online payment for the application?",
        searchable = false
    )
    private YesOrNo hasCompletedOnlinePayment;
}
