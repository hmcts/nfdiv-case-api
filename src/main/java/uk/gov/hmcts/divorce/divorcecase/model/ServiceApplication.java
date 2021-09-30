package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;

import java.time.LocalDate;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ServiceApplication {

    @CCD(
        label = "Application date"
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate receivedServiceApplicationDate;

    @CCD(
        label = "Service application type",
        hint = "What type of service application has been received?",
        typeOverride = FixedList,
        typeParameterOverride = "ServiceApplicationType"
    )
    private ServiceApplicationType serviceApplicationType;

    @CCD(
        label = "Added date"
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate receivedServiceAddedDate;

    @CCD(
        label = "Service Application Granted"
    )
    private YesOrNo serviceApplicationGranted;

    @CCD(
        label = "Reason for refusal",
        typeOverride = TextArea
    )
    private String serviceApplicationRefusalReason;

    @CCD(
        label = "Application decision date"
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate serviceApplicationDecisionDate;
}
