package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.time.LocalDate;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.Email;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Bailiff {

    @CCD(label = "Court name")
    private String localCourtName;

    @CCD(
        label = "Email address",
        typeOverride = Email
    )
    private String localCourtEmail;

    @CCD(label = "Certificate of Service")
    private DivorceDocument certificateOfServiceDocument;

    @CCD(
        label = "Certificate of service date"
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate certificateOfServiceDate;

    @CCD(
        label = "Did bailiff serve successfully?"
    )
    private YesOrNo successfulServedByBailiff;

    @CCD(
        label = "Reason for failure to serve",
        typeOverride = TextArea
    )
    private String reasonFailureToServeByBailiff;
}
