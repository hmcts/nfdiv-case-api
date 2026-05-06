package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.access.DefaultAccess;

import java.time.LocalDateTime;
import java.util.Set;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.MultiSelectList;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class Hearing {

    @CCD(
        label = "Date of hearing",
        access = DefaultAccess.class
    )
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime dateOfHearing;

    @CCD(
        label = "Venue of hearing",
        access = DefaultAccess.class
    )
    private String venueOfHearing;

    @CCD(
        label = "Hearing attendance",
        access = DefaultAccess.class,
        hint = "Choose at least one of the following",
        typeOverride = MultiSelectList,
        typeParameterOverride = "HearingAttendance"
    )
    private Set<HearingAttendance> hearingAttendance;

    @CCD(
        label = "Hearing reminder notification has been sent",
        access = DefaultAccess.class
    )
    private YesOrNo hasHearingReminderBeenSent;
}
