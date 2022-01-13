package uk.gov.hmcts.divorce.citizen.notification.conditionalorder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
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

    private static final String CO_REVIEWED_BY_DATE = "date email received plus 21 days";

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommonContent commonContent;

    @Override
    public void sendToApplicant1(final CaseData caseData, final Long id) {
        if (Objects.nonNull(caseData.getConditionalOrder().getConditionalOrderApplicant1Questions().getSubmittedDate())) {
            log.info("Notifying applicant 1 that their conditional order application has been submitted: {}", id);
            if(caseData.getApplicationType().isSole()) {
                notificationService.sendEmail(
                    caseData.getApplicant1().getEmail(),
                    CITIZEN_APPLIED_FOR_CONDITIONAL_ORDER,
                    templateVars(caseData, id, caseData.getApplicant1(), caseData.getApplicant2()),
                    caseData.getApplicant1().getLanguagePreference()
                );
            } else {
                notificationService.sendEmail(
                    caseData.getApplicant1().getEmail(),
                    JOINT_APPLIED_FOR_CONDITIONAL_ORDER,
                    templateVars(caseData, id, caseData.getApplicant1(), caseData.getApplicant2()),
                    caseData.getApplicant1().getLanguagePreference()
                );
            }
        } else {
            log.info("Notifying applicant 1 that their partner has submitted a conditional order application: {}", id);
            notificationService.sendEmail(
                caseData.getApplicant1().getEmail(),
                JOINT_PARTNER_APPLIED_FOR_CONDITIONAL_ORDER,
                templateVars(caseData, id, caseData.getApplicant1(), caseData.getApplicant2()),
                caseData.getApplicant1().getLanguagePreference()
            );
        }
    }

    @Override
    public void sendToApplicant2(final CaseData caseData, final Long id) {
        if (Objects.nonNull(caseData.getConditionalOrder().getConditionalOrderApplicant2Questions().getSubmittedDate())) {
            log.info("Notifying applicant 2 that their conditional order application has been submitted: {}", id);
            notificationService.sendEmail(
                caseData.getApplicant2().getEmail(),
                JOINT_APPLIED_FOR_CONDITIONAL_ORDER,
                templateVars(caseData, id, caseData.getApplicant2(), caseData.getApplicant1()),
                caseData.getApplicant2().getLanguagePreference()
            );

        } else {
            log.info("Notifying applicant 2 that their partner has submitted a conditional order application: {}", id);
            notificationService.sendEmail(
                caseData.getApplicant2().getEmail(),
                JOINT_PARTNER_APPLIED_FOR_CONDITIONAL_ORDER,
                templateVars(caseData, id, caseData.getApplicant2(), caseData.getApplicant1()),
                caseData.getApplicant2().getLanguagePreference()
            );
        }
    }

    private Map<String, String> templateVars(CaseData caseData, Long id, Applicant applicant, Applicant partner) {
        Map<String, String> templateVars = commonContent.mainTemplateVars(caseData, id, applicant, partner);
        templateVars.put(CO_REVIEWED_BY_DATE,
            caseData.getConditionalOrder()
                .getConditionalOrderApplicant1Questions()
                .getSubmittedDate()
                .plusDays(21).format(DATE_TIME_FORMATTER));
        return templateVars;
    }
}
