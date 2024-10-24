package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.access.DefaultAccess;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.Collection;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.Email;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestForInformationOfflineResponseDoc {
    @CCD(
            label = "Sender",
            typeOverride = FixedList,
            typeParameterOverride = "RequestForInformationResponseParties",
            access = {DefaultAccess.class}
    )
    private RequestForInformationResponseParties rfiOfflineResponseDocSender;

    @CCD(
            label = "Name",
            access = {DefaultAccess.class}
    )
    private String rfiOfflineResponseDocSenderName;

    @CCD(
        label = "Email address",
        typeOverride = Email,
        access = {DefaultAccess.class}
    )
    private String rfiOfflineResponseDocSenderEmail;

    @CCD(
        label = "Notes",
        typeOverride = TextArea,
        access = {DefaultAccess.class}
    )
    private String rfiOfflineResponseDocNotes;

    @CCD(
        label = "Document",
        typeParameterOverride = "DivorceDocument"
    )
    private DivorceDocument rfiOfflineResponseDoc;
}
