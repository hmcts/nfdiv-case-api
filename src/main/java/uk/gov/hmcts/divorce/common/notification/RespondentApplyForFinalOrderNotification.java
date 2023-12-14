package uk.gov.hmcts.divorce.common.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ISSUE_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.NOT_PROVIDED;
import static uk.gov.hmcts.divorce.notification.CommonContent.SIGN_IN_URL;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.RESPONDENT_APPLY_FOR_FINAL_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.RESPONDENT_SOLICITOR_APPLY_FOR_FINAL_ORDER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;

@Component
@Slf4j
public class RespondentApplyForFinalOrderNotification implements ApplicantNotification {

    @Autowired
    private CommonContent commonContent;

    @Autowired
    private NotificationService notificationService;

    @Override
    public void sendToApplicant2(final CaseData caseData, final Long id) {
        final Applicant applicant1 = caseData.getApplicant1();
        final Applicant applicant2 = caseData.getApplicant2();

        log.info("Notifying respondent that they can apply for a final order: {}", id);

        notificationService.sendEmail(
            caseData.getApplicant2EmailAddress(),
            RESPONDENT_APPLY_FOR_FINAL_ORDER,
            templateVars(caseData, id, applicant2, applicant1),
            applicant2.getLanguagePreference(),
            id
        );
    }

    @Override
    public void sendToApplicant2Solicitor(final CaseData caseData, final Long id) {
        final Applicant applicant2 = caseData.getApplicant2();

        log.info("Notifying respondent's solicitor that they can apply for a final order: {}", id);

        notificationService.sendEmail(
            applicant2.getSolicitor().getEmail(),
            RESPONDENT_SOLICITOR_APPLY_FOR_FINAL_ORDER,
            solicitorTemplateVars(caseData, id, applicant2),
            applicant2.getLanguagePreference(),
            id
        );
    }

    private Map<String, String> templateVars(CaseData caseData, Long id, Applicant applicant, Applicant partner) {
        return commonContent.conditionalOrderTemplateVars(caseData, id, applicant, partner);
    }

    private Map<String, String> solicitorTemplateVars(CaseData caseData, Long id, Applicant applicant) {
        var templateVars = commonContent.basicTemplateVars(caseData, id);

        templateVars.put(ISSUE_DATE, caseData.getApplication().getIssueDate().format(DATE_TIME_FORMATTER));
        templateVars.put(SIGN_IN_URL, commonContent.getProfessionalUsersSignInUrl(id));

        Solicitor applicantSolicitor = applicant.getSolicitor();
        templateVars.put(SOLICITOR_NAME, applicantSolicitor.getName());
        templateVars.put(
            SOLICITOR_REFERENCE,
            isNotEmpty(applicantSolicitor.getReference()) ? applicantSolicitor.getReference() : NOT_PROVIDED);

        return templateVars;
    }
}
