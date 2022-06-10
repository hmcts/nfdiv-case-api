package uk.gov.hmcts.divorce.divorcecase.model;

import uk.gov.hmcts.ccd.sdk.api.CCD;

import java.time.LocalDate;

@CCD
public class DateAndHour {
    private LocalDate date;
    private String hour;
}
