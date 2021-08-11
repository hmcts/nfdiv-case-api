package uk.gov.hmcts.divorce.citizen.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Map;

import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT1_APPLICANT2_REJECTED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT2_APPLICANT2_REJECTED;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.DIVORCE_OR_END_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.FOR_A_APPLICATION;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.FOR_DIVORCE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.FOR_YOUR_APPLICATION;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.FOR_YOUR_DIVORCE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.TO_DIVORCE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.TO_END_CIVIL_PARTNERSHIP;

@Component
@Slf4j
public class Applicant2NotBrokenNotification {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommonContent commonContent;

    public void sendToApplicant1(CaseData caseData, Long id) {
        Map<String, String> templateVars = commonContent.templateVarsForApplicant(
            caseData, caseData.getApplicant1(), caseData.getApplicant2());

        if (caseData.getDivorceOrDissolution().isDivorce()) {
            templateVars.put(FOR_YOUR_APPLICATION, FOR_DIVORCE);
            templateVars.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP, TO_DIVORCE);
        } else {
            templateVars.put(FOR_YOUR_APPLICATION, TO_END_CIVIL_PARTNERSHIP);
            templateVars.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP, TO_END_CIVIL_PARTNERSHIP);
        }

        log.info("Sending applicant 2 reject notification to applicant 1 for case : {}", id);

        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            JOINT_APPLICANT1_APPLICANT2_REJECTED,
            templateVars,
            caseData.getApplicant1().getLanguagePreference()
        );
    }

    public void sendToApplicant2(CaseData caseData, Long id) {
        Map<String, String> templateVars = commonContent.templateVarsForApplicant(
            caseData, caseData.getApplicant2(), caseData.getApplicant1());

        if (caseData.getDivorceOrDissolution().isDivorce()) {
            templateVars.put(FOR_YOUR_APPLICATION, FOR_YOUR_DIVORCE);
            templateVars.put(FOR_A_APPLICATION, "for a divorce");
        } else {
            templateVars.put(FOR_YOUR_APPLICATION, TO_END_CIVIL_PARTNERSHIP);
            templateVars.put(FOR_A_APPLICATION, TO_END_CIVIL_PARTNERSHIP);
        }

        log.info("Sending applicant 2 reject notification to applicant 1 for case : {}", id);

        notificationService.sendEmail(
            caseData.getCaseInvite().getApplicant2InviteEmailAddress(),
            JOINT_APPLICANT2_APPLICANT2_REJECTED,
            templateVars,
            caseData.getApplicant1().getLanguagePreference()
        );
    }

}
