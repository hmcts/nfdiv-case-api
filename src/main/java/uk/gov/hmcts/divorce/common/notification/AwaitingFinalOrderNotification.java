package uk.gov.hmcts.divorce.common.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.print.LetterPrinter;
import uk.gov.hmcts.divorce.document.print.documentpack.ApplyForFinalOrderDocumentPack;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Map;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.divorce.notification.CommonContent.DATE_FINAL_ORDER_ELIGIBLE_FROM_PLUS_3_MONTHS;
import static uk.gov.hmcts.divorce.notification.CommonContent.DATE_OF_ISSUE;
import static uk.gov.hmcts.divorce.notification.CommonContent.FINAL_ORDER_OVERDUE_DATE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DISSOLUTION;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_JOINT;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_REMINDER;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_SOLE;
import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.CommonContent.SIGN_IN_URL;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.UNION_TYPE;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.APPLICANT_APPLY_FOR_FINAL_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.APPLY_FOR_FINAL_ORDER_SOLICITOR;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.getDateTimeFormatterForPreferredLanguage;

@Component
@Slf4j
@RequiredArgsConstructor
public class AwaitingFinalOrderNotification implements ApplicantNotification {

    private final CommonContent commonContent;

    private final NotificationService notificationService;

    private final ApplyForFinalOrderDocumentPack applyForFinalOrderDocumentPack;

    private final LetterPrinter letterPrinter;

    @Override
    public void sendToApplicant1(final CaseData caseData, final Long id) {
        log.info("Notifying applicant 1 that they can apply for a final order: {}", id);
        final Applicant applicant1 = caseData.getApplicant1();
        final Applicant applicant2 = caseData.getApplicant2();

        notificationService.sendEmail(
            applicant1.getEmail(),
            APPLICANT_APPLY_FOR_FINAL_ORDER,
            templateVars(caseData, id, applicant1, applicant2),
            applicant1.getLanguagePreference(),
            id
        );
    }

    @Override
    public void sendToApplicant2(final CaseData caseData, final Long id) {
        final Applicant applicant1 = caseData.getApplicant1();
        final Applicant applicant2 = caseData.getApplicant2();

        if (!caseData.getApplicationType().isSole()) {
            log.info("Notifying applicant 2 (joint application) that they can apply for a final order: {}", id);

            notificationService.sendEmail(
                caseData.getApplicant2EmailAddress(),
                APPLICANT_APPLY_FOR_FINAL_ORDER,
                templateVars(caseData, id, applicant2, applicant1),
                applicant2.getLanguagePreference(),
                id
            );
        }
    }

    @Override
    public void sendToApplicant1Solicitor(final CaseData caseData, final Long id) {
        log.info("Notifying applicant 1 solicitor that they can apply for a final order: {}", id);

        Applicant applicant1 = caseData.getApplicant1();
        final Map<String, String> templateVars = commonSolicitorTemplateVars(caseData, id, applicant1);

        notificationService.sendEmail(
            applicant1.getSolicitor().getEmail(),
            APPLY_FOR_FINAL_ORDER_SOLICITOR,
            templateVars,
            applicant1.getLanguagePreference(),
            id
        );
    }

    @Override
    public void sendToApplicant2Solicitor(final CaseData caseData, final Long id) {
        if (!caseData.getApplicationType().isSole()) {
            log.info("Notifying applicant 2 solicitor (joint application) that they can apply for a final order: {}", id);

            Applicant applicant2 = caseData.getApplicant2();
            final Map<String, String> templateVars = commonSolicitorTemplateVars(caseData, id, applicant2);

            notificationService.sendEmail(
                applicant2.getSolicitor().getEmail(),
                APPLY_FOR_FINAL_ORDER_SOLICITOR,
                templateVars,
                applicant2.getLanguagePreference(),
                id
            );
        }
    }

    @Override
    public void sendToApplicant1Offline(CaseData caseData, Long caseId) {
        log.info("Notifying offline {} that they can apply for a final order: {}",
            caseData.getApplicationType().isSole() ? "applicant" : "applicant 1", caseId);
        var documentPack = applyForFinalOrderDocumentPack.getDocumentPack(caseData, caseData.getApplicant1());
        letterPrinter.sendLetters(
            caseData,
            caseId,
            caseData.getApplicant1(),
            documentPack,
            applyForFinalOrderDocumentPack.getLetterId());
    }

    @Override
    public void sendToApplicant2Offline(CaseData caseData, Long caseId) {
        if (!caseData.getApplicationType().isSole()) {
            log.info("Notifying offline applicant 2 that they can apply for a final order: {}", caseId);
            var documentPack = applyForFinalOrderDocumentPack.getDocumentPack(caseData, caseData.getApplicant2());
            letterPrinter.sendLetters(
                caseData,
                caseId,
                caseData.getApplicant2(),
                documentPack,
                applyForFinalOrderDocumentPack.getLetterId());
        }
    }

    private Map<String, String> templateVars(CaseData caseData, Long id, Applicant applicant, Applicant partner) {
        Map<String, String> templateVars = commonContent.conditionalOrderTemplateVars(caseData, id, applicant, partner);
        templateVars.put(IS_REMINDER, NO);
        templateVars.put(DATE_FINAL_ORDER_ELIGIBLE_FROM_PLUS_3_MONTHS,
            caseData.getFinalOrder().getDateFinalOrderEligibleToRespondent()
                    .format(getDateTimeFormatterForPreferredLanguage(applicant.getLanguagePreference())));
        templateVars.put(FINAL_ORDER_OVERDUE_DATE, caseData.getFinalOrder().getDateFinalOrderEligibleFrom().plusMonths(12)
            .format(getDateTimeFormatterForPreferredLanguage(applicant.getLanguagePreference())));
        return templateVars;
    }

    private Map<String, String> commonSolicitorTemplateVars(CaseData caseData, final Long id, Applicant applicant) {

        final Map<String, String> templateVars = commonContent.basicTemplateVars(caseData, id);

        templateVars.put(SOLICITOR_NAME, applicant.getSolicitor().getName());
        templateVars.put(UNION_TYPE, commonContent.getUnionType(caseData));
        templateVars.put(DATE_OF_ISSUE, caseData.getApplication().getIssueDate().format(DATE_TIME_FORMATTER));
        templateVars.put(SOLICITOR_REFERENCE, nonNull(applicant.getSolicitor().getReference())
            ? applicant.getSolicitor().getReference()
            : "not provided");
        templateVars.put(SIGN_IN_URL, commonContent.getProfessionalUsersSignInUrl(id));
        templateVars.put(IS_DIVORCE, caseData.isDivorce() ? YES : NO);
        templateVars.put(IS_DISSOLUTION, !caseData.isDivorce() ? YES : NO);
        templateVars.put(IS_SOLE, caseData.getApplicationType().isSole() ? YES : NO);
        templateVars.put(IS_JOINT, !caseData.getApplicationType().isSole() ? YES : NO);
        templateVars.put(FINAL_ORDER_OVERDUE_DATE, caseData.getFinalOrder().getDateFinalOrderEligibleFrom().plusMonths(12)
            .format(getDateTimeFormatterForPreferredLanguage(applicant.getLanguagePreference())));

        return templateVars;
    }
}
