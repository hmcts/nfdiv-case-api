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

import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedRadioList;
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

    @CCD(
        label = "retiredCoWhoPaysCosts",
        typeOverride = FixedRadioList,
        typeParameterOverride = "WhoPaysCostOrder"
    )
    private WhoPaysCostOrder coWhoPaysCosts;

    @CCD(
        label = "retiredCoJudgeWhoPaysCosts",
        typeOverride = FixedRadioList,
        typeParameterOverride = "WhoPaysCostOrder"
    )
    private WhoPaysCostOrder coJudgeWhoPaysCosts;

    @CCD(
        label = "retiredCoJudgeTypeCostsDecision",
        typeOverride = FixedRadioList,
        typeParameterOverride = "CostOrderList"
    )
    private CostOrderList coJudgeTypeCostsDecision;

    @CCD(label = "Site ID for selected court")
    private String selectedDivorceCentreSiteId;

    @CCD(
        label = "retiredCoTypeCostsDecision",
        typeOverride = FixedRadioList,
        typeParameterOverride = "CostOrderList"
    )
    private CostOrderList coTypeCostsDecision;

    @CCD(label = "Do legal proceedings exist (respondent)?")
    private YesOrNo legalProceedingsExist;

    @CCD(label = "Legal proceedings details (respondent)")
    private String legalProceedingsDescription;

    @CCD(label = "Does court have jurisdiction")
    private YesOrNo doYouAgreeCourtHasJurisdiction;

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
        "dateConditionalOrderSubmitted", data -> data.put("coDateSubmitted", data.get("dateConditionalOrderSubmitted")),
        "legalProceedingsExist", data -> data.put("applicant2LegalProceedings", data.get("legalProceedingsExist")),
        "legalProceedingsDescription", data -> data.put("applicant2LegalProceedingsDetails", data.get("legalProceedingsDescription")),
        "doYouAgreeCourtHasJurisdiction", data -> data.put("jurisdictionAgree", data.get("doYouAgreeCourtHasJurisdiction"))
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
