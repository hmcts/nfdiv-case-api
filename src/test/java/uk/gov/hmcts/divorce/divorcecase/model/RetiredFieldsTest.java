package uk.gov.hmcts.divorce.divorcecase.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static uk.gov.hmcts.divorce.divorcecase.model.Application.ThePrayer.I_CONFIRM;
import static uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderCourt.BURY_ST_EDMUNDS;
import static uk.gov.hmcts.divorce.divorcecase.model.Solicitor.Prayer.CONFIRM;

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
        data.put("coDateSubmitted", "2021-11-13");
        data.put("legalProceedingsExist", "YES");
        data.put("legalProceedingsDescription", "value");
        data.put("doYouAgreeCourtHasJurisdiction", "YES");
        data.put("serviceApplicationType", "type");
        data.put("coCourtName", "serviceCentre");
        data.put("applicant1PrayerHasBeenGiven", "Yes");
        data.put("applicant2PrayerHasBeenGiven", "Yes");
        data.put("coAddNewDocuments", "YES");
        data.put("coDocumentsUploaded", Collections.emptyList());
        data.put("coIsEverythingInPetitionTrue", "YES");
        data.put("coIsEverythingInApplicationTrue", "YES");
        data.put("alternativeServiceApplications", new ArrayList<LinkedHashMap<String, Object>>());
        data.put("disputeApplication", "YES");
        data.put("applicant1SolicitorAgreeToReceiveEmails", "Yes");
        data.put("applicant2SolicitorAgreeToReceiveEmails", "No");
        data.put("coClarificationResponse", "some text");
        data.put("marriageIsSameSexCouple", "Yes");
        data.put("applicant2KeepContactDetailsConfidential", "Yes");
        data.put("jointApplicantsRemindedCanApplyForConditionalOrder", "Yes");
        data.put("applicant1ApplyForConditionalOrderStarted", "YES");
        data.put("applicant2ApplyForConditionalOrderStarted", "YES");
        data.put("applicant1ContinueApplication", "YES");
        data.put("applicant2ContinueApplication", "YES");
        data.put("coChangeOrAddToApplication", "YES");
        data.put("coApplyForConditionalOrder", "YES");

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
            entry("coApplicant1SubmittedDate", "2021-11-13"),
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
            entry("applicant2PrayerHasBeenGivenCheckbox", Set.of(I_CONFIRM)),
            entry("coAddNewDocuments", "YES"),
            entry("coDocumentsUploaded", emptyList()),
            entry("coIsEverythingInPetitionTrue", null),
            entry("coIsEverythingInApplicationTrue", "YES"),
            entry("howToRespondApplication", "disputeDivorce"),
            entry("coIsEverythingInApplicationTrue", "YES"),
            entry("alternativeServiceApplications", null),
            entry("applicant1SolicitorAgreeToReceiveEmailsCheckbox", Set.of(CONFIRM)),
            entry("applicant2SolicitorAgreeToReceiveEmailsCheckbox", emptySet()),
            entry("coClarificationResponses", singletonList(ListValue.<String>builder().value("some text").build())),
            entry("marriageFormationType", "sameSexCouple"),
            entry("coApplicant1ApplyForConditionalOrderStarted", "YES"),
            entry("coApplicant2ApplyForConditionalOrderStarted", "YES"),
            entry("coApplicant1ContinueApplication", "YES"),
            entry("coApplicant2ContinueApplication", "YES"),
            entry("coApplicant1IsEverythingInApplicationTrue", "YES"),
            entry("coApplicant1ChangeOrAddToApplication", "YES"),
            entry("coApplicant1ApplyForConditionalOrder", "YES"),
            entry("applicantsRemindedCanApplyForConditionalOrder", "Yes")
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
    void shouldMigrateDisputeApplicationWhenDisputeApplicationValueIsNo() {
        final var data = new HashMap<String, Object>();
        data.put("disputeApplication", "No");

        RetiredFields.migrate(data);

        assertThat(data).contains(
            entry("howToRespondApplication", "withoutDisputeDivorce")
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


    @ParameterizedTest
    @MethodSource("judgeAndLaParametersWithOutput")
    void shouldMigrateGeneralReferralJudgeAndLegalAdvisorDetails(
        Map<String, Object> inputData, String output
    ) {

        RetiredFields.migrate(inputData);

        assertThat(inputData).contains(
            entry("generalReferralJudgeOrLegalAdvisorDetails", output)
        );
    }

    @Test
    void shouldMigrateGeneralReferralJudgeAndLegalAdvisorDetailsWhenNoValuesArePresent() {
        final var data = new HashMap<String, Object>();
        data.put("generalReferralJudgeDetails", null);
        data.put("generalReferralLegalAdvisorDetails", null);

        RetiredFields.migrate(data);

        assertThat(data).doesNotContain(
            entry("generalReferralJudgeOrLegalAdvisorDetails", null)
        );
    }

    @Test
    void shouldMigrateServiceApplications() {

        final Map<String, Object> alternativeServiceObject = new LinkedHashMap<>();
        alternativeServiceObject.put("alternativeServiceType", "deemed");

        final HashMap<String, Object> listValueMap = new LinkedHashMap<>();
        listValueMap.put("id", "1");
        listValueMap.put("value", alternativeServiceObject);

        final ArrayList<LinkedHashMap<String, Object>> listValues = new ArrayList<>();
        listValues.add((LinkedHashMap<String, Object>) listValueMap);

        final var data = new HashMap<String, Object>();
        data.put("alternativeServiceApplications", listValues);

        List<ListValue<AlternativeServiceOutcome>> alternativeServiceOutcomes =
            RetiredFields.transformAlternativeServiceApplications(data);

        assertThat(alternativeServiceOutcomes.size()).isEqualTo(1);
        assertThat(alternativeServiceOutcomes.get(0).getValue().getAlternativeServiceType()).isEqualTo(AlternativeServiceType.DEEMED);

    }

    @Test
    void shouldMigrateJurisdictionDisagreeReason() {
        final var data = new HashMap<String, Object>();
        data.put("jurisdictionDisagreeReason", "Jurisdiction Disagree Reason");

        final var result = RetiredFields.migrate(data);

        assertThat(result).contains(
            entry("jurisdictionDisagreeReason", null),
            entry("reasonCourtsOfEnglandAndWalesHaveNoJurisdiction", "Jurisdiction Disagree Reason")
        );
    }

    @Test
    void shouldMigrateGeneralOrderJudgeNameWhenLaNameIsNotPresent() {
        final var data = new HashMap<String, Object>();
        data.put("generalOrderJudgeName", "some judge name");

        final var result = RetiredFields.migrate(data);

        assertThat(result).contains(
            entry("generalOrderJudgeOrLegalAdvisorName", "some judge name")
        );
    }

    @Test
    void shouldMigrateGeneralOrderJudgeAndLaNameWhenBothLaAndJudgeNamesArePresent() {
        final var data = new TreeMap<String, Object>();
        data.put("generalOrderJudgeName", "judge");
        data.put("generalOrderLegalAdvisorName", "la");

        final var result = RetiredFields.migrate(data);

        assertThat(result).contains(
            entry("generalOrderJudgeOrLegalAdvisorName", "la judge")
        );
    }

    @Test
    void shouldMigratePaymentFields() {
        final var data = new TreeMap<String, Object>();
        data.put("disputingFee", OrderSummary.builder().build());
        data.put("paymentMethod", ServicePaymentMethod.FEE_PAY_BY_HWF);
        data.put("feeAccountNumber", "12345");
        data.put("feeAccountReferenceNumber", "REF001");
        data.put("helpWithFeesReferenceNumber", "HWF1234");

        final var result = RetiredFields.migrate(data);

        assertThat(result).contains(
            entry("disputingFeeOrderSummary", OrderSummary.builder().build()),
            entry("servicePaymentFeePaymentMethod", ServicePaymentMethod.FEE_PAY_BY_HWF),
            entry("servicePaymentFeeAccountNumber", "12345"),
            entry("servicePaymentFeeAccountReferenceNumber", "REF001"),
            entry("servicePaymentFeeHelpWithFeesReferenceNumber", "HWF1234")
        );
    }

    private static Stream<Arguments> judgeAndLaParametersWithOutput() {
        final var judgeAndLaDataMap = new HashMap<String, Object>();
        judgeAndLaDataMap.put("generalReferralJudgeDetails", "judge");
        judgeAndLaDataMap.put("generalReferralLegalAdvisorDetails", "la");

        final var onlyLaMap = new HashMap<String, Object>();
        onlyLaMap.put("generalReferralJudgeDetails", null);
        onlyLaMap.put("generalReferralLegalAdvisorDetails", "la");

        final var onlyJudgeMap = new HashMap<String, Object>();
        onlyJudgeMap.put("generalReferralJudgeDetails", "judge");
        onlyJudgeMap.put("generalReferralLegalAdvisorDetails", null);

        return Stream.of(
            Arguments.of(judgeAndLaDataMap, "judge la"),
            Arguments.of(onlyLaMap, "la"),
            Arguments.of(onlyJudgeMap, "judge")
        );
    }

}
