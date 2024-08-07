package uk.gov.hmcts.divorce.noticeofchange.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@AllArgsConstructor
public class ChangeOfRepresentative {

    @CCD(label = "Party")
    private String party;

    @CCD(label = "Client name")
    private String clientName;

    @CCD(label = "Date")
    private final String addedDateTime;

    @CCD(label = "Updated by")
    private String updatedBy;

    @CCD(label = "Updated via")
    private String updatedVia;

    @CCD(label = "Added representative")
    private final Representative addedRepresentative;

    @CCD(label = "Removed representative")
    private final Representative removedRepresentative;
}
