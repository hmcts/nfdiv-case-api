package uk.gov.hmcts.divorce.common.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingConditionalOrder;
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
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_APPLICANT_AOS_SUBMITTED_AWAITING_CO;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_RESPONDENT_AOS_SUBMITTED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_RESPONDENT_AOS_SUBMITTED_AWAITING_CO;
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
    public void sendToApplicant1(final CaseDetails<CaseData, State> caseDetails) {
        log.info("Sending AOS not disputed notification to Applicant for: {}", caseDetails.getId());
        CaseData caseData = caseDetails.getData();

        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            caseDetails.getState() == AwaitingConditionalOrder ? SOLE_APPLICANT_AOS_SUBMITTED_AWAITING_CO : SOLE_APPLICANT_AOS_SUBMITTED,
            notDisputedTemplateVars(caseData, caseDetails.getId(), caseData.getApplicant1(), caseData.getApplicant2()),
            caseData.getApplicant1().getLanguagePreference(),
            caseDetails.getId()
        );
    }

    @Override
    public void sendToApplicant2(final CaseDetails<CaseData, State> caseDetails) {
        log.info("Sending AOS not disputed notification to Respondent for: {}", caseDetails.getId());
        CaseData caseData = caseDetails.getData();

        notificationService.sendEmail(
            caseData.getApplicant2EmailAddress(),
            caseDetails.getState() == AwaitingConditionalOrder ? SOLE_RESPONDENT_AOS_SUBMITTED_AWAITING_CO : SOLE_RESPONDENT_AOS_SUBMITTED,
            notDisputedTemplateVars(caseData, caseDetails.getId(), caseData.getApplicant2(), caseData.getApplicant1()),
            caseData.getApplicant2().getLanguagePreference(),
            caseDetails.getId()
        );
    }

    @Override
    public void sendToApplicant1Solicitor(final CaseDetails<CaseData, State> caseDetails) {
        log.info("Sending AOS not disputed notification to Applicant Solicitor for: {}", caseDetails.getId());
        CaseData caseData = caseDetails.getData();

        notificationService.sendEmail(
            caseData.getApplicant1().getSolicitor().getEmail(),
            SOLE_AOS_SUBMITTED_APPLICANT_1_SOLICITOR,
            solicitorTemplateVars(caseData, caseDetails.getId(), caseData.getApplicant1()),
            caseData.getApplicant1().getLanguagePreference(),
            caseDetails.getId()
        );
    }

    @Override
    public void sendToApplicant2Solicitor(final CaseDetails<CaseData, State> caseDetails) {
        log.info("Sending AOS not disputed notification to Respondent Solicitor for: {}", caseDetails.getId());
        CaseData caseData = caseDetails.getData();

        notificationService.sendEmail(
            caseData.getApplicant2().getSolicitor().getEmail(),
            SOLE_AOS_SUBMITTED_RESPONDENT_SOLICITOR,
            solicitorTemplateVars(caseData, caseDetails.getId(), caseData.getApplicant2()),
            caseData.getApplicant2().getLanguagePreference(),
            caseDetails.getId()
        );
    }

    private Map<String, String> notDisputedTemplateVars(CaseData caseData, Long id, Applicant applicant, Applicant partner) {
        Map<String, String> templateVars = commonContent.mainTemplateVars(caseData, id, applicant, partner);
        templateVars.put(APPLY_FOR_CO_DATE, caseData.getDueDate() != null
            ? caseData.getDueDate().format(getDateTimeFormatterForPreferredLanguage(applicant.getLanguagePreference()))
            : ""
        );
        return templateVars;
    }

    private Map<String, String> solicitorTemplateVars(CaseData caseData, Long id, Applicant applicant) {
        var templateVars = commonContent.basicTemplateVars(caseData, id, applicant.getLanguagePreference());

        templateVars.put(IS_DIVORCE, caseData.isDivorce() ? YES : NO);
        templateVars.put(IS_DISSOLUTION, !caseData.isDivorce() ? YES : NO);
        templateVars.put(SIGN_IN_URL, commonContent.getProfessionalUsersSignInUrl(id));

        templateVars.put(ISSUE_DATE_PLUS_37_DAYS, "");
        templateVars.put(ISSUE_DATE_PLUS_141_DAYS,
            caseData.getApplication().getIssueDate().plusDays(holdingOffsetDays).format(DATE_TIME_FORMATTER));
        templateVars.put(DATE_OF_ISSUE, caseData.getApplication().getIssueDate().format(DATE_TIME_FORMATTER));
        templateVars.put(SOLICITOR_NAME, applicant.getSolicitor().getName());
        templateVars.put(
            SOLICITOR_REFERENCE,
            isNotEmpty(applicant.getSolicitor().getReference()) ? applicant.getSolicitor().getReference() : NOT_PROVIDED
        );

        templateVars.put(IS_UNDISPUTED, YES);
        templateVars.put(IS_DISPUTED, NO);

        return templateVars;
    }
}
