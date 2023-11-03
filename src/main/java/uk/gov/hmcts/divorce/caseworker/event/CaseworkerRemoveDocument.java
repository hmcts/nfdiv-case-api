package uk.gov.hmcts.divorce.caseworker.event;

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
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.DocumentRemovalService;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.divorce.divorcecase.model.State.POST_SUBMISSION_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Component
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
                .optional(CaseDocuments::getDocumentsGenerated)
                .optional(CaseDocuments::getDocumentsUploaded)
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {

        final var beforeCaseData = beforeDetails.getData();
        final var currentCaseData = details.getData();
        List<ListValue<DivorceDocument>> documentsToRemove = new ArrayList<>();

        documentsToRemove.addAll(removeDocumentFromList(
            beforeCaseData.getDocuments().getApplicant1DocumentsUploaded(),
            currentCaseData.getDocuments().getApplicant1DocumentsUploaded()
        ));

        documentsToRemove.addAll(removeDocumentFromList(
            beforeCaseData.getDocuments().getDocumentsGenerated(),
            currentCaseData.getDocuments().getDocumentsGenerated()
        ));

        documentsToRemove.addAll(removeDocumentFromList(
            beforeCaseData.getDocuments().getDocumentsUploaded(),
            currentCaseData.getDocuments().getDocumentsUploaded()
        ));

        if (!documentsToRemove.isEmpty()) {
            documentRemovalService.deleteDocumentFromDocumentStore(documentsToRemove);
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(currentCaseData)
            .build();
    }

    private List<ListValue<DivorceDocument>> removeDocumentFromList(final List<ListValue<DivorceDocument>> beforeDocs,
                                                                    final List<ListValue<DivorceDocument>> currentDocs) {

        List<ListValue<DivorceDocument>> documentsToRemove = new ArrayList<>();

        if (beforeDocs != null & currentDocs != null) {
            beforeDocs.forEach(document -> {
                if (!currentDocs.contains(document)) {
                    documentsToRemove.add(document);
                }
            });
        }

        return documentsToRemove;
    }
}
