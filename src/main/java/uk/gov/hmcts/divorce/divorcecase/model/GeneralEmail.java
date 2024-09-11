package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.DynamicMultiSelectList;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.access.CaseworkerAccessOnlyAccess;
import uk.gov.hmcts.divorce.divorcecase.model.access.CaseworkerDeleteAccess;
import uk.gov.hmcts.divorce.divorcecase.model.access.DefaultAccess;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
        label = "Select general order documents to attach",
        access = {CaseworkerAccessOnlyAccess.class}
    )
    private DynamicMultiSelectList geGeneralOrderDocumentNames;

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

    @JsonIgnore
    public boolean hasBeenDelivered(List<ListValue<GeneralEmailDetails>> deliveredEmails) {
        if (CollectionUtils.isEmpty(deliveredEmails)) {
            return false;
        }

        return deliveredEmails.stream()
            .map(ListValue::getValue)
            .anyMatch(this::identicalEmailDetails);
    }

    @JsonIgnore
    private boolean identicalEmailDetails(GeneralEmailDetails deliveredEmail) {
        Set<String> deliveredEmailDocLinks = deliveredEmail.getGeneralEmailAttachmentLinks() == null
            ? Collections.emptySet()
            : deliveredEmail.getGeneralEmailAttachmentLinks().stream()
                .map(listValue -> listValue.getValue().getUrl())
                .collect(Collectors.toSet());

        return deliveredEmailDocLinks.equals(generalEmailDocLinks())
                && deliveredEmail.getGeneralEmailParties().equals(generalEmailParties)
                && deliveredEmail.getGeneralEmailBody().equals(generalEmailDetails);
    }

    @JsonIgnore
    private Set<String> generalEmailDocLinks() {
        return generalEmailAttachments == null
            ? Collections.emptySet()
            : generalEmailAttachments.stream()
            .map(listValue -> listValue.getValue().getDocumentLink().getUrl())
            .collect(Collectors.toSet());
    }
}
