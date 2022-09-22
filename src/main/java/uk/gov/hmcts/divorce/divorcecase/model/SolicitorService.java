package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.access.DefaultAccess;

import java.time.LocalDate;
import java.util.Set;

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
        typeOverride = Date
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfService;

    @CCD(
        label = "What Documents did you serve?",
        typeOverride = TextArea
    )
    private String documentsServed;

    @CCD(
        label = "On whom did you serve?",
        hint = "(if appropriate include their position e.g. partner, director)"
    )
    private String onWhomServed;

    @CCD(
        label = "How did you serve the documents?",
        typeOverride = FixedList,
        typeParameterOverride = "DocumentsServedHow"
    )
    private DocumentsServedHow howServed;

    @CCD(label = "Details of Service")
    private String serviceDetails;

    @CCD(
        hint = "Note: if you tick this box, you will need to ensure you upload proof of service before submitting",
        access = {DefaultAccess.class}
    )
    private Set<ServiceProcessedByProcessServer> serviceProcessedByProcessServer;

    @Getter
    @AllArgsConstructor
    public enum ServiceProcessedByProcessServer implements HasLabel {

        @JsonProperty("serviceProcessed")
        CONFIRM("I confirm that this was processed by a process server");

        private final String label;
    }

    @CCD(
        label = "Give the address where you served the documents?",
        hint = "Include the fax or DX number, e-mail address or other electronic identification",
        typeOverride = TextArea
    )
    private String addressServed;

    @CCD(
        label = "Who served?",
        typeOverride = FixedRadioList,
        typeParameterOverride = "DocumentsServedBeingThe"
    )
    private DocumentsServedBeingThe beingThe;

    @CCD(
        label = "Location",
        typeOverride = FixedList,
        typeParameterOverride = "DocumentsServedWhere"
    )
    private DocumentsServedWhere locationServed;

    @CCD(label = "Specify Location")
    private String specifyLocationServed;

    @CCD(label = "Solicitor’s Name")
    private String serviceSotName;

    @CCD(label = "Solicitor’s Firm")
    private String serviceSotFirm;

    @CCD(label = "I believe that the facts stated in the application are true.")
    private YesOrNo statementOfTruth;

    @CCD(label = " ")
    private String truthStatement;

    public String getTruthStatement() {
        return "I believe that the facts stated in the application are true.";
    }
}
