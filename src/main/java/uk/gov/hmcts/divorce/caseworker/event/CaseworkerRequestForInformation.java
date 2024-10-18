package uk.gov.hmcts.divorce.caseworker.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.caseworker.service.notification.RequestForInformationNotification;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.RequestForInformation;
import uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationList;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.notification.exception.NotificationTemplateException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationSoleParties.APPLICANT;
import static uk.gov.hmcts.divorce.divorcecase.model.State.InformationRequested;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Slf4j
@RequiredArgsConstructor
@Component
public class CaseworkerRequestForInformation implements CCDConfig<CaseData, State, UserRole> {

    public static final String CASEWORKER_REQUEST_FOR_INFORMATION = "caseworker-request-for-information";
    public static final String REQUEST_FOR_INFORMATION_NOTIFICATION_FAILED_ERROR
        = "Unable to send Request for Information Notification for Case Id: ";
    public static final String NO_VALID_EMAIL_ERROR = "You cannot send an email because no email address has been provided for: ";
    public static final String NOT_ONLINE_ERROR = "You cannot send an email because the following party is offline: ";
    public static final String THE_APPLICANT = "the Applicant";
    public static final String APPLICANT_1 = "Applicant 1";
    public static final String APPLICANT_2 = "Applicant 2";
    public static final String SOLICITOR = "'s Solicitor";
    public static final String NO_VALID_EMAIL_PROVIDED_ERROR = "You must provide a valid email address.";
    public static final String PROVIDED_EMAIL_MUST_NOT_MATCH_EMAIL_ON_CASE_ERROR =
        "Please use create general letter event to request information from offline parties.";

    private final RequestForInformationNotification requestForInformationNotification;

