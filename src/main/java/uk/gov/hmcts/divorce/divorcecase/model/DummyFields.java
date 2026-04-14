package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.access.DefaultAccess;
import uk.gov.hmcts.divorce.divorcecase.model.access.SystemUpdateAndSuperUserAccess;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DummyFields {

    @CCD(
        label = "EXUI4347 - Set Date Automatically?",
        access = {DefaultAccess.class},
        searchable = false
    )
    private YesOrNo dummySetDateAutomatically;

    @CCD(
        label = "EXUI4347 - Dummy Date",
        access = {DefaultAccess.class},
        searchable = false
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dummyDate;

    @CCD(
        label = "EXUI3839 - Dummy Enum",
        access = {SystemUpdateAndSuperUserAccess.class}
    )
    private DummyEnum dummyEnumField;

    @CCD(
        label = "Null All Dummy Fields",
        access = {DefaultAccess.class},
        searchable = false
    )
    private YesOrNo nullAllDummyFields;

    @Getter
    @AllArgsConstructor
    public enum DummyEnum implements HasLabel {

        @JsonProperty("DummyEnum1")
        DUMMY_ENUM_1("Dummy Enum 1"),

        @JsonProperty("DummyEnum2")
        DUMMY_ENUM_2("Dummy Enum 2");

        private final String label;
    }

}
