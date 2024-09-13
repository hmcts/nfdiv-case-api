package uk.gov.hmcts.divorce.divorcecase.model;

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
        label = "Please provide more information about general application type",
        typeOverride = TextArea
    )
    private String generalApplicationTypeOtherComments;

    @CCD(
        label = "Choose General Application Fee Type",
        typeOverride = FixedRadioList,
        typeParameterOverride = "GeneralApplicationFee"
    )
    private GeneralApplicationFee generalApplicationFeeType;

    @CCD(
            label = "General Application Document"
    )
    private DivorceDocument generalApplicationDocument;

    @CCD(
            label = "General Application Documents",
            typeOverride = Collection,
            typeParameterOverride = "DivorceDocument",
            access = {DefaultAccess.class, CaseworkerDeleteAccess.class}
    )
    private List<ListValue<DivorceDocument>> generalApplicationDocuments;

    @CCD(
        label = "Additional comments about the supporting document",
        typeOverride = TextArea
    )
    private String generalApplicationDocumentComments;

    @JsonUnwrapped(prefix = "generalApplicationFee")
    @Builder.Default
    private FeeDetails generalApplicationFee = new FeeDetails();

    @CCD(
        label = "Is this an urgent application?"
    )
    private YesOrNo generalApplicationUrgentCase;

    @CCD(
        label = "How does this qualify as an urgent application?",
        typeOverride = TextArea
    )
    private String generalApplicationUrgentCaseReason;
}
