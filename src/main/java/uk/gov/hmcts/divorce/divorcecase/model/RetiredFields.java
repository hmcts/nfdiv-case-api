package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.map.HashedMap;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor.Prayer;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableMap;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.Collection;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedRadioList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.Application.ThePrayer;
import static uk.gov.hmcts.divorce.divorcecase.model.Application.ThePrayer.I_CONFIRM;
import static uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderCourt.BURY_ST_EDMUNDS;
import static uk.gov.hmcts.divorce.divorcecase.model.HowToRespondApplication.DISPUTE_DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.HowToRespondApplication.WITHOUT_DISPUTE_DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.Solicitor.Prayer.CONFIRM;

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

    @CCD(label = "Retired YesNo field used for prayer")
    private YesOrNo applicant1PrayerHasBeenGiven;

    @CCD(
        label = "Upload new documents page removed hence field retired"
    )
    private YesOrNo coAddNewDocuments;

    @CCD(
        label = "Upload new documents page removed hence field retired",
        typeOverride = Collection,
        typeParameterOverride = "DivorceDocument"
    )
    private List<ListValue<DivorceDocument>> coDocumentsUploaded;

    @CCD(
        label = "Renamed petition to application"
    )
    private YesOrNo coIsEverythingInPetitionTrue;

    @CCD(
        label = "Retired applicant 1 who is the financial order for?"
    )
    private Set<FinancialOrderFor> applicant1FinancialOrderFor;

    @CCD(
        label = "Retired applicant 2 who is the financial order for?"
    )
    private Set<FinancialOrderFor> applicant2FinancialOrderFor;

    @CCD(
        label = "Previous Service Applications",
        typeOverride = Collection,
        typeParameterOverride = "AlternativeService"
    )
    private List<ListValue<AlternativeService>> alternativeServiceApplications;

    @CCD(
        label = "Retired respondent wants to dispute the application"
    )
    private YesOrNo disputeApplication;

    @CCD(
        label = "Retired further details for Judge",
        typeOverride = TextArea
    )
    private String generalReferralJudgeDetails;

    @CCD(
        label = "Retired further details for Legal Advisor",
        typeOverride = TextArea
    )
    private String generalReferralLegalAdvisorDetails;

    @CCD(
        label = "Retired YesNo field used for solicitor agreeing to receive emails"
    )
    private YesOrNo solicitorAgreeToReceiveEmails;

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
        init.put("applicant1PrayerHasBeenGiven",
            data -> data.put("applicant1PrayerHasBeenGivenCheckbox", transformApplicant1PrayerHasBeenGivenField(data)));
        init.put("coIsEverythingInPetitionTrue",
            data -> data.put("coIsEverythingInApplicationTrue", data.get("coIsEverythingInPetitionTrue")));
        init.put("alternativeServiceApplications",
            data -> data.put("alternativeServiceOutcomes", transformAlternativeServiceApplications(data)));
        init.put("disputeApplication",
            data -> data.put("howToRespondApplication", transformDisputeApplication(data)));
        init.put("generalReferralJudgeDetails",
            data -> data.put(
                "generalReferralJudgeOrLegalAdvisorDetails",
                transformGeneralReferralDetails(data, "generalReferralJudgeDetails")
            ));
        init.put("generalReferralLegalAdvisorDetails",
            data -> data.put(
                "generalReferralJudgeOrLegalAdvisorDetails",
                transformGeneralReferralDetails(data, "generalReferralLegalAdvisorDetails")
            ));
        init.put("solicitorAgreeToReceiveEmails",
            data -> data.put("solicitorAgreeToReceiveEmailsCheckbox", transformSolicitorAgreeToReceiveEmailsField(data)));

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

    @SuppressWarnings({"unchecked", "PMD"})
    public static List<ListValue<AlternativeServiceOutcome>> transformAlternativeServiceApplications(Map<String, Object> data) {

        ArrayList<LinkedHashMap<String, Object>> oldListValues =
            (ArrayList<LinkedHashMap<String, Object>>) data.get("alternativeServiceApplications");

        List<ListValue<AlternativeServiceOutcome>> newListValues = new ArrayList<>();

        for (LinkedHashMap<String, Object> obj : oldListValues) {

            LinkedHashMap<String, Object> entry = (LinkedHashMap<String, Object>) obj.get("value");

            AlternativeServiceOutcome alternativeServiceOutcome = AlternativeServiceOutcome.builder()
                .alternativeServiceType(
                    getEnumValueFromJsonProperty(AlternativeServiceType.class, (String) entry.get("alternativeServiceType")))
                .serviceApplicationRefusalReason((String) entry.get("serviceApplicationRefusalReason"))
                .localCourtName((String) entry.get("localCourtName"))
                .localCourtEmail((String) entry.get("localCourtEmail"))
                .reasonFailureToServeByBailiff((String) entry.get("reasonFailureToServeByBailiff"))
                .paymentMethod(
                    getEnumValueFromJsonProperty(ServicePaymentMethod.class, (String) entry.get("paymentMethod")))
                .serviceApplicationGranted(
                    getEnumValueFromJsonProperty(YesOrNo.class, (String) entry.get("serviceApplicationGranted")))
                .successfulServedByBailiff(
                    getEnumValueFromJsonProperty(YesOrNo.class, (String) entry.get("successfulServedByBailiff")))
                .receivedServiceApplicationDate(getFormattedLocalDate((String) entry.get("receivedServiceApplicationDate")))
                .receivedServiceAddedDate(getFormattedLocalDate((String) entry.get("receivedServiceAddedDate")))
                .serviceApplicationDecisionDate(getFormattedLocalDate((String) entry.get("serviceApplicationDecisionDate")))
                .deemedServiceDate(getFormattedLocalDate((String) entry.get("deemedServiceDate")))
                .certificateOfServiceDate(getFormattedLocalDate((String) entry.get("certificateOfServiceDate")))
                .build();

            var listValue = ListValue
                .<AlternativeServiceOutcome>builder()
                .id((String) obj.get("id"))
                .value(alternativeServiceOutcome)
                .build();
            newListValues.add(listValue);
        }
        return newListValues;
    }

    public static LocalDate getFormattedLocalDate(String dateToParse) {

        if (null != dateToParse) {
            DateTimeFormatter localDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.UK);
            try {
                return LocalDate.parse((CharSequence) dateToParse, localDateFormatter);
            } catch (DateTimeParseException ex) {
                // If the date stored in CCD fails validation, just store a null date in the migrated data
                return null;
            }
        } else {
            return null;
        }
    }

    public static <T extends Enum<T>> T getEnumValueFromJsonProperty(Class<T> enumClass, String jsonPropertyValue) {
        for (Field field : enumClass.getFields()) {
            if (field.getAnnotation(JsonProperty.class).value().equals(jsonPropertyValue)) {
                return Enum.valueOf(enumClass, field.getName());
            }
        }
        return null;
    }

    private static String transformDisputeApplication(Map<String, Object> data) {
        String value = (String) data.get("disputeApplication");
        return YES.getValue().equalsIgnoreCase(value)
            ? DISPUTE_DIVORCE.getType()
            : WITHOUT_DISPUTE_DIVORCE.getType();
    }

    private static String transformGeneralReferralDetails(Map<String, Object> data, String retiredFieldName) {
        String retiredFieldValue = (String) data.get(retiredFieldName);
        String newFieldValue = (String) data.get("generalReferralJudgeOrLegalAdvisorDetails");
        if (null != newFieldValue) {
            return retiredFieldValue.concat(" ").concat(newFieldValue);
        }
        return retiredFieldValue;
    }

    private static Set<Prayer> transformSolicitorAgreeToReceiveEmailsField(Map<String, Object> data) {
        String value = (String) data.get("solicitorAgreeToReceiveEmails");
        return YES.getValue().equalsIgnoreCase(value)
            ? Set.of(CONFIRM)
            : emptySet();
    }
}
