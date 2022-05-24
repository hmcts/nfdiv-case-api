package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.elasticsearch.common.TriConsumer;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;
import uk.gov.hmcts.ccd.sdk.type.CaseLink;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static uk.gov.hmcts.divorce.divorcecase.model.CivilPartnershipBroken.CIVIL_PARTNERSHIP_BROKEN;
import static uk.gov.hmcts.divorce.divorcecase.model.MarriageBroken.MARRIAGE_BROKEN;

@Data
@NoArgsConstructor
public class RetiredFields {

    @CCD(label = "Case data version")
    private int dataVersion;

    private String exampleRetiredField;
    private CaseLink previousCaseId;
    private String paperFormServiceOutsideUK;

    @Getter
    @AllArgsConstructor
    public enum ThePrayer implements HasLabel {

        @JsonProperty("Yes")
        I_CONFIRM("Yes");


        private final String label;
    }

    private Set<ThePrayer> applicant1PrayerHasBeenGivenCheckbox;
    private Set<ThePrayer> applicant2PrayerHasBeenGivenCheckbox;
    private ServiceMethod solServiceMethod;
    private YesOrNo applicant1ScreenHasMarriageBroken;
    private DivorceDocument d11Document;

    @JsonIgnore
    private static final TriConsumer<Map<String, Object>, String, Object> DO_NOTHING = (data, key, val) -> {
    };

    @JsonIgnore
    private static final Map<String, TriConsumer<Map<String, Object>, String, Object>> migrations = Map.of(
        "exampleRetiredField", moveTo("applicant1FirstName"),
        "solServiceMethod", moveTo("serviceMethod"),
        "applicant1ScreenHasMarriageBroken", migrateMarriageBroken(),
        "d11Document", (data, key, val) -> data.put("answerReceivedSupportingDocuments",
            List.of(ListValue
                .<DivorceDocument>builder()
                .id("1")
                .value((DivorceDocument) val)
                .build()
            )
        ),
        "generalApplicationFeeAccountNumber", (data, key, val) -> data.put("generalApplicationFeePbaNumbers",
            DynamicList
                .builder()
                .value(DynamicListElement.builder().label(String.valueOf(val)).build())
                .listItems(List.of(DynamicListElement.builder().label(String.valueOf(val)).build()))
                .build()
        )
    );

    /**
     * This function will iterate over the properties in the given map and check for a migration. If one is found
     * it will be executed and the map will be mutated by the migration function (usually to migrate data from
     * the old field to the new one).
     * <br>
     * Note that the migrated field will be nulled after the migration has run. Therefore, it is not possible to
     * use a migration to modify data in a field, only modify it while moving it to another.
     */
    public static Map<String, Object> migrate(Map<String, Object> data) {

        for (String key : migrations.keySet()) {
            if (data.containsKey(key) && null != data.get(key)) {
                migrations.get(key).apply(data, key, data.get(key));
                data.put(key, null);
            }
        }

        data.put("dataVersion", getVersion());

        return data;
    }

    public static int getVersion() {
        return migrations.size();
    }

    private static TriConsumer<Map<String, Object>, String, Object> moveTo(String newFieldName) {
        return (data, key, val) -> data.put(newFieldName, val);
    }

    private static TriConsumer<Map<String, Object>, String, Object> migrateMarriageBroken() {

        return (data, key, val) -> {

            YesOrNo applicant1ScreenHasMarriageBroken = (YesOrNo)data.get("applicant1ScreenHasMarriageBroken");

            if (applicant1ScreenHasMarriageBroken.toBoolean()) {
                DivorceOrDissolution divorceOrDissolution = (DivorceOrDissolution) data.get("divorceOrDissolution");
                if (divorceOrDissolution.isDivorce()) {
                    data.put("applicant1HasMarriageBroken", MARRIAGE_BROKEN);
                } else {
                    data.put("applicant1HasCivilPartnershipBroken", CIVIL_PARTNERSHIP_BROKEN);
                }
            }
        };
    }
}
