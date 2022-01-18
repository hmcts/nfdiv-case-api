package uk.gov.hmcts.divorce.citizen.notification.conditionalorder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderQuestions;
import uk.gov.hmcts.divorce.divorcecase.model.Gender;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.HashMap;
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
    private static final String PARTNER_DID_NOT_APPLY_DUE_DATE = "partnerDidNotApply due date";

    private static final String APPLICANT1 = "applicant 1";
    private static final String APPLICANT2 = "applicant 2";

    private String submittingUserId;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommonContent commonContent;

    @Override
    public void sendToApplicant1(final CaseData caseData, final Long id) {
        if (!applicant2SubmittedOrder(caseData)) {
            log.info("Notifying applicant 1 that their conditional order application has been submitted: {}", id);
            notificationService.sendEmail(
                caseData.getApplicant1().getEmail(),
                caseData.getApplicationType().isSole() ? CITIZEN_APPLIED_FOR_CONDITIONAL_ORDER : JOINT_APPLIED_FOR_CONDITIONAL_ORDER,
                templateVars(caseData, id, caseData.getApplicant1(), caseData.getApplicant2(), APPLICANT1),
                caseData.getApplicant1().getLanguagePreference()
            );
        } else if (!alreadyApplied(caseData, APPLICANT1)) {
            log.info("Notifying applicant 1 that their partner has submitted a conditional order application: {}", id);
            notificationService.sendEmail(
                caseData.getApplicant1().getEmail(),
                JOINT_PARTNER_APPLIED_FOR_CONDITIONAL_ORDER,
                partnerTemplateVars(caseData, id, caseData.getApplicant1(), caseData.getApplicant2(), APPLICANT2),
                caseData.getApplicant1().getLanguagePreference()
            );
        }
    }

    @Override
    public void sendToApplicant2(final CaseData data, final Long id) {
        if (applicant2SubmittedOrder(data)) {
            log.info("Notifying applicant 2 that their conditional order application has been submitted: {}", id);
            notificationService.sendEmail(
                data.getApplicant2().getEmail(),
                JOINT_APPLIED_FOR_CONDITIONAL_ORDER,
                templateVars(data, id, data.getApplicant2(), data.getApplicant1(), APPLICANT2),
                data.getApplicant2().getLanguagePreference()
            );
        } else if (!data.getApplicationType().isSole() && !alreadyApplied(data, APPLICANT2)) {
            log.info("Notifying applicant 2 that their partner has submitted a conditional order application: {}", id);
            notificationService.sendEmail(
                data.getApplicant2().getEmail(),
                JOINT_PARTNER_APPLIED_FOR_CONDITIONAL_ORDER,
                partnerTemplateVars(data, id, data.getApplicant2(), data.getApplicant1(), APPLICANT1),
                data.getApplicant2().getLanguagePreference()
            );
        }
    }

    private Map<String, String> templateVars(CaseData caseData, Long id, Applicant applicant, Applicant partner, String whichApplicant) {
        Map<String, String> templateVars = commonContent.mainTemplateVars(caseData, id, applicant, partner);
        templateVars.put(PLUS_21_DUE_DATE,
            coQuestions(caseData, whichApplicant).getSubmittedDate().plusDays(21).format(DATE_TIME_FORMATTER));
        if (!caseData.getApplicationType().isSole()) {
            templateVars.putAll(jointTemplateVars(caseData, partner, whichApplicant));
        }
        return templateVars;
    }

    private Map<String, String> partnerTemplateVars(CaseData data, Long id, Applicant applicant, Applicant partner, String whichPartner) {
        Map<String, String> templateVars = commonContent.mainTemplateVars(data, id, applicant, partner);
        templateVars.put(PLUS_14_DUE_DATE,
            coQuestions(data, whichPartner).getSubmittedDate().plusDays(14).format(DATE_TIME_FORMATTER));
        return templateVars;
    }

    private Map<String, String> jointTemplateVars(CaseData data, Applicant partner, String whichApplicant) {
        boolean partnerApplied = alreadyApplied(data, whichPartner(whichApplicant));
        Map<String, String> templateVars = new HashMap<>();
        templateVars.put(WIFE_APPLIED, yesNo(partnerApplied && data.isDivorce() && Gender.FEMALE.equals(partner.getGender())));
        templateVars.put(HUSBAND_APPLIED, yesNo(partnerApplied && data.isDivorce() && Gender.MALE.equals(partner.getGender())));
        templateVars.put(CIVIL_PARTNER_APPLIED, yesNo(partnerApplied && !data.isDivorce()));
        templateVars.put(PARTNER_APPLIED, yesNo(partnerApplied));
        templateVars.put(WIFE_DID_NOT_APPLY,
            yesNo(!partnerApplied && data.isDivorce() && Gender.FEMALE.equals(partner.getGender())));
        templateVars.put(HUSBAND_DID_NOT_APPLY,
            yesNo(!partnerApplied && data.isDivorce() && Gender.MALE.equals(partner.getGender())));
        templateVars.put(CIVIL_PARTNER_DID_NOT_APPLY, yesNo(!partnerApplied && !data.isDivorce()));
        templateVars.put(PARTNER_DID_NOT_APPLY, yesNo(!partnerApplied));
        templateVars.put(PARTNER_DID_NOT_APPLY_DUE_DATE,
            !partnerApplied ? coQuestions(data, whichApplicant).getSubmittedDate().plusDays(14).format(DATE_TIME_FORMATTER) : "");
        return templateVars;
    }

    private boolean applicant2SubmittedOrder(final CaseData caseData) {
        return caseData.getCaseInvite().isApplicant2(submittingUserId);
    }

    private boolean alreadyApplied(CaseData caseData, String whichApplicant) {
        return Objects.nonNull(coQuestions(caseData, whichApplicant).getSubmittedDate());
    }

    private ConditionalOrderQuestions coQuestions(CaseData caseData, String whichApplicant) {
        return whichApplicant.equalsIgnoreCase(APPLICANT1)
            ? caseData.getConditionalOrder().getConditionalOrderApplicant1Questions()
            : caseData.getConditionalOrder().getConditionalOrderApplicant2Questions();
    }

    private String whichPartner(String whichApplicant) {
        return whichApplicant.equalsIgnoreCase(APPLICANT1) ? APPLICANT2 : APPLICANT1;
    }

    private String yesNo(boolean condition) {
        return condition ? "yes" : "no";
    }

    public void setSubmittingUserId(String userId) {
        submittingUserId = userId;
    }
}
