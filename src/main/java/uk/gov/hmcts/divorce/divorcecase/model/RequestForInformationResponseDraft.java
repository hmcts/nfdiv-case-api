package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.access.DefaultAccess;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.util.List;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.Collection;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestForInformationResponseDraft {

    @CCD(
        label = "Write your response below if the court has asked for additional information. If the court has just asked for documents, "
            + " then you do not need to write anything unless you think it's useful information.",
        typeOverride = TextArea,
        access = {DefaultAccess.class}
    )
    private String rfiDraftResponseDetails;

    @CCD(
        label = "Upload documents",
        typeOverride = Collection,
        typeParameterOverride = "DivorceDocument"
    )
    private List<ListValue<DivorceDocument>> rfiDraftResponseDocs;

    @CCD(
        label = "Cannot upload all or some requested documents",
        access = {DefaultAccess.class}
    )
    private YesOrNo rfiDraftResponseCannotUploadDocs;
}
