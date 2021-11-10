package uk.gov.hmcts.divorce.divorcecase.model;

import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderCourt.BURY_ST_EDMUNDS;

class RetiredFieldsTest {

    @Test
    void migrateShouldMigrateSomeFieldsAndLeaveOthersAlone() {
        var data = new HashMap<String, Object>();
        data.put("applicant1FirstName", "This will be overwritten");
        data.put("exampleRetiredField", "This will be nulled");
        data.put("applicant1LastName", "This will be left alone");
        data.put("applicant1ContactDetailsConfidential", "keep");
        data.put("applicant2ContactDetailsConfidential", "share");
        data.put("coCourtName", "serviceCentre");
        data.put("courtName", "serviceCentre");

        var result = RetiredFields.migrate(data);

        assertThat(result.get("applicant1FirstName")).isEqualTo("This will be nulled");
        assertThat(result.get("exampleRetiredField")).isNull();
        assertThat(result.get("applicant1ContactDetailsConfidential")).isNull();
        assertThat(result.get("applicant2ContactDetailsConfidential")).isNull();
        assertThat(result.get("applicant1LastName")).isEqualTo("This will be left alone");
        assertThat(result.get("coCourtName")).isNull();
        assertThat(result.get("courtName")).isNull();
        assertThat(result.get("coCourt")).isEqualTo(BURY_ST_EDMUNDS.getCourtId());
        assertThat(result.get("court")).isEqualTo(BURY_ST_EDMUNDS.getCourtId());
    }
}
