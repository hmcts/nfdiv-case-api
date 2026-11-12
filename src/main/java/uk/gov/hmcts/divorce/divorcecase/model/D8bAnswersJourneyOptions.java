package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.access.RespondentSolicitorAccess;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public class D8bAnswersJourneyOptions implements ApplicationAnswers {

    @CCD(
        label = "Respondent uploaded documents",
        access = {RespondentSolicitorAccess.class},
        searchable = false
    )
    private List<ListValue<DivorceDocument>> solD8bDocuments;
}
