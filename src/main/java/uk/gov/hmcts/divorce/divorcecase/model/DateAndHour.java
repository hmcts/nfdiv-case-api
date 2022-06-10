package uk.gov.hmcts.divorce.divorcecase.model;

import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;

import java.time.LocalDate;

@CCD
@Data
public class DateAndHour {
    @CCD(label = "date")
    private LocalDate date;
    @CCD(label = "hour")
    private String hour;
}
