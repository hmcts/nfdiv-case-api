package uk.gov.hmcts.divorce.common.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.common.config.EmailTemplatesConfig;
import uk.gov.hmcts.divorce.common.service.HoldingPeriodService;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.DUE_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ISSUE_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.NOT_PROVIDED;
import static uk.gov.hmcts.divorce.notification.CommonContent.ACCESS_CODE;
import static uk.gov.hmcts.divorce.notification.CommonContent.APPLICATION_REFERENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.CREATE_ACCOUNT_LINK;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DISSOLUTION;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_REMINDER;
import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.CommonContent.REVIEW_DEADLINE_DATE;
import static uk.gov.hmcts.divorce.notification.CommonContent.SIGN_IN_URL;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.SUBMISSION_RESPONSE_DATE;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.APPLICANT_SOLICITOR_SERVICE;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICATION_ACCEPTED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_SOLICITOR_NOTICE_OF_PROCEEDINGS;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.RESPONDENT_SOLICITOR_NOTICE_OF_PROCEEDINGS;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_APPLICANT_APPLICATION_ACCEPTED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_APPLICANT_SOLICITOR_NOTICE_OF_PROCEEDINGS;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_APPLICANT_SOLICITOR_NOTICE_OF_PROCEEDINGS_REISSUE;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_RESPONDENT_APPLICATION_ACCEPTED;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.getDateTimeFormatterForPreferredLanguage;

@Component
@Slf4j
public class ApplicationIssuedNotification implements ApplicantNotification {

    private static final String RESPONDENT_SIGN_IN_DIVORCE_URL = "respondentSignInDivorceUrl";
    private static final String RESPONDENT_SIGN_IN_DISSOLUTION_URL = "respondentSignInDissolutionUrl";
    private static final String CASE_ID = "case id";
    private static final String UNION_TYPE = "union type";

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommonContent commonContent;

    @Autowired
    private EmailTemplatesConfig config;

    @Autowired
    private HoldingPeriodService holdingPeriodService;

    @Override
    public void sendToApplicant1(final CaseData caseData, final Long caseId) {

        final String email = caseData.getApplicant1().getEmail();
        final LanguagePreference languagePreference = caseData.getApplicant1().getLanguagePreference();

        if (caseData.getApplicationType().isSole()) {
            if (!caseData.getApplication().isPersonalServiceMethod()) {
                log.info("Sending sole application issued notification to applicant 1 for case : {}", caseId);

                notificationService.sendEmail(
                    email,
                    SOLE_APPLICANT_APPLICATION_ACCEPTED,
                    soleApplicant1TemplateVars(caseData, caseId, languagePreference),
                    languagePreference,
                    caseId
                );
            }
        } else {
            log.info("Sending joint application issued notification to applicant 1 for case : {}", caseId);

            notificationService.sendEmail(
                email,
                JOINT_APPLICATION_ACCEPTED,
                commonTemplateVars(caseData, caseId, caseData.getApplicant1(), caseData.getApplicant2()),
                languagePreference,
                caseId
            );
        }
    }

    @Override
    public void sendToApplicant1Solicitor(final CaseData caseData, final Long caseId) {

        final String email = caseData.getApplicant1().getSolicitor().getEmail();
        final LanguagePreference languagePreference = caseData.getApplicant1().getLanguagePreference();
        boolean isSolicitorServiceMethod = caseData.getApplication().isSolicitorServiceMethod();

        if (isSolicitorServiceMethod) {
            log.info("Sending Personal Service email to applicant solicitor.  Case ID: {}", caseId);

            notificationService.sendEmail(
                email,
                APPLICANT_SOLICITOR_SERVICE,
                templateVars(caseData, caseId, languagePreference),
                languagePreference,
                caseId
            );
        } else if (caseData.getApplicationType().isSole()) {

            log.info("Sending Notice Of Proceedings email to applicant solicitor.  Case ID: {}", caseId);

            notificationService.sendEmail(
                email,
                caseData.getApplication().getReissueDate() != null
                    ? SOLE_APPLICANT_SOLICITOR_NOTICE_OF_PROCEEDINGS_REISSUE
                    : SOLE_APPLICANT_SOLICITOR_NOTICE_OF_PROCEEDINGS,
                applicant1SolicitorNoticeOfProceedingsTemplateVars(caseData, caseId),
                languagePreference,
                caseId
            );
        } else if (!caseData.getApplicationType().isSole()) {
            log.info("Sending Notice Of Proceedings email to applicant 1 solicitor for joint case.  Case ID: {}", caseId);

            notificationService.sendEmail(
                email,
                JOINT_SOLICITOR_NOTICE_OF_PROCEEDINGS,
                applicant1SolicitorNoticeOfProceedingsTemplateVars(caseData, caseId),
                ENGLISH,
                caseId);
        }
    }

