package uk.gov.hmcts.divorce.bulkaction.data;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.*;
import uk.gov.hmcts.divorce.divorcecase.model.Court;
import uk.gov.hmcts.divorce.divorcecase.model.access.CaseworkerAccess;
import uk.gov.hmcts.divorce.divorcecase.model.access.DefaultAccess;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.Collection;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class BulkActionCaseData {

    @JsonIgnore
    private static final int FINAL_ORDER_OFFSET_WEEKS = 6;

    @JsonIgnore
    private static final int FINAL_ORDER_OFFSET_DAYS = 1;

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

    @CCD(
        label = "Case references",
        access = {DefaultAccess.class}
    )
    private DynamicList caseReferences;

    @JsonIgnore
    public LocalDate getDateFinalOrderEligibleFrom(LocalDateTime dateTime) {
        return dateTime.toLocalDate().plusWeeks(FINAL_ORDER_OFFSET_WEEKS).plusDays(FINAL_ORDER_OFFSET_DAYS);
    }

    @JsonIgnore
    public List<ListValue<BulkListCaseDetails>> calculateProcessedCases(final List<ListValue<BulkListCaseDetails>> unprocessedBulkCases) {
        if (isEmpty(unprocessedBulkCases)) {
            return bulkListCaseDetails;
        }

        List<String> unprocessedCaseIds = unprocessedBulkCases
            .stream()
            .map(lv -> lv.getValue().getCaseReference().getCaseReference())
            .collect(toList());

        return bulkListCaseDetails
            .stream()
            .filter(lv -> !unprocessedCaseIds.contains(lv.getValue().getCaseReference().getCaseReference()))
            .collect(toList());
    }

    @JsonIgnore
    public DynamicList transformBulkCasesToDynamicList() {
        List<DynamicListElement> caseReferences = bulkListCaseDetails.stream()
            .map(c -> DynamicListElement.builder()
                .code(UUID.randomUUID())
                .label(c.getValue().getCaseReference().getCaseReference())
                .build()
            )
            .collect(Collectors.toList());

        return DynamicList.builder()
            .value(DynamicListElement.builder().build())
            .listItems(caseReferences)
            .build();
    }
}
