package uk.gov.hmcts.divorce.common.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.print.LetterPrinter;
import uk.gov.hmcts.divorce.document.print.documentpack.ApplyForConditionalOrderDocumentPack;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Map;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.divorce.notification.CommonContent.DATE_OF_ISSUE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DISSOLUTION;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_FINAL_ORDER;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_REMINDER;
import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.CommonContent.SIGN_IN_URL;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.UNION_TYPE;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.APPLICANT_SOLICITOR_CAN_APPLY_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.CITIZEN_APPLY_FOR_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLY_FOR_CONDITIONAL_FINAL_ORDER_SOLICITOR;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;

@Component
@Slf4j
@RequiredArgsConstructor
public class AwaitingConditionalOrderNotification implements ApplicantNotification {

    private final CommonContent commonContent;
    private final NotificationService notificationService;
    private final LetterPrinter letterPrinter;
    private final ApplyForConditionalOrderDocumentPack applyForConditionalOrderDocumentPack;

    @Override
    public void sendToApplicant1(final CaseData caseData, final Long id) {
        log.info("Notifying applicant 1 that they can apply for a conditional order: {}", id);
        final Applicant applicant1 = caseData.getApplicant1();
        final Map<String, String> templateVars = commonContent
            .conditionalOrderTemplateVars(caseData, id, applicant1, caseData.getApplicant2());
        templateVars.put(IS_REMINDER, NO);

        notificationService.sendEmail(
            applicant1.getEmail(),
            CITIZEN_APPLY_FOR_CONDITIONAL_ORDER,
            templateVars,
            applicant1.getLanguagePreference(),
            id
        );
    }

    @Override
    public void sendToApplicant1Solicitor(final CaseData caseData, final Long id) {
        log.info("Notifying applicant 1 solicitor that they can apply for a conditional order: {}", id);

        Applicant applicant1 = caseData.getApplicant1();
        final Map<String, String> templateVars = commonSolicitorTemplateVars(caseData, id, applicant1);

        if (caseData.getApplicationType().isSole()) {
            notificationService.sendEmail(
                applicant1.getSolicitor().getEmail(),
                APPLICANT_SOLICITOR_CAN_APPLY_CONDITIONAL_ORDER,
                templateVars,
                applicant1.getLanguagePreference(),
                id
            );
        } else {
            templateVars.put(IS_CONDITIONAL_ORDER, YES);
            templateVars.put(IS_FINAL_ORDER, NO);

            notificationService.sendEmail(
                applicant1.getSolicitor().getEmail(),
                JOINT_APPLY_FOR_CONDITIONAL_FINAL_ORDER_SOLICITOR,
                templateVars,
                applicant1.getLanguagePreference(),
                id
            );
        }
    }

    @Override
    public void sendToApplicant1Offline(final CaseData caseData, final Long id) {
        log.info("Notifying applicant 1 offline that they can apply for a conditional order: {}", id);
        final Applicant applicant1 = caseData.getApplicant1();
        var documentPackInfo = applyForConditionalOrderDocumentPack.getDocumentPack(caseData, applicant1);
        letterPrinter.sendLetters(caseData, id, applicant1, documentPackInfo, applyForConditionalOrderDocumentPack.getLetterId());
    }

    public void sendToApplicant2(final CaseData caseData, final Long id) {
        if (!caseData.getApplicationType().isSole() && nonNull(caseData.getApplicant2().getEmail())) {
            log.info("Notifying applicant 2 that they can apply for a conditional order: {}", id);
            final Applicant applicant2 = caseData.getApplicant2();
            final Map<String, String> templateVars = commonContent
                .conditionalOrderTemplateVars(caseData, id, applicant2, caseData.getApplicant1());
            templateVars.put(IS_REMINDER, NO);

            notificationService.sendEmail(
                applicant2.getEmail(),
                CITIZEN_APPLY_FOR_CONDITIONAL_ORDER,
                templateVars,
                applicant2.getLanguagePreference(),
                id
            );
        }
    }

    @Override
    public void sendToApplicant2Solicitor(final CaseData caseData, final Long id) {
        if (!caseData.getApplicationType().isSole()) {
            log.info("Notifying applicant 2 solicitor that they can apply for a conditional order: {}", id);

            Applicant applicant2 = caseData.getApplicant2();
            final Map<String, String> templateVars = commonSolicitorTemplateVars(caseData, id, applicant2);
            templateVars.put(IS_CONDITIONAL_ORDER, YES);
            templateVars.put(IS_FINAL_ORDER, NO);

            notificationService.sendEmail(
                applicant2.getSolicitor().getEmail(),
                JOINT_APPLY_FOR_CONDITIONAL_FINAL_ORDER_SOLICITOR,
                templateVars,
                applicant2.getLanguagePreference(),
                id
            );
        }
    }

    @Override
    public void sendToApplicant2Offline(final CaseData caseData, final Long id) {
        if (!caseData.getApplicationType().isSole()) {
            log.info("Notifying applicant 2 offline that they can apply for a conditional order: {}", id);
            final Applicant applicant2 = caseData.getApplicant2();
            var documentPackInfo = applyForConditionalOrderDocumentPack.getDocumentPack(caseData, applicant2);
            letterPrinter.sendLetters(caseData, id, applicant2, documentPackInfo, applyForConditionalOrderDocumentPack.getLetterId());
        }
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

        return templateVars;
    }
}
