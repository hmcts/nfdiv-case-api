package uk.gov.hmcts.divorce.divorcecase.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.DynamicMultiSelectList;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.access.CaseworkerAccessOnlyAccess;
import uk.gov.hmcts.divorce.divorcecase.model.access.CaseworkerDeleteAccess;
import uk.gov.hmcts.divorce.divorcecase.model.access.DefaultAccess;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.util.List;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.Collection;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.Email;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GeneralEmail {

    @CCD(
        label = "Address to",
        typeOverride = FixedList,
        typeParameterOverride = "GeneralParties"
    )
    private GeneralParties generalEmailParties;

    @CCD(
        label = "Recipient's email",
        typeOverride = Email
    )
    private String generalEmailOtherRecipientEmail;

    @CCD(
        label = "Recipient's name"
    )
    private String generalEmailOtherRecipientName;

    @CCD(
        label = "Please provide details",
        typeOverride = TextArea
    )
    private String generalEmailDetails;

    @CCD(
        label = "Select uploaded documents to attach",
        access = {CaseworkerAccessOnlyAccess.class}
    )
    private DynamicMultiSelectList geUploadedDocumentNames;

    @CCD(
        label = "Select generated documents to attach",
        access = {CaseworkerAccessOnlyAccess.class}
    )
    private DynamicMultiSelectList geGeneratedDocumentNames;

    @CCD(
        label = "Select scanned documents to attach",
        access = {CaseworkerAccessOnlyAccess.class}
    )
    private DynamicMultiSelectList geScannedDocumentNames;

    @CCD(
        label = "Select applicant 1 documents to attach",
        access = {CaseworkerAccessOnlyAccess.class}
    )
    private DynamicMultiSelectList geApplicant1DocumentNames;

    @CCD(
        label = "Select applicant 2 documents to attach",
        access = {CaseworkerAccessOnlyAccess.class}
    )
    private DynamicMultiSelectList geApplicant2DocumentNames;

    @CCD(
        label = "Add attachments",
        typeOverride = Collection,
        typeParameterOverride = "DivorceDocument",
        access = {DefaultAccess.class, CaseworkerDeleteAccess.class}
    )
    private List<ListValue<DivorceDocument>> generalEmailAttachments;

    @CCD(
        label = "Attached documents",
        access = {CaseworkerAccessOnlyAccess.class}
    )
    private DynamicMultiSelectList geAttachedDocumentNames;
}
