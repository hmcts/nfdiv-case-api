package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;

@Data
@NoArgsConstructor
public class RetiredFields {

    @CCD(label = "Case data version")
    private int dataVersion;

    @CCD(label = "retired")
    private String exampleRetiredField;

    @CCD(label = "retiredApp1ContactDetailsConfidential")
    private ConfidentialAddress applicant1ContactDetailsConfidential;

    @CCD(label = "retiredApp2ContactDetailsConfidential")
    private ConfidentialAddress applicant2ContactDetailsConfidential;

    @CCD(label = "retiredApp1LegalProceedingsRelated")
    private Set<LegalProceedingsRelated> applicant1LegalProceedingsRelated;

    @CCD(label = "retiredApp2LegalProceedingsRelated")
    private Set<LegalProceedingsRelated> applicant2LegalProceedingsRelated;

    @CCD(label = "retiredDateConditionalOrderSubmitted")
    private LocalDateTime dateConditionalOrderSubmitted;

    @CCD(label = "retiredWhoPaysCosts")
    private WhoPaysCostOrder whoPaysCosts;

    @CCD(label = "retiredJudgeWhoPaysCosts")
    private WhoPaysCostOrder judgeWhoPaysCosts;

    @CCD(label = "retiredJudgeTypeCostsDecision")
    private CostOrderList judgeTypeCostsDecision;

    @JsonIgnore
    private static final Map<String, Consumer<Map<String, Object>>> migrations = Map.of(
        "exampleRetiredField", data -> data.put("applicant1FirstName", data.get("exampleRetiredField")),
        "applicant1ContactDetailsConfidential",
        data -> data.put(
            "applicant1KeepContactDetailsConfidential",
            transformContactDetailsConfidentialField("applicant1ContactDetailsConfidential", data)
        ),
        "applicant2ContactDetailsConfidential",
        data -> data.put(
            "applicant2KeepContactDetailsConfidential",
            transformContactDetailsConfidentialField("applicant2ContactDetailsConfidential", data)
        ),
        "applicant1FinancialOrderForRemoved", data -> { },
        "applicant2FinancialOrderForRemoved", data -> { },
        "dateConditionalOrderSubmitted", data -> data.put("coDateSubmitted", data.get("dateConditionalOrderSubmitted"))
    );

    public static Map<String, Object> migrate(Map<String, Object> data) {

        for (String key : migrations.keySet()) {
            if (data.containsKey(key) && null != data.get(key)) {
                migrations.get(key).accept(data);
                data.put(key, null);
            }
        }

        data.put("dataVersion", getVersion());

        return data;
    }

    public static int getVersion() {
        return migrations.size();
    }

    private static YesOrNo transformContactDetailsConfidentialField(String confidentialFieldName, Map<String, Object> data) {
        String confidentialFieldValue = (String) data.get(confidentialFieldName);
        return ConfidentialAddress.KEEP.getLabel().equalsIgnoreCase(confidentialFieldValue) ? YES : NO;
    }
}
