package uk.gov.hmcts.divorce.common.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.divorce.common.model.access.DefaultAccess;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AcknowledgementOfService {

    @CCD(
        label = "Date AOS submitted to HMCTS",
        access = {DefaultAccess.class}
    )
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime dateAosSubmitted;

}