    @Override
    public void sendToApplicant2(final CaseData caseData, final Long caseId) {

        final String email = caseData.getApplicant2EmailAddress();
        final LanguagePreference languagePreference = caseData.getApplicant2().getLanguagePreference();

        if (caseData.getApplicationType().isSole()) {
            if (isNotBlank(email) && caseData.getApplication().isCourtServiceMethod() && !caseData.getApplicant2().isBasedOverseas()) {
                log.info("Sending sole application issued notification to respondent for case : {}", caseId);

                notificationService.sendEmail(
                    email,
                    SOLE_RESPONDENT_APPLICATION_ACCEPTED,
                    soleRespondentTemplateVars(caseData, caseId),
                    languagePreference,
                    caseId
                );
            }
        } else {
            log.info("Sending joint application issued notification to applicant 2 for case : {}", caseId);

            notificationService.sendEmail(
                email,
                JOINT_APPLICATION_ACCEPTED,
                commonTemplateVars(caseData, caseId, caseData.getApplicant2(), caseData.getApplicant1()),
                languagePreference,
                caseId
            );
        }
    }

    @Override
    public void sendToApplicant2Solicitor(final CaseData caseData, final Long caseId) {

        final String email = caseData.getApplicant2().getSolicitor().getEmail();

        if (caseData.getApplicationType().isSole() && caseData.getApplication().isCourtServiceMethod() && isNotBlank(email)) {

            log.info("Sending Notice Of Proceedings email to respondent solicitor.  Case ID: {}", caseId);
            notificationService.sendEmail(
                email,
                RESPONDENT_SOLICITOR_NOTICE_OF_PROCEEDINGS,
                soleRespondentSolicitorNoticeOfProceedingsTemplateVars(caseData, caseId),
                ENGLISH,
                caseId
            );

        } else if (!caseData.getApplicationType().isSole() && !caseData.getApplication().isSolicitorServiceMethod()) {
            log.info("Sending Notice Of Proceedings email to applicant 2 solicitor for joint case.  Case ID: {}", caseId);

            notificationService.sendEmail(
                email,
                JOINT_SOLICITOR_NOTICE_OF_PROCEEDINGS,
                applicant2SolicitorNoticeOfProceedingsTemplateVars(caseData, caseId),
                ENGLISH,
                caseId);
        }
    }

    private Map<String, String> soleApplicant1TemplateVars(final CaseData caseData, Long id, LanguagePreference languagePreference) {
        final Map<String, String> templateVars = commonTemplateVars(caseData, id, caseData.getApplicant1(), caseData.getApplicant2());
        templateVars.put(
            REVIEW_DEADLINE_DATE,
            holdingPeriodService.getRespondByDateFor(caseData.getApplication().getIssueDate())
                    .format(getDateTimeFormatterForPreferredLanguage(languagePreference))
        );

        return templateVars;
    }

    private Map<String, String> soleRespondentTemplateVars(final CaseData caseData, Long id) {
        final Map<String, String> templateVars = commonTemplateVars(caseData, id, caseData.getApplicant2(), caseData.getApplicant1());
        templateVars.put(IS_REMINDER, NO);
        templateVars.put(
            REVIEW_DEADLINE_DATE, holdingPeriodService.getRespondByDateFor(caseData.getApplication().getIssueDate())
                    .format(getDateTimeFormatterForPreferredLanguage(caseData.getApplicant2().getLanguagePreference())));
        templateVars.put(
            CREATE_ACCOUNT_LINK,
            config.getTemplateVars()
                .get(caseData.isDivorce() ? RESPONDENT_SIGN_IN_DIVORCE_URL : RESPONDENT_SIGN_IN_DISSOLUTION_URL)
        );
        templateVars.put(ACCESS_CODE, caseData.getCaseInvite().accessCode());
        return templateVars;
    }

    private Map<String, String> commonTemplateVars(final CaseData caseData, Long id, Applicant applicant, Applicant partner) {
        final Map<String, String> templateVars = commonContent.mainTemplateVars(caseData, id, applicant, partner);
        templateVars.put(
            SUBMISSION_RESPONSE_DATE, holdingPeriodService.getDueDateFor(caseData.getApplication().getIssueDate())
                    .format(getDateTimeFormatterForPreferredLanguage(applicant.getLanguagePreference())));
        return templateVars;
    }

