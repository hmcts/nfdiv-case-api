package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.divorce.divorcecase.model.access.CaseworkerAccessBetaOnlyAccess;

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
        access = {CaseworkerAccessBetaOnlyAccess.class}
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate generalOrderDate;

    @CCD(
        label = "Who is the general order for?",
        hint = "Select all parties the general order should be made available to",
        access = {CaseworkerAccessBetaOnlyAccess.class},
        typeOverride = MultiSelectList,
        typeParameterOverride = "GeneralOrderDivorceParties"
    )
    private Set<GeneralOrderDivorceParties> generalOrderDivorceParties;


    @CCD(
        label = "Recitals",
        typeOverride = TextArea,
        access = {CaseworkerAccessBetaOnlyAccess.class}
    )
    private String generalOrderRecitals;

    @CCD(
        label = "Select Judge",
        access = {CaseworkerAccessBetaOnlyAccess.class},
        typeOverride = FixedList,
        typeParameterOverride = "GeneralOrderJudge"
    )
    private GeneralOrderJudge generalOrderJudgeType;

    @CCD(
        label = "Name of Judge",
        hint = "Surname of Judge",
        access = {CaseworkerAccessBetaOnlyAccess.class}
    )
    private String generalOrderJudgeName;

    @CCD(
        label = "General order details",
        typeOverride = TextArea,
        hint = "Surname of Judge",
        access = {CaseworkerAccessBetaOnlyAccess.class}
    )
    private String generalOrderDetails;

    @CCD(
        label = "General Order Draft",
        access = {CaseworkerAccessBetaOnlyAccess.class}
    )
    private Document generalOrderDraft;
}
