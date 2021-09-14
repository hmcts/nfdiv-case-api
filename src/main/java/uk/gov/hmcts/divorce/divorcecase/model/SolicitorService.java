package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.divorce.divorcecase.model.access.DefaultAccess;

import java.time.LocalDate;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.Date;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedRadioList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public class SolicitorService {

    @CCD(
        label = "Date of Service",
        typeOverride = Date,
        access = {DefaultAccess.class}
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfService;

    @CCD(
        label = "What Documents did you serve?",
        typeOverride = TextArea,
        access = {DefaultAccess.class}
    )
    private String documentsServed;

    @CCD(
        label = "On whom did you serve?",
        hint = "(if appropriate include their position e.g. partner, director)",
        access = {DefaultAccess.class}
    )
    private String onWhomServed;

    @CCD(
        label = "How did you serve the documents?",
        typeOverride = FixedList,
        typeParameterOverride = "DocumentsServedHow",
        access = {DefaultAccess.class}
    )
    private DocumentsServedHow howServed;

    @CCD(
        label = "Give the address where you served the documents?",
        hint = "Include the fax or DX number, e-mail address or other electronic identification",
        typeOverride = TextArea,
        access = {DefaultAccess.class}
    )
    private String addressServed;

    @CCD(
        label = "Being the",
        typeOverride = FixedRadioList,
        typeParameterOverride = "DocumentsServedBeingThe",
        access = {DefaultAccess.class}
    )
    private DocumentsServedBeingThe beingThe;

    @CCD(
        label = "Location",
        typeOverride = FixedList,
        typeParameterOverride = "DocumentsServedWhere",
        access = {DefaultAccess.class}
    )
    private DocumentsServedWhere locationServed;

    @CCD(
        label = "Specify Location",
        access = {DefaultAccess.class}
    )
    private String specifyLocationServed;

    @CCD(
        label = "Solicitor’s Name",
        access = {DefaultAccess.class}
    )
    private String serviceSotName;

    @CCD(
        label = "Solicitor’s Firm",
        access = {DefaultAccess.class}
    )
    private String serviceSotFirm;

}
