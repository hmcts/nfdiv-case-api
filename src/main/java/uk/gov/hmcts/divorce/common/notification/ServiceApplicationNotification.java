package uk.gov.hmcts.divorce.common.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.EmailTemplateName;
import uk.gov.hmcts.divorce.notification.NotificationService;
import uk.gov.hmcts.divorce.notification.exception.NotificationTemplateException;

import java.util.Map;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static uk.gov.hmcts.divorce.citizen.notification.GeneralApplicationReceivedNotification.IS_BAILIFF_SERVICE;
import static uk.gov.hmcts.divorce.citizen.notification.GeneralApplicationReceivedNotification.IS_DEEMED_SERVICE;
import static uk.gov.hmcts.divorce.citizen.notification.GeneralApplicationReceivedNotification.IS_DISPENSE_SERVICE;
import static uk.gov.hmcts.divorce.common.notification.ConditionalOrderPronouncedNotification.MISSING_FIELD_MESSAGE;
import static uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType.BAILIFF;
import static uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType.DEEMED;
import static uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType.DISPENSED;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_JOINT;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_SOLE;
import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SERVICE_APPLICATION_GRANTED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SERVICE_APPLICATION_GRANTED_SOLICITOR;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SERVICE_APPLICATION_REJECTED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SERVICE_APPLICATION_REJECTED_SOLICITOR;

@Component
@Slf4j
public class ServiceApplicationNotification implements ApplicantNotification {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommonContent commonContent;

    private static final String LOGGER_MESSAGE = "Notifying applicant that service application for {} was {}";

    @Override
    public void sendToApplicant1(final CaseData caseData, final Long id) {
        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            getEmailTemplate(caseData, id),
            getApplicantServiceApplicationVars(caseData, id),
            caseData.getApplicant1().getLanguagePreference(),
            id);
    }

    @Override
    public void sendToApplicant1Solicitor(final CaseData caseData, final Long id) {
        notificationService.sendEmail(
            caseData.getApplicant1().getSolicitor().getEmail(),
            getEmailTemplate(caseData, id),
            getSolicitorServiceApplicationVars(caseData, id),
            caseData.getApplicant1().getLanguagePreference(),
            id);
    }

    private Map<String, String> getApplicantServiceApplicationVars(final CaseData caseData, final Long id) {

        Map<String, String> templateVars =
            commonContent.mainTemplateVars(caseData, id, caseData.getApplicant1(), caseData.getApplicant2());

        AlternativeServiceType alternativeServiceType = caseData.getAlternativeService().getAlternativeServiceType();

        templateVars.put(IS_DEEMED_SERVICE, DEEMED.equals(alternativeServiceType) ? YES : NO);
        templateVars.put(IS_DISPENSE_SERVICE, DISPENSED.equals(alternativeServiceType) ? YES : NO);
        templateVars.put(IS_BAILIFF_SERVICE, BAILIFF.equals(alternativeServiceType) ? YES : NO);

        return templateVars;
    }

    private Map<String, String> getSolicitorServiceApplicationVars(final CaseData caseData, final Long id) {

        Map<String, String> templateVars =
            commonContent.solicitorTemplateVarsPreIssue(caseData, id, caseData.getApplicant1());

        AlternativeServiceType alternativeServiceType = caseData.getAlternativeService().getAlternativeServiceType();

        templateVars.put(IS_DEEMED_SERVICE, DEEMED.equals(alternativeServiceType) ? YES : NO);
        templateVars.put(IS_DISPENSE_SERVICE, DISPENSED.equals(alternativeServiceType) ? YES : NO);
        templateVars.put(IS_BAILIFF_SERVICE, BAILIFF.equals(alternativeServiceType) ? YES : NO);
        templateVars.put(IS_SOLE, caseData.getApplicationType().isSole() ? YES : NO);
        templateVars.put(IS_JOINT, !caseData.getApplicationType().isSole() ? YES : NO);

        return templateVars;
    }

    private EmailTemplateName getEmailTemplate(final CaseData caseData, final Long caseId) {

        final AlternativeService alternativeService = caseData.getAlternativeService();
        final Applicant applicant = caseData.getApplicant1();
        if (isNull(alternativeService.getServiceApplicationGranted())) {
            throw new NotificationTemplateException(format(MISSING_FIELD_MESSAGE, "serviceApplicationGranted", caseId));
        }

        final AlternativeServiceType alternativeServiceType = alternativeService.getAlternativeServiceType();
        if (alternativeService.isApplicationGranted()) {
            log.info(LOGGER_MESSAGE, alternativeServiceType == null ? "" : alternativeServiceType.getLabel(), "granted");
            return (applicant.isRepresented()) ? SERVICE_APPLICATION_GRANTED_SOLICITOR : SERVICE_APPLICATION_GRANTED;
        } else {
            log.info(LOGGER_MESSAGE, alternativeServiceType == null ? "" : alternativeServiceType.getLabel(), "rejected");
            return (applicant.isRepresented()) ? SERVICE_APPLICATION_REJECTED_SOLICITOR : SERVICE_APPLICATION_REJECTED;
        }
    }
}
