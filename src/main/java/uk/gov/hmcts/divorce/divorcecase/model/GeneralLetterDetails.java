package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedList;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GeneralLetterDetails {

    @CCD(label = "General letter date")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime generalLetterDateTime;

    @CCD(
        label = "Address to",
        typeOverride = FixedList,
        typeParameterOverride = "GeneralParties"
    )
    private GeneralParties generalLetterParties;

    @CCD(label = "General letter created by")
    private String generalLetterCreatedBy;

    @CCD(label = "General letter")
    private Document generalLetterLink;

    @CCD(label = "Attachments")
    private List<ListValue<Document>> generalLetterAttachmentLinks;

    @JsonIgnore
    public List<Document> getAttachedDocuments() {
        if (CollectionUtils.isEmpty(generalLetterAttachmentLinks)) {
            return Collections.emptyList();
        }

        return generalLetterAttachmentLinks.stream()
            .map(attachment -> Optional.ofNullable(attachment)
                .map(ListValue::getValue)
                .orElse(null)
            )
            .filter(Objects::nonNull)
            .toList();
    }
}
