package uk.gov.hmcts.divorce.common.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.NOT_PROVIDED;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DISSOLUTION;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.CommonContent.SIGN_IN_URL;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_RESPONDENT_SOLICITOR_AOS_SUBMITTED;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;

@Component
@Slf4j
public class SoleApplicationSolicitorSubmitAosNotification implements ApplicantNotification {

    private static final String ISSUE_DATE = "issueDate";
    private static final String IS_NOT_DISPUTED = "isNotDisputed";
    private static final String IS_DISPUTED = "isDisputed";
    private static final String ISSUE_DATE_PLUS_37_DAYS = "issue date plus 37 days";
    private static final String ISSUE_DATE_PLUS_141_DAYS = "issue date plus 141 days";

    @Autowired
    private CommonContent commonContent;

    @Autowired
    private NotificationService notificationService;

    @Override
    public void sendToApplicant2Solicitor(final CaseData caseData, final Long id) {
        log.info("Sending Applicant2Solicitor submitted AOS notification to Applicant2Solicitor for: {}", id);

        notificationService.sendEmail(
            caseData.getApplicant2().getSolicitor().getEmail(),
            SOLE_RESPONDENT_SOLICITOR_AOS_SUBMITTED,
            applicant2SolicitorTemplateVars(caseData, id),
            caseData.getApplicant2().getLanguagePreference()
        );
    }

    private Map<String, String> applicant2SolicitorTemplateVars(CaseData caseData, Long id) {
        var templateVars = commonContent.basicTemplateVars(caseData, id);

        templateVars.put(IS_DIVORCE, caseData.isDivorce() ? YES : NO);
        templateVars.put(IS_DISSOLUTION, !caseData.isDivorce() ? YES : NO);
        templateVars.put(SOLICITOR_NAME, caseData.getApplicant2().getSolicitor().getName());
        templateVars.put(SIGN_IN_URL, commonContent.getProfessionalUsersSignInUrl(id));

        templateVars.put(ISSUE_DATE_PLUS_37_DAYS, caseData.getAcknowledgementOfService().isDisputed()
            ? caseData.getApplication().getIssueDate().plusDays(37).format(DATE_TIME_FORMATTER) : "");

        templateVars.put(ISSUE_DATE, caseData.getApplication().getIssueDate().format(DATE_TIME_FORMATTER));
        templateVars.put(ISSUE_DATE_PLUS_141_DAYS, !caseData.getAcknowledgementOfService().isDisputed()
            ? caseData.getApplication().getIssueDate().plusDays(141).format(DATE_TIME_FORMATTER) + '.' : "");

        templateVars.put(IS_NOT_DISPUTED, caseData.getAcknowledgementOfService().isDisputed() ? NO : YES);
        templateVars.put(IS_DISPUTED, caseData.getAcknowledgementOfService().isDisputed() ? YES : NO);

        templateVars.put(
            SOLICITOR_REFERENCE,
            isNotEmpty(caseData.getApplicant2().getSolicitor().getReference())
                ? caseData.getApplicant2().getSolicitor().getReference()
                : NOT_PROVIDED);



        return templateVars;
    }
}
