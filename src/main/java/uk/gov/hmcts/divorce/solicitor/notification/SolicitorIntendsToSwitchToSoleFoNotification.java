package uk.gov.hmcts.divorce.solicitor.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Map;

import static java.lang.String.join;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.NOT_PROVIDED;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.OTHER_APPLICANT_INTENDS_TO_SWITCH_TO_SOLE_FO_CITIZEN;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.OTHER_APPLICANT_INTENDS_TO_SWITCH_TO_SOLE_FO_SOLICITOR;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;

@Slf4j
@Component
public class SolicitorIntendsToSwitchToSoleFoNotification implements ApplicantNotification {

    public static final String DATE_PLUS_14_DAYS = "date plus 14 days";
    public static final String APPLICANT_1_NAME = "applicant 1 name";
    public static final String APPLICANT_2_NAME = "applicant 2 name";

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommonContent commonContent;

    @Autowired
    private Clock clock;

    @Override
    public void sendToApplicant1Solicitor(CaseData caseData, Long caseId) {

        if (YES.equals(caseData.getFinalOrder().getDoesApplicant2IntendToSwitchToSole())) {
            log.info("Notifying applicant 1 solicitor that other applicant intends to switch to sole fo : {}", caseId);

            notificationService.sendEmail(
                caseData.getApplicant1().getSolicitor().getEmail(),
                OTHER_APPLICANT_INTENDS_TO_SWITCH_TO_SOLE_FO_SOLICITOR,
                solicitorTemplateContent(caseData, caseId, caseData.getApplicant1(), caseData.getApplicant2()),
                caseData.getApplicant1().getLanguagePreference()
            );
        }
    }

    @Override
    public void sendToApplicant2Solicitor(CaseData caseData, Long caseId) {

        if (YES.equals(caseData.getFinalOrder().getDoesApplicant1IntendToSwitchToSole())) {
            log.info("Notifying applicant 2 solicitor that other applicant intends to switch to sole fo : {}", caseId);

            notificationService.sendEmail(
                caseData.getApplicant2().getSolicitor().getEmail(),
                OTHER_APPLICANT_INTENDS_TO_SWITCH_TO_SOLE_FO_SOLICITOR,
                solicitorTemplateContent(caseData, caseId, caseData.getApplicant2(), caseData.getApplicant1()),
                caseData.getApplicant2().getLanguagePreference()
            );
        }
    }

    @Override
    public void sendToApplicant2(CaseData caseData, Long caseId) {

        if (YES.equals(caseData.getFinalOrder().getDoesApplicant1IntendToSwitchToSole())) {
            log.info("Notifying applicant 2 that other applicant intends to switch to sole fo : {}", caseId);

            Map<String, String> templateContent = commonContent.mainTemplateVars(
                caseData,
                caseId,
                caseData.getApplicant2(),
                caseData.getApplicant1()
            );
            templateContent.put(DATE_PLUS_14_DAYS, LocalDate.now(clock).plusDays(14).format(DATE_TIME_FORMATTER));

            notificationService.sendEmail(
                caseData.getApplicant2EmailAddress(),
                OTHER_APPLICANT_INTENDS_TO_SWITCH_TO_SOLE_FO_CITIZEN,
                templateContent,
                caseData.getApplicant2().getLanguagePreference()
            );
        }
    }

    private Map<String, String> solicitorTemplateContent(CaseData caseData, Long caseId, Applicant applicant, Applicant partner) {
        Map<String, String> templateContent = commonContent.mainTemplateVars(caseData, caseId, applicant, partner);

        templateContent.put(APPLICANT_1_NAME,
            join(" ", caseData.getApplicant1().getFirstName(), caseData.getApplicant1().getLastName()));
        templateContent.put(APPLICANT_2_NAME,
            join(" ", caseData.getApplicant2().getFirstName(), caseData.getApplicant2().getLastName()));
        templateContent.put(SOLICITOR_REFERENCE,
            isNotEmpty(applicant.getSolicitor().getReference())
                ? applicant.getSolicitor().getReference()
                : NOT_PROVIDED);
        templateContent.put(SOLICITOR_NAME, applicant.getSolicitor().getName());
        templateContent.put(DATE_PLUS_14_DAYS, LocalDate.now(clock).plusDays(14).format(DATE_TIME_FORMATTER));

        return templateContent;
    }
}