    private Map<String, String> applicant1SolicitorNoticeOfProceedingsTemplateVars(final CaseData caseData, final Long caseId) {

        Applicant applicant = caseData.getApplicant1();

        final Map<String, String> templateVars = commonSolicitorNoticeOfProceedingsTemplateVars(caseData, caseId, applicant);

        templateVars.put(SOLICITOR_NAME, applicant.getSolicitor().getName());
        templateVars.put(
            SOLICITOR_REFERENCE,
            isNotEmpty(applicant.getSolicitor().getReference())
                ? applicant.getSolicitor().getReference()
                : NOT_PROVIDED);

        templateVars.put(SUBMISSION_RESPONSE_DATE,
            holdingPeriodService.getDueDateFor(caseData.getApplication().getIssueDate())
                    .format(DATE_TIME_FORMATTER));

        return templateVars;
    }

    private Map<String, String> soleRespondentSolicitorNoticeOfProceedingsTemplateVars(final CaseData caseData, final Long caseId) {

        Applicant applicant2 = caseData.getApplicant2();

        final Map<String, String> templateVars = commonSolicitorNoticeOfProceedingsTemplateVars(caseData, caseId, applicant2);

        templateVars.put(SOLICITOR_NAME, applicant2.getSolicitor().getName());
        templateVars.put(
            SOLICITOR_REFERENCE,
            isNotEmpty(applicant2.getSolicitor().getReference())
                ? applicant2.getSolicitor().getReference()
                : NOT_PROVIDED);

        templateVars.put(SUBMISSION_RESPONSE_DATE,
            caseData.getApplication().getIssueDate().plusDays(16)
                    .format(DATE_TIME_FORMATTER));

        return templateVars;
    }

    private Map<String, String> applicant2SolicitorNoticeOfProceedingsTemplateVars(final CaseData caseData, final Long caseId) {

        Applicant applicant2 = caseData.getApplicant2();

        final Map<String, String> templateVars = commonSolicitorNoticeOfProceedingsTemplateVars(caseData, caseId, applicant2);

        templateVars.put(SOLICITOR_NAME, applicant2.getSolicitor().getName());
        templateVars.put(
            SOLICITOR_REFERENCE,
            isNotEmpty(applicant2.getSolicitor().getReference())
                ? applicant2.getSolicitor().getReference()
                : NOT_PROVIDED);

        templateVars.put(SUBMISSION_RESPONSE_DATE,
            holdingPeriodService.getDueDateFor(caseData.getApplication().getIssueDate())
                    .format(DATE_TIME_FORMATTER));

        return templateVars;
    }

    private Map<String, String> commonSolicitorNoticeOfProceedingsTemplateVars(final CaseData caseData,
                                                                               final Long caseId, Applicant applicant) {
        final Map<String, String> templateVars = commonContent.basicTemplateVars(caseData, caseId);

        templateVars.put(CASE_ID, caseId.toString());
        templateVars.put(IS_DIVORCE, caseData.isDivorce() ? YES : NO);
        templateVars.put(IS_DISSOLUTION, !caseData.isDivorce() ? YES : NO);
        templateVars.put(SIGN_IN_URL, commonContent.getProfessionalUsersSignInUrl(caseId));
        templateVars.put(ISSUE_DATE, caseData.getApplication().getIssueDate()
                .format(DATE_TIME_FORMATTER));
        templateVars.put(DUE_DATE, caseData.getDueDate()
                .format(DATE_TIME_FORMATTER));

        return templateVars;
    }

    private Map<String, String> templateVars(final CaseData caseData,
                                             final Long caseId,
                                             final LanguagePreference languagePreference) {

        String solicitorReference = isNotEmpty(caseData.getApplicant1().getSolicitor().getReference())
            ? caseData.getApplicant1().getSolicitor().getReference()
            : "not provided";

        final Map<String, String> templateVars = commonContent.basicTemplateVars(caseData, caseId);
        templateVars.put(SOLICITOR_NAME, caseData.getApplicant1().getSolicitor().getName());
        templateVars.put(SIGN_IN_URL, commonContent.getProfessionalUsersSignInUrl(caseId));
        templateVars.put(APPLICATION_REFERENCE, String.valueOf(caseId));
        templateVars.put(UNION_TYPE, commonContent.getUnionType(caseData, languagePreference));
        templateVars.put(SOLICITOR_REFERENCE, solicitorReference);
        return templateVars;
    }
}
