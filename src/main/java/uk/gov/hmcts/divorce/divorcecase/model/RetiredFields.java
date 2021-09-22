package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

import java.util.Map;
import java.util.function.Consumer;

@Data
@NoArgsConstructor
public class RetiredFields {

    @CCD(label = "Case data version")
    private int dataVersion;

    @CCD(label = "retired")
    private String exampleRetiredField;

    @JsonIgnore
    private static final Map<String, Consumer<Map<String, Object>>> migrations = Map.of(
        "exampleRetiredField", data -> data.put("applicant1FirstName", data.get("exampleRetiredField"))
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
}
