package uk.gov.hmcts.divorce.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.DynamicMultiSelectList;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.ScannedDocument;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralEmail;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static java.util.Collections.singletonList;
import static java.util.stream.Stream.ofNullable;
import static org.apache.commons.collections4.ListUtils.emptyIfNull;
import static uk.gov.hmcts.divorce.divorcecase.model.State.POST_SUBMISSION_STATES_WITH_WITHDRAWN_AND_REJECTED;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
public class CaseworkerPrepareGeneralEmailAttachments implements CCDConfig<CaseData, State, UserRole> {

    public static final int MAX_NUMBER_GENERAL_EMAIL_ATTACHMENTS = 10;

    public static final String CASEWORKER_PREPARE_GENERAL_EMAIL = "caseworker-prepare-general-email";

    private enum AttachedDocumentType {
        APP1_UPLOADED,
        APP2_UPLOADED,
        UPLOADED,
        GENERATED,
        SCANNED,
        GENERAL_ORDER
    }

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_PREPARE_GENERAL_EMAIL)
            .forStates(POST_SUBMISSION_STATES_WITH_WITHDRAWN_AND_REJECTED)
            .name("Prepare email attachments")
            .description("Prepare general email attachments")
            .showSummary()
            .showEventNotes()
            .aboutToStartCallback(this::aboutToStart)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .grant(CREATE_READ_UPDATE, CASE_WORKER)
            .grantHistoryOnly(SUPER_USER, LEGAL_ADVISOR, JUDGE))
            .page("prepareGeneralEmail", this::midEvent)
            .pageLabel("Prepare general email attachments")
            .label("labelSelectDocuments", "### Select from existing documents")
            .complex(CaseData::getDocuments)
            .done()
            .complex(CaseData::getGeneralEmail)
                .optional(GeneralEmail::getGeUploadedDocumentNames)
                .optional(GeneralEmail::getGeGeneratedDocumentNames)
                .optional(GeneralEmail::getGeScannedDocumentNames)
                .optional(GeneralEmail::getGeApplicant1DocumentNames)
                .optional(GeneralEmail::getGeApplicant2DocumentNames)
                .optional(GeneralEmail::getGeGeneralOrderDocumentNames)
                .optional(GeneralEmail::getGeneralEmailAttachments)
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(final CaseDetails<CaseData, State> details) {
        log.info("{} about to start callback invoked for Case Id: {}", CASEWORKER_PREPARE_GENERAL_EMAIL, details.getId());
        final CaseData caseData = details.getData();

        caseData.getGeneralEmail().setGeneralEmailAttachments(null);
        caseData.getGeneralEmail().setGeUploadedDocumentNames(getDivorceDocumentNames(caseData, AttachedDocumentType.UPLOADED));
        caseData.getGeneralEmail().setGeGeneratedDocumentNames(getDivorceDocumentNames(caseData, AttachedDocumentType.GENERATED));
        caseData.getGeneralEmail().setGeApplicant1DocumentNames(getDivorceDocumentNames(caseData, AttachedDocumentType.APP1_UPLOADED));
        caseData.getGeneralEmail().setGeApplicant2DocumentNames(getDivorceDocumentNames(caseData, AttachedDocumentType.APP2_UPLOADED));
        addScannedDocumentNamesToGeneralEmail(caseData);
        addGeneralOrderDocumentNamesToGeneralEmail(caseData);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {

        final CaseData caseData = details.getData();

        final boolean invalidGeneralEmailAttachments = ofNullable(caseData.getGeneralEmail().getGeneralEmailAttachments())
            .flatMap(Collection::stream)
            .anyMatch(divorceDocument -> ObjectUtils.isEmpty(divorceDocument.getValue().getDocumentLink()));

        if (invalidGeneralEmailAttachments) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .errors(singletonList("Please ensure all General Email attachments have been uploaded before continuing"))
                .build();
        }

        if (getTotalNumberOfAttachments(caseData) > MAX_NUMBER_GENERAL_EMAIL_ATTACHMENTS) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .errors(singletonList(String.format("Number of attachments on General Email cannot exceed %s",
                    MAX_NUMBER_GENERAL_EMAIL_ATTACHMENTS)))
                .build();
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {
        final CaseData caseData = details.getData();
        GeneralEmail generalEmail = caseData.getGeneralEmail();

        if (generalEmail.getGeneralEmailAttachments() != null) {
            addAttachedDocumentNamesToGeneralEmail(caseData);
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    private int getTotalNumberOfAttachments(CaseData caseData) {
        GeneralEmail generalEmail = caseData.getGeneralEmail();

        return (generalEmail.getGeUploadedDocumentNames() != null ? generalEmail.getGeUploadedDocumentNames().getValue().size() : 0)
            + (generalEmail.getGeGeneratedDocumentNames() != null ? generalEmail.getGeGeneratedDocumentNames().getValue().size() : 0)
            + (generalEmail.getGeScannedDocumentNames() != null ? generalEmail.getGeScannedDocumentNames().getValue().size() : 0)
            + (generalEmail.getGeApplicant1DocumentNames() != null ? generalEmail.getGeApplicant1DocumentNames().getValue().size() : 0)
            + (generalEmail.getGeApplicant2DocumentNames() != null ? generalEmail.getGeApplicant2DocumentNames().getValue().size() : 0)
            + (generalEmail.getGeGeneralOrderDocumentNames() != null ? generalEmail.getGeGeneralOrderDocumentNames().getValue().size() : 0)
            + (generalEmail.getGeneralEmailAttachments() != null ? generalEmail.getGeneralEmailAttachments().size() : 0);
    }

    private DynamicMultiSelectList getDivorceDocumentNames(final CaseData caseData, final AttachedDocumentType type) {
        List<DynamicListElement> lastSelection = getLastSelectedListElements(caseData, type);

        List<ListValue<DivorceDocument>> caseDocuments = null;

        switch (type) {
            case UPLOADED -> caseDocuments = caseData.getDocuments().getDocumentsUploaded();
            case APP1_UPLOADED -> caseDocuments = caseData.getDocuments().getApplicant1DocumentsUploaded();
            case APP2_UPLOADED -> caseDocuments = caseData.getDocuments().getApplicant2DocumentsUploaded();
            case GENERATED -> caseDocuments = caseData.getDocuments().getDocumentsGenerated();
            default -> caseDocuments = null;
        }

        List<DynamicListElement> uploadedDocNames =
            emptyIfNull(caseDocuments)
                .stream()
                .filter(this::divorceDocumentHasFileAttached)
                .map(documentListValue ->
                    DynamicListElement
                        .builder()
                        .label(documentListValue.getValue().getDocumentLink().getFilename())
                        .code(UUID.fromString(documentListValue.getId())).build()
                ).toList();

        DynamicMultiSelectList emailDocNamesDynamicList = DynamicMultiSelectList
            .builder()
            .listItems(uploadedDocNames)
            .value(lastSelection)
            .build();

        return emailDocNamesDynamicList;
    }

    private boolean divorceDocumentHasFileAttached(ListValue<DivorceDocument> divorceDocumentListValue) {
        var divorceDocumentLink = divorceDocumentListValue.getValue().getDocumentLink();

        return divorceDocumentLink != null && divorceDocumentLink.getFilename() != null;
    }

    private static List<DynamicListElement> getLastSelectedListElements(CaseData caseData, AttachedDocumentType type) {
        List<DynamicListElement> lastSelection = null;

        DynamicMultiSelectList lastList = null;

        switch (type) {
            case UPLOADED -> lastList = caseData.getGeneralEmail().getGeUploadedDocumentNames();
            case APP1_UPLOADED -> lastList = caseData.getGeneralEmail().getGeApplicant1DocumentNames();
            case APP2_UPLOADED -> lastList = caseData.getGeneralEmail().getGeApplicant2DocumentNames();
            case GENERATED -> lastList = caseData.getGeneralEmail().getGeGeneratedDocumentNames();
            case SCANNED -> lastList = caseData.getGeneralEmail().getGeScannedDocumentNames();
            case GENERAL_ORDER -> lastList = caseData.getGeneralEmail().getGeGeneralOrderDocumentNames();
            default -> lastList = null;
        }

        if (lastList != null
            && (lastList.getValue().size() > 0)) {
            lastSelection = lastList.getValue();
        }
        return lastSelection;
    }

    private void addScannedDocumentNamesToGeneralEmail(final CaseData caseData) {
        List<DynamicListElement> lastSelection = getLastSelectedListElements(caseData, AttachedDocumentType.SCANNED);

        List<DynamicListElement> scannedDocNames =
            emptyIfNull(caseData.getDocuments().getScannedDocuments())
                .stream()
                .filter(this::scannedDocumentHasFileAttached)
                .map(documentListValue ->
                    DynamicListElement
                        .builder()
                        .label(documentListValue.getValue().getUrl().getFilename())
                        .code(UUID.fromString(documentListValue.getId())).build()
                ).toList();

        DynamicMultiSelectList emailDocNamesDynamicList = DynamicMultiSelectList
            .builder()
            .listItems(scannedDocNames)
            .value(lastSelection)
            .build();

        caseData.getGeneralEmail().setGeScannedDocumentNames(emailDocNamesDynamicList);
    }

    private boolean scannedDocumentHasFileAttached(ListValue<ScannedDocument> scannedDocumentListValue) {
        var scannedDocumentUrl = scannedDocumentListValue.getValue().getUrl();

        return scannedDocumentUrl != null && scannedDocumentUrl.getFilename() != null;
    }

    private void addGeneralOrderDocumentNamesToGeneralEmail(final CaseData caseData) {
        List<DynamicListElement> lastSelection = getLastSelectedListElements(caseData, AttachedDocumentType.GENERAL_ORDER);

        List<DynamicListElement> docNames =
            emptyIfNull(caseData.getGeneralOrders())
                .stream()
                .map(documentListValue ->
                    DynamicListElement
                        .builder()
                        .label(documentListValue.getValue().getGeneralOrderDocument().getDocumentLink().getFilename())
                        .code(UUID.fromString(documentListValue.getId())).build()
                ).toList();

        DynamicMultiSelectList emailDocNamesDynamicList = DynamicMultiSelectList
            .builder()
            .listItems(docNames)
            .value(lastSelection)
            .build();

        caseData.getGeneralEmail().setGeGeneralOrderDocumentNames(emailDocNamesDynamicList);
    }

    private void addAttachedDocumentNamesToGeneralEmail(final CaseData caseData) {

        List<DynamicListElement> attachedDocNames =
            emptyIfNull(caseData.getGeneralEmail().getGeneralEmailAttachments())
                .stream()
                .map(documentListValue ->
                    DynamicListElement
                        .builder()
                        .label(documentListValue.getValue().getDocumentLink().getFilename())
                        .code(UUID.fromString(documentListValue.getId())).build()
                ).toList();

        DynamicMultiSelectList emailDocNamesDynamicList = DynamicMultiSelectList
            .builder()
            .listItems(attachedDocNames)
            .value(attachedDocNames)
            .build();

        caseData.getGeneralEmail().setGeAttachedDocumentNames(emailDocNamesDynamicList);
    }
}
