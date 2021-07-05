package uk.gov.hmcts.divorce.bulkaction.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.divorce.common.model.ApplicationType;
import uk.gov.hmcts.divorce.common.model.access.DefaultAccess;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedList;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class BulkActionCaseData {

    @CCD(
        label = "Application type",
        access = {DefaultAccess.class},
        typeOverride = FixedList,
        typeParameterOverride = "ApplicationType"
    )
    private ApplicationType applicationType;

}
