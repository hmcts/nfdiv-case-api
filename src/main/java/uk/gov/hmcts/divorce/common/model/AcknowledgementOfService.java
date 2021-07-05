package uk.gov.hmcts.divorce.common.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.model.access.DefaultAccess;

import java.time.LocalDateTime;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AcknowledgementOfService {

    @CCD(
        label = "Has the respondent read the application for divorce?",
        access = AosAccess.class
    )
    private YesOrNo confirmReadPetition;

    @CCD(
        label = "Respondent agreed to claimed jurisdiction?",
        access = AosAccess.class
    )
    private YesOrNo jurisdictionAgree;

    @CCD(
        label = "Reason respondent disagreed to claimed jurisdiction",
        typeOverride = TextArea,
        access = AosAccess.class
    )
    private String jurisdictionDisagreeReason;

    @CCD(
        label = "Do legal proceedings exist (respondent)?",
        access = AosAccess.class
    )
    private YesOrNo legalProceedingsExist;

    @CCD(
        label = "Legal proceedings details (respondent)",
        typeOverride = TextArea,
        access = AosAccess.class
    )
    private String legalProceedingsDescription;

    @CCD(
        label = "Does respondent agree to costs?",
        typeOverride = FixedList,
        typeParameterOverride = "RespAgreeToCostsEnum",
        access = AosAccess.class
    )
    private RespAgreeToCostsEnum agreeToCosts;

    @CCD(
        label = "Respondent's costs amount",
        access = AosAccess.class
    )
    private String costsAmount;

    @CCD(
        label = "Respondent's costs reason",
        typeOverride = TextArea,
        access = AosAccess.class
    )
    private String costsReason;


    @CCD(
        label = "Date AOS submitted to HMCTS",
        access = {DefaultAccess.class}
    )
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime dateAosSubmitted;

}
