package uk.gov.hmcts.divorce.citizen.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Optional;

import static uk.gov.hmcts.divorce.notification.EmailTemplateName.NOC_TO_SOLS_EMAIL_OLD_SOL_REMOVED_SELF;

@RequiredArgsConstructor
@Component
@Slf4j
public class NocSolRemovedSelfNotifications implements ApplicantNotification {

    private final NotificationService notificationService;
    private final CommonContent commonContent;

    @Override
    public void sendToApplicant1OldSolicitor(final CaseData oldCaseData, final Long id) {
        log.info("Sending NOC removed self as representative notification to app1OldSolicitor: {}", id);

        sendToOldSolicitor(oldCaseData.getApplicant1(), id);
    }

    @Override
    public void sendToApplicant2OldSolicitor(final CaseData oldCaseData, final Long id) {
        log.info("Sending NOC removed self as representative notification to app2OldSolicitor: {}", id);

        sendToOldSolicitor(oldCaseData.getApplicant2(), id);
    }

    private void sendToOldSolicitor(final Applicant applicant, final Long id) {
        Optional.ofNullable(applicant)
            .map(Applicant::getSolicitor)
            .map(Solicitor::getEmail)
            .filter(StringUtils::isNotEmpty)
            .ifPresent(email -> notificationService.sendEmail(
                email,
                NOC_TO_SOLS_EMAIL_OLD_SOL_REMOVED_SELF,
                commonContent.nocOldSolsTemplateVars(id, applicant),
                applicant.getLanguagePreference(),
                id
            ));
    }
}
