package uk.gov.hmcts.divorce.bulkaction.data;

import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderCourt.BURY_ST_EDMUNDS;

public class BulkCaseRetiredFieldsTest {

    @Test
    void migrateShouldMigrateSomeFieldsAndLeaveOthersAlone() {
        final var data = new HashMap<String, Object>();
        data.put("courtName", "serviceCentre");

        final var result = BulkCaseRetiredFields.migrate(data);

        assertThat(result).contains(
            entry("bulkCaseDataVersion", 1),
            entry("courtName", null),
            entry("court", BURY_ST_EDMUNDS.getCourtId())
        );
    }

    @Test
    void shouldIgnoreFieldIfPresentAndSetToNullOrEmpty() {
        final var data = new HashMap<String, Object>();
        data.put("courtName", null);

        final var result = BulkCaseRetiredFields.migrate(data);

        assertThat(result.get("courtName")).isNull();
        assertThat(result.get("court")).isNull();
    }
}
