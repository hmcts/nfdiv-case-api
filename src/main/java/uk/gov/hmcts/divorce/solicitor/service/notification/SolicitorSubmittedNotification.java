package uk.gov.hmcts.divorce.solicitor.service.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.Solicitor;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOL_APPLICANT_SOLICITOR_APPLICATION_SUBMITTED;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLICATION_REFERENCE;

@Slf4j
@Component
public class SolicitorSubmittedNotification {

    private static final String NO_EMAIL_SENT_FOR_CASE =
        "No applicant solicitor email is provided so no email sent for case id : {}";
    private static final String SENDING_APPLICATION_EMAIL =
        "Sending application submitted notification to applicant solicitor for case id : {}";

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommonContent commonContent;

    public void send(final CaseData caseData, final Long caseId) {

        final Solicitor solicitor = caseData.getApplicant1().getSolicitor();
        final Map<String, String> templateVars = commonContent.templateVarsFor(caseData);
        templateVars.put(APPLICATION_REFERENCE, formatId(caseId));

        if (solicitor != null && isNotEmpty(solicitor.getEmail())) {
            notificationService.sendEmail(
                solicitor.getEmail(),
                SOL_APPLICANT_SOLICITOR_APPLICATION_SUBMITTED,
                templateVars,
                caseData.getApplicant1().getLanguagePreference());

            log.info(SENDING_APPLICATION_EMAIL, caseId);

        } else {
            log.info(NO_EMAIL_SENT_FOR_CASE, caseId);
        }
    }
}
