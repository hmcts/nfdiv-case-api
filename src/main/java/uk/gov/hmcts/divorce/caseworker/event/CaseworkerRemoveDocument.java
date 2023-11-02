package uk.gov.hmcts.divorce.caseworker.event;

import org.apache.commons.io.FilenameUtils;
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
import uk.gov.hmcts.divorce.document.DocumentManagementClient;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.divorce.divorcecase.model.State.POST_SUBMISSION_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Component
public class CaseworkerRemoveDocument implements CCDConfig<CaseData, State, UserRole> {

    @Autowired
    private DocumentManagementClient documentManagementClient;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private IdamService idamService;

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

        var beforeApplicant1DocumentsUploaded = beforeCaseData.getDocuments().getApplicant1DocumentsUploaded();
        var currentApplicant1DocumentsUploaded = currentCaseData.getDocuments().getApplicant1DocumentsUploaded();
        if (beforeApplicant1DocumentsUploaded != null & currentApplicant1DocumentsUploaded != null) {
            beforeApplicant1DocumentsUploaded.forEach(document -> {
                if (!currentApplicant1DocumentsUploaded.contains(document)) {
                    documentsToRemove.add(document);
                }
            });
        }

        var beforeDocumentsGenerated = beforeCaseData.getDocuments().getDocumentsGenerated();
        var currentDocumentsGenerated = currentCaseData.getDocuments().getDocumentsGenerated();
        if (beforeDocumentsGenerated != null & currentDocumentsGenerated != null) {
            beforeDocumentsGenerated.forEach(document -> {
                if (!currentDocumentsGenerated.contains(document)) {
                    documentsToRemove.add(document);
                }
            });
        }

        var beforeDocumentsUploaded = beforeCaseData.getDocuments().getDocumentsUploaded();
        var currentDocumentsUploaded = currentCaseData.getDocuments().getDocumentsUploaded();
        if (beforeDocumentsUploaded != null & currentDocumentsUploaded != null) {
            beforeDocumentsUploaded.forEach(document -> {
                if (!currentDocumentsUploaded.contains(document)) {
                    documentsToRemove.add(document);
                }
            });
        }

        if (!documentsToRemove.isEmpty()) {
            final User systemUser = idamService.retrieveSystemUpdateUserDetails();
            final UserDetails userDetails = systemUser.getUserDetails();
            final String rolesCsv = String.join(",", userDetails.getRoles());

            documentsToRemove.forEach(document -> {
                documentManagementClient.deleteDocument(
                    systemUser.getAuthToken(),
                    authTokenGenerator.generate(),
                    rolesCsv,
                    userDetails.getId(),
                    FilenameUtils.getName(document.getValue().getDocumentLink().getUrl()),
                    true
                );
            });
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(currentCaseData)
            .build();
    }
}
