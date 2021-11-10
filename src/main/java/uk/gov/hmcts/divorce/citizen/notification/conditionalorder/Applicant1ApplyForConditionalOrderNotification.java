package uk.gov.hmcts.divorce.citizen.notification.conditionalorder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Gender;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Map;

import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.CommonContent.isDivorce;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.CITIZEN_APPLY_FOR_CONDITIONAL_ORDER;

@Component
@Slf4j
public class Applicant1ApplyForConditionalOrderNotification {

    public static final String JOINT_CONDITIONAL_ORDER = "joint conditional order";
    public static final String HUSBAND_JOINT = "husbandJoint";
    public static final String WIFE_JOINT = "wifeJoint";
    public static final String CIVIL_PARTNER_JOINT = "civilPartnerJoint";

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommonContent commonContent;

    public void sendToApplicant1(CaseData caseData, Long id) {
        log.info("Sending notification to applicant 1 to notify them that they can apply for a conditional order: {}", id);

        Map<String, String> templateVars = commonContent.mainTemplateVars(caseData, id, caseData.getApplicant1(), caseData.getApplicant2());

        boolean jointApplication = !caseData.getApplicationType().isSole();
        templateVars.put(JOINT_CONDITIONAL_ORDER, jointApplication ? YES : NO);
        templateVars.put(HUSBAND_JOINT,
            jointApplication && isDivorce(caseData) && caseData.getApplicant2().getGender().equals(Gender.MALE) ? YES : NO);
        templateVars.put(WIFE_JOINT,
            jointApplication && isDivorce(caseData) && caseData.getApplicant2().getGender().equals(Gender.FEMALE) ? YES : NO);
        templateVars.put(CIVIL_PARTNER_JOINT, jointApplication && !isDivorce(caseData) ? YES : NO);

        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            CITIZEN_APPLY_FOR_CONDITIONAL_ORDER,
            templateVars,
            caseData.getApplicant1().getLanguagePreference()
        );
    }
}
