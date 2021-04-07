package uk.gov.hmcts.reform.divorce.ccd.model;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.reform.divorce.ccd.access.DefaultAccess;
import uk.gov.hmcts.reform.divorce.ccd.model.enums.DocumentType;

import java.util.Date;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

@Data
@ComplexType(name = "DivorceDocument")
@Builder
public class DivorceDocument {

    @CCD(
        label = "Date added",
        access = { DefaultAccess.class }
    )
    private final Date documentDateAdded;

    @CCD(
        label = "DocumentComment",
        access = { DefaultAccess.class }
    )
    private final String documentComment;

    @CCD(
        label = "File name",
        access = { DefaultAccess.class }
    )
    private final String documentFileName;

    @CCD(
        label = "Type",
        typeOverride = FixedList,
        typeParameterOverride = "DocumentType",
        access = { DefaultAccess.class }
    )
    private final DocumentType documentType;

    @CCD(
        label = "Email content",
        typeOverride = TextArea,
        access = { DefaultAccess.class }
    )
    private String documentEmailContent;

    @CCD(
        label = "Document Url",
        access = { DefaultAccess.class }
    )
    private final Document documentLink;
}
