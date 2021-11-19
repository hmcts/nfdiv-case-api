package uk.gov.hmcts.divorce.divorcecase.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Set;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static uk.gov.hmcts.divorce.divorcecase.model.Application.ThePrayer.I_CONFIRM;
import static uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderCourt.BURY_ST_EDMUNDS;

public class RetiredFieldsTest {

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
        data.put("applicant1PrayerHasBeenGiven", "Yes");
        data.put("coAddNewDocuments", "YES");
        data.put("coDocumentsUploaded", Collections.emptyList());
        data.put("coIsEverythingInPetitionTrue", "YES");
        data.put("coIsEverythingInApplicationTrue", "YES");
        data.put("alternativeServiceApplications", new ArrayList<LinkedHashMap<String, Object>>());

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
            entry("coCourt", BURY_ST_EDMUNDS.getCourtId()),
            entry("applicant1PrayerHasBeenGivenCheckbox", Set.of(I_CONFIRM)),
            entry("coAddNewDocuments", "YES"),
            entry("coDocumentsUploaded", emptyList()),
            entry("coIsEverythingInPetitionTrue", null),
            entry("coIsEverythingInApplicationTrue", "YES"),
            entry("alternativeServiceApplications", null)

        );
    }

    @Test
    void shouldIgnoreFieldIfPresentAndSetToNullOrEmpty() {
        final var data = new HashMap<String, Object>();
        data.put("coCourtName", null);

        final var result = RetiredFields.migrate(data);

        assertThat(result.get("coCourtName")).isNull();
        assertThat(result.get("coCourt")).isNull();
    }

    @Test
    void shouldSetPrayerAnswerAsNoToEmptySet() {
        final var data = new HashMap<String, Object>();
        data.put("applicant1PrayerHasBeenGiven", "No");

        RetiredFields.migrate(data);

        assertThat(data).contains(
            entry("applicant1PrayerHasBeenGivenCheckbox", emptySet())
        );
    }

    @Test
    void shouldReturnValidLocalDateIfFormatCorrect() {
        DateTimeFormatter localDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.UK);
        LocalDate expectedDate = LocalDate.now();
        LocalDate localDate = RetiredFields.getFormattedLocalDate(expectedDate.format(localDateFormatter));
        assertThat(localDate).isEqualTo(expectedDate);
    }

    @Test
    void shouldReturnNullLocalDateIfFormatInvalid() {
        DateTimeFormatter localDateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd", Locale.UK);
        LocalDate expectedDate = LocalDate.now();
        LocalDate localDate = RetiredFields.getFormattedLocalDate(expectedDate.format(localDateFormatter));
        assertThat(localDate).isNull();
    }

    @Test
    void shouldReturnNullLocalDateIfNullStringSent() {
        LocalDate localDate = RetiredFields.getFormattedLocalDate(null);
        assertThat(localDate).isNull();
    }

    @Test
    void shouldReturnValidEnumForValidEnumJsonProperty() {
        AlternativeServiceType expectedAlternativeServiceType = AlternativeServiceType.BAILIFF;
        AlternativeServiceType alternativeServiceType =
            RetiredFields.getEnumValueFromJsonProperty(AlternativeServiceType.class, "bailiff");
        assertThat(alternativeServiceType).isEqualTo(expectedAlternativeServiceType);

        ServicePaymentMethod expectedServicePaymentMethod = ServicePaymentMethod.FEE_PAY_BY_HWF;
        ServicePaymentMethod servicePaymentMethod =
            RetiredFields.getEnumValueFromJsonProperty(ServicePaymentMethod.class, "feePayByHelp");
        assertThat(servicePaymentMethod).isEqualTo(expectedServicePaymentMethod);
    }

    @Test
    void shouldReturnNullForInvalidEnumJsonProperty() {
        AlternativeServiceType alternativeServiceType =
            RetiredFields.getEnumValueFromJsonProperty(AlternativeServiceType.class, "xxxxx");
        assertThat(alternativeServiceType).isNull();
    }

    @Test
    void shouldReturnNullForNullEnumJsonProperty() {
        AlternativeServiceType alternativeServiceType =
            RetiredFields.getEnumValueFromJsonProperty(AlternativeServiceType.class, null);
        assertThat(alternativeServiceType).isNull();
    }


}
