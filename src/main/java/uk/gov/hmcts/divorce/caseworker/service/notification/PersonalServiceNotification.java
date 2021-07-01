package uk.gov.hmcts.divorce.caseworker.service.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.Solicitor;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Map;

import static uk.gov.hmcts.divorce.common.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.APPLICANT_SOLICITOR_PERSONAL_SERVICE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.SOLICITOR_NAME;

@Component
@Slf4j
public class PersonalServiceNotification {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommonContent commonContent;

    public void send(final CaseData caseData, final Long caseId) {

        final Solicitor applicantSolicitor = caseData.getApplicant1().getSolicitor();

        notificationService.sendEmail(
            applicantSolicitor.getEmail(),
            APPLICANT_SOLICITOR_PERSONAL_SERVICE,
            templateVars(caseData, caseId),
            ENGLISH
        );
    }

    private Map<String, String> templateVars(final CaseData caseData, final Long caseId) {

        final Map<String, String> templateVars = commonContent.commonNotificationTemplateVars(caseData, caseId);
        templateVars.put(SOLICITOR_NAME, caseData.getApplicant1().getSolicitor().getName());
        return templateVars;
    }
}
