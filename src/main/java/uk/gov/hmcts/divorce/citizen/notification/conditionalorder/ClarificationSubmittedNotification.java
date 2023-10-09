package uk.gov.hmcts.divorce.citizen.notification.conditionalorder;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.EmailTemplateName;
import uk.gov.hmcts.divorce.notification.NotificationService;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Map;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.divorce.notification.CommonContent.CO_SUBMISSION_DATE_PLUS_DAYS;
import static uk.gov.hmcts.divorce.notification.CommonContent.PRONOUNCE_BY_DATE;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.CITIZEN_CLARIFICATION_SUBMITTED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.CITIZEN_PARTNER_CLARIFICATION_SUBMITTED;
import static uk.gov.hmcts.divorce.notification.FormatUtil.getDateTimeFormatterForPreferredLanguage;

@Component
@Slf4j
public class ClarificationSubmittedNotification implements ApplicantNotification {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommonContent commonContent;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private CcdAccessService ccdAccessService;

    @Autowired
    private Clock clock;

    @Override
    public void sendToApplicant1(CaseData caseData, Long caseId) {

        if (caseData.getApplicationType().isSole()) {
            log.info("Notifying applicant 1 of the next steps after they have submitted clarification: {}", caseId);
            notificationService.sendEmail(
                caseData.getApplicant1().getEmail(),
                CITIZEN_CLARIFICATION_SUBMITTED,
                getTemplateContent(caseData, caseId, caseData.getApplicant1(), caseData.getApplicant2()),
                caseData.getApplicant1().getLanguagePreference(),
                caseId
            );
        } else {
            EmailTemplateName templateId;
            if (isApplicant1(caseId)) {
                templateId = CITIZEN_CLARIFICATION_SUBMITTED;
                log.info("Notifying applicant 1 of the next steps after they have submitted clarification: {}", caseId);
            } else {
                templateId = CITIZEN_PARTNER_CLARIFICATION_SUBMITTED;
                log.info("Notifying applicant 1 that their partner has submitted clarification: {}", caseId);
            }

            notificationService.sendEmail(
                caseData.getApplicant1().getEmail(),
                templateId,
                getTemplateContent(caseData, caseId, caseData.getApplicant1(), caseData.getApplicant2()),
                caseData.getApplicant1().getLanguagePreference(),
                caseId
            );
        }
    }

    @Override
    public void sendToApplicant2(CaseData caseData, Long caseId) {

        if (!caseData.getApplicationType().isSole()) {

            EmailTemplateName templateId;
            if (isApplicant1(caseId)) {
                templateId = CITIZEN_PARTNER_CLARIFICATION_SUBMITTED;
                log.info("Notifying applicant 2 that their partner has submitted clarification: {}", caseId);
            } else {
                templateId = CITIZEN_CLARIFICATION_SUBMITTED;
                log.info("Notifying applicant 2 of the next steps after they have submitted clarification: {}", caseId);
            }

            notificationService.sendEmail(
                caseData.getApplicant2().getEmail(),
                templateId,
                getTemplateContent(caseData, caseId, caseData.getApplicant2(), caseData.getApplicant1()),
                caseData.getApplicant2().getLanguagePreference(),
                caseId
            );
        }
    }

    private boolean isApplicant1(Long caseId) {
        return ccdAccessService.isApplicant1(request.getHeader(AUTHORIZATION), caseId);
    }

    private Map<String, String> getTemplateContent(final CaseData caseData,
                                                   final Long caseId,
                                                   final Applicant applicant,
                                                   final Applicant partner) {

        Map<String, String> templateContent = commonContent.mainTemplateVars(caseData, caseId, applicant, partner);
        templateContent.put(PRONOUNCE_BY_DATE, LocalDate.now(clock).plusDays(CO_SUBMISSION_DATE_PLUS_DAYS)
                .format(getDateTimeFormatterForPreferredLanguage(applicant.getLanguagePreference())));
        return templateContent;
    }
}
