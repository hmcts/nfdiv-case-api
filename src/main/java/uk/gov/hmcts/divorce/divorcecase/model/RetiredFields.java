package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.map.HashedMap;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.access.CaseworkerAccess;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableMap;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedRadioList;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.Application.ThePrayer;
import static uk.gov.hmcts.divorce.divorcecase.model.Application.ThePrayer.I_CONFIRM;
import static uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderCourt.BURY_ST_EDMUNDS;

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

    @CCD(
        label = "Service application type",
        typeOverride = FixedList,
        typeParameterOverride = "AlternativeServiceType"
    )
    private AlternativeServiceType serviceApplicationType;

    @CCD(
        label = "retiredCoCourtName"
    )
    private Court coCourtName;

    @CCD(
        label = "retiredBulkActionCourtName",
        access = {CaseworkerAccess.class}
    )
    private Court courtName;


    @CCD(label = "Retired YesNo field used for prayer")
    private YesOrNo applicant1PrayerHasBeenGiven;

    @JsonIgnore
    private static final Consumer<Map<String, Object>> DO_NOTHING = data -> {
    };

    @JsonIgnore
    private static final Map<String, Consumer<Map<String, Object>>> migrations;

    static {
        final Map<String, Consumer<Map<String, Object>>> init = new HashedMap<>();

        init.put("exampleRetiredField",
            data -> data.put("applicant1FirstName", data.get("exampleRetiredField")));
        init.put("applicant1ContactDetailsConfidential",
            data -> data.put(
                "applicant1KeepContactDetailsConfidential",
                transformContactDetailsConfidentialField("applicant1ContactDetailsConfidential", data)
            ));
        init.put("applicant2ContactDetailsConfidential",
            data -> data.put(
                "applicant2KeepContactDetailsConfidential",
                transformContactDetailsConfidentialField("applicant2ContactDetailsConfidential", data)
            ));
        init.put("applicant1FinancialOrderForRemoved", DO_NOTHING);
        init.put("applicant2FinancialOrderForRemoved", DO_NOTHING);
        init.put("dateConditionalOrderSubmitted",
            data -> data.put("coDateSubmitted", data.get("dateConditionalOrderSubmitted")));
        init.put("legalProceedingsExist",
            data -> data.put("applicant2LegalProceedings", data.get("legalProceedingsExist")));
        init.put("legalProceedingsDescription",
            data -> data.put("applicant2LegalProceedingsDetails", data.get("legalProceedingsDescription")));
        init.put("doYouAgreeCourtHasJurisdiction",
            data -> data.put("jurisdictionAgree", data.get("doYouAgreeCourtHasJurisdiction")));
        init.put("serviceApplicationType",
            data -> data.put("alternativeServiceType", data.get("serviceApplicationType")));
        init.put("coCourtName",
            data -> data.put("coCourt", BURY_ST_EDMUNDS.getCourtId()));
        init.put("courtName",
            data -> data.put("court", BURY_ST_EDMUNDS.getCourtId()));
        init.put("applicant1PrayerHasBeenGiven",
            data -> data.put("applicant1PrayerHasBeenGivenCheckbox", transformApplicant1PrayerHasBeenGivenField(data)));

        migrations = unmodifiableMap(init);
    }

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

    private static Set<ThePrayer> transformApplicant1PrayerHasBeenGivenField(Map<String, Object> data) {
        String value = (String) data.get("applicant1PrayerHasBeenGiven");
        return YES.getValue().equalsIgnoreCase(value)
            ? Set.of(I_CONFIRM)
            : emptySet();
    }
}
