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
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationSoleParties.APPLICANT;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Applicant2Approved;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingApplicant1Response;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingApplicant2Response;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingDocuments;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingHWFDecision;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingHWFEvidence;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingHWFPartPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingRequestedInformation;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Draft;
import static uk.gov.hmcts.divorce.divorcecase.model.State.InformationRequested;
import static uk.gov.hmcts.divorce.divorcecase.model.State.RequestedInformationSubmitted;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
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
    public static final String USE_CREATE_GENERAL_LETTER_FOR_OFFLINE_PARTIES_ERROR =
        "Please use create general letter event to request information from offline parties.";
    public static final String USE_CREATE_GENERAL_LETTER_FOR_RESPONDENT_ERROR =
        "Please use create general letter event to request information from the respondent";

    public static final String USE_CREATE_GENERAL_EMAIL_FOR_RESPONDENT_ERROR =
        "Please use create general email event to request information from the respondent";
    public static final String USE_CORRECT_PARTY_ERROR = "Please use the correct option to contact online parties.";

    private final RequestForInformationNotification requestForInformationNotification;

    private final NotificationDispatcher notificationDispatcher;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_REQUEST_FOR_INFORMATION)
            .forStates(Draft,
                AwaitingHWFDecision,
                AwaitingHWFEvidence,
                AwaitingHWFPartPayment,
                AwaitingPayment,
                AwaitingDocuments,
                AwaitingApplicant1Response,
                AwaitingApplicant2Response,
                Applicant2Approved,
                InformationRequested,
                AwaitingRequestedInformation,
                RequestedInformationSubmitted,
                Submitted)
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
            errors.add(USE_CREATE_GENERAL_LETTER_FOR_OFFLINE_PARTIES_ERROR);
        }
    }

    private void areBothEmailsValid(CaseData caseData, List<String> errors) {
        boolean applicant1EmailInvalid = isApplicantEmailInvalid(caseData.getApplicant1());
        boolean applicant2EmailInvalid = isApplicantEmailInvalid(caseData.getApplicant2());
        if (applicant1EmailInvalid) {
            errors.add(getErrorString(NO_VALID_EMAIL_ERROR, caseData, caseData.getApplicant1()));
        }
        if (applicant2EmailInvalid) {
            errors.add(getErrorString(NO_VALID_EMAIL_ERROR, caseData, caseData.getApplicant2()));
        }
        if (applicant1EmailInvalid || applicant2EmailInvalid) {
            errors.add(USE_CREATE_GENERAL_LETTER_FOR_OFFLINE_PARTIES_ERROR);
        }
    }

    private void doesEmailMatch(String email, Applicant applicant, CaseData caseData, List<String> errors) {
        final boolean appIsRespondent = SOLE_APPLICATION.equals(caseData.getApplicationType())
            && applicant.equals(caseData.getApplicant2());

        if (null != applicant.getEmail() && email.equals(applicant.getEmail().toLowerCase().trim())) {
            if (appIsRespondent) {
                errors.add(
                    applicant.isApplicantOffline()
                        ? USE_CREATE_GENERAL_LETTER_FOR_RESPONDENT_ERROR + "."
                        : USE_CREATE_GENERAL_EMAIL_FOR_RESPONDENT_ERROR + "."
                );
            } else {
                errors.add(
                    applicant.isApplicantOffline()
                        ? USE_CREATE_GENERAL_LETTER_FOR_OFFLINE_PARTIES_ERROR
                        : USE_CORRECT_PARTY_ERROR
                );
            }
        } else if (applicant.isRepresented()
            && null != applicant.getSolicitor().getEmail()
            && email.equals(applicant.getSolicitor().getEmail().toLowerCase().trim())) {

            if (appIsRespondent) {
                errors.add(
                    applicant.getSolicitor().hasAgreedToReceiveEmails()
                        ? USE_CREATE_GENERAL_EMAIL_FOR_RESPONDENT_ERROR + SOLICITOR + "."
                        : USE_CREATE_GENERAL_LETTER_FOR_RESPONDENT_ERROR + SOLICITOR + "."
                );
            } else {
                errors.add(
                    applicant.getSolicitor().hasAgreedToReceiveEmails()
                        ? USE_CORRECT_PARTY_ERROR
                        : USE_CREATE_GENERAL_LETTER_FOR_OFFLINE_PARTIES_ERROR
                );
            }
        }
    }

    private void doesEmailMatchApplicantOrSolicitor(CaseData caseData, String email, List<String> errors) {
        final int errorSize = errors.size();
        doesEmailMatch(email, caseData.getApplicant1(), caseData, errors);
        if (errors.size() == errorSize) {
            doesEmailMatch(email, caseData.getApplicant2(), caseData, errors);
        }
    }

    private void isOtherEmailValid(CaseData caseData, String email, List<String> errors) {
        if (null == email) {
            errors.add(NO_VALID_EMAIL_PROVIDED_ERROR);
        } else {
            String cleanEmail = email.toLowerCase().trim();
            if (isEmpty(cleanEmail) || cleanEmail.length() < 6) { //shortest valid web email address = ?@?.??
                errors.add(NO_VALID_EMAIL_PROVIDED_ERROR);
            } else {
                doesEmailMatchApplicantOrSolicitor(caseData, cleanEmail, errors);
            }
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
            errors.add(USE_CREATE_GENERAL_LETTER_FOR_OFFLINE_PARTIES_ERROR);
        }
    }

    private void areBothApplicantsOnline(CaseData caseData, List<String> errors) {
        boolean applicant1Offline = isApplicantFlaggedOffline(caseData.getApplicant1());
        boolean applicant2Offline = isApplicantFlaggedOffline(caseData.getApplicant2());
        if (applicant1Offline) {
            errors.add(getErrorString(NOT_ONLINE_ERROR, caseData, caseData.getApplicant1()));
        }
        if (applicant2Offline) {
            errors.add(getErrorString(NOT_ONLINE_ERROR, caseData, caseData.getApplicant2()));
        }
        if (applicant1Offline || applicant2Offline) {
            errors.add(USE_CREATE_GENERAL_LETTER_FOR_OFFLINE_PARTIES_ERROR);
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
