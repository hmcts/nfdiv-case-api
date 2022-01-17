package uk.gov.hmcts.divorce.citizen.notification.conditionalorder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderQuestions;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.EmailTemplateName;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.divorce.notification.EmailTemplateName.CITIZEN_APPLIED_FOR_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLIED_FOR_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_PARTNER_APPLIED_FOR_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;

@Component
@Slf4j
public class AppliedForConditionalOrderNotification implements ApplicantNotification {

    private static final String PLUS_21_DUE_DATE = "date email received plus 21 days";
    private static final String PLUS_14_DUE_DATE = "application for CO date plus 14 days";
    private static final String WIFE_APPLIED = "wifeApplied";
    private static final String HUSBAND_APPLIED = "husbandApplied";
    private static final String CIVIL_PARTNER_APPLIED = "civilPartnerApplied";
    private static final String PARTNER_APPLIED = "partnerApplied";
    private static final String WIFE_DID_NOT_APPLY = "wifeDidNotApply";
    private static final String HUSBAND_DID_NOT_APPLY = "husbandDidNotApply";
    private static final String CIVIL_PARTNER_DID_NOT_APPLY = "civilPartnerDidNotApply";
    private static final String PARTNER_DID_NOT_APPLY = "partnerDidNotApply";
    private static final String PLUS_21_DUE_DATE_DUPLICATED = "date plus three weeks";//TODO: check is same date as sole above

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommonContent commonContent;

    @Override
    public void sendToApplicant1(final CaseData caseData, final Long id) {
        EmailTemplateName templateName;
        Map<String, String> templateVars;
        boolean partnerAlsoApplied =
            Objects.isNull(caseData.getConditionalOrder().getConditionalOrderApplicant2Questions().getSubmittedDate());

        if (applicantApplyingForCOrder(caseData.getConditionalOrder().getConditionalOrderApplicant1Questions(),
            caseData.getConditionalOrder().getConditionalOrderApplicant2Questions())) {
            log.info("Notifying applicant 1 that their conditional order application has been submitted: {}", id);
            if (caseData.getApplicationType().isSole()) {
                templateName = CITIZEN_APPLIED_FOR_CONDITIONAL_ORDER;
                templateVars = soleTemplateVars(caseData, id, caseData.getApplicant1(), caseData.getApplicant2());
            } else {
                templateName = JOINT_APPLIED_FOR_CONDITIONAL_ORDER;
                templateVars = jointTemplateVars(caseData, id, caseData.getApplicant1(), caseData.getApplicant2(), partnerAlsoApplied);
            }
        } else {
            if (Objects.isNull(caseData.getConditionalOrder().getConditionalOrderApplicant1Questions().getSubmittedDate()))
            log.info("Notifying applicant 1 that their partner has submitted a conditional order application: {}", id);
            templateName = JOINT_PARTNER_APPLIED_FOR_CONDITIONAL_ORDER;
            templateVars = partnerAppliedTemplateVars(caseData, id, caseData.getApplicant1(), caseData.getApplicant2(),
                caseData.getConditionalOrder().getConditionalOrderApplicant2Questions());
        }
        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            templateName,
            templateVars,
            caseData.getApplicant1().getLanguagePreference()
        );
    }

    @Override
    public void sendToApplicant2(final CaseData caseData, final Long id) {
        EmailTemplateName templateName;
        Map<String, String> templateVars;
        boolean partnerAlsoApplied =
            Objects.isNull(caseData.getConditionalOrder().getConditionalOrderApplicant2Questions().getSubmittedDate());
        if (applicantApplyingForCOrder(caseData.getConditionalOrder().getConditionalOrderApplicant2Questions(),
            caseData.getConditionalOrder().getConditionalOrderApplicant1Questions())) {
            log.info("Notifying applicant 2 that their conditional order application has been submitted: {}", id);
            templateName = JOINT_APPLIED_FOR_CONDITIONAL_ORDER;
            templateVars = jointTemplateVars(caseData, id, caseData.getApplicant2(), caseData.getApplicant1(), partnerAlsoApplied);
        } else {
            if (caseData.getApplicationType().isSole()) {
                return;
            }
            log.info("Notifying applicant 2 that their partner has submitted a conditional order application: {}", id);
            templateName = JOINT_PARTNER_APPLIED_FOR_CONDITIONAL_ORDER;
            templateVars = partnerAppliedTemplateVars(caseData, id, caseData.getApplicant2(), caseData.getApplicant1(),
                caseData.getConditionalOrder().getConditionalOrderApplicant1Questions());
        }
        notificationService.sendEmail(
            caseData.getApplicant2().getEmail(),
            templateName,
            templateVars,
            caseData.getApplicant2().getLanguagePreference()
        );
    }

    private boolean applicantApplyingForCOrder(ConditionalOrderQuestions applicantQuestions, ConditionalOrderQuestions partnerQuestions) {
        return Objects.nonNull(applicantQuestions.getSubmittedDate()) &&
            (Objects.isNull(partnerQuestions.getSubmittedDate()) ||
                applicantQuestions.getSubmittedDate().isAfter(partnerQuestions.getSubmittedDate()));
    }

    private Map<String, String> soleTemplateVars(CaseData caseData, Long id, Applicant applicant, Applicant partner) {
        Map<String, String> templateVars = commonContent.mainTemplateVars(caseData, id, applicant, partner);
        templateVars.put(PLUS_21_DUE_DATE,
            caseData.getConditionalOrder()
                .getConditionalOrderApplicant1Questions()
                .getSubmittedDate()
                .plusDays(21).format(DATE_TIME_FORMATTER));
        return templateVars;
    }

    private Map<String, String> jointTemplateVars(CaseData caseData, Long id, Applicant applicant, Applicant partner, boolean partnerApplied) {
        Map<String, String> templateVars = commonContent.mainTemplateVars(caseData, id, applicant, partner);
        templateVars.put(WIFE_APPLIED, "");
        templateVars.put(HUSBAND_APPLIED, "");
        templateVars.put(CIVIL_PARTNER_APPLIED, "");
        templateVars.put(PARTNER_APPLIED, "");
        templateVars.put(WIFE_DID_NOT_APPLY, "");
        templateVars.put(HUSBAND_DID_NOT_APPLY, "");
        templateVars.put(CIVIL_PARTNER_DID_NOT_APPLY, "");
        templateVars.put(PARTNER_DID_NOT_APPLY, "");
        templateVars.put(PLUS_21_DUE_DATE_DUPLICATED,
            caseData.getConditionalOrder()
                .getConditionalOrderApplicant1Questions()
                .getSubmittedDate()
                .plusDays(21).format(DATE_TIME_FORMATTER));
        return templateVars;
    }

    private Map<String, String> partnerAppliedTemplateVars(CaseData caseData, Long id, Applicant applicant, Applicant partner,
                                                           ConditionalOrderQuestions conditionalOrderQuestions) {
        Map<String, String> templateVars = commonContent.mainTemplateVars(caseData, id, applicant, partner);
        templateVars.put(PLUS_14_DUE_DATE, conditionalOrderQuestions.getSubmittedDate().plusDays(14).format(DATE_TIME_FORMATTER));
        return templateVars;
    }
}
