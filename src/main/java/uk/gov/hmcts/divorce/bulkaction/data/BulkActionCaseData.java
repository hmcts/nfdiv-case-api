package uk.gov.hmcts.divorce.bulkaction.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.access.CaseworkerAccess;

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
        label = "Case list",
        typeOverride = Collection,
        typeParameterOverride = "BulkListCaseDetails",
        access = {CaseworkerAccess.class}
    )
    private List<ListValue<BulkListCaseDetails>> bulkListCaseDetails;
}
