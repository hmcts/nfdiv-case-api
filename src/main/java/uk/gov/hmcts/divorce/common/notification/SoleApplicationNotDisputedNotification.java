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
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.NOT_PROVIDED;
import static uk.gov.hmcts.divorce.notification.CommonContent.DATE_OF_ISSUE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DISPUTED;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DISSOLUTION;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_UNDISPUTED;
import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.CommonContent.SIGN_IN_URL;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_AOS_SUBMITTED_APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_AOS_SUBMITTED_RESPONDENT_SOLICITOR;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_APPLICANT_AOS_SUBMITTED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_RESPONDENT_AOS_SUBMITTED;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.getDateTimeFormatterForPreferredLanguage;

@Component
@Slf4j
public class SoleApplicationNotDisputedNotification implements ApplicantNotification {

    private static final String APPLY_FOR_CO_DATE = "apply for CO date";
    private static final String ISSUE_DATE_PLUS_37_DAYS = "issue date plus 37 days";
    private static final String ISSUE_DATE_PLUS_141_DAYS = "issue date plus 141 days";

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommonContent commonContent;

    @Value("${case_progression.holding_offset_days}")
    private int holdingOffsetDays;

    @Override
    public void sendToApplicant1(final CaseData caseData, final Long id) {
        log.info("Sending Aos not disputed notification to applicant");

        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            SOLE_APPLICANT_AOS_SUBMITTED,
            notDisputedTemplateVars(caseData, id, caseData.getApplicant1(), caseData.getApplicant2()),
            caseData.getApplicant1().getLanguagePreference(),
            id
        );
    }

    @Override
    public void sendToApplicant2(CaseData caseData, Long id) {
        log.info("Sending Aos not disputed notification to respondent");

        notificationService.sendEmail(
            caseData.getApplicant2EmailAddress(),
            SOLE_RESPONDENT_AOS_SUBMITTED,
            notDisputedTemplateVars(caseData, id, caseData.getApplicant2(), caseData.getApplicant1()),
            caseData.getApplicant2().getLanguagePreference(),
            id
        );
    }

    @Override
    public void sendToApplicant1Solicitor(final CaseData caseData, final Long id) {
        log.info("Sending Aos not disputed notification to applicant's solicitor");

        notificationService.sendEmail(
            caseData.getApplicant1().getSolicitor().getEmail(),
            SOLE_AOS_SUBMITTED_APPLICANT_1_SOLICITOR,
            applicant1SolicitorTemplateVars(caseData, id),
            ENGLISH,
            id
        );
    }

    @Override
    public void sendToApplicant2Solicitor(final CaseData caseData, final Long id) {
        log.info("Sending Applicant2Solicitor submitted AOS notification to Applicant2Solicitor for: {}", id);

        notificationService.sendEmail(
            caseData.getApplicant2().getSolicitor().getEmail(),
            SOLE_AOS_SUBMITTED_RESPONDENT_SOLICITOR,
            applicant2SolicitorTemplateVars(caseData, id),
            caseData.getApplicant2().getLanguagePreference(),
            id
        );
    }

    private Map<String, String> notDisputedTemplateVars(CaseData caseData, Long id, Applicant applicant, Applicant partner) {
        Map<String, String> templateVars = commonContent.mainTemplateVars(caseData, id, applicant, partner);
        templateVars.put(APPLY_FOR_CO_DATE, caseData.getDueDate()
                .format(getDateTimeFormatterForPreferredLanguage(applicant.getLanguagePreference())));
        return templateVars;
    }

    private Map<String, String> applicant1SolicitorTemplateVars(CaseData caseData, Long id) {
        Map<String, String> templateVars = solicitorTemplateVars(caseData, id, caseData.getApplicant1());
        Solicitor applicant1Solicitor = caseData.getApplicant1().getSolicitor();

        templateVars.put(SOLICITOR_NAME, caseData.getApplicant1().getSolicitor().getName());
        templateVars.put(
            SOLICITOR_REFERENCE,
            isNotEmpty(applicant1Solicitor.getReference()) ? applicant1Solicitor.getReference() : NOT_PROVIDED
        );

        return templateVars;
    }

    private Map<String, String> applicant2SolicitorTemplateVars(CaseData caseData, Long id) {
        Map<String, String> templateVars = solicitorTemplateVars(caseData, id, caseData.getApplicant2());

        templateVars.put(SOLICITOR_NAME, caseData.getApplicant2().getSolicitor().getName());
        templateVars.put(SIGN_IN_URL, commonContent.getProfessionalUsersSignInUrl(id));

        templateVars.put(ISSUE_DATE_PLUS_141_DAYS,
            caseData.getApplication().getIssueDate().plusDays(holdingOffsetDays).format(DATE_TIME_FORMATTER));

        templateVars.put(
            SOLICITOR_REFERENCE,
            isNotEmpty(caseData.getApplicant2().getSolicitor().getReference())
                ? caseData.getApplicant2().getSolicitor().getReference()
                : NOT_PROVIDED);

        return templateVars;
    }

    private Map<String, String> solicitorTemplateVars(CaseData caseData, Long id, Applicant applicant) {
        var templateVars = commonContent.basicTemplateVars(caseData, id);

        templateVars.put(IS_DIVORCE, caseData.isDivorce() ? YES : NO);
        templateVars.put(IS_DISSOLUTION, !caseData.isDivorce() ? YES : NO);
        templateVars.put(SIGN_IN_URL, commonContent.getProfessionalUsersSignInUrl(id));

        templateVars.put(ISSUE_DATE_PLUS_37_DAYS, "");
        templateVars.put(DATE_OF_ISSUE, caseData.getApplication().getIssueDate().format(DATE_TIME_FORMATTER));

        templateVars.put(IS_UNDISPUTED, YES);
        templateVars.put(IS_DISPUTED, NO);

        return templateVars;
    }
}
