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
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.*;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Component
@Slf4j
public class CaseworkerUpdateApplicant2Email implements CCDConfig<CaseData, State, UserRole> {

    public static final String CASEWORKER_UPDATE_APP2_EMAIL = "caseworker-update-app2-email";

    @Autowired
    private EmailUpdateService emailUpdateService;

    private static final String EMAIL_LABEL = "${%s} email address";
    private static final String RESPONDENTS_OR_APPLICANT2S = "labelContentRespondentsOrApplicant2s";

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
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(final CaseDetails<CaseData, State> details,
                                                                  final CaseDetails<CaseData, State> detailsBefore) {
        log.info("midEvent callback invoked for {}, Case Id: {}", CASEWORKER_UPDATE_APP2_EMAIL, details.getId());

        CaseData caseData = details.getData();
        CaseData caseDataBefore = detailsBefore.getData();

        if (!validApplicant2ContactDetails(caseDataBefore, caseData)) {

            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .errors(singletonList("Please use the 'Update offline status' event before removing the email address."))
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
        log.info("aboutToSubmit callback invoked for {}, Case Id: {}", CASEWORKER_UPDATE_APP2_EMAIL, details.getId());

        CaseData caseData = details.getData();
        CaseData caseDataBefore = beforeDetails.getData();

        if (!caseData.getApplicant2().isRepresented()) {
            final CaseDetails<CaseData, State> result = emailUpdateService.processUpdateForApplicant2(details);
            String newEmail = caseData.getApplicant2().getEmail();

            if (caseDataBefore.getApplicant2().getEmail() != null
                && !caseDataBefore.getApplicant2().getEmail().isBlank()) {
                emailUpdateService.sendNotificationToOldEmail(beforeDetails, newEmail, false);
            }
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(result.getData())
                .build();
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    private boolean validApplicant2ContactDetails(CaseData caseDataBefore, CaseData caseData) {

        if (caseDataBefore.getApplicant2().getEmail() != null && !caseDataBefore.getApplicant2().getEmail().isBlank()) {
            if (!caseDataBefore.getApplicant2().isRepresented()
                && !caseData.getApplicant2().isApplicantOffline()
                && (caseData.getApplicant2().getEmail() == null || caseData.getApplicant2().getEmail().isBlank())) {
                return false;
            }
        }
        return true;
    }

    private String getLabel(final String label, final Object... value) {
        return String.format(label, value);
    }
}
