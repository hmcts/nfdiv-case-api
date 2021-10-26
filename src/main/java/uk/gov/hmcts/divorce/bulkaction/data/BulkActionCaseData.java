package uk.gov.hmcts.divorce.bulkaction.data;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.Court;
import uk.gov.hmcts.divorce.divorcecase.model.access.CaseworkerAccess;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.Collection;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class BulkActionCaseData {

    @CCD(
        label = "Case title",
        access = {CaseworkerAccess.class}
    )
    private String caseTitle;

    @CCD(
        label = "Court name",
        access = {CaseworkerAccess.class}
    )
    private Court courtName;

    @CCD(
        label = "Date and time of hearing",
        access = {CaseworkerAccess.class}
    )
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime dateAndTimeOfHearing;

    @CCD(
        label = "Pronouncement Judge",
        access = {CaseworkerAccess.class}
    )
    private String pronouncementJudge;

    @CCD(
        label = "Has the judge pronounced?",
        access = {CaseworkerAccess.class}
    )
    private YesOrNo hasJudgePronounced;

    @CCD(
        label = "Conditional Order pronounced date",
        access = {CaseworkerAccess.class}
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate pronouncedDate;

    @CCD(
        label = "Date Final Order Eligible From",
        access = {CaseworkerAccess.class}
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateFinalOrderEligibleFrom;

    @CCD(
        label = "Case list",
        typeOverride = Collection,
        typeParameterOverride = "BulkListCaseDetails",
        access = {CaseworkerAccess.class}
    )
    private List<ListValue<BulkListCaseDetails>> bulkListCaseDetails;

    @CCD(
        label = "Cases that have successfully processed",
        typeOverride = Collection,
        typeParameterOverride = "BulkListCaseDetails",
        access = {CaseworkerAccess.class}
    )
    private List<ListValue<BulkListCaseDetails>> processedCaseDetails;

    @CCD(
        label = "Cases that have errored",
        typeOverride = Collection,
        typeParameterOverride = "BulkListCaseDetails",
        access = {CaseworkerAccess.class}
    )
    private List<ListValue<BulkListCaseDetails>> erroredCaseDetails;

}
