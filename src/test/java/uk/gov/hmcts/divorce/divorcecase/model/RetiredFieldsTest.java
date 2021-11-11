package uk.gov.hmcts.divorce.divorcecase.model;

import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderCourt.BURY_ST_EDMUNDS;

class RetiredFieldsTest {

    @Test
    void migrateShouldMigrateSomeFieldsAndLeaveOthersAlone() {
        final var data = new HashMap<String, Object>();
        data.put("applicant1FirstName", "This will be overwritten");
        data.put("exampleRetiredField", "This will be nulled");
        data.put("applicant1LastName", "This will be left alone");
        data.put("applicant1ContactDetailsConfidential", "keep");
        data.put("applicant2ContactDetailsConfidential", "share");
        data.put("applicant1FinancialOrderForRemoved", "value");
        data.put("applicant2FinancialOrderForRemoved", "value");
        data.put("dateConditionalOrderSubmitted", "2021-11-11");
        data.put("legalProceedingsExist", "YES");
        data.put("legalProceedingsDescription", "value");
        data.put("doYouAgreeCourtHasJurisdiction", "YES");
        data.put("serviceApplicationType", "type");
        data.put("coCourtName", "serviceCentre");
        data.put("courtName", "serviceCentre");

        final var result = RetiredFields.migrate(data);

        assertThat(result.get("applicant1FirstName")).isEqualTo("This will be nulled");
        assertThat(result.get("exampleRetiredField")).isNull();
        assertThat(result.get("applicant1LastName")).isEqualTo("This will be left alone");
        assertThat(result.get("applicant1ContactDetailsConfidential")).isNull();
        assertThat(result.get("applicant2ContactDetailsConfidential")).isNull();
        assertThat(result.get("applicant1FinancialOrderForRemoved")).isNull();
        assertThat(result.get("applicant2FinancialOrderForRemoved")).isNull();
        assertThat(result.get("dateConditionalOrderSubmitted")).isNull();
        assertThat(result.get("coDateSubmitted")).isEqualTo("2021-11-11");
        assertThat(result.get("legalProceedingsExist")).isNull();
        assertThat(result.get("applicant2LegalProceedings")).isEqualTo("YES");
        assertThat(result.get("legalProceedingsDescription")).isNull();
        assertThat(result.get("applicant2LegalProceedingsDetails")).isEqualTo("value");
        assertThat(result.get("doYouAgreeCourtHasJurisdiction")).isNull();
        assertThat(result.get("jurisdictionAgree")).isEqualTo("YES");
        assertThat(result.get("serviceApplicationType")).isNull();
        assertThat(result.get("alternativeServiceType")).isEqualTo("type");
        assertThat(result.get("coCourtName")).isNull();
        assertThat(result.get("courtName")).isNull();
        assertThat(result.get("coCourt")).isEqualTo(BURY_ST_EDMUNDS.getCourtId());
        assertThat(result.get("court")).isEqualTo(BURY_ST_EDMUNDS.getCourtId());
    }

    @Test
    void shouldIgnoreFieldIfPresentAndSetToNull() {
        final var data = new HashMap<String, Object>();
        data.put("courtName", null);

        final var result = RetiredFields.migrate(data);

        assertThat(result.get("courtName")).isNull();
        assertThat(result.get("court")).isNull();
    }
}
