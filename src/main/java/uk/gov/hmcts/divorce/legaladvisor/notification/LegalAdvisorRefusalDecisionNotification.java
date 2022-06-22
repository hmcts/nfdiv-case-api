package uk.gov.hmcts.divorce.legaladvisor.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.RefusalOption;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Map;

import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.RefusalOption.MORE_INFO;
import static uk.gov.hmcts.divorce.divorcecase.model.RefusalOption.REJECT;
import static uk.gov.hmcts.divorce.notification.CommonContent.APPLICANT;
import static uk.gov.hmcts.divorce.notification.CommonContent.APPLICANT_1;
import static uk.gov.hmcts.divorce.notification.CommonContent.APPLICANT_2;
import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.CommonContent.RESPONDENT;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLICITOR_CO_REFUSED_SOLE_JOINT;

@Component
@Slf4j
public class LegalAdvisorRefusalDecisionNotification implements ApplicantNotification {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommonContent commonContent;

    @Override
    public void sendToApplicant1Solicitor(final CaseData caseData, final Long caseId) {

        RefusalOption refusalOption = caseData.getConditionalOrder().getRefusalDecision();

        log.info("Sending CO refused notification to applicant 1 solicitor, with reason {} for case : {}",
            refusalOption.getLabel(), caseId);

        sendEmail(caseData, caseId, caseData.getApplicant1(), refusalOption);

        log.info("Successfully sent CO refused notification to applicant 2 with reason {} for case : {}",
            refusalOption.getLabel(), caseId);
    }

    @Override
    public void sendToApplicant2Solicitor(final CaseData caseData, final Long caseId) {

        RefusalOption refusalOption = caseData.getConditionalOrder().getRefusalDecision();

        log.info("Sending CO refused notification to applicant 2 solicitor with reason {} for case : {}",
            refusalOption.getLabel(), caseId);

        sendEmail(caseData, caseId, caseData.getApplicant2(), refusalOption);

        log.info("Successfully sent CO refused notification to applicant 2 with reason {} for case : {}",
            refusalOption.getLabel(), caseId);
    }

    private void sendEmail(final CaseData caseData, final Long caseId, final Applicant applicant, RefusalOption refusalOption) {

        final Map<String, String> templateVars = commonContent.solicitorTemplateVars(caseData, caseId, applicant);

        boolean isSole = caseData.getApplicationType().isSole();

        templateVars.put("moreInfo", refusalOption.equals(MORE_INFO) ? YES : NO);
        templateVars.put("amendApplication", refusalOption.equals(REJECT) ? YES : NO);
        templateVars.put("isJoint", isSole ? NO : YES);
        templateVars.put("applicant1Label", isSole ? APPLICANT : APPLICANT_1);
        templateVars.put("applicant2Label", isSole ? RESPONDENT : APPLICANT_2);

        notificationService.sendEmail(
            applicant.getSolicitor().getEmail(),
            SOLICITOR_CO_REFUSED_SOLE_JOINT,
            templateVars,
            ENGLISH
        );

    }
}
