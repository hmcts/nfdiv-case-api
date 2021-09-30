package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.time.LocalDateTime;
import java.util.List;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.Collection;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public class ConditionalOrder {

    @CCD(
        label = "Date Conditional Order submitted to HMCTS"
    )
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime dateSubmitted;

    @CCD(
        label = "Link to respondent answers"
    )
    private Document respondentAnswersLink;

    @CCD(
        label = "Does the petitioner want to continue with the divorce and apply for a conditional order?"
    )
    private YesOrNo applyForConditionalOrder;

    @CCD(
        label = "Link to online petition"
    )
    private Document onlinePetitionLink;

    @CCD(
        label = "Do you need to change your application or add anything?",
        hint = "If you change or add anything which means your application has to be sent to your "
                + "husband/wife again you may have to pay a £95 fee"
    )
    private YesOrNo changeOrAddToApplication;

    @CCD(
        label = "Is everything stated in this divorce petition true?"
    )
    private YesOrNo isEverythingInPetitionTrue;

    @CCD(
        label = "Do you need to upload any other documents?"
    )
    private YesOrNo addNewDocuments;

    @CCD(
        label = "Documents uploaded at DN stage",
        typeOverride = Collection,
        typeParameterOverride = "DivorceDocument"
    )
    private List<ListValue<DivorceDocument>> documentsUploaded;

}
