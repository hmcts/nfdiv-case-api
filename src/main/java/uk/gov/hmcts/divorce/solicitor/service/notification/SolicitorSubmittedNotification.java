package uk.gov.hmcts.divorce.solicitor.service.notification;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.EmailTemplateName;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOL_APPLICANT_SOLICITOR_AMENDED_APPLICATION_SUBMITTED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOL_APPLICANT_SOLICITOR_APPLICATION_SUBMITTED;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLICATION_REFERENCE;

@Slf4j
@Component
public class SolicitorSubmittedNotification {

    private static final String NO_EMAIL_SENT_FOR_CASE =
        "No applicant solicitor email is provided so no email sent for case id : {}";
    private static final String SENDING_AMENDED_APPLICATION_EMAIL =
        "Sending amended application submitted notification to applicant solicitor for case id : {}";
    private static final String SENDING_APPLICATION_EMAIL =
        "Sending application submitted notification to applicant solicitor for case id : {}";

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommonContent commonContent;

    public void send(final CaseData caseData, final Long caseId) {

        final String petitionerSolicitorEmail = caseData.getPetitionerSolicitorEmail();
        final Map<String, String> templateVars = commonContent.templateVarsFor(caseData);
        templateVars.put(APPLICATION_REFERENCE, formatId(caseId));

        if (isNotEmpty(petitionerSolicitorEmail)) {

            final ImmutablePair<EmailTemplateName, String> emailInfo = getSolicitorEmailTemplate(caseData);

            notificationService.sendEmail(
                petitionerSolicitorEmail,
                emailInfo.getKey(),
                templateVars,
                caseData.getLanguagePreference());

            log.info(emailInfo.getValue(), caseId);
        } else {
            log.info(NO_EMAIL_SENT_FOR_CASE, caseId);
        }
    }

    private ImmutablePair<EmailTemplateName, String> getSolicitorEmailTemplate(final CaseData caseData) {

        if (caseData.hasPreviousCaseId()) {
            return ImmutablePair.of(SOL_APPLICANT_SOLICITOR_AMENDED_APPLICATION_SUBMITTED, SENDING_AMENDED_APPLICATION_EMAIL);
        }

        return ImmutablePair.of(SOL_APPLICANT_SOLICITOR_APPLICATION_SUBMITTED, SENDING_APPLICATION_EMAIL);
    }
}
