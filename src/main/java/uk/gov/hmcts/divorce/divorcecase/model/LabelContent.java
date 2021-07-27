package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public class LabelContent {

    private String applicant2;

    private String applicant2UC;

    private String unionType;

    private String unionTypeUC;

    public void setUnionType(DivorceOrDissolution divorceOrDissolution) {
        if (divorceOrDissolution.isDivorce()) {
            unionType = "divorce";
            unionTypeUC = "Divorce";
        } else {
            unionType = "dissolution";
            unionTypeUC = "Dissolution";
        }
    }

    public void setApplicationTYpe(ApplicationType applicationType) {
        if (applicationType.isSole()) {
            applicant2 = "respondent";
            applicant2UC = "Respondent";
        } else {
            applicant2 = "applicant 2";
            applicant2UC = "Applicant 2";
        }
    }
}
