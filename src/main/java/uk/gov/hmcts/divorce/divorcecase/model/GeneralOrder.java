package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.divorce.divorcecase.model.access.CaseworkerAccessOnlyAccess;

import java.time.LocalDate;
import java.util.Set;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.MultiSelectList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GeneralOrder {

    @CCD(
        label = "Court order date",
        access = {CaseworkerAccessOnlyAccess.class}
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate generalOrderDate;

    @CCD(
        label = "Who is the general order for?",
        hint = "Select all parties the general order should be made available to",
        access = {CaseworkerAccessOnlyAccess.class},
        typeOverride = MultiSelectList,
        typeParameterOverride = "GeneralOrderDivorceParties"
    )
    private Set<GeneralOrderDivorceParties> generalOrderDivorceParties;


    @CCD(
        label = "Recitals",
        typeOverride = TextArea,
        access = {CaseworkerAccessOnlyAccess.class}
    )
    private String generalOrderRecitals;

    @CCD(
        label = "Select Judge or Legal Advisor",
        access = {CaseworkerAccessOnlyAccess.class},
        typeOverride = FixedList,
        typeParameterOverride = "GeneralOrderJudgeOrLegalAdvisorType"
    )
    private GeneralOrderJudgeOrLegalAdvisorType generalOrderJudgeOrLegalAdvisorType;

    @CCD(
        label = "Name Of Judge or Legal Advisor",
        access = {CaseworkerAccessOnlyAccess.class}
    )
    private String generalOrderJudgeOrLegalAdvisorName;

    @CCD(
        label = "Name of Venue the Judge or Legal Advisor is Sitting",
        access = {CaseworkerAccessOnlyAccess.class}
    )
    private String generalOrderJudgeOrLegalAdvisorVenue;

    @CCD(
        label = "General order details",
        typeOverride = TextArea,
        access = {CaseworkerAccessOnlyAccess.class}
    )
    private String generalOrderDetails;

    @CCD(
        label = "General Order Draft",
        access = {CaseworkerAccessOnlyAccess.class}
    )
    private Document generalOrderDraft;
}
