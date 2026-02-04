package uk.gov.hmcts.divorce.caseworker.event;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.divorce.divorcecase.model.State.POST_SUBMISSION_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Component
public class CaseworkerUploadAmendedApplication implements CCDConfig<CaseData, State, UserRole> {

    public static final String CASEWORKER_UPLOAD_AMENDED_APPLICATION = "caseworker-upload-amended-application";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_UPLOAD_AMENDED_APPLICATION)
            .forStates(POST_SUBMISSION_STATES)
            .name("Upload amended application")
            .description("Upload amended application")
            .showSummary()
            .showEventNotes()
            .grant(CREATE_READ_UPDATE_DELETE, CASE_WORKER, SUPER_USER))
            .page("uploadAmendedApplication")
            .pageLabel("Upload Amended Application")
            .complex(CaseData::getDocuments)
                .mandatory(CaseDocuments::getAmendedApplications)
                .done();
    }
}
