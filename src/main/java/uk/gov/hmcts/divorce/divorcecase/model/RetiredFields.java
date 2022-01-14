package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.map.HashedMap;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor.Prayer;
import uk.gov.hmcts.divorce.divorcecase.model.access.Applicant2Access;
import uk.gov.hmcts.divorce.divorcecase.model.access.CaseworkerAccessBetaOnlyAccess;
import uk.gov.hmcts.divorce.divorcecase.model.access.DefaultAccess;
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
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableMap;
import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.Collection;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedRadioList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.Application.ThePrayer;
import static uk.gov.hmcts.divorce.divorcecase.model.Application.ThePrayer.I_CONFIRM;
import static uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderCourt.BURY_ST_EDMUNDS;
import static uk.gov.hmcts.divorce.divorcecase.model.ContactDetailsType.PRIVATE;
import static uk.gov.hmcts.divorce.divorcecase.model.ContactDetailsType.PUBLIC;
import static uk.gov.hmcts.divorce.divorcecase.model.HowToRespondApplication.DISPUTE_DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.HowToRespondApplication.WITHOUT_DISPUTE_DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.MarriageFormation.OPPOSITE_SEX_COUPLE;
import static uk.gov.hmcts.divorce.divorcecase.model.MarriageFormation.SAME_SEX_COUPLE;
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
        label = "Retired applicant 1 solicitor agreeing to receive emails"
    )
    private YesOrNo applicant1SolicitorAgreeToReceiveEmails;

    @CCD(
        label = "Retired applicant 2 solicitor agreeing to receive emails"
    )
    private YesOrNo applicant2SolicitorAgreeToReceiveEmails;

    @CCD(
        label = "Reason respondent disagreed to claimed jurisdiction",
        typeOverride = TextArea
    )
    private String jurisdictionDisagreeReason;

    @CCD(
        label = "Retired clarification response",
        typeOverride = TextArea
    )
    private String coClarificationResponse;

    @CCD(
        label = "Retire same sex couple",
        access = {DefaultAccess.class}
    )
    private YesOrNo marriageIsSameSexCouple;

    @CCD(label = "Retired applicant 1 keep contact details private")
    private YesOrNo applicant1KeepContactDetailsConfidential;

    @CCD(
        label = "Retired applicant 1 Keep contact details private",
        access = {DefaultAccess.class, Applicant2Access.class}
    )
    private YesOrNo applicant2KeepContactDetailsConfidential;

    @CCD(
        label = "Retired flag indicating notification to applicant 1 they can apply for a Conditional Order already sent",
        access = {DefaultAccess.class}
    )
    private YesOrNo applicant1NotifiedCanApplyForConditionalOrder;

    @CCD(
        label = "Retired flag indicating notification to joint applicants they can apply for a Conditional Order already sent",
        access = {DefaultAccess.class}
    )
    private YesOrNo jointApplicantsNotifiedCanApplyForConditionalOrder;

    @CCD(
        label = "Retired flag indicating reminder to joint applicants they can apply for a Conditional Order already sent",
        access = {DefaultAccess.class}
    )
    private YesOrNo jointApplicantsRemindedCanApplyForConditionalOrder;

    @CCD(
        label = "Retired Date Conditional Order submitted to HMCTS, split into applicant1 and applicant2 submission dates"
    )
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime coDateSubmitted;

    @CCD(label = "retiredApplicant1ApplyForConditionalOrderStarted")
    private YesOrNo applicant1ApplyForConditionalOrderStarted;

    @CCD(label = "retiredApplicant2ApplyForConditionalOrderStarted")
    private YesOrNo applicant2ApplyForConditionalOrderStarted;

    @CCD(label = "retiredApplicant1ContinueApplication")
    private YesOrNo applicant1ContinueApplication;

    @CCD(label = "retiredApplicant2ContinueApplication")
    private YesOrNo applicant2ContinueApplication;

    @CCD(label = "retiredCoIsEverythingInApplicationTrue")
    private YesOrNo coIsEverythingInApplicationTrue;

    @CCD(label = "retiredCoChangeOrAddToApplication")
    private YesOrNo coChangeOrAddToApplication;

    @CCD(label = "retiredCoApplyForConditionalOrder")
    private YesOrNo coApplyForConditionalOrder;

    @CCD(label = "retiredCoApplicantStatementOfTruth")
    private YesOrNo coApplicantStatementOfTruth;

    @CCD(
        label = "Retire select judge",
        typeOverride = FixedList,
        typeParameterOverride = "GeneralOrderJudgeOrLegalAdvisorType"
    )
    private GeneralOrderJudgeOrLegalAdvisorType generalOrderJudgeType;

    @CCD(
        label = "Retire name of Judge",
        access = {CaseworkerAccessBetaOnlyAccess.class}
    )
    private String generalOrderJudgeName;

    @CCD(
        label = "Retire name of Legal Advisor"
    )
    private String generalOrderLegalAdvisorName;

    @CCD(
        label = "Spouse Type",
        typeOverride = FixedRadioList,
        typeParameterOverride = "WhoDivorcing"
    )
    private WhoDivorcing applicant1DivorceWho;

    @CCD(
        label = "Spouse Type",
        typeOverride = FixedRadioList,
        typeParameterOverride = "WhoDivorcing"
    )
    private WhoDivorcing applicant2DivorceWho;

    @CCD(
        label = "Retire applicant 2 prayer"
    )
    private YesOrNo applicant2PrayerHasBeenGiven;

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
            data -> data.put("coApplicant1SubmittedDate", data.get("dateConditionalOrderSubmitted")));
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
            data -> data.put("applicant1PrayerHasBeenGivenCheckbox",
                transformApplicantPrayerHasBeenGivenField(data,"applicant1PrayerHasBeenGiven"))
        );
        init.put("applicant2PrayerHasBeenGiven",
            data -> data.put("applicant2PrayerHasBeenGivenCheckbox",
                transformApplicantPrayerHasBeenGivenField(data,"applicant2PrayerHasBeenGiven"))
        );
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
        init.put("applicant1SolicitorAgreeToReceiveEmails",
            data -> data.put(
                "applicant1SolicitorAgreeToReceiveEmailsCheckbox",
                transformSolicitorAgreeToReceiveEmailsField(data, "applicant1SolicitorAgreeToReceiveEmails"))
        );
        init.put("applicant2SolicitorAgreeToReceiveEmails",
            data -> data.put(
                "applicant2SolicitorAgreeToReceiveEmailsCheckbox",
                transformSolicitorAgreeToReceiveEmailsField(data, "applicant2SolicitorAgreeToReceiveEmails"))
        );
        init.put("jurisdictionDisagreeReason",
            data -> data.put("reasonCourtsOfEnglandAndWalesHaveNoJurisdiction", data.get("jurisdictionDisagreeReason")));
        init.put("coClarificationResponse",
            data -> data.put("coClarificationResponses", transformClarificationResponse(data)));
        init.put("applicant1KeepContactDetailsConfidential",
            data -> data.put("applicant1ContactDetailsType", transformContactDetails(data, "applicant1KeepContactDetailsConfidential")));
        init.put("applicant2KeepContactDetailsConfidential",
            data -> data.put("applicant2ContactDetailsType", transformContactDetails(data, "applicant2KeepContactDetailsConfidential")));
        init.put("marriageIsSameSexCouple",
            data -> data.put("marriageFormationType", transformSameSexToMarriageFormation(data)));
        init.put("coDateSubmitted",
            data -> data.put("coApplicant1SubmittedDate", data.get("coDateSubmitted")));
        init.put("jointApplicantsRemindedCanApplyForConditionalOrder", data ->
            data.put("applicantsRemindedCanApplyForConditionalOrder", data.get("jointApplicantsRemindedCanApplyForConditionalOrder")));
        init.put("applicant1ApplyForConditionalOrderStarted",
            data -> data.put("coApplicant1ApplyForConditionalOrderStarted", data.get("applicant1ApplyForConditionalOrderStarted")));
        init.put("applicant2ApplyForConditionalOrderStarted",
            data -> data.put("coApplicant2ApplyForConditionalOrderStarted", data.get("applicant2ApplyForConditionalOrderStarted")));
        init.put("applicant1ContinueApplication",
            data -> data.put("coApplicant1ContinueApplication", data.get("applicant1ContinueApplication")));
        init.put("applicant2ContinueApplication",
            data -> data.put("coApplicant2ContinueApplication", data.get("applicant2ContinueApplication")));
        init.put("coIsEverythingInApplicationTrue",
            data -> data.put("coApplicant1IsEverythingInApplicationTrue", data.get("coIsEverythingInApplicationTrue")));
        init.put("coChangeOrAddToApplication",
            data -> data.put("coApplicant1ChangeOrAddToApplication", data.get("coChangeOrAddToApplication")));
        init.put("coApplicantStatementOfTruth",
            data -> data.put("coApplicant1StatementOfTruth", data.get("coApplicantStatementOfTruth")));
        init.put("coApplyForConditionalOrder",
            data -> data.put("coApplicant1ApplyForConditionalOrder", data.get("coApplyForConditionalOrder")));
        init.put("generalOrderJudgeType",
            data -> data.put("generalOrderJudgeOrLegalAdvisorType", data.get("generalOrderJudgeType")));
        init.put("generalOrderJudgeName",
            data -> data.put("generalOrderJudgeOrLegalAdvisorName",
                transformJudgeOrLegalAdvisorName(data, "generalOrderJudgeName")
            )
        );
        init.put("generalOrderLegalAdvisorName",
            data -> data.put("generalOrderJudgeOrLegalAdvisorName",
                transformJudgeOrLegalAdvisorName(data, "generalOrderLegalAdvisorName")
            )
        );

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

    private static String transformContactDetailsConfidentialField(String confidentialFieldName, Map<String, Object> data) {
        String confidentialFieldValue = (String) data.get(confidentialFieldName);
        return ConfidentialAddress.KEEP.getLabel().equalsIgnoreCase(confidentialFieldValue) ? YES.getValue() : NO.getValue();
    }

    private static Set<ThePrayer> transformApplicantPrayerHasBeenGivenField(Map<String, Object> data, String field) {
        String value = (String) data.get(field);
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

    private static Set<Prayer> transformSolicitorAgreeToReceiveEmailsField(Map<String, Object> data, String retiredFieldName) {
        String value = (String) data.get(retiredFieldName);
        return YES.getValue().equalsIgnoreCase(value)
            ? Set.of(CONFIRM)
            : emptySet();
    }

    private static List<ListValue<String>> transformClarificationResponse(Map<String, Object> data) {
        String clarificationResponseText = (String) data.get("coClarificationResponse");
        return singletonList(ListValue.<String>builder().value(clarificationResponseText).build());
    }

    private static String transformContactDetails(Map<String, Object> data, String contactDetailsField) {
        String value = (String) data.get(contactDetailsField);
        return YES.getValue().equalsIgnoreCase(value)
            ? PRIVATE.getType()
            : PUBLIC.getType();
    }

    private static String transformSameSexToMarriageFormation(Map<String, Object> data) {
        String value = (String) data.get("marriageIsSameSexCouple");
        return YES.getValue().equalsIgnoreCase(value)
            ? SAME_SEX_COUPLE.getType()
            : OPPOSITE_SEX_COUPLE.getType();
    }


    private static String transformJudgeOrLegalAdvisorName(Map<String, Object> data, String retiredField) {
        String newJudgeOrLaFieldNameValue = (String) data.get("generalOrderJudgeOrLegalAdvisorName");
        String retiredJudgeOrLaFieldNameValue = (String) data.get(retiredField);

        if (isNotEmpty(newJudgeOrLaFieldNameValue)) {
            return newJudgeOrLaFieldNameValue + " " + retiredJudgeOrLaFieldNameValue;
        }
        return retiredJudgeOrLaFieldNameValue;
    }
}
