package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.groovy.parser.antlr4.util.StringUtils;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.BulkScanEnvelope;
import uk.gov.hmcts.ccd.sdk.type.CaseLink;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.ScannedDocument;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.caseworker.model.CaseNote;
import uk.gov.hmcts.divorce.divorcecase.model.access.Applicant2Access;
import uk.gov.hmcts.divorce.divorcecase.model.access.CaseworkerAccess;
import uk.gov.hmcts.divorce.divorcecase.model.access.CaseworkerAccessBetaOnlyAccess;
import uk.gov.hmcts.divorce.divorcecase.model.access.CaseworkerAndSuperUserAccess;
import uk.gov.hmcts.divorce.divorcecase.model.access.CaseworkerBulkScanAccess;
import uk.gov.hmcts.divorce.divorcecase.model.access.CaseworkerCourtAdminWithSolicitorAccess;
import uk.gov.hmcts.divorce.divorcecase.model.access.DefaultAccess;
import uk.gov.hmcts.divorce.document.model.ConfidentialDivorceDocument;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toCollection;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.Collection;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedRadioList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class CaseData {

    @CCD(
        label = "Application type",
        access = {DefaultAccess.class},
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

    @JsonUnwrapped()
    @Builder.Default
    private FinalOrder finalOrder = new FinalOrder();

    @JsonUnwrapped
    @Builder.Default
    private GeneralOrder generalOrder = new GeneralOrder();

    @JsonUnwrapped
    @Builder.Default
    private GeneralEmail generalEmail = new GeneralEmail();

    @JsonUnwrapped
    @Builder.Default
    private GeneralReferral generalReferral = new GeneralReferral();

    @CCD(
        label = "General Referrals",
        typeOverride = Collection,
        typeParameterOverride = "GeneralReferral"
    )
    private List<ListValue<GeneralReferral>> generalReferrals;

    @CCD(
        label = "Previous Service Applications",
        typeOverride = Collection,
        typeParameterOverride = "AlternativeServiceOutcome",
        access = {CaseworkerAccessBetaOnlyAccess.class}
    )
    private List<ListValue<AlternativeServiceOutcome>> alternativeServiceOutcomes;

    @JsonUnwrapped
    @Builder.Default
    @CCD(access = {CaseworkerAccessBetaOnlyAccess.class})
    private AlternativeService alternativeService = new AlternativeService();

    @CCD(
        label = "Applicant 1 uploaded documents",
        typeOverride = Collection,
        typeParameterOverride = "DivorceDocument",
        access = {DefaultAccess.class}
    )
    private List<ListValue<DivorceDocument>> applicant1DocumentsUploaded;

    @CCD(
        label = "Applicant 2 Documents uploaded",
        typeOverride = Collection,
        typeParameterOverride = "DivorceDocument",
        access = {Applicant2Access.class}
    )
    private List<ListValue<DivorceDocument>> applicant2DocumentsUploaded;

    @CCD(
        label = "RDC",
        hint = "Regional divorce unit",
        access = {DefaultAccess.class}
    )
    private Court divorceUnit;

    @CCD(
        label = "Documents generated",
        typeOverride = Collection,
        typeParameterOverride = "DivorceDocument",
        access = {DefaultAccess.class}
    )
    private List<ListValue<DivorceDocument>> documentsGenerated;

    @CCD(
        label = "Documents uploaded",
        typeOverride = Collection,
        typeParameterOverride = "DivorceDocument",
        access = {DefaultAccess.class}
    )
    private List<ListValue<DivorceDocument>> documentsUploaded;

    @CCD(
        label = "Upload D11 Document",
        access = {CaseworkerAccessBetaOnlyAccess.class}
    )
    private DivorceDocument d11Document;

    @CCD(
        label = "Confidential documents uploaded",
        typeOverride = Collection,
        typeParameterOverride = "ConfidentialDivorceDocument",
        access = {CaseworkerCourtAdminWithSolicitorAccess.class}
    )
    private List<ListValue<ConfidentialDivorceDocument>> confidentialDocumentsUploaded;

    @CCD(
        label = "General Orders",
        access = {CaseworkerAccessBetaOnlyAccess.class}
    )
    private List<ListValue<DivorceGeneralOrder>> generalOrders;

    @CCD(
        label = "Case ID for previously Amended Case, which was challenged by the respondent",
        access = {DefaultAccess.class}
    )
    private CaseLink previousCaseId;

    @CCD(
        label = "Due Date",
        access = {DefaultAccess.class}
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;

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
        access = {CaseworkerAccess.class}
    )
    private String bulkListCaseReference;

    @CCD(access = {DefaultAccess.class})
    @JsonUnwrapped
    private RetiredFields retiredFields;

    @CCD(access = {CaseworkerAccess.class})
    private String hyphenatedCaseRef;

    @CCD(
        label = "Scanned documents",
        typeOverride = Collection,
        typeParameterOverride = "ScannedDocument"
    )
    private List<ListValue<ScannedDocument>> scannedDocuments;

    private YesOrNo evidenceHandled;

    @CCD(
        access = {CaseworkerAccess.class}
    )
    @JsonUnwrapped(prefix = "noc")
    private NoticeOfChange noticeOfChange;

    @CCD(
        label = "Bulk Scan Envelopes",
        typeOverride = Collection,
        typeParameterOverride = "BulkScanEnvelope",
        access = {CaseworkerBulkScanAccess.class}
    )
    private List<ListValue<BulkScanEnvelope>> bulkScanEnvelopes;

    @CCD(
        label = "Exception record reference",
        access = {CaseworkerBulkScanAccess.class}
    )
    private String bulkScanCaseReference;

    @JsonUnwrapped(prefix = "paperForm")
    @Builder.Default
    @CCD(access = {CaseworkerBulkScanAccess.class})
    private PaperFormDetails paperFormDetails = new PaperFormDetails();

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
    public boolean isAmendedCase() {
        return null != previousCaseId;
    }

    @JsonIgnore
    public boolean isSoleApplicationOrApplicant2HasAgreedHwf() {
        return null != applicationType
            && applicationType.isSole()
            || null != application.getApplicant2HelpWithFees()
            && null != application.getApplicant2HelpWithFees().getNeedHelp()
            && application.getApplicant2HelpWithFees().getNeedHelp().toBoolean();
    }

    @JsonIgnore
    public void addToDocumentsGenerated(final ListValue<DivorceDocument> listValue) {

        final List<ListValue<DivorceDocument>> documents = getDocumentsGenerated();

        if (isEmpty(documents)) {
            final List<ListValue<DivorceDocument>> documentList = new ArrayList<>();
            documentList.add(listValue);
            setDocumentsGenerated(documentList);
        } else {
            documents.add(0, listValue); // always add to start top of list
        }
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

    public void sortUploadedDocuments(List<ListValue<DivorceDocument>> previousDocuments) {
        if (isEmpty(previousDocuments)) {
            return;
        }

        Set<String> previousListValueIds = previousDocuments
            .stream()
            .map(ListValue::getId)
            .collect(toCollection(HashSet::new));

        //Split the collection into two lists one without id's(newly added documents) and other with id's(existing documents)
        Map<Boolean, List<ListValue<DivorceDocument>>> documentsWithoutIds =
            this.getDocumentsUploaded()
                .stream()
                .collect(groupingBy(listValue -> !previousListValueIds.contains(listValue.getId())));

        this.setDocumentsUploaded(sortDocuments(documentsWithoutIds));
    }

    public void sortConfidentialDocuments(List<ListValue<ConfidentialDivorceDocument>> previousDocuments) {
        if (isEmpty(previousDocuments)) {
            return;
        }

        Set<String> previousListValueIds = previousDocuments
            .stream()
            .map(ListValue::getId)
            .collect(toCollection(HashSet::new));

        //Split the collection into two lists one without id's(newly added documents) and other with id's(existing documents)
        Map<Boolean, List<ListValue<ConfidentialDivorceDocument>>> documentsWithoutIds =
            this.getConfidentialDocumentsUploaded()
                .stream()
                .collect(groupingBy(listValue -> !previousListValueIds.contains(listValue.getId())));

        List<ListValue<ConfidentialDivorceDocument>> sortedDocuments = new ArrayList<>();
        sortedDocuments.addAll(0, documentsWithoutIds.get(true)); // add new documents to start of the list
        sortedDocuments.addAll(1, documentsWithoutIds.get(false));

        sortedDocuments.forEach(
            uploadedDocumentListValue -> uploadedDocumentListValue.setId(String.valueOf(UUID.randomUUID()))
        );

        this.setConfidentialDocumentsUploaded(sortedDocuments);
    }

    public void sortApplicant1UploadedDocuments(List<ListValue<DivorceDocument>> previousDocuments) {
        if (isEmpty(previousDocuments)) {
            return;
        }

        Set<String> previousListValueIds = previousDocuments
            .stream()
            .map(ListValue::getId)
            .collect(toCollection(HashSet::new));

        //Split the collection into two lists one without id's(newly added documents) and other with id's(existing documents)
        Map<Boolean, List<ListValue<DivorceDocument>>> documentsWithoutIds =
            this.getApplicant1DocumentsUploaded()
                .stream()
                .collect(groupingBy(listValue -> !previousListValueIds.contains(listValue.getId())));

        this.setApplicant1DocumentsUploaded(sortDocuments(documentsWithoutIds));
    }

    private List<ListValue<DivorceDocument>> sortDocuments(final Map<Boolean, List<ListValue<DivorceDocument>>> documentsWithoutIds) {

        final List<ListValue<DivorceDocument>> sortedDocuments = new ArrayList<>();

        final var newDocuments = documentsWithoutIds.get(true);
        final var previousDocuments = documentsWithoutIds.get(false);

        if (null != newDocuments) {
            sortedDocuments.addAll(0, newDocuments); // add new documents to start of the list
            sortedDocuments.addAll(1, previousDocuments);
            sortedDocuments.forEach(
                uploadedDocumentListValue -> uploadedDocumentListValue.setId(String.valueOf(UUID.randomUUID()))
            );
            return sortedDocuments;
        }

        return previousDocuments;
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
    public void addToDocumentsUploaded(final ListValue<DivorceDocument> listValue) {

        final List<ListValue<DivorceDocument>> documents = getDocumentsUploaded();

        if (isEmpty(documents)) {
            final List<ListValue<DivorceDocument>> documentList = new ArrayList<>();
            documentList.add(listValue);
            setDocumentsUploaded(documentList);
        } else {
            documents.add(0, listValue); // always add to start top of list
        }
    }

    @JsonIgnore
    public boolean isDivorce() {
        return divorceOrDissolution.isDivorce();
    }
}
