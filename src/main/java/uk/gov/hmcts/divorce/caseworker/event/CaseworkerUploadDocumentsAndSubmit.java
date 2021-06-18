package uk.gov.hmcts.divorce.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.State;
import uk.gov.hmcts.divorce.common.model.UserRole;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import static uk.gov.hmcts.divorce.common.model.State.AwaitingDocuments;
import static uk.gov.hmcts.divorce.common.model.State.Submitted;
import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_COURTADMIN_CTSC;
import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_COURTADMIN_RDU;
import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_SUPERUSER;
import static uk.gov.hmcts.divorce.common.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.common.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.common.model.access.Permissions.READ;

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
            .displayOrder(1)
            .aboutToStartCallback(this::aboutToStart)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .explicitGrants()
            .grant(CREATE_READ_UPDATE, CASEWORKER_COURTADMIN_CTSC, CASEWORKER_COURTADMIN_RDU)
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
            .optional(CaseData::getDocumentsUploaded)
            .mandatory(CaseData::getDocumentUploadComplete);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(final CaseDetails<CaseData, State> details) {

        log.info("Caseworker upload documents and submit about to start callback invoked");

        final CaseData caseData = details.getData();

        caseData.setDocumentUploadComplete(null);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {

        log.info("Caseworker upload documentsand submit about to submit callback invoked");

        final CaseData caseData = details.getData();

        allowCaseToBeSubmitted(caseData);

        if (caseData.getDocumentUploadComplete().toBoolean()) {
            return transitionToSubmitted(details, caseData);
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(details.getState())
            .build();
    }

    private AboutToStartOrSubmitResponse<CaseData, State> transitionToSubmitted(final CaseDetails<CaseData, State> details,
                                                                                final CaseData caseData) {

        final List<String> submittedErrors = Submitted.validate(caseData);

        final State state;
        if (submittedErrors.isEmpty()) {
            caseData.setDateSubmitted(LocalDateTime.now(clock));
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

    private CaseData allowCaseToBeSubmitted(final CaseData caseData) {
        caseData.setApplicant1WantsToHavePapersServedAnotherWay(null);
        caseData.setCannotUploadSupportingDocument(null);

        return caseData;
    }
}
