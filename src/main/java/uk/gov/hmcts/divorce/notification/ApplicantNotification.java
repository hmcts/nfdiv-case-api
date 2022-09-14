package uk.gov.hmcts.divorce.notification;

import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import java.util.Map;

import static com.microsoft.applicationinsights.boot.dependencies.apachecommons.lang3.StringUtils.isNotEmpty;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.NOT_PROVIDED;
import static uk.gov.hmcts.divorce.notification.CommonContent.DATE_OF_ISSUE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DISSOLUTION;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_FINAL_ORDER;
import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.CommonContent.SIGN_IN_URL;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_SOLICITOR_BOTH_APPLIED_CO_FO;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;

public interface ApplicantNotification {

    default void sendToApplicant1(final CaseData caseData, final Long caseId) {
        //No operation
    }

    default void sendToApplicant1Solicitor(final CaseData caseData, final Long caseId) {
        //No operation
    }

    default void sendToApplicant1Offline(final CaseData caseData, final Long caseId) {
        //No operation
    }

    default void sendToApplicant2(final CaseData caseData, final Long caseId) {
        //No operation
    }

    default void sendToApplicant2Solicitor(final CaseData caseData, final Long caseId) {
        //No operation
    }

    default void sendToApplicant2Offline(final CaseData caseData, final Long caseId) {
        //No operation
    }

    static void sendBothSolicitorsAppliedForFinalOrderNotification(CaseData caseData, Long caseId, Applicant applicant,
                                                                    CommonContent commonContent, NotificationService notificationService) {
        var templateVars = solicitorTemplateVars(caseData, caseId, applicant, commonContent);
        notificationService.sendEmail(
                applicant.getSolicitor().getEmail(),
                JOINT_SOLICITOR_BOTH_APPLIED_CO_FO,
                templateVars,
                applicant.getLanguagePreference()
        );
    }

    private static Map<String, String> solicitorTemplateVars(final CaseData caseData, final Long caseId, Applicant applicant,
                                                        CommonContent commonContent) {
        Map<String, String> templateVars = commonContent.basicTemplateVars(caseData, caseId);

        templateVars.put(IS_CONDITIONAL_ORDER, NO);
        templateVars.put(IS_FINAL_ORDER, YES);
        templateVars.put(SOLICITOR_NAME, applicant.getSolicitor().getName());
        templateVars.put(SOLICITOR_REFERENCE,
                isNotEmpty(applicant.getSolicitor().getReference()) ? applicant.getSolicitor().getReference() : NOT_PROVIDED);
        templateVars.put(IS_DIVORCE, caseData.getDivorceOrDissolution().isDivorce() ? YES : NO);
        templateVars.put(IS_DISSOLUTION, !caseData.getDivorceOrDissolution().isDivorce() ? YES : NO);
        templateVars.put(SIGN_IN_URL, commonContent.getProfessionalUsersSignInUrl(caseId));
        templateVars.put(DATE_OF_ISSUE, caseData.getApplication().getIssueDate().format(DATE_TIME_FORMATTER));

        return templateVars;
    }
}
