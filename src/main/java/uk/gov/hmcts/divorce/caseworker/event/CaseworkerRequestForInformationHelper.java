package uk.gov.hmcts.divorce.caseworker.event;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.RequestForInformation;
import uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationJointParties;
import uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationSoleParties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.lang.Boolean.TRUE;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Component
public class CaseworkerRequestForInformationHelper {

    public static final String NO_VALID_EMAIL_ERROR
        = "You cannot send an email because no email address has been provided for ";
    public static final String THE_APPLICANT = "the Applicant";
    public static final String THIS_PARTY = "this party.";
    public static final String APPLICANT_1 = "Applicant 1";
    public static final String APPLICANT_2 = "Applicant 2";
    public static final String SOLICITOR = "'s Solicitor.";
    public static final String FULL_STOP = ".";

    private void setBothValues(CaseData caseData) {
        setValues(caseData, caseData.getApplicant1(), false);
        setValues(caseData, caseData.getApplicant2(), true);
    }

    private void setValues(CaseData caseData, Applicant applicant) {
        setValues(caseData, applicant, false);
    }

    private void setValues(CaseData caseData, Applicant applicant, Boolean setSecondary) {
        final boolean isRepresented = applicant.isRepresented();
        final RequestForInformation requestForInformation = caseData.getRequestForInformationList().getRequestForInformation();
        final String emailAddress = isRepresented ? applicant.getSolicitor().getEmail() : applicant.getEmail();
        final String name = isRepresented ? applicant.getSolicitor().getName() : applicant.getFullName();
        if (TRUE.equals(setSecondary)) {
            requestForInformation.setRequestForInformationSecondaryEmailAddress(emailAddress);
            requestForInformation.setRequestForInformationSecondaryName(name);
        } else {
            requestForInformation.setRequestForInformationEmailAddress(emailAddress);
            requestForInformation.setRequestForInformationName(name);
        }
    }

    private void addRequestToList(CaseData caseData) {
        final ListValue<RequestForInformation> request = new ListValue<>();
        request.setValue(caseData.getRequestForInformationList().getRequestForInformation());

        if (isEmpty(caseData.getRequestForInformationList().getRequestsForInformation())) {
            List<ListValue<RequestForInformation>> requests = new ArrayList<>();
            requests.add(request);
            caseData.getRequestForInformationList().setRequestsForInformation(requests);
        } else {
            caseData.getRequestForInformationList().getRequestsForInformation().add(request);
        }
    }

    public CaseData setParties(CaseData caseData) {
        final RequestForInformation requestForInformation = caseData.getRequestForInformationList().getRequestForInformation();
        final RequestForInformationSoleParties soleAddressToOption = requestForInformation.getRequestForInformationSoleParties();
        final RequestForInformationJointParties jointAddressToOption = requestForInformation.getRequestForInformationJointParties();
        if (RequestForInformationSoleParties.APPLICANT.equals(soleAddressToOption)
            || RequestForInformationJointParties.APPLICANT1.equals(jointAddressToOption)) {
            setValues(caseData, caseData.getApplicant1());
        } else if (RequestForInformationJointParties.APPLICANT2.equals(jointAddressToOption)) {
            setValues(caseData, caseData.getApplicant2());
        } else if (RequestForInformationJointParties.BOTH.equals(jointAddressToOption)) {
            setBothValues(caseData);
        }

        addRequestToList(caseData);

        caseData.getRequestForInformationList().setRequestForInformation(new RequestForInformation()); //Prevent pre-pop next event run

        return caseData;
    }

    private String getValidationErrorString(CaseData caseData, Applicant applicant) {
        String error = NO_VALID_EMAIL_ERROR;
        if (caseData.getApplicationType().isSole()) {
            error += THE_APPLICANT;
        } else {
            if (applicant.equals(caseData.getApplicant1())) {
                error += APPLICANT_1;
            } else {
                error += APPLICANT_2;
            }
        }
        return error + (applicant.isRepresented() ? SOLICITOR : FULL_STOP);
    }

    private List<String> getValidationError(CaseData caseData, Applicant applicant) {
        return Collections.singletonList(getValidationErrorString(caseData, applicant));
    }

    private boolean isApplicantEmailValid(Applicant applicant) {
        if (applicant.isRepresented()) {
            return isNotEmpty(applicant.getSolicitor().getEmail());
        } else {
            return isNotEmpty(applicant.getEmail());
        }
    }

    private List<String> isEmailValid(CaseData caseData, Applicant applicant) {
        return isApplicantEmailValid(applicant) ? new ArrayList<>() : getValidationError(caseData, applicant);
    }

    private List<String> areBothEmailsValid(CaseData caseData) {
        List<String> errors = new ArrayList<>();
        boolean app1Valid = isApplicantEmailValid(caseData.getApplicant1());
        boolean app2Valid = isApplicantEmailValid(caseData.getApplicant2());
        if (!app1Valid) {
            errors.add(getValidationErrorString(caseData, caseData.getApplicant1()));
        }
        if (!app2Valid) {
            errors.add(getValidationErrorString(caseData, caseData.getApplicant2()));
        }
        return errors;
    }

    private List<String> isOtherEmailValid(String email) {
        return isNotEmpty(email) ? new ArrayList<>() : Collections.singletonList(NO_VALID_EMAIL_ERROR + THIS_PARTY);
    }

    public List<String> areEmailsValid(CaseData caseData) {
        RequestForInformation requestForInformation = caseData.getRequestForInformationList().getRequestForInformation();

        RequestForInformationSoleParties soleRecipient = requestForInformation.getRequestForInformationSoleParties();
        RequestForInformationJointParties jointRecipient = requestForInformation.getRequestForInformationJointParties();

        return caseData.getApplicationType().isSole()
            ? switch (soleRecipient) {
            case APPLICANT -> isEmailValid(caseData, caseData.getApplicant1());
            case OTHER -> isOtherEmailValid(requestForInformation.getRequestForInformationEmailAddress());
        }
            : switch (jointRecipient) {
            case APPLICANT1 -> isEmailValid(caseData, caseData.getApplicant1());
            case APPLICANT2 -> isEmailValid(caseData, caseData.getApplicant2());
            case BOTH -> areBothEmailsValid(caseData);
            case OTHER -> isOtherEmailValid(requestForInformation.getRequestForInformationEmailAddress());
        };
    }
}
