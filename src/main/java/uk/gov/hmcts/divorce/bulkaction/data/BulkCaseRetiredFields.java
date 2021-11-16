package uk.gov.hmcts.divorce.bulkaction.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.map.HashedMap;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.divorce.divorcecase.model.Court;
import uk.gov.hmcts.divorce.divorcecase.model.access.CaseworkerAccess;

import java.util.Map;
import java.util.function.Consumer;

import static java.util.Collections.unmodifiableMap;
import static uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderCourt.BURY_ST_EDMUNDS;

@Data
@NoArgsConstructor
public class BulkCaseRetiredFields {

    @CCD(label = "Bulk Case data version")
    private int bulkCaseDataVersion;

    @CCD(
        label = "retiredBulkActionCourtName",
        access = {CaseworkerAccess.class}
    )
    private Court courtName;

    @JsonIgnore
    private static final Map<String, Consumer<Map<String, Object>>> migrations;

    static {
        final Map<String, Consumer<Map<String, Object>>> init = new HashedMap<>();

        init.put("courtName",
            data -> data.put("court", BURY_ST_EDMUNDS.getCourtId()));

        migrations = unmodifiableMap(init);
    }

    public static Map<String, Object> migrate(Map<String, Object> data) {

        for (String key : migrations.keySet()) {
            if (data.containsKey(key) && null != data.get(key)) {
                migrations.get(key).accept(data);
                data.put(key, null);
            }
        }

        data.put("bulkCaseDataVersion", getVersion());

        return data;
    }

    public static int getVersion() {
        return migrations.size();
    }
}
