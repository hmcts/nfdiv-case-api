package uk.gov.hmcts.divorce.document.model;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.Document;

import java.util.Date;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

@Data
@Builder
public class DivorceDocument {

    @CCD(
        label = "Date added"
    )
    private final Date documentDateAdded;

    @CCD(
        label = "Comment"
    )
    private final String documentComment;

    @CCD(
        label = "File name"
    )
    private final String documentFileName;

    @CCD(
        label = "Type",
        typeOverride = FixedList,
        typeParameterOverride = "DocumentType"
    )
    private final DocumentType documentType;

    @CCD(
        label = "Email content",
        typeOverride = TextArea
    )
    private String documentEmailContent;

    @CCD(
        label = "Document Url"
    )
    private final Document documentLink;
}
