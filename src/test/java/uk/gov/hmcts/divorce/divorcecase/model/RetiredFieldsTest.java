package uk.gov.hmcts.divorce.divorcecase.model;

import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
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

        assertThat(result).contains(
            entry("applicant1FirstName", "This will be nulled"),
            entry("exampleRetiredField", null),
            entry("applicant1LastName", "This will be left alone"),
            entry("applicant1ContactDetailsConfidential", null),
            entry("applicant2ContactDetailsConfidential", null),
            entry("applicant1FinancialOrderForRemoved", null),
            entry("applicant2FinancialOrderForRemoved", null),
            entry("dateConditionalOrderSubmitted", null),
            entry("coDateSubmitted", "2021-11-11"),
            entry("legalProceedingsExist", null),
            entry("applicant2LegalProceedings", "YES"),
            entry("legalProceedingsDescription", null),
            entry("applicant2LegalProceedingsDetails", "value"),
            entry("doYouAgreeCourtHasJurisdiction", null),
            entry("jurisdictionAgree", "YES"),
            entry("serviceApplicationType", null),
            entry("alternativeServiceType", "type"),
            entry("coCourtName", null),
            entry("courtName", null),
            entry("coCourt", BURY_ST_EDMUNDS.getCourtId()),
            entry("court", BURY_ST_EDMUNDS.getCourtId())
        );
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
