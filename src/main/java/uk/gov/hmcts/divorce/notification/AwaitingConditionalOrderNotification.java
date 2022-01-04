package uk.gov.hmcts.divorce.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Gender;

import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.divorce.notification.CommonContent.IS_REMINDER;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.CommonContent.isDivorce;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.CITIZEN_APPLY_FOR_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLICITOR_AWAITING_CONDITIONAL_ORDER;

@Component
@Slf4j
public class AwaitingConditionalOrderNotification {

    public static final String JOINT_CONDITIONAL_ORDER = "joint conditional order";
    public static final String HUSBAND_JOINT = "husbandJoint";
    public static final String WIFE_JOINT = "wifeJoint";
    public static final String CIVIL_PARTNER_JOINT = "civilPartnerJoint";

    @Autowired
    private CommonContent commonContent;

    @Autowired
    private NotificationService notificationService;

    public void sendToApplicant1(CaseData caseData, Long id, boolean isReminder) {
        if (caseData.getApplicant1().isRepresented()) {
            sendToSolicitor(caseData, id, caseData.getApplicant1());
        } else {
            log.info("Notifying applicant 1 that they can apply for a conditional order: {}", id);
            sendToCitizen(caseData, id, caseData.getApplicant1(), caseData.getApplicant2(), isReminder);
        }
    }

    public void sendToApplicant2(CaseData caseData, Long id, boolean isReminder) {
        if (caseData.getApplicant2().isRepresented()) {
            sendToSolicitor(caseData, id, caseData.getApplicant2());
        } else if (Objects.nonNull(caseData.getApplicant2().getEmail())) {
            log.info("Notifying applicant 2 that they can apply for a conditional order: {}", id);
            sendToCitizen(caseData, id, caseData.getApplicant2(), caseData.getApplicant1(), isReminder);
        }
    }

    private void sendToCitizen(CaseData caseData, Long id, Applicant applicant, Applicant partner, boolean isReminder) {
        log.info("Notifying applicant that they can apply for a conditional order: {}", id);
        notificationService.sendEmail(
            applicant.getEmail(),
            CITIZEN_APPLY_FOR_CONDITIONAL_ORDER,
            templateVars(caseData, id, applicant, partner, isReminder),
            applicant.getLanguagePreference()
        );
    }

    private void sendToSolicitor(CaseData caseData, Long caseId, Applicant applicant) {

        final Map<String, String> templateVars = commonContent.basicTemplateVars(caseData, caseId);
        templateVars.put(SOLICITOR_NAME, applicant.getSolicitor().getName());

        notificationService.sendEmail(
            applicant.getSolicitor().getEmail(),
            SOLICITOR_AWAITING_CONDITIONAL_ORDER,
            templateVars,
            applicant.getLanguagePreference()
        );
        log.info("Successfully sent awaiting conditional order notification for case : {}", caseId);
    }

    private Map<String, String> templateVars(CaseData caseData, Long id, Applicant applicant, Applicant partner, boolean isReminder) {
        Map<String, String> templateVars = commonContent.mainTemplateVars(caseData, id, applicant, partner);
        boolean jointApplication = !caseData.getApplicationType().isSole();
        templateVars.put(JOINT_CONDITIONAL_ORDER, jointApplication ? YES : CommonContent.NO);
        templateVars.put(HUSBAND_JOINT,
            jointApplication && isDivorce(caseData) && partner.getGender().equals(Gender.MALE) ? YES : CommonContent.NO);
        templateVars.put(WIFE_JOINT,
            jointApplication && isDivorce(caseData) && partner.getGender().equals(Gender.FEMALE) ? YES : CommonContent.NO);
        templateVars.put(CIVIL_PARTNER_JOINT, jointApplication && !isDivorce(caseData) ? YES : CommonContent.NO);
        templateVars.put(IS_REMINDER, isReminder ? YES : CommonContent.NO);
        return templateVars;
    }
}
