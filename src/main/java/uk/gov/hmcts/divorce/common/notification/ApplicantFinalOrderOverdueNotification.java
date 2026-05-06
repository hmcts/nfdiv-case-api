package uk.gov.hmcts.divorce.common.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.document.print.LetterPrinter;
import uk.gov.hmcts.divorce.document.print.documentpack.SoleApplicantFinalOrderOverdueDocumentPack;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ISSUE_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.NOT_PROVIDED;
import static uk.gov.hmcts.divorce.notification.CommonContent.FINAL_ORDER_OVERDUE_DATE;
import static uk.gov.hmcts.divorce.notification.CommonContent.SIGN_IN_URL;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_APPLICANT_FINAL_ORDER_OVERDUE;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_APPLICANT_SOLICITOR_FINAL_ORDER_OVERDUE;
import static uk.gov.hmcts.divorce.notification.FormatUtil.getDateTimeFormatterForPreferredLanguage;

@Component
@Slf4j
@RequiredArgsConstructor
public class ApplicantFinalOrderOverdueNotification implements ApplicantNotification {

    private final CommonContent commonContent;

    private final NotificationService notificationService;
    private final SoleApplicantFinalOrderOverdueDocumentPack soleApplicantFinalOrderOverdueDocumentPack;
    private final LetterPrinter letterPrinter;

    @Override
    public void sendToApplicant1(final CaseData caseData, final Long id) {
        if (caseData.getApplicationType().isSole()) {
            log.info("Sending notification to applicant 1 that final order is overdue: {}", id);
            final Applicant applicant1 = caseData.getApplicant1();
            final Applicant applicant2 = caseData.getApplicant2();

            notificationService.sendEmail(
                applicant1.getEmail(),
                SOLE_APPLICANT_FINAL_ORDER_OVERDUE,
                templateVars(caseData, id, applicant1, applicant2),
                applicant1.getLanguagePreference(),
                id
            );
        }
    }

    @Override
    public void sendToApplicant1Solicitor(final CaseData caseData, final Long id) {
        if (caseData.getApplicationType().isSole()) {
            final Applicant applicant1 = caseData.getApplicant1();

            log.info("Notifying applicant's solicitor that final order is overdue: {}", id);

            notificationService.sendEmail(
                applicant1.getSolicitor().getEmail(),
                SOLE_APPLICANT_SOLICITOR_FINAL_ORDER_OVERDUE,
                solicitorTemplateVars(caseData, id, applicant1),
                applicant1.getLanguagePreference(),
                id
            );
        }
    }

    @Override
    public void sendToApplicant1Offline(final CaseData caseData, final Long id) {
        if (caseData.getApplicationType().isSole()) {
            log.info("Notifying offline applicant that final order is overdue: {}", id);
            var documentPack = soleApplicantFinalOrderOverdueDocumentPack.getDocumentPack(caseData, caseData.getApplicant1());
            letterPrinter.sendLetters(
                caseData,
                id,
                caseData.getApplicant1(),
                documentPack,
                soleApplicantFinalOrderOverdueDocumentPack.getLetterId());
        }
    }

    private Map<String, String> solicitorTemplateVars(CaseData caseData, Long id, Applicant applicant) {
        var languagePreference = applicant.getLanguagePreference();
        var templateVars = commonContent.basicTemplateVars(caseData, id, languagePreference);

        templateVars.put(ISSUE_DATE, caseData.getApplication().getIssueDate().format(
            getDateTimeFormatterForPreferredLanguage(languagePreference)
        ));
        templateVars.put(SIGN_IN_URL, commonContent.getProfessionalUsersSignInUrl(id));

        Solicitor applicantSolicitor = applicant.getSolicitor();
        templateVars.put(SOLICITOR_NAME, applicantSolicitor.getName());
        templateVars.put(
            SOLICITOR_REFERENCE,
            isNotEmpty(applicantSolicitor.getReference()) ? applicantSolicitor.getReference() : NOT_PROVIDED);

        return templateVars;
    }

    private Map<String, String> templateVars(CaseData caseData, Long id, Applicant applicant, Applicant partner) {
        Map<String, String> templateVars = commonContent.conditionalOrderTemplateVars(caseData, id, applicant, partner);
        templateVars.put(FINAL_ORDER_OVERDUE_DATE, caseData.getConditionalOrder().getGrantedDate().plusMonths(12)
            .format(getDateTimeFormatterForPreferredLanguage(applicant.getLanguagePreference())));
        return templateVars;
    }
}
