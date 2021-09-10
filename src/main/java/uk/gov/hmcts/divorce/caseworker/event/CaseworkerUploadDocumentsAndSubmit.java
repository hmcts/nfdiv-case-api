package uk.gov.hmcts.divorce.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingDocuments;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_COURTADMIN;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_SUPERUSER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.READ;
import static uk.gov.hmcts.divorce.divorcecase.validation.ApplicationValidation.validateSubmission;

@Slf4j
@Component
public class CaseworkerUploadDocumentsAndSubmit implements CCDConfig<CaseData, State, UserRole> {

    public static final String CASEWORKER_UPLOAD_DOCUMENTS_AND_SUBMIT = "caseworker-upload-documents-and-submit";

    @Autowired
    private Clock clock;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_UPLOAD_DOCUMENTS_AND_SUBMIT)
            .forState(AwaitingDocuments)
            .name("Upload documents and submit")
            .description("Upload documents from the applicant and submit")
            .aboutToStartCallback(this::aboutToStart)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .explicitGrants()
            .grant(CREATE_READ_UPDATE, CASEWORKER_COURTADMIN)
            .grant(READ, SOLICITOR, CASEWORKER_SUPERUSER, CASEWORKER_LEGAL_ADVISOR))
            .page("caseworkerUploadDocuments")
            .pageLabel("Upload the documents")
            .label(
                "LabelUploadDocumentsPara-1",
                "You need to upload a digital photo or scan of the marriage certificate.\n\n"
                    + "You can also upload other documents that you need to send to the court, e.g.\n"
                    + "- Certified translation of a non-English marriage certificate\n"
                    + "- Change of name deed\n\n"
                    + "The image must be of the entire document and has to be readable by court staff. "
                    + "You can upload image files with jpg, jpeg, bmp, tif, tiff or PDF file extensions, maximum size 100MB per file")
            .optional(CaseData::getApplicant1DocumentsUploaded)
            .complex(CaseData::getApplication)
                .mandatory(Application::getDocumentUploadComplete)
                .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(final CaseDetails<CaseData, State> details) {

        log.info("Caseworker upload documents and submit about to start callback invoked");

        final CaseData caseData = details.getData();

        caseData.getApplication().setDocumentUploadComplete(null);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {

        log.info("Caseworker upload documents and submit about to submit callback invoked");

        final CaseData caseData = details.getData();
        final Application application = caseData.getApplication();

        allowCaseToBeSubmitted(application);

        //sort app1 documents in descending order so latest documents appears first
        caseData.sortApplicant1UploadedDocuments(beforeDetails.getData().getApplicant1DocumentsUploaded());

        if (application.getDocumentUploadComplete().toBoolean()) {
            return transitionToSubmitted(details, caseData);
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(details.getState())
            .build();
    }

    private AboutToStartOrSubmitResponse<CaseData, State> transitionToSubmitted(final CaseDetails<CaseData, State> details,
                                                                                final CaseData caseData) {

        final List<String> submittedErrors = validateSubmission(caseData.getApplication());

        final State state;
        if (submittedErrors.isEmpty()) {
            caseData.getApplication().setDateSubmitted(LocalDateTime.now(clock));
            state = Submitted;
        } else {
            state = details.getState();
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(state)
            .errors(submittedErrors)
            .build();
    }

    private void allowCaseToBeSubmitted(final Application application) {
        application.setApplicant1WantsToHavePapersServedAnotherWay(null);
        application.setApplicant1CannotUploadSupportingDocument(null);
    }
}
