package uk.gov.hmcts.divorce.caseworker.event;

import lombok.extern.slf4j.Slf4j;
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
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.util.List;

import static uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments.hasDeletedDocuments;
import static uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments.sortByNewest;
import static uk.gov.hmcts.divorce.divorcecase.model.State.POST_SUBMISSION_STATES_WITH_WITHDRAWN_AND_REJECTED;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Component
@Slf4j
public class CaseworkerUploadDocument implements CCDConfig<CaseData, State, UserRole> {
    public static final String CASEWORKER_UPLOAD_DOCUMENT = "caseworker-upload-document";
    public static final String ERROR_REMOVE_DOCUMENTS = "Documents cannot be removed by 'Upload document'. "
        + "Use 'Remove documents' to remove documents.";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_UPLOAD_DOCUMENT)
            .forStates(POST_SUBMISSION_STATES_WITH_WITHDRAWN_AND_REJECTED)
            .name("Upload document")
            .description("Upload document")
            .aboutToSubmitCallback(this::aboutToSubmit)
            .showSummary(false)
            .showEventNotes()
            .grant(CREATE_READ_UPDATE_DELETE, CASE_WORKER)
            .grantHistoryOnly(SOLICITOR, SUPER_USER, LEGAL_ADVISOR, JUDGE))
            .page("uploadDocument")
            .pageLabel("Upload document")
            .complex(CaseData::getDocuments)
                .optional(CaseDocuments::getDocumentsUploaded)
                .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {
        log.info("Callback invoked for {}, Case Id: {}", CASEWORKER_UPLOAD_DOCUMENT, details.getId());

        var caseData = details.getData();
        final var beforeCaseData = beforeDetails.getData();

        final List<ListValue<DivorceDocument>> after = caseData.getDocuments().getDocumentsUploaded();
        final List<ListValue<DivorceDocument>> before = beforeCaseData.getDocuments().getDocumentsUploaded();

        if (hasDeletedDocuments(after, before)) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .errors(List.of(ERROR_REMOVE_DOCUMENTS))
                .build();
        }

        caseData.getDocuments().setDocumentsUploaded(sortByNewest(
            beforeDetails.getData().getDocuments().getDocumentsUploaded(),
            caseData.getDocuments().getDocumentsUploaded()
        ));

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }
}