    private final NotificationDispatcher notificationDispatcher;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_REQUEST_FOR_INFORMATION)
            .forAllStates()
            .name("Request For Information")
            .description("Request for information")
            .showSummary()
            .showEventNotes()
            .endButtonLabel("Submit")
            .aboutToSubmitCallback(this::aboutToSubmit)
            .grant(CREATE_READ_UPDATE, CASE_WORKER)
            .grantHistoryOnly(SUPER_USER, LEGAL_ADVISOR, JUDGE))
            .page("requestForInformation", this::midEvent)
            .pageLabel("Request For Information")
            .readonlyNoSummary(CaseData::getApplicationType, "requestForInformationSoleParties=\"NEVER_SHOW\"")
            .complex(CaseData::getRequestForInformationList)
                .complex(RequestForInformationList::getRequestForInformation)
                    .mandatory(RequestForInformation::getRequestForInformationSoleParties, "applicationType=\"soleApplication\"")
                    .mandatory(RequestForInformation::getRequestForInformationJointParties, "applicationType=\"jointApplication\"")
                    .mandatory(RequestForInformation::getRequestForInformationName, "requestForInformationSoleParties=\"other\" "
                        + "OR requestForInformationJointParties=\"other\"")
                    .mandatory(RequestForInformation::getRequestForInformationEmailAddress, "requestForInformationSoleParties=\"other\" "
                        + "OR requestForInformationJointParties=\"other\"")
                    .mandatory(RequestForInformation::getRequestForInformationDetails)
                .done()
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {

        log.info("{} midEvent callback invoked for Case Id: {}", CASEWORKER_REQUEST_FOR_INFORMATION, details.getId());

        List<String> errors = new ArrayList<>();
        areApplicantsOnline(details.getData(), errors);
        areEmailsValid(details.getData(), errors);
        if (!errors.isEmpty()) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .errors(errors)
                .build();
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {

        log.info("{} about to submit callback invoked for Case Id: {}", CASEWORKER_REQUEST_FOR_INFORMATION, details.getId());

        final CaseData caseData = details.getData();
        final RequestForInformation requestForInformation = caseData.getRequestForInformationList().getRequestForInformation();

        requestForInformation.setValues(caseData);

        caseData.getRequestForInformationList().addRequestToList(requestForInformation);

        try {
            notificationDispatcher.sendRequestForInformationNotification(
                requestForInformationNotification,
                caseData,
                details.getId()
            );
        } catch (final NotificationTemplateException e) {
            log.error("Request for Information Notification for Case Id {} failed with message: {}", details.getId(), e.getMessage(), e);
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .errors(Collections.singletonList(REQUEST_FOR_INFORMATION_NOTIFICATION_FAILED_ERROR + details.getId()))
                .build();
        }

        //Prevent pre-populating fields for new request
        caseData.getRequestForInformationList().setRequestForInformation(new RequestForInformation());

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(InformationRequested)
            .build();
    }

    private String getErrorString(String errorStart, CaseData caseData, Applicant applicant) {
        String error = errorStart;
        if (caseData.getApplicationType().isSole()) {
            error += THE_APPLICANT;
        } else {
            error += caseData.getApplicant1().equals(applicant) ? APPLICANT_1 : APPLICANT_2;
        }
        if (applicant.isRepresented()) {
            error += SOLICITOR;
        }
        return error;
    }

    private boolean isApplicantEmailInvalid(Applicant applicant) {
        return applicant.isRepresented()
            ? isEmpty(applicant.getSolicitor().getEmail())
            : isEmpty(applicant.getEmail());
    }

    private void isEmailValid(CaseData caseData, Applicant applicant, List<String> errors) {
        if (isApplicantEmailInvalid(applicant)) {
            errors.add(getErrorString(NO_VALID_EMAIL_ERROR, caseData, applicant));
        }
    }

    private void areBothEmailsValid(CaseData caseData, List<String> errors) {
        if (isApplicantEmailInvalid(caseData.getApplicant1())) {
            errors.add(getErrorString(NO_VALID_EMAIL_ERROR, caseData, caseData.getApplicant1()));
        }
        if (isApplicantEmailInvalid(caseData.getApplicant2())) {
            errors.add(getErrorString(NO_VALID_EMAIL_ERROR, caseData, caseData.getApplicant2()));
        }
    }

    private void addEmailToList(String email, List<String> emailAddresses) {
        if (null != email) {
            emailAddresses.add(email);
        }
    }

    private boolean doesEmailMatchApplicantOrSolicitor(CaseData caseData, String email) {
        final Applicant applicant1 = caseData.getApplicant1();
        final Applicant applicant2 = caseData.getApplicant2();

        List<String> emailAddresses = new ArrayList<>();
        addEmailToList(applicant1.getEmail(), emailAddresses);
        if (applicant1.isRepresented()) {
            addEmailToList(applicant1.getSolicitor().getEmail(), emailAddresses);
        }
        addEmailToList(applicant2.getEmail(), emailAddresses);
        if (applicant2.isRepresented()) {
            addEmailToList(applicant2.getSolicitor().getEmail(), emailAddresses);
        }

        return !emailAddresses.isEmpty() && emailAddresses.contains(email);
    }

    private void isOtherEmailValid(CaseData caseData, String email, List<String> errors) {
        if (isEmpty(email)) {
            errors.add(NO_VALID_EMAIL_PROVIDED_ERROR);
        } else if (doesEmailMatchApplicantOrSolicitor(caseData, email)) {
            errors.add(PROVIDED_EMAIL_MUST_NOT_MATCH_EMAIL_ON_CASE_ERROR);
        }
    }

    private void areEmailsValid(CaseData caseData, List<String> errors) {
        RequestForInformation requestForInformation = caseData.getRequestForInformationList().getRequestForInformation();

        if (caseData.getApplicationType().isSole()) {
            switch (requestForInformation.getRequestForInformationSoleParties()) {
                case APPLICANT -> isEmailValid(caseData, caseData.getApplicant1(), errors);
                case OTHER -> isOtherEmailValid(caseData, requestForInformation.getRequestForInformationEmailAddress(), errors);
                default -> { }
            }
        } else {
            switch (requestForInformation.getRequestForInformationJointParties()) {
                case APPLICANT1 -> isEmailValid(caseData, caseData.getApplicant1(), errors);
                case APPLICANT2 -> isEmailValid(caseData, caseData.getApplicant2(), errors);
                case BOTH -> areBothEmailsValid(caseData, errors);
                case OTHER -> isOtherEmailValid(caseData, requestForInformation.getRequestForInformationEmailAddress(), errors);
                default -> { }
            }
        }
    }

    private boolean isApplicantFlaggedOffline(Applicant applicant) {
        return applicant.isRepresented()
            ? !applicant.getSolicitor().hasAgreedToReceiveEmails()
            : applicant.isApplicantOffline();
    }

    private void isApplicantOnline(CaseData caseData, Applicant applicant, List<String> errors) {
        if (isApplicantFlaggedOffline(applicant)) {
            errors.add(getErrorString(NOT_ONLINE_ERROR, caseData, applicant));
        }
    }

    private void areBothApplicantsOnline(CaseData caseData, List<String> errors) {
        if (isApplicantFlaggedOffline(caseData.getApplicant1())) {
            errors.add(getErrorString(NOT_ONLINE_ERROR, caseData, caseData.getApplicant1()));
        }
        if (isApplicantFlaggedOffline(caseData.getApplicant2())) {
            errors.add(getErrorString(NOT_ONLINE_ERROR, caseData, caseData.getApplicant2()));
        }
    }

    private void areApplicantsOnline(CaseData caseData, List<String> errors) {
        RequestForInformation requestForInformation = caseData.getRequestForInformationList().getRequestForInformation();

        if (caseData.getApplicationType().isSole()) {
            if (APPLICANT.equals(requestForInformation.getRequestForInformationSoleParties())) {
                isApplicantOnline(caseData, caseData.getApplicant1(), errors);
            }
        } else {
            switch (requestForInformation.getRequestForInformationJointParties()) {
                case APPLICANT1 -> isApplicantOnline(caseData, caseData.getApplicant1(), errors);
                case APPLICANT2 -> isApplicantOnline(caseData, caseData.getApplicant2(), errors);
                case BOTH -> areBothApplicantsOnline(caseData, errors);
                default -> { }
            }
        }
    }
}
