package uk.gov.hmcts.divorce.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.caseworker.service.EmailUpdateService;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static java.util.Collections.singletonList;
import static uk.gov.hmcts.divorce.divorcecase.model.State.POST_SUBMISSION_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Component
@Slf4j
public class CaseworkerUpdateApplicant2Email implements CCDConfig<CaseData, State, UserRole> {

    public static final String CASEWORKER_UPDATE_APP2_EMAIL = "caseworker-update-app2-email";

    @Autowired
    private EmailUpdateService emailUpdateService;

    private static final String EMAIL_LABEL = "${%s} email address";
    private static final String RESPONDENTS_OR_APPLICANT2S = "labelContentRespondentsOrApplicant2s";
    private static final String NEVER_SHOW = "applicant2Email=\"NEVER_SHOW\"";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_UPDATE_APP2_EMAIL)
            .forStates(POST_SUBMISSION_STATES)
            .name("Update applicant2 email")
            .description("Update respondent/applicant2 email")
            .aboutToSubmitCallback(this::aboutToSubmit)
            .showSummary()
            .showEventNotes()
            .grant(CREATE_READ_UPDATE_DELETE, SUPER_USER, CASE_WORKER)
            .grantHistoryOnly(
                SOLICITOR,
                LEGAL_ADVISOR))
            .page("updateApp2Email")
            .pageLabel("Update respondent/applicant2 email")
            .complex(CaseData::getApplicant2)
                .optionalWithLabel(Applicant::getEmail, getLabel(EMAIL_LABEL, RESPONDENTS_OR_APPLICANT2S))
                .readonlyNoSummary(Applicant::getOffline,NEVER_SHOW)
                .label("willNotReceiveInvite", getInviteNotSentLabel(),"applicant2Offline = \"Yes\"")
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(final CaseDetails<CaseData, State> details,
                                                                  final CaseDetails<CaseData, State> detailsBefore) {
        log.info("midEvent callback invoked for {}, Case Id: {}", CASEWORKER_UPDATE_APP2_EMAIL, details.getId());

        CaseData caseData = details.getData();
        CaseData caseDataBefore = detailsBefore.getData();

        if (!validApplicant2Update(caseDataBefore, caseData)) {

            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .errors(singletonList("You cannot leave the email field blank. "
                    + "You can only use this event to update the email of the party."))
                .build();
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {
        log.info("{} aboutToSubmit callback invoked for Case Id: {}", CASEWORKER_UPDATE_APP2_EMAIL, details.getId());

        final CaseDetails<CaseData, State> result = emailUpdateService.processEmailUpdate(details, beforeDetails, false);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(result.getData())
            .build();
    }

    private boolean validApplicant2Update(CaseData caseDataBefore, CaseData caseData) {

        if (caseDataBefore.getApplicant2().getEmail() != null && !caseDataBefore.getApplicant2().getEmail().isBlank()
            && (caseData.getApplicant2().getEmail() == null || caseData.getApplicant2().getEmail().isBlank())) {
            return false;
        }
        return true;
    }

    private String getLabel(final String label, final Object... value) {
        return String.format(label, value);
    }

    private String getInviteNotSentLabel() {
        return "*The party is offline. You can update their email but they will not be invited to the case. "
            + "Please use Notice of Change to invite them to gain access to the case online.*";
    }
}
