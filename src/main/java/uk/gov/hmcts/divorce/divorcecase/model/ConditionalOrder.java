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
import uk.gov.hmcts.divorce.divorcecase.model.access.DefaultAccess;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.Collection;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.Date;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public class ConditionalOrder {

    @CCD(
        label = "Date Conditional Order submitted to HMCTS",
        access = {DefaultAccess.class}
    )
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime dateConditionalOrderSubmitted;

    @CCD(
        label = "Link to respondent answers",
        access = {DefaultAccess.class}
    )
    private Document respondentAnswersLink;

    @CCD(
        label = "Does the petitioner want to continue with the divorce and apply for a decree nisi?",
        access = {DefaultAccess.class}
    )
    private YesOrNo applyForDecreeNisi;

    @CCD(
        label = "Link to online petition",
        access = {DefaultAccess.class}
    )
    private Document onlinePetitionLink;

    @CCD(
        label = "Do you need to change your application or add anything?",
        hint = "If you change or add anything which means your application has to be sent to your "
                + "husband/wife again you may have to pay a £95 fee",
        access = {DefaultAccess.class}
    )
    private YesOrNo changeOrAddToApplication;

    @CCD(
        label = "Is everything stated in this divorce petition true?",
        access = {DefaultAccess.class}
    )
    private YesOrNo isEverythingInPetitionTrue;

    @CCD(
        label = "Does the applicant find it intolerable to live with the respondent?",
        access = {DefaultAccess.class}
    )
    private YesOrNo doesApplicantFindItIntolerable;

    @CCD(
        label = "Date the applicant found out about the adultery",
        typeOverride = Date,
        access = {DefaultAccess.class}
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfAdulteryKnowledge;

    @CCD(
        label = "Has the applicant lived apart from the respondent since finding out about the adultery?",
        access = {DefaultAccess.class}
    )
    private YesOrNo livedApartFrom;

    @CCD(
        label = "Additional details of the living arrangements since finding out about the adultery",
        typeOverride = TextArea,
        access = {DefaultAccess.class}
    )
    private String adulteryAdditionalDetails;

    @CCD(
        label = "Do you need to upload any other documents?",
        access = {DefaultAccess.class}
    )
    private YesOrNo addNewDocuments;

    @CCD(
        label = "Documents uploaded at DN stage",
        typeOverride = Collection,
        typeParameterOverride = "DivorceDocument",
        access = {DefaultAccess.class}
    )
    private List<ListValue<DivorceDocument>> dnDocumentsUploaded;

}
