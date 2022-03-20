package uk.gov.hmcts.divorce.document.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.Document;

import java.time.LocalDate;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

@Data
@NoArgsConstructor
@Builder
public class ConfidentialDivorceDocument {

    @CCD(
        label = "Type",
        typeOverride = FixedList,
        typeParameterOverride = "ConfidentialDocumentsReceived"
    )
    private ConfidentialDocumentsReceived confidentialDocumentsReceived;

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

    @CCD(
        label = "Date added"
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate documentDateAdded;

    @CCD(
        label = "Comment"
    )
    private String documentComment;

    @CCD(
        label = "File name"
    )
    private String documentFileName;

    //Add handwritten constructor as a workaround for @JsonUnwrapped prefix issue
    @JsonCreator
    public ConfidentialDivorceDocument(
        @JsonProperty("confidentialDocumentsReceived") ConfidentialDocumentsReceived confidentialDocumentsReceived,
        @JsonProperty("documentEmailContent") String documentEmailContent,
        @JsonProperty("documentLink") Document documentLink,
        @JsonProperty("documentDateAdded") LocalDate documentDateAdded,
        @JsonProperty("documentComment") String documentComment,
        @JsonProperty("documentFileName") String documentFileName) {

        this.confidentialDocumentsReceived = confidentialDocumentsReceived;
        this.documentEmailContent = documentEmailContent;
        this.documentLink = documentLink;
        this.documentDateAdded = documentDateAdded;
        this.documentComment = documentComment;
        this.documentFileName = documentFileName;
    }
}
