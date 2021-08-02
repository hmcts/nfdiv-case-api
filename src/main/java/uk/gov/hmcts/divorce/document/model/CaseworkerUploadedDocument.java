package uk.gov.hmcts.divorce.document.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.Document;

import java.util.Date;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CaseworkerUploadedDocument {

    @CCD(
        label = "Date added"
    )
    private Date documentDateAdded;

    @CCD(
        label = "Comment"
    )
    private String documentComment;

    @CCD(
        label = "File name"
    )
    private String documentFileName;

    @CCD(
        label = "Type",
        typeOverride = FixedList,
        typeParameterOverride = "CaseworkerUploadedDocumentType"
    )
    private CaseworkerUploadedDocumentType documentType;

    @CCD(
        label = "Email content",
        typeOverride = TextArea
    )
    private String documentEmailContent;

    @CCD(
        label = "Document Url",
        regex = ".pdf,.tif,.tiff,.jpg,.jpeg,.png"
    )
    private Document documentLink;
}
