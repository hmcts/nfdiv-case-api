package uk.gov.hmcts.divorce.common.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.NOT_PROVIDED;
import static uk.gov.hmcts.divorce.notification.CommonContent.DATE_OF_ISSUE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DISPUTED;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DISSOLUTION;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_UNDISPUTED;
import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.CommonContent.PARTNER;
import static uk.gov.hmcts.divorce.notification.CommonContent.SIGN_IN_URL;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.SUBMISSION_RESPONSE_DATE;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_AOS_SUBMITTED_APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_AOS_SUBMITTED_RESPONDENT_SOLICITOR;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_APPLICANT_DISPUTED_AOS_SUBMITTED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_RESPONDENT_DISPUTED_AOS_SUBMITTED;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;

@Component
@Slf4j
public class SoleApplicationDisputedNotification implements ApplicantNotification {

    private static final String ISSUE_DATE_PLUS_37_DAYS = "issue date plus 37 days";
    private static final String ISSUE_DATE_PLUS_141_DAYS = "issue date plus 141 days";

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommonContent commonContent;

    @Value("${submit_aos.dispute_offset_days}")
    private int disputeDueDateOffsetDays;

    @Override
    public void sendToApplicant1(final CaseData caseData, final Long id) {
        log.info("Sending Aos disputed notification to applicant");

        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            SOLE_APPLICANT_DISPUTED_AOS_SUBMITTED,
            disputedTemplateVars(caseData, id, caseData.getApplicant1(), caseData.getApplicant2()),
            caseData.getApplicant1().getLanguagePreference()
        );
    }

    @Override
    public void sendToApplicant2(final CaseData caseData, final Long id) {
        log.info("Sending Aos disputed notification to respondent");

        final var templateContent =
            disputedTemplateVars(caseData, id, caseData.getApplicant2(), caseData.getApplicant1());

        if (WELSH.equals(caseData.getApplicant1().getLanguagePreference())) {
            templateContent.put(PARTNER, commonContent.getPartnerWelshContent(caseData, caseData.getApplicant2()));
        }

        notificationService.sendEmail(
            caseData.getApplicant2EmailAddress(),
            SOLE_RESPONDENT_DISPUTED_AOS_SUBMITTED,
            templateContent,
            caseData.getApplicant1().getLanguagePreference()
        );
    }

    @Override
    public void sendToApplicant1Solicitor(final CaseData caseData, final Long id) {
        log.info("Sending Aos disputed notification to applicant's solicitor");

        notificationService.sendEmail(
            caseData.getApplicant1().getSolicitor().getEmail(),
            SOLE_AOS_SUBMITTED_APPLICANT_1_SOLICITOR,
            applicant1SolicitorTemplateVars(caseData, id),
            ENGLISH
        );
    }

    @Override
    public void sendToApplicant2Solicitor(final CaseData caseData, final Long id) {
        log.info("Sending Applicant2Solicitor submitted AOS notification to Applicant2Solicitor for: {}", id);

        notificationService.sendEmail(
            caseData.getApplicant2().getSolicitor().getEmail(),
            SOLE_AOS_SUBMITTED_RESPONDENT_SOLICITOR,
            applicant2SolicitorTemplateVars(caseData, id),
            caseData.getApplicant2().getLanguagePreference()
        );
    }

    private Map<String, String> disputedTemplateVars(CaseData caseData, Long id, Applicant applicant, Applicant partner) {
        Map<String, String> templateVars = commonContent.mainTemplateVars(caseData, id, applicant, partner);
        templateVars.put(SUBMISSION_RESPONSE_DATE,
            caseData.getApplication().getIssueDate().plusDays(disputeDueDateOffsetDays).format(DATE_TIME_FORMATTER));
        return templateVars;
    }

    private Map<String, String> applicant1SolicitorTemplateVars(CaseData caseData, Long id) {
        Map<String, String> templateVars = solicitorTemplateVars(caseData, id);

        Solicitor applicant1Solicitor = caseData.getApplicant1().getSolicitor();
        templateVars.put(SOLICITOR_NAME, caseData.getApplicant1().getSolicitor().getName());

        templateVars.put(
            SOLICITOR_REFERENCE,
            isNotEmpty(applicant1Solicitor.getReference()) ? applicant1Solicitor.getReference() : NOT_PROVIDED
        );

        return templateVars;
    }

    private Map<String, String> applicant2SolicitorTemplateVars(CaseData caseData, Long id) {
        Map<String, String> templateVars = solicitorTemplateVars(caseData, id);

        Solicitor applicant2Solicitor = caseData.getApplicant2().getSolicitor();
        templateVars.put(SOLICITOR_NAME, caseData.getApplicant2().getSolicitor().getName());
        templateVars.put(ISSUE_DATE_PLUS_141_DAYS, "");

        templateVars.put(
            SOLICITOR_REFERENCE,
            isNotEmpty(applicant2Solicitor.getReference()) ? applicant2Solicitor.getReference() : NOT_PROVIDED);

        return templateVars;
    }

    private Map<String, String> solicitorTemplateVars(CaseData caseData, Long id) {
        var templateVars = commonContent.basicTemplateVars(caseData, id);

        templateVars.put(IS_DIVORCE, caseData.isDivorce() ? YES : NO);
        templateVars.put(IS_DISSOLUTION, !caseData.isDivorce() ? YES : NO);
        templateVars.put(SIGN_IN_URL, commonContent.getProfessionalUsersSignInUrl(id));

        templateVars.put(ISSUE_DATE_PLUS_37_DAYS,
            caseData.getApplication().getIssueDate().plusDays(disputeDueDateOffsetDays).format(DATE_TIME_FORMATTER));
        templateVars.put(DATE_OF_ISSUE, caseData.getApplication().getIssueDate().format(DATE_TIME_FORMATTER));

        templateVars.put(IS_UNDISPUTED, NO);
        templateVars.put(IS_DISPUTED, YES);

        return templateVars;
    }
}
