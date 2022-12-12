package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.access.Applicant2Access;
import uk.gov.hmcts.divorce.divorcecase.model.access.CaseworkerAccess;
import uk.gov.hmcts.divorce.divorcecase.model.access.DefaultAccess;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.UUID.randomUUID;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.Collection;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedRadioList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.MultiSelectList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ClarificationReason.OTHER;
import static uk.gov.hmcts.divorce.divorcecase.model.RefusalOption.MORE_INFO;
import static uk.gov.hmcts.divorce.divorcecase.model.RefusalOption.REJECT;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public class ConditionalOrder {

    @JsonUnwrapped(prefix = "Applicant1")
    @Builder.Default
    @CCD(access = {DefaultAccess.class})
    private ConditionalOrderQuestions conditionalOrderApplicant1Questions = new ConditionalOrderQuestions();

    @JsonUnwrapped(prefix = "Applicant2")
    @Builder.Default
    @CCD(access = {DefaultAccess.class, Applicant2Access.class})
    private ConditionalOrderQuestions conditionalOrderApplicant2Questions = new ConditionalOrderQuestions();

    @CCD(
        label = "Link to respondent answers"
    )
    private Document respondentAnswersLink;

    @CCD(
        label = "Link to online petition"
    )
    private Document onlinePetitionLink;

    @CCD(
        label = "Link to scanned D84 form"
    )
    private Document scannedD84Form;

    @CCD(
        label = "Date D84 form was scanned"
    )
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime dateD84FormScanned;

    @CCD(
        label = "What application type is the D84?",
        typeOverride = FixedRadioList,
        typeParameterOverride = "OfflineApplicationType"
    )
    private OfflineApplicationType d84ApplicationType;

    @CCD(
        label = "Who is submitting the D84?",
        typeOverride = FixedRadioList,
        typeParameterOverride = "OfflineWhoApplying"
    )
    private OfflineWhoApplying d84WhoApplying;

    @CCD(
        label = "Switched to sole"
    )
    private YesOrNo switchedToSole;

    @CCD(
        label = "Link to alternative service document"
    )
    private Document lastAlternativeServiceDocumentLink;

    @CCD(
        label = "Grant Conditional Order?"
    )
    private YesOrNo granted;

    @CCD(
        label = "Grant Cost Order?"
    )
    private YesOrNo claimsGranted;

    @CCD(
        label = "Make costs order information",
        typeOverride = TextArea
    )
    private String claimsCostsOrderInformation;

    @CCD(
        label = "Legal advisor decision date"
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate decisionDate;

    @CCD(
        label = "Conditional Order granted date"
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate grantedDate;

    @CCD(
        label = "Refusal decision",
        typeOverride = FixedRadioList,
        typeParameterOverride = "RefusalOption"
    )
    private RefusalOption refusalDecision;

    @CCD(
        label = "Additional info",
        typeOverride = TextArea
    )
    private String refusalAdminErrorInfo;

    @CCD(
        label = "Make a free text order",
        typeOverride = TextArea
    )
    private String refusalRejectionAdditionalInfo;

    @CCD(
        label = "How many times have we tried the remind applicant apply for CO cron"
    )
    private int cronRetriesRemindApplicantApplyCo;

    @CCD(
        label = "Provide a refusal reason",
        hint = "Choose at least one of the following",
        typeOverride = MultiSelectList,
        typeParameterOverride = "ClarificationReason"
    )
    private Set<ClarificationReason> refusalClarificationReason;

    @CCD(
        label = "Clarification additional information",
        typeOverride = TextArea
    )
    private String refusalClarificationAdditionalInfo;

    @CCD(
        label = "Clarification additional information (Translated)",
        typeOverride = TextArea
    )
    private String refusalClarificationAdditionalInfoTranslated;

    @CCD(
        label = "Translated To?",
        typeOverride = FixedRadioList,
        typeParameterOverride = "TranslatedToLanguage"
    )
    private TranslatedToLanguage refusalClarificationAdditionalInfoTranslatedTo;

    @CCD(
        label = "List of responses for Conditional Order clarification",
        typeOverride = Collection,
        typeParameterOverride = "TextArea"
    )
    private List<ListValue<String>> clarificationResponses;

    @CCD(
        label = "Documents uploaded for the Conditional Order Clarification",
        typeOverride = Collection,
        typeParameterOverride = "DivorceDocument"
    )
    private List<ListValue<DivorceDocument>> clarificationUploadDocuments;

    @CCD(
        label = "Applicant cannot upload all or some Conditional Order Clarification documents",
        access = {DefaultAccess.class}
    )
    private YesOrNo cannotUploadClarificationDocuments;

    @CCD(
        label = "Case on digital Conditional Order Outcome"
    )
    private YesOrNo outcomeCase;

    @CCD(
        label = "Court name"
    )
    private ConditionalOrderCourt court;

    @CCD(
        label = "Date and time of hearing"
    )
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime dateAndTimeOfHearing;

    @CCD(
        label = "Pronouncement Judge"
    )
    private String pronouncementJudge;

    @CCD(
        label = "Grant Cost Order?",
        typeOverride = FixedRadioList,
        typeParameterOverride = "JudgeCostsClaimGranted"
    )
    private JudgeCostsClaimGranted judgeCostsClaimGranted;

    @CCD(
        label = "Additional info",
        typeOverride = TextArea
    )
    private String judgeCostsOrderAdditionalInfo;

    @CCD(
        label = "Link to certificate of entitlement",
        access = {CaseworkerAccess.class}
    )
    private DivorceDocument certificateOfEntitlementDocument;

    @CCD(
        label = "Has offline certificate of entitlement been sent to Applicant 1"
    )
    private YesOrNo offlineCertificateOfEntitlementDocumentSentToApplicant1;

    @CCD(
        label = "Has offline certificate of entitlement been sent to Applicant 2"
    )
    private YesOrNo offlineCertificateOfEntitlementDocumentSentToApplicant2;

    @CCD(
        label = "Refusal Rejection reasons",
        access = {DefaultAccess.class}
    )
    private Document refusalOrderDocument;

    @CCD(
        label = "Conditional Order Granted"
    )
    private DivorceDocument conditionalOrderGrantedDocument;

    @CCD(
        label = "Legal Advisor Decisions Submitted",
        typeOverride = Collection,
        typeParameterOverride = "LegalAdvisorDecision"
    )
    private List<ListValue<LegalAdvisorDecision>> legalAdvisorDecisions;

    @CCD(
        label = "Clarification Responses Submitted",
        typeOverride = Collection,
        typeParameterOverride = "ClarificationResponse"
    )
    private List<ListValue<ClarificationResponse>> clarificationResponsesSubmitted;

    @CCD(
        label = "Date Conditional Order rescinded"
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate rescindedDate;

    @CCD(
        label = "Service Confirmed"
    )
    private YesOrNo serviceConfirmed;

    @CCD(
        label = "Documents uploaded for Proof of Service",
        typeOverride = Collection,
        typeParameterOverride = "DivorceDocument"
    )
    private List<ListValue<DivorceDocument>> proofOfServiceUploadDocuments;

    @CCD(
        label = "Is latest approved service application a bailiff application?"
    )
    private YesOrNo lastApprovedServiceApplicationIsBailiffApplication;

    @CCD(
        label = "Certificate of service date"
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate certificateOfServiceDate;

    @CCD(
        label = "Did bailiff serve successfully?"
    )
    private YesOrNo successfulServedByBailiff;

    @CCD(
        label = "Is a required admin clarification now submitted?",
        access = {CaseworkerAccess.class}
    )
    private YesOrNo isAdminClarificationSubmitted;

    @JsonIgnore
    public boolean areClaimsGranted() {
        return nonNull(claimsGranted) && claimsGranted.toBoolean();
    }

    @JsonIgnore
    public boolean hasConditionalOrderBeenGranted() {
        return YES.equals(granted);
    }

    @JsonIgnore
    public void resetRefusalFields() {
        this.setGranted(null);
        this.setRefusalDecision(null);
        this.setRefusalClarificationReason(null);
        this.setRefusalClarificationAdditionalInfo(null);
        this.setRefusalAdminErrorInfo(null);
        this.setRefusalRejectionAdditionalInfo(null);
    }

    @JsonIgnore
    public void resetClarificationFields() {
        this.setClarificationResponses(new ArrayList<>());
        this.setCannotUploadClarificationDocuments(null);
        this.setClarificationUploadDocuments(new ArrayList<>());
    }

    @JsonIgnore
    public boolean hasOfflineCertificateOfEntitlementBeenSentToApplicant1() {
        return YES.equals(offlineCertificateOfEntitlementDocumentSentToApplicant1);
    }

    @JsonIgnore
    public boolean hasOfflineCertificateOfEntitlementBeenSentToApplicant2() {
        return YES.equals(offlineCertificateOfEntitlementDocumentSentToApplicant2);
    }

    @JsonIgnore
    public boolean isConditionalOrderPending() {
        return isNull(conditionalOrderApplicant1Questions.getSubmittedDate());
    }

    @JsonIgnore
    public boolean cannotUploadClarificationDocumentsBoolean() {
        return nonNull(cannotUploadClarificationDocuments) && cannotUploadClarificationDocuments.toBoolean();
    }

    @JsonIgnore
    public <T> List<ListValue<T>> addAuditRecord(final List<ListValue<T>> auditRecords,
                                                 final T value) {

        final var listItemId = String.valueOf(randomUUID());
        final var listValue = new ListValue<>(listItemId, value);
        final List<ListValue<T>> list = isEmpty(auditRecords)
            ? new ArrayList<>()
            : auditRecords;

        list.add(0, listValue);

        return list;
    }

    @JsonIgnore
    public LegalAdvisorDecision populateLegalAdvisorDecision(LocalDate decisionDate) {

        if (hasConditionalOrderBeenGranted()) {

            return LegalAdvisorDecision.builder()
                .granted(getGranted())
                .decisionDate(getDecisionDate())
                .build();

        } else if (MORE_INFO.equals(getRefusalDecision())) {

            Set<ClarificationReason> reasonsSet =  new HashSet<>();
            if (isNotEmpty(getRefusalClarificationReason())) {
                reasonsSet.addAll(getRefusalClarificationReason().stream()
                    .filter(reason -> !OTHER.equals(reason))
                    .collect(Collectors.toSet()));
            }

            return LegalAdvisorDecision.builder()
                .granted(getGranted())
                .decisionDate(decisionDate)
                .refusalDecision(getRefusalDecision())
                .refusalClarificationReason(reasonsSet)
                .refusalClarificationAdditionalInfo(getRefusalClarificationAdditionalInfo())
                .build();

        } else if (REJECT.equals(getRefusalDecision())) {

            return LegalAdvisorDecision.builder()
                .granted(getGranted())
                .decisionDate(decisionDate)
                .refusalDecision(getRefusalDecision())
                .refusalRejectionAdditionalInfo(getRefusalRejectionAdditionalInfo())
                .build();

        } else {

            return LegalAdvisorDecision.builder()
                .granted(getGranted())
                .decisionDate(decisionDate)
                .refusalDecision(getRefusalDecision())
                .refusalAdminErrorInfo(getRefusalAdminErrorInfo())
                .build();

        }
    }

    @JsonIgnore
    public boolean shouldEnableSwitchToSoleCoForApplicant1() {
        return
            YES.equals(getConditionalOrderApplicant1Questions().getIsSubmitted())
                && !YES.equals(getConditionalOrderApplicant2Questions().getIsSubmitted())
                && isNotEmpty(getConditionalOrderApplicant1Questions().getSubmittedDate())
                && getConditionalOrderApplicant1Questions().getSubmittedDate().plusDays(14).isBefore(LocalDateTime.now());
    }

    @JsonIgnore
    public boolean shouldEnableSwitchToSoleCoForApplicant2() {
        return
            YES.equals(getConditionalOrderApplicant2Questions().getIsSubmitted())
                && !YES.equals(getConditionalOrderApplicant1Questions().getIsSubmitted())
                && isNotEmpty(getConditionalOrderApplicant2Questions().getSubmittedDate())
                && getConditionalOrderApplicant2Questions().getSubmittedDate().plusDays(14).isBefore(LocalDateTime.now());
    }

    @JsonIgnore
    public boolean isLastApprovedServiceApplicationBailiffApplication() {
        return YES.equals(lastApprovedServiceApplicationIsBailiffApplication);
    }

    @JsonIgnore
    public boolean hasServiceBeenConfirmed() {
        return YES.equals(serviceConfirmed);
    }
}
