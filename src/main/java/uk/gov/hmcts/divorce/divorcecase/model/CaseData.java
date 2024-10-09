package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.CaseLink;
import uk.gov.hmcts.ccd.sdk.type.ChangeOrganisationRequest;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.ccd.sdk.type.ScannedDocument;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.caseworker.model.CaseNote;
import uk.gov.hmcts.divorce.divorcecase.model.access.AcaSystemUserAccess;
import uk.gov.hmcts.divorce.divorcecase.model.access.Applicant2Access;
import uk.gov.hmcts.divorce.divorcecase.model.access.CaseworkerAccess;
import uk.gov.hmcts.divorce.divorcecase.model.access.CaseworkerAccessOnlyAccess;
import uk.gov.hmcts.divorce.divorcecase.model.access.CaseworkerAndSuperUserAccess;
import uk.gov.hmcts.divorce.divorcecase.model.access.CaseworkerBulkScanAccess;
import uk.gov.hmcts.divorce.divorcecase.model.access.CaseworkerWithCAAAccess;
import uk.gov.hmcts.divorce.divorcecase.model.access.DefaultAccess;
import uk.gov.hmcts.divorce.divorcecase.model.access.SolicitorAndSystemUpdateAccess;
import uk.gov.hmcts.divorce.divorcecase.model.access.SystemUpdateAndSuperUserAccess;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.divorce.noticeofchange.model.ChangeOfRepresentative;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static java.lang.Integer.parseInt;
import static java.util.Collections.singletonList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.CasePaymentHistoryViewer;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.Collection;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedRadioList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments.addDocumentToTop;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.FEMALE;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.MALE;
import static uk.gov.hmcts.divorce.divorcecase.model.PaymentStatus.SUCCESS;
import static uk.gov.hmcts.divorce.divorcecase.model.SolicitorPaymentMethod.FEES_HELP_WITH;
import static uk.gov.hmcts.divorce.divorcecase.model.SupplementaryCaseType.JUDICIAL_SEPARATION;
import static uk.gov.hmcts.divorce.divorcecase.model.SupplementaryCaseType.NA;
import static uk.gov.hmcts.divorce.divorcecase.model.SupplementaryCaseType.SEPARATION;
import static uk.gov.hmcts.divorce.divorcecase.model.WhoDivorcing.HUSBAND;
import static uk.gov.hmcts.divorce.divorcecase.model.WhoDivorcing.WIFE;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_APPLICATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.FINAL_ORDER_APPLICATION;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class CaseData {

    @CCD(
        label = "Application type",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class},
        typeOverride = FixedRadioList,
        typeParameterOverride = "ApplicationType"
    )
    private ApplicationType applicationType;

    @CCD(
        label = "Divorce or dissolution?",
        access = {DefaultAccess.class},
        typeOverride = FixedRadioList,
        typeParameterOverride = "DivorceOrDissolution"
    )
    private DivorceOrDissolution divorceOrDissolution;

    @Setter(AccessLevel.NONE)
    @CCD(
        label = "Judicial separation, separation, or nullity?",
        access = {DefaultAccess.class},
        typeOverride = FixedRadioList,
        typeParameterOverride = "SupplementaryCaseType"
    )
    @Builder.Default
    private SupplementaryCaseType supplementaryCaseType = NA;

    @JsonUnwrapped(prefix = "labelContent")
    @Builder.Default
    @CCD(access = {DefaultAccess.class})
    private LabelContent labelContent = new LabelContent();

    @JsonUnwrapped(prefix = "applicant1")
    @Builder.Default
    @CCD(access = {DefaultAccess.class})
    private Applicant applicant1 = new Applicant();

    @JsonUnwrapped(prefix = "applicant2")
    @Builder.Default
    @CCD(access = {DefaultAccess.class, Applicant2Access.class})
    private Applicant applicant2 = new Applicant();

    @JsonUnwrapped()
    @Builder.Default
    private Application application = new Application();

    @JsonUnwrapped()
    @CCD(access = {DefaultAccess.class})
    private CaseInvite caseInvite;

    @JsonUnwrapped()
    @Builder.Default
    private AcknowledgementOfService acknowledgementOfService = new AcknowledgementOfService();

    @JsonUnwrapped(prefix = "co")
    @Builder.Default
    @CCD(access = {DefaultAccess.class})
    private ConditionalOrder conditionalOrder = new ConditionalOrder();

    @CCD(access = {DefaultAccess.class, Applicant2Access.class})
    private String citizenPaymentCallbackUrl;

    @JsonUnwrapped()
    @Builder.Default
    private FinalOrder finalOrder = new FinalOrder();

    @JsonUnwrapped
    @Builder.Default
    private GeneralOrder generalOrder = new GeneralOrder();

    @JsonUnwrapped
    @Builder.Default
    @CCD(access = {SystemUpdateAndSuperUserAccess.class})
    private GeneralEmail generalEmail = new GeneralEmail();

    @JsonUnwrapped
    @Builder.Default
    private GeneralLetter generalLetter = new GeneralLetter();

    @JsonUnwrapped
    @Builder.Default
    @CCD(access = {SystemUpdateAndSuperUserAccess.class})
    private GeneralReferral generalReferral = new GeneralReferral();

    @JsonUnwrapped
    @Builder.Default
    @CCD(access = {SolicitorAndSystemUpdateAccess.class})
    private GeneralApplication generalApplication = new GeneralApplication();

    @CCD(
        label = "General Applications",
        typeOverride = Collection,
        typeParameterOverride = "GeneralApplication",
        access = {SolicitorAndSystemUpdateAccess.class}
    )
    private List<ListValue<GeneralApplication>> generalApplications;

    @CCD(
        label = "General Referrals",
        typeOverride = Collection,
        typeParameterOverride = "GeneralReferral",
        access = {SystemUpdateAndSuperUserAccess.class}
    )
    private List<ListValue<GeneralReferral>> generalReferrals;

    @JsonUnwrapped
    @Builder.Default
    private Hearing hearing = new Hearing();

    @CCD(
        label = "Is case judicial separation?",
        access = {DefaultAccess.class}
    )
    private YesOrNo isJudicialSeparation;

    @CCD(
        label = "Previous Service Applications",
        typeOverride = Collection,
        typeParameterOverride = "AlternativeServiceOutcome",
        access = {CaseworkerAccessOnlyAccess.class}
    )
    private List<ListValue<AlternativeServiceOutcome>> alternativeServiceOutcomes;

    @JsonUnwrapped
    @Builder.Default
    @CCD(access = {CaseworkerAccessOnlyAccess.class})
    private AlternativeService alternativeService = new AlternativeService();

    @JsonUnwrapped
    @Builder.Default
    private CaseDocuments documents = new CaseDocuments();

    @CCD(
        label = "RDC",
        hint = "Regional divorce unit",
        access = {DefaultAccess.class}
    )
    private Court divorceUnit;

    @CCD(
        label = "General Orders",
        access = {CaseworkerAccessOnlyAccess.class}
    )
    private List<ListValue<DivorceGeneralOrder>> generalOrders;

    @CCD(
        label = "Due Date",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;

    @CCD(
        label = "Awaiting answer start date",
        access = {DefaultAccess.class, CaseworkerAccess.class}
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate awaitingJsAnswerStartDate;

    @CCD(
        label = "Notes",
        typeOverride = Collection,
        typeParameterOverride = "CaseNote",
        access = {CaseworkerAndSuperUserAccess.class}
    )
    private List<ListValue<CaseNote>> notes;

    @CCD(
        label = "Add a case note",
        hint = "Enter note",
        typeOverride = TextArea,
        access = {CaseworkerAndSuperUserAccess.class}
    )
    private String note;

    @CCD(
        label = "Bulk list case reference",
        typeOverride = FieldType.CaseLink,
        access = {CaseworkerAccess.class}
    )
    private CaseLink bulkListCaseReferenceLink;

    @CCD(access = {DefaultAccess.class})
    @JsonUnwrapped
    private RetiredFields retiredFields;

    @CCD(access = {CaseworkerAccess.class})
    private String hyphenatedCaseRef;

    @CCD(access = {AcaSystemUserAccess.class})
    private ChangeOrganisationRequest<CaseRoleID> changeOrganisationRequestField;

    @CCD(
            access = {DefaultAccess.class, AcaSystemUserAccess.class, CaseworkerAccess.class},
            label = "Change of representatives"
    )
    @Builder.Default
    private List<ListValue<ChangeOfRepresentative>> changeOfRepresentatives = new ArrayList<>();

    @CCD(
        access = {CaseworkerAccess.class}
    )
    @JsonUnwrapped(prefix = "noc")
    private NoticeOfChange noticeOfChange;

    @JsonUnwrapped(prefix = "paperForm")
    @Builder.Default
    @CCD(access = {CaseworkerBulkScanAccess.class})
    private PaperFormDetails paperFormDetails = new PaperFormDetails();

    @CCD(
        label = "General emails",
        typeOverride = Collection,
        typeParameterOverride = "GeneralEmailDetails",
        access = {SystemUpdateAndSuperUserAccess.class}
    )
    private List<ListValue<GeneralEmailDetails>> generalEmails;

    @CCD(
        label = "Confidential general emails",
        typeOverride = Collection,
        typeParameterOverride = "GeneralEmailDetails",
        access = {SystemUpdateAndSuperUserAccess.class}
    )
    private List<ListValue<GeneralEmailDetails>> confidentialGeneralEmails;

    @CCD(
        typeOverride = CasePaymentHistoryViewer,
        access = {SystemUpdateAndSuperUserAccess.class}
    )
    private String paymentHistoryField;

    @JsonUnwrapped
    @Builder.Default
    private BulkScanMetaInfo bulkScanMetaInfo = new BulkScanMetaInfo();

    @CCD(
        label = "General letters",
        typeOverride = Collection,
        typeParameterOverride = "GeneralLetterDetails",
        access = {SystemUpdateAndSuperUserAccess.class}
    )
    private List<ListValue<GeneralLetterDetails>> generalLetters;

    @CCD(
        label = "Sent notifications",
        access = {DefaultAccess.class}
    )
    @Builder.Default
    private SentNotifications sentNotifications = new SentNotifications();

    @JsonIgnore
    public String formatCaseRef(long caseId) {
        String temp = String.format("%016d", caseId);
        return String.format("%4s-%4s-%4s-%4s",
            temp.substring(0, 4),
            temp.substring(4, 8),
            temp.substring(8, 12),
            temp.substring(12, 16)
        );
    }

    @JsonIgnore
    public boolean isSoleApplicationOrApplicant2HasAgreedHwf() {
        return nonNull(applicationType)
            && applicationType.isSole()
            || nonNull(application.getApplicant2HelpWithFees())
            && nonNull(application.getApplicant2HelpWithFees().getNeedHelp())
            && application.getApplicant2HelpWithFees().getNeedHelp().toBoolean()
            || FEES_HELP_WITH.equals(application.getSolPaymentHowToPay());
    }

    @JsonIgnore
    public boolean isWelshApplication() {
        if (applicationType.isSole()) {
            return YES.equals(applicant1.getLanguagePreferenceWelsh())
                || YES.equals(applicant1.getUsedWelshTranslationOnSubmission());
        } else {
            return YES.equals(applicant1.getLanguagePreferenceWelsh())
                || YES.equals(applicant2.getLanguagePreferenceWelsh())
                || YES.equals(applicant1.getUsedWelshTranslationOnSubmission())
                || YES.equals(applicant2.getUsedWelshTranslationOnSubmission());
        }
    }

    private void enforceDivorceOrDissolution() {
        if (SEPARATION.equals(this.supplementaryCaseType)) {
            this.divorceOrDissolution = DISSOLUTION; // set Dissolution when Separation
        } else {
            this.divorceOrDissolution = DIVORCE; // set Divorce when JS
        }
    }

    public void setSupplementaryCaseType(SupplementaryCaseType supplementaryCaseType) {
        this.supplementaryCaseType = supplementaryCaseType;
        if (JUDICIAL_SEPARATION.equals(this.supplementaryCaseType) || SEPARATION.equals(this.supplementaryCaseType)) { // Setting JS or Sep
            this.enforceDivorceOrDissolution(); // Set divorceOrDissolution based on supplementaryCaseType
        }
    }

    @JsonIgnore
    public boolean isJudicialSeparationCase() {
        return JUDICIAL_SEPARATION.equals(this.supplementaryCaseType) || SEPARATION.equals(this.supplementaryCaseType);
    }

    @JsonIgnore
    public boolean hasNaOrNullSupplementaryCaseType() {
        return NA.equals(this.supplementaryCaseType) || isNull(this.supplementaryCaseType);
    }

    @JsonIgnore
    public String getApplicant2EmailAddress() {
        final String applicant2Email = applicant2.getEmail();

        if (StringUtils.isEmpty(applicant2Email)) {
            if (nonNull(caseInvite)) {
                return caseInvite.applicant2InviteEmailAddress();
            } else {
                return null;
            }
        }

        return applicant2Email;
    }

    public void archiveAlternativeServiceApplicationOnCompletion() {

        AlternativeService alternativeService = this.getAlternativeService();

        if (null != alternativeService) {

            alternativeService.setReceivedServiceAddedDate(LocalDate.now());

            AlternativeServiceOutcome alternativeServiceOutcome = alternativeService.getOutcome();

            if (isEmpty(this.getAlternativeServiceOutcomes())) {

                List<ListValue<AlternativeServiceOutcome>> listValues = new ArrayList<>();

                var listValue = ListValue
                    .<AlternativeServiceOutcome>builder()
                    .id("1")
                    .value(alternativeServiceOutcome)
                    .build();

                listValues.add(listValue);
                this.setAlternativeServiceOutcomes(listValues);

            } else {

                var listValue = ListValue
                    .<AlternativeServiceOutcome>builder()
                    .value(alternativeServiceOutcome)
                    .build();

                int listValueIndex = 0;
                this.getAlternativeServiceOutcomes().add(0, listValue);
                for (ListValue<AlternativeServiceOutcome> asListValue : this.getAlternativeServiceOutcomes()) {
                    asListValue.setId(String.valueOf(listValueIndex++));
                }
            }
            // Null the current AlternativeService object instance in the CaseData so that a new one can be created
            this.setAlternativeService(null);
        }
    }

    @JsonIgnore
    public boolean isDivorce() {
        return divorceOrDissolution.isDivorce();
    }

    @JsonIgnore
    public void deriveAndPopulateApplicantGenderDetails() {
        Gender app1Gender;
        Gender app2Gender;
        WhoDivorcing whoDivorcing;
        if (this.getDivorceOrDissolution().isDivorce()) {
            // for a divorce we ask who is applicant1 divorcing to infer applicant2's gender, then use the marriage
            // formation to infer applicant 1's gender
            whoDivorcing = this.getApplication().getDivorceWho();
            app2Gender = whoDivorcing == HUSBAND ? MALE : FEMALE;
            app1Gender = this.getApplication().getMarriageDetails().getFormationType().getPartnerGender(app2Gender);
        } else {
            // for a dissolution we ask for applicant1's gender and use the marriage formation to infer applicant 2's
            // gender and who they are divorcing
            app1Gender = this.getApplicant1().getGender();
            app2Gender = this.getApplication().getMarriageDetails().getFormationType().getPartnerGender(app1Gender);
            whoDivorcing = app2Gender == MALE ? HUSBAND : WIFE;
        }

        this.getApplicant1().setGender(app1Gender);
        this.getApplicant2().setGender(app2Gender);
        this.getApplication().setDivorceWho(whoDivorcing);
    }

    @JsonIgnore
    public void updateCaseDataWithPaymentDetails(
        OrderSummary applicationFeeOrderSummary,
        CaseData caseData,
        String paymentReference
    ) {
        var payment = Payment
            .builder()
            .amount(parseInt(applicationFeeOrderSummary.getPaymentTotal()))
            .channel("online")
            .feeCode(applicationFeeOrderSummary.getFees().get(0).getValue().getCode())
            .reference(paymentReference)
            .status(SUCCESS)
            .build();


        var application = caseData.getApplication();

        if (isEmpty(application.getApplicationPayments())) {
            List<ListValue<Payment>> payments = new ArrayList<>();
            payments.add(new ListValue<>(UUID.randomUUID().toString(), payment));
            application.setApplicationPayments(payments);
        } else {
            application.getApplicationPayments()
                .add(new ListValue<>(UUID.randomUUID().toString(), payment));
        }
    }

    @JsonIgnore
    public Optional<AlternativeServiceOutcome> getFirstAlternativeServiceOutcome() {
        return Stream.ofNullable(getAlternativeServiceOutcomes())
            .flatMap(java.util.Collection::stream)
            .map(ListValue::getValue)
            .findFirst();
    }

    @JsonIgnore
    public void unlinkFromTheBulkCase() {
        setBulkListCaseReferenceLink(null);

        ConditionalOrder conditionalOrder = getConditionalOrder();

        if (conditionalOrder != null) {
            conditionalOrder.setCourt(null);
            conditionalOrder.setDateAndTimeOfHearing(null);
            conditionalOrder.setPronouncementJudge(null);
            conditionalOrder.setCertificateOfEntitlementDocument(null);
        }
    }

    @JsonIgnore
    public void reclassifyScannedDocumentToChosenDocumentType(DocumentType documentType,
                                                              Clock clock,
                                                              String filename) {

        Optional<ListValue<ScannedDocument>> scannedDocumentOptional =
            emptyIfNull(documents.getScannedDocuments())
                .stream()
                .filter(scannedDoc -> scannedDoc.getValue().getFileName().equals(filename))
                .findFirst();

        scannedDocumentOptional.ifPresent(
            scannedDocumentListValue ->
                reclassifyScannedDocumentToChosenDocumentType(
                    documentType,
                    clock,
                    scannedDocumentListValue.getValue())
        );
    }

    @JsonIgnore
    public void reclassifyScannedDocumentToChosenDocumentType(DocumentType documentType,
                                                              Clock clock,
                                                              ScannedDocument scannedDocument) {

        DivorceDocument divorceDocument = documents.mapScannedDocumentToDivorceDocument(
            scannedDocument,
            documentType,
            clock
        );

        List<ListValue<DivorceDocument>> updatedDocumentsUploaded = addDocumentToTop(
            documents.getDocumentsUploaded(),
            divorceDocument
        );

        documents.setDocumentsUploaded(updatedDocumentsUploaded);

        if (CONDITIONAL_ORDER_APPLICATION.equals(documentType)) {
            documents.setDocumentsGenerated(
                addDocumentToTop(documents.getDocumentsGenerated(), divorceDocument)
            );
            conditionalOrder.setScannedD84Form(divorceDocument.getDocumentLink());
            conditionalOrder.setDateD84FormScanned(scannedDocument.getScannedDate());
        }

        if (FINAL_ORDER_APPLICATION.equals(documentType)) {
            finalOrder.setScannedD36Form(divorceDocument.getDocumentLink());
            finalOrder.setDateD36FormScanned(scannedDocument.getScannedDate());
        }
    }

    @JsonIgnore
    public void updateCaseWithGeneralApplication() {
        GeneralApplication generalApplication = this.getGeneralApplication();

        generalApplication.getGeneralApplicationDocuments().forEach(divorceDocumentListValue -> {
            divorceDocumentListValue.getValue().setDocumentType(DocumentType.GENERAL_APPLICATION);
            this.getDocuments().setDocumentsUploaded(
                addDocumentToTop(this.getDocuments().getDocumentsUploaded(), divorceDocumentListValue.getValue()));
        });

        final ListValue<GeneralApplication> generalApplicationListValue = ListValue.<GeneralApplication>builder()
            .id(UUID.randomUUID().toString())
            .value(generalApplication)
            .build();

        if (isNull(this.getGeneralApplications())) {
            this.setGeneralApplications(singletonList(generalApplicationListValue));
        } else {
            this.getGeneralApplications().add(0, generalApplicationListValue);
        }
    }
}
