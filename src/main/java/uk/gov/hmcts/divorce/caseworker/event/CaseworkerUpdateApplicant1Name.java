package uk.gov.hmcts.divorce.caseworker.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.notification.UpdateApplicant1NameNotification;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.divorce.divorcecase.model.State.POST_SUBMISSION_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Component
@Slf4j
@RequiredArgsConstructor
public class CaseworkerUpdateApplicant1Name implements CCDConfig<CaseData, State, UserRole> {

    private final NotificationDispatcher notificationDispatcher;
    private final UpdateApplicant1NameNotification updateApplicant1NameNotification;

    public static final String CASEWORKER_UPDATE_APP1_NAME = "caseworker-update-app1-name";


    private static final String FIRST_NAME_LABEL = "${%s} first name";
    private static final String LAST_NAME_LABEL = "${%s} last name";
    private static final String APPLICANTS_OR_APPLICANT1S = "labelContentApplicantsOrApplicant1s";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_UPDATE_APP1_NAME)
            .forStates(POST_SUBMISSION_STATES)
            .name("Update App1 name")
            .description("Update App1 name")
            .showSummary()
            .showEventNotes()
            .aboutToStartCallback(this::aboutToStart)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .grant(CREATE_READ_UPDATE_DELETE, CASE_WORKER)
            .grantHistoryOnly(CASE_WORKER))
            .page("updateApp1Name", this::midEvent)
            .pageLabel("Update applicant1 name")
            .complex(CaseData::getApplicant1)
                .mandatoryWithLabel(Applicant::getFirstName, getLabel(FIRST_NAME_LABEL, APPLICANTS_OR_APPLICANT1S))
                .mandatoryWithLabel(Applicant::getLastName, getLabel(LAST_NAME_LABEL, APPLICANTS_OR_APPLICANT1S))
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(final CaseDetails<CaseData, State> details) {
        log.info("{} about to start callback invoked for Case Id: {}", CASEWORKER_UPDATE_APP1_NAME, details.getId());

        final List<String> validationErrors = validateRequiredFirstNameAndLastName(details.getData());
        if (!validationErrors.isEmpty()) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .errors(validationErrors)
                .build();
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .build();
    }

    private List<String> validateRequiredFirstNameAndLastName(CaseData caseData) {
        List<String> errors = new ArrayList<>();
        if (StringUtils.isBlank(caseData.getApplicant1().getFirstName())) {
            errors.add("Applicant first name is not provided to amend their first name");
        }
        if (StringUtils.isBlank(caseData.getApplicant1().getLastName())) {
            errors.add("Applicant last name is not provided  to amend their last name");
        }
        return errors;
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {

        log.info("{} midlevel callback invoked for Case Id: {}", CASEWORKER_UPDATE_APP1_NAME, details.getId());
        CaseData caseData = details.getData();

        log.info("Validating name for Case Id: {}", details.getId());
        final List<String> caseValidationErrors = validateFirstAndLastName(caseData);

        if (!isEmpty(caseValidationErrors)) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .errors(caseValidationErrors)
                .build();
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    private List<String> validateFirstAndLastName(CaseData caseData) {
        List<String> errors = new ArrayList<>();

        if (caseData.getApplicant1().getFirstName().trim().length() <= 3) {
            errors.add("First name must be more than 3 characters long");
        }
        if (caseData.getApplicant1().getLastName().trim().length() <= 3) {
            errors.add("Last name must be more than 3 characters long");
        }

        return errors;
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {
        log.info("{} aboutToSubmit callback invoked for Case Id: {}", CASEWORKER_UPDATE_APP1_NAME, details.getId());

        if (!details.getData().getApplicant1().getFirstName().equals(beforeDetails.getData().getApplicant1().getFirstName())
            || !details.getData().getApplicant1().getLastName().equals(beforeDetails.getData().getApplicant1().getLastName())) {
            log.info("Applicant 1 name updated for Case Id: {}", details.getId());
            notificationDispatcher.send(updateApplicant1NameNotification, details.getData(), details.getId());
        }
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .build();
    }

    private String getLabel(final String label, final Object... value) {
        return String.format(label, value);
    }
}
