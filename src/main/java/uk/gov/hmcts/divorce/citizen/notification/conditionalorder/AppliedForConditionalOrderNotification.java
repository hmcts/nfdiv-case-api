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

import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.CITIZEN_APPLIED_FOR_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLIED_FOR_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_PARTNER_APPLIED_FOR_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;

@Component
@Slf4j
public class AppliedForConditionalOrderNotification implements ApplicantNotification {

    static final String PLUS_21_DUE_DATE = "date email received plus 21 days";
    static final String PLUS_14_DUE_DATE = "application for CO date plus 14 days";
    static final String WIFE_APPLIED = "wifeApplied";
    static final String HUSBAND_APPLIED = "husbandApplied";
    static final String CIVIL_PARTNER_APPLIED = "civilPartnerApplied";
    static final String PARTNER_APPLIED = "partnerApplied";
    static final String WIFE_DID_NOT_APPLY = "wifeDidNotApply";
    static final String HUSBAND_DID_NOT_APPLY = "husbandDidNotApply";
    static final String CIVIL_PARTNER_DID_NOT_APPLY = "civilPartnerDidNotApply";
    static final String PARTNER_DID_NOT_APPLY = "partnerDidNotApply";
    static final String PARTNER_DID_NOT_APPLY_DUE_DATE = "partnerDidNotApply due date";

    static final String APPLICANT1 = "applicant 1";
    static final String APPLICANT2 = "applicant 2";

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
        Map<String, String> templateVars = defaultJointTemplateVars();
        if (alreadyApplied(data, whichPartner(whichApplicant))) {
            templateVars.putAll(divorceDissolutionJointTemplateVars(
                partner,
                data.isDivorce(),
                WIFE_APPLIED,
                HUSBAND_APPLIED,
                CIVIL_PARTNER_APPLIED
            ));
            templateVars.put(PARTNER_APPLIED, YES);
        } else {
            templateVars.putAll(divorceDissolutionJointTemplateVars(
                partner,
                data.isDivorce(),
                WIFE_DID_NOT_APPLY,
                HUSBAND_DID_NOT_APPLY,
                CIVIL_PARTNER_DID_NOT_APPLY
            ));
            templateVars.put(PARTNER_DID_NOT_APPLY, YES);
            templateVars.put(PARTNER_DID_NOT_APPLY_DUE_DATE,
                coQuestions(data, whichApplicant).getSubmittedDate().plusDays(14).format(DATE_TIME_FORMATTER));
        }
        return templateVars;
    }

    private Map<String, String> divorceDissolutionJointTemplateVars(
        Applicant partner, boolean isDivorce, String wifeVar, String husbandVar, String civilPartnerVar
    ) {
        Map<String, String> templateVars = new HashMap<>();
        if (isDivorce) {
            templateVars.put(wifeVar, Gender.FEMALE.equals(partner.getGender()) ? YES : NO);
            templateVars.put(husbandVar, Gender.MALE.equals(partner.getGender()) ? YES : NO);
        } else {
            templateVars.put(civilPartnerVar, YES);
        }
        return templateVars;
    }

    private Map<String, String> defaultJointTemplateVars() {
        Map<String, String> templateVars = new HashMap<>();
        templateVars.put(WIFE_APPLIED, NO);
        templateVars.put(HUSBAND_APPLIED, NO);
        templateVars.put(CIVIL_PARTNER_APPLIED, NO);
        templateVars.put(PARTNER_APPLIED, NO);
        templateVars.put(WIFE_DID_NOT_APPLY, NO);
        templateVars.put(HUSBAND_DID_NOT_APPLY, NO);
        templateVars.put(CIVIL_PARTNER_DID_NOT_APPLY, NO);
        templateVars.put(PARTNER_DID_NOT_APPLY, NO);
        templateVars.put(PARTNER_DID_NOT_APPLY_DUE_DATE, "");
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

    public void setSubmittingUserId(String userId) {
        submittingUserId = userId;
    }
}
