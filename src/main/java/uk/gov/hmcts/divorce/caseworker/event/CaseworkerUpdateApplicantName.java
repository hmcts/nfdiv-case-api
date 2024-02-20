package uk.gov.hmcts.divorce.caseworker.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.caseworker.event.page.UpdateApplicantName;
import uk.gov.hmcts.divorce.caseworker.service.notification.Applicant1NameChangeNotification;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.notification.NotificationService;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.divorce.caseworker.event.page.UpdateApplicantName.TITLE;
import static uk.gov.hmcts.divorce.divorcecase.model.State.POST_SUBMISSION_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.*;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.APPLICATION_SUBMITTED;

@Component
@RequiredArgsConstructor
@Slf4j
public class CaseworkerUpdateApplicantName implements CCDConfig<CaseData, State, UserRole> {
    public static final String CASEWORKER_UPDATE_APPLICANT_NAME = "caseworker-update-applicant-name";
    @Autowired
    private UpdateApplicantName updateApplicantName;
    @Autowired
    private NotificationDispatcher notificationDispatcher;
    @Autowired
    private Applicant1NameChangeNotification applicant1NameChangeNotification;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        var pageBuilder = addEventConfig(configBuilder);
        updateApplicantName.addTo(pageBuilder);
    }

    private PageBuilder addEventConfig(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        return new PageBuilder(configBuilder
            .event(CASEWORKER_UPDATE_APPLICANT_NAME)
            .forStates(POST_SUBMISSION_STATES)
            .name(TITLE)
            .description(TITLE)
            .aboutToStartCallback(this::aboutToStart)
            .submittedCallback(this::submitted)
            .showSummary()
            .showEventNotes()
            .grant(CREATE_READ_UPDATE, CASE_WORKER)
            .grantHistoryOnly(
                SUPER_USER,
                LEGAL_ADVISOR));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(final CaseDetails<CaseData, State> details) {
        log.info("Start callback invoked for Case Id: {}", details.getId());

        final List<String> errors = validateNameExists(details);
        if (!errors.isEmpty()) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(details.getData())
                .errors(errors)
                .build();
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .build();
    }

    public SubmittedCallbackResponse submitted(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails) {

        log.info("Submitted callback invoked for case id {} ", details.getId());
        notificationDispatcher.send(applicant1NameChangeNotification, details.getData(), details.getId());

        return SubmittedCallbackResponse.builder().build();
    }

    protected List<String> validateNameExists(final CaseDetails<CaseData, State> details) {
        List<String> errors = new ArrayList<>();
        var casedata = details.getData();
        var applicant = casedata.getApplicant1();

        if (null == applicant.getFirstName() && null == applicant.getLastName()) {
            errors.add("Applicant Name does not exist");
        }

        return errors;
    }
}
