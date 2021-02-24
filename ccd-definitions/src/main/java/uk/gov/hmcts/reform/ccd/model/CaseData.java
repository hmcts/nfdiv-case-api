package uk.gov.hmcts.reform.ccd.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.gov.hmcts.ccd.sdk.types.CCD;

import static uk.gov.hmcts.ccd.sdk.types.FieldType.Date;
import static uk.gov.hmcts.ccd.sdk.types.FieldType.FixedList;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@EqualsAndHashCode()
public class CaseData {

    @JsonProperty("D8legalProcess")
    @CCD(
        label = "Type",
        hint = "Legal process (divorce, dissolution, judicial separation)",
        type = FixedList,
        typeParameter = "legalProcessEnum")
    private String d8legalProcess;

    @JsonProperty("D8caseReference")
    @CCD(
        label = "FamilyMan reference",
        hint = "FamilyMan case reference")
    private String d8caseReference;

    @JsonProperty("createdDate")
    @CCD(
        label = "Created date",
        hint = "Date case was created",
        type = Date)
    private String createdDate;
}
