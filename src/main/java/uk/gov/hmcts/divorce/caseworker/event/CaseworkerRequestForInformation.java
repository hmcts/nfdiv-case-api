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
import uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationJointParties;
import uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationList;
import uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationSoleParties;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.notification.exception.NotificationTemplateException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
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

        List<String> errors = areApplicantsOnline(details.getData());
        errors.addAll(areEmailsValid(details.getData()));
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

        caseData.getRequestForInformationList().getRequestForInformation().setValues(caseData);

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

    private String getErrorString(String errorString, CaseData caseData, Applicant applicant) {
        String error = errorString;
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

    private List<String> getInvalidEmailError(CaseData caseData, Applicant applicant) {
        return Collections.singletonList(getErrorString(NO_VALID_EMAIL_ERROR, caseData, applicant));
    }

    private List<String> getOfflinePartyError(CaseData caseData, Applicant applicant) {
        return Collections.singletonList(getErrorString(NOT_ONLINE_ERROR, caseData, applicant));
    }

    private boolean isApplicantEmailValid(Applicant applicant) {
        if (applicant.isRepresented()) {
            return isNotEmpty(applicant.getSolicitor().getEmail());
        } else {
            return isNotEmpty(applicant.getEmail());
        }
    }

    private List<String> isEmailValid(CaseData caseData, Applicant applicant) {
        return isApplicantEmailValid(applicant) ? new ArrayList<>() : getInvalidEmailError(caseData, applicant);
    }

    private List<String> areBothEmailsValid(CaseData caseData) {
        List<String> errors = new ArrayList<>();
        boolean app1Valid = isApplicantEmailValid(caseData.getApplicant1());
        boolean app2Valid = isApplicantEmailValid(caseData.getApplicant2());
        if (!app1Valid) {
            errors.add(getErrorString(NO_VALID_EMAIL_ERROR, caseData, caseData.getApplicant1()));
        }
        if (!app2Valid) {
            errors.add(getErrorString(NO_VALID_EMAIL_ERROR, caseData, caseData.getApplicant2()));
        }
        return errors;
    }

    private List<String> doesEmailMatchApplicantOrSolicitor(CaseData caseData, String email) {
        final Applicant applicant1 = caseData.getApplicant1();
        final Applicant applicant2 = caseData.getApplicant2();

        List<String> emailAddresses = new ArrayList<>();
        if (null != applicant1.getEmail()) {
            emailAddresses.add(applicant1.getEmail());
        }
        if (applicant1.isRepresented() && null != applicant1.getSolicitor().getEmail()) {
            emailAddresses.add(applicant1.getSolicitor().getEmail());
        }
        if (null != caseData.getApplicant2().getEmail()) {
            emailAddresses.add(caseData.getApplicant2().getEmail());
        }
        if (applicant2.isRepresented() && null != applicant2.getSolicitor().getEmail()) {
            emailAddresses.add(applicant2.getSolicitor().getEmail());
        }

        return !emailAddresses.isEmpty() && emailAddresses.contains(email)
            ? Collections.singletonList(PROVIDED_EMAIL_MUST_NOT_MATCH_EMAIL_ON_CASE_ERROR)
            : new ArrayList<>();
    }

    private List<String> isOtherEmailValid(CaseData caseData, String email) {
        return isNotEmpty(email)
            ? doesEmailMatchApplicantOrSolicitor(caseData, email)
            : Collections.singletonList(NO_VALID_EMAIL_PROVIDED_ERROR);
    }

    private List<String> areEmailsValid(CaseData caseData) {
        RequestForInformation requestForInformation = caseData.getRequestForInformationList().getRequestForInformation();

        RequestForInformationSoleParties soleRecipient = requestForInformation.getRequestForInformationSoleParties();
        RequestForInformationJointParties jointRecipient = requestForInformation.getRequestForInformationJointParties();

        return caseData.getApplicationType().isSole()
            ? switch (soleRecipient) {
            case APPLICANT -> isEmailValid(caseData, caseData.getApplicant1());
            case OTHER -> isOtherEmailValid(caseData, requestForInformation.getRequestForInformationEmailAddress());
        }
            : switch (jointRecipient) {
            case APPLICANT1 -> isEmailValid(caseData, caseData.getApplicant1());
            case APPLICANT2 -> isEmailValid(caseData, caseData.getApplicant2());
            case BOTH -> areBothEmailsValid(caseData);
            case OTHER -> isOtherEmailValid(caseData, requestForInformation.getRequestForInformationEmailAddress());
        };
    }

    private List<String> areBothApplicantsOnline(CaseData caseData) {
        List<String> errors = new ArrayList<>();
        boolean app1Valid = isApplicantFlaggedOnline(caseData.getApplicant1());
        boolean app2Valid = isApplicantFlaggedOnline(caseData.getApplicant2());
        if (!app1Valid) {
            errors.add(getErrorString(NOT_ONLINE_ERROR, caseData, caseData.getApplicant1()));
        }
        if (!app2Valid) {
            errors.add(getErrorString(NOT_ONLINE_ERROR, caseData, caseData.getApplicant2()));
        }
        return errors;
    }

    private boolean isApplicantFlaggedOnline(Applicant applicant) {
        if (applicant.isRepresented()) {
            return applicant.getSolicitor().hasAgreedToReceiveEmails();
        } else {
            return !applicant.isApplicantOffline();
        }
    }

    private List<String> isApplicantOnline(CaseData caseData, Applicant applicant) {
        return isApplicantFlaggedOnline(applicant) ? new ArrayList<>() : getOfflinePartyError(caseData, applicant);
    }

    private List<String> areApplicantsOnline(CaseData caseData) {
        RequestForInformation requestForInformation = caseData.getRequestForInformationList().getRequestForInformation();

        RequestForInformationSoleParties soleRecipient = requestForInformation.getRequestForInformationSoleParties();
        RequestForInformationJointParties jointRecipient = requestForInformation.getRequestForInformationJointParties();

        return caseData.getApplicationType().isSole()
            ? switch (soleRecipient) {
            case APPLICANT -> isApplicantOnline(caseData, caseData.getApplicant1());
            case OTHER -> new ArrayList<>();
        }
            : switch (jointRecipient) {
            case APPLICANT1 -> isApplicantOnline(caseData, caseData.getApplicant1());
            case APPLICANT2 -> isApplicantOnline(caseData, caseData.getApplicant2());
            case BOTH -> areBothApplicantsOnline(caseData);
            case OTHER -> new ArrayList<>();
        };
    }
}
