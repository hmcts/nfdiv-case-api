package uk.gov.hmcts.divorce.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplication;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.DocumentRemovalService;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.model.DocumentType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.hmcts.divorce.divorcecase.model.State.POST_SUBMISSION_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Component
@Slf4j
public class CaseworkerRemoveDocument implements CCDConfig<CaseData, State, UserRole> {

    @Autowired
    private DocumentRemovalService documentRemovalService;

    public static final String CASEWORKER_REMOVE_DOCUMENT = "caseworker-remove-document";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_REMOVE_DOCUMENT)
            .forStates(POST_SUBMISSION_STATES)
            .name("Remove documents")
            .description("Remove uploaded and generated documents")
            .showEventNotes()
            .aboutToSubmitCallback(this::aboutToSubmit)
            .grant(CREATE_READ_UPDATE_DELETE, SUPER_USER)
            .grantHistoryOnly(CASE_WORKER))
            .page("removeDocuments")
            .pageLabel("Remove uploaded and generated documents")
            .complex(CaseData::getDocuments)
                .optional(CaseDocuments::getApplicant1DocumentsUploaded)
                .optional(CaseDocuments::getApplicant2DocumentsUploaded)
                .optional(CaseDocuments::getDocumentsGenerated)
                .optional(CaseDocuments::getDocumentsUploaded)
                .optional(CaseDocuments::getScannedDocuments)
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {

        final var beforeCaseData = beforeDetails.getData();
        final var currentCaseData = details.getData();

        handleDeletionOfGeneralApplicationDocuments(beforeCaseData, currentCaseData);
        handleDeletionOfDivorceDocuments(beforeCaseData, currentCaseData);
        handleDeletionOfScannedDocuments(beforeCaseData, currentCaseData);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(currentCaseData)
            .build();
    }

    private void handleDeletionOfDivorceDocuments(CaseData beforeCaseData, CaseData currentCaseData) {
        List<ListValue<DivorceDocument>> divorceDocsToRemove = new ArrayList<>();

        divorceDocsToRemove.addAll(findDocumentsForRemoval(
            beforeCaseData.getDocuments().getApplicant1DocumentsUploaded(),
            currentCaseData.getDocuments().getApplicant1DocumentsUploaded()
        ));

        divorceDocsToRemove.addAll(findDocumentsForRemoval(
            beforeCaseData.getDocuments().getApplicant2DocumentsUploaded(),
            currentCaseData.getDocuments().getApplicant2DocumentsUploaded()
        ));

        divorceDocsToRemove.addAll(findDocumentsForRemoval(
            beforeCaseData.getDocuments().getDocumentsGenerated(),
            currentCaseData.getDocuments().getDocumentsGenerated()
        ));

        divorceDocsToRemove.addAll(findDocumentsForRemoval(
            beforeCaseData.getDocuments().getDocumentsUploaded(),
            currentCaseData.getDocuments().getDocumentsUploaded()
        ));

        if (!divorceDocsToRemove.isEmpty()) {
            documentRemovalService.deleteDocument(divorceDocsToRemove);
        }
    }

    private List<ListValue<DivorceDocument>> findDocumentsForRemoval(final List<ListValue<DivorceDocument>> beforeDocs,
                                                                     final List<ListValue<DivorceDocument>> currentDocs) {

        List<ListValue<DivorceDocument>> documentsToRemove = new ArrayList<>();

        if (beforeDocs != null && currentDocs != null) {
            beforeDocs.forEach(document -> {
                if (!currentDocs.contains(document)) {
                    documentsToRemove.add(document);
                }
            });
        }

        return documentsToRemove;
    }

    private void handleDeletionOfScannedDocuments(CaseData beforeCaseData, CaseData currentCaseData) {
        documentRemovalService.handleDeletionOfScannedDocuments(beforeCaseData, currentCaseData);
    }

    private void handleDeletionOfGeneralApplicationDocuments(CaseData beforeCaseData, CaseData currentCaseData) {
        List<ListValue<DivorceDocument>> uploadedDocsToRemove = new ArrayList<>();
        List<ListValue<DivorceDocument>> generalAppDocs = new ArrayList<>();

        uploadedDocsToRemove.addAll(findDocumentsForRemoval(
            beforeCaseData.getDocuments().getDocumentsUploaded(),
            currentCaseData.getDocuments().getDocumentsUploaded()
        ));

        if (!uploadedDocsToRemove.isEmpty()) {
            generalAppDocs = uploadedDocsToRemove
                .stream()
                .filter(divorceDocumentListValue ->
                    divorceDocumentListValue.getValue().getDocumentType() != null
                        && divorceDocumentListValue.getValue().getDocumentType().equals(DocumentType.GENERAL_APPLICATION))
                .collect(Collectors.toList());
        }

        if (!generalAppDocs.isEmpty()) {
            for (ListValue<DivorceDocument> generalAppDoc : generalAppDocs) {
                log.info("General App Doc to remove : {} ", generalAppDoc.getValue().getDocumentLink().getUrl());
                handleDeletionOfGeneralApplicationDocument(currentCaseData, generalAppDoc.getValue());
            }
        }
    }

    private void handleDeletionOfGeneralApplicationDocument(CaseData caseData, DivorceDocument document) {
        deleteFromCurrentGeneralApplication(caseData, document);
        deleteFromGeneralApplicationCollection(caseData, document);
    }

    private void deleteFromCurrentGeneralApplication(CaseData caseData, DivorceDocument document) {
        final GeneralApplication generalApplication = caseData.getGeneralApplication();
        if (generalApplication != null && generalApplication.getGeneralApplicationDocuments() != null
            && !generalApplication.getGeneralApplicationDocuments().isEmpty()) {
            generalApplication.setGeneralApplicationDocuments(
                generalApplication.getGeneralApplicationDocuments()
                    .stream()
                    .filter(genAppDoc -> !genAppDoc.getValue().getDocumentLink().equals(document.getDocumentLink()))
                    .collect(Collectors.toList())
            );
        }
    }

    private void deleteFromGeneralApplicationCollection(CaseData caseData, DivorceDocument document) {
        if (caseData.getGeneralApplications() == null || caseData.getGeneralApplications().isEmpty()) {
            return;
        }

        for (ListValue<GeneralApplication> generalApplicationListValue : caseData.getGeneralApplications()) {
            final GeneralApplication generalApplication = generalApplicationListValue.getValue();

            if (generalApplication.getGeneralApplicationDocuments() != null
                && !generalApplication.getGeneralApplicationDocuments().isEmpty()) {
                generalApplication.setGeneralApplicationDocuments(
                    generalApplication.getGeneralApplicationDocuments()
                        .stream()
                        .filter(genAppDoc -> !genAppDoc.getValue().getDocumentLink().equals(document.getDocumentLink()))
                        .collect(Collectors.toList())
                );
            }
        }
    }
}
