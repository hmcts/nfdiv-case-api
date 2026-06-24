package uk.gov.hmcts.divorce.caseworker.event;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
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
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Component
public class CaseworkerRemoveGeneralEmails implements CCDConfig<CaseData, State, UserRole> {

    private static final String REMOVE_GENERAL_EMAILS = "Remove general emails";
    public static final String CASEWORKER_REMOVE_GENERAL_EMAILS = "caseworker-remove-general-emails";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_REMOVE_GENERAL_EMAILS)
            .forStates(POST_SUBMISSION_STATES)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .name(REMOVE_GENERAL_EMAILS)
            .description(REMOVE_GENERAL_EMAILS)
            .showEventNotes()
            .grant(CREATE_READ_UPDATE_DELETE, SUPER_USER)
            .grantHistoryOnly(CASE_WORKER))
            .page("removeGeneralEmails")
            .pageLabel(REMOVE_GENERAL_EMAILS)
            .optional(CaseData::getGeneralEmails)
            .optional(CaseData::getConfidentialGeneralEmails)
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {
        final var caseData = details.getData();
        if (CollectionUtils.isEmpty(caseData.getGeneralEmails())) {
            caseData.setGeneralEmails(null);
        }
        if (CollectionUtils.isEmpty(caseData.getConfidentialGeneralEmails())) {
            caseData.setConfidentialGeneralEmails(null);
        }
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }
}
