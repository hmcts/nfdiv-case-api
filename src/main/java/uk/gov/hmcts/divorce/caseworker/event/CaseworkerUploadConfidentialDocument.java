package uk.gov.hmcts.divorce.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.divorce.divorcecase.model.State.POST_SUBMISSION_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.READ;

@Component
@Slf4j
public class CaseworkerUploadConfidentialDocument implements CCDConfig<CaseData, State, UserRole> {
    public static final String CASEWORKER_UPLOAD_CONFIDENTIAL_DOCUMENT = "caseworker-upload-confidential-document";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_UPLOAD_CONFIDENTIAL_DOCUMENT)
            .forStates(POST_SUBMISSION_STATES)
            .name("Upload confidential document")
            .description("Upload confidential document")
            .aboutToSubmitCallback(this::aboutToSubmit)
            .showSummary(false)
            .showEventNotes()
            .grant(CREATE_READ_UPDATE, CASE_WORKER)
            .grant(READ, SUPER_USER, LEGAL_ADVISOR))
            .page("uploadConfidentialDocuments")
            .pageLabel("Upload Confidential Documents")
            .optional(CaseData::getConfidentialDocumentsUploaded);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {
        log.info("Callback invoked for {}", CASEWORKER_UPLOAD_CONFIDENTIAL_DOCUMENT);

        var caseData = details.getData();

        caseData.sortConfidentialDocuments(beforeDetails.getData().getConfidentialDocumentsUploaded());

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }
}
