package uk.gov.hmcts.divorce.common.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.document.print.LetterPrinter;
import uk.gov.hmcts.divorce.document.print.documentpack.ConditionalOrderPronouncedDocumentPack;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.EmailTemplateName;
import uk.gov.hmcts.divorce.notification.NotificationService;
import uk.gov.hmcts.divorce.notification.exception.NotificationTemplateException;

import java.time.format.DateTimeFormatter;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static uk.gov.hmcts.divorce.notification.CommonContent.APPLICANT;
import static uk.gov.hmcts.divorce.notification.CommonContent.APPLICANT1_LABEL;
import static uk.gov.hmcts.divorce.notification.CommonContent.APPLICANT2_LABEL;
import static uk.gov.hmcts.divorce.notification.CommonContent.APPLICANT_1;
import static uk.gov.hmcts.divorce.notification.CommonContent.APPLICANT_2;
import static uk.gov.hmcts.divorce.notification.CommonContent.COURT_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.CO_PRONOUNCEMENT_DATE_PLUS_43;
import static uk.gov.hmcts.divorce.notification.CommonContent.CO_PRONOUNCEMENT_DATE_PLUS_43_PLUS_3_MONTHS;
import static uk.gov.hmcts.divorce.notification.CommonContent.DATE_OF_HEARING;
import static uk.gov.hmcts.divorce.notification.CommonContent.RESPONDENT;
import static uk.gov.hmcts.divorce.notification.CommonContent.UNION_TYPE;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.CITIZEN_CONDITIONAL_ORDER_PRONOUNCED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_RESPONDENT_CONDITIONAL_ORDER_PRONOUNCED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLICITOR_CONDITIONAL_ORDER_PRONOUNCED;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.getDateTimeFormatterForPreferredLanguage;

@Component
@Slf4j
@RequiredArgsConstructor
public class ConditionalOrderPronouncedNotification implements ApplicantNotification {

    public static final String MISSING_FIELD_MESSAGE = "Notification failed with missing field '%s' for Case Id: %s";

    @Value("${final_order.eligible_from_offset_days}")
    private long finalOrderOffsetDays;

    @Value("${final_order.respondent_eligible_from_offset_months}")
    private long finalOrderRespondentOffsetMonth;

    private final NotificationService notificationService;

    private final CommonContent commonContent;

    private final ConditionalOrderPronouncedDocumentPack conditionalOrderPronouncedDocumentPack;
    private final LetterPrinter letterPrinter;

    @Override
    public void sendToApplicant1(final CaseData caseData, final Long caseId) {
        log.info("Notifying applicant 1 that their conditional order application has been pronounced: {}", caseId);

        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            CITIZEN_CONDITIONAL_ORDER_PRONOUNCED,
            templateVars(caseData, caseId, caseData.getApplicant1(), caseData.getApplicant2()),
            caseData.getApplicant1().getLanguagePreference(),
            caseId
        );
    }

    @Override
    public void sendToApplicant1Solicitor(CaseData caseData, Long caseId) {
        log.info("Notifying applicant 1 solicitor that their conditional order application has been pronounced: {}", caseId);

        notificationService.sendEmail(
            caseData.getApplicant1().getSolicitor().getEmail(),
            SOLICITOR_CONDITIONAL_ORDER_PRONOUNCED,
            solicitorTemplateVars(caseData, caseId, caseData.getApplicant1()),
            caseData.getApplicant1().getLanguagePreference(),
            caseId
        );
    }

    @Override
    public void sendToApplicant1Offline(final CaseData caseData, final Long caseId) {
        log.info("Sending conditional order letter to applicant 1 for case: {}", caseId);
        final Applicant applicant1 = caseData.getApplicant1();
        var documentPackInfo = conditionalOrderPronouncedDocumentPack.getDocumentPack(caseData, applicant1);
        letterPrinter.sendLetters(caseData, caseId, applicant1, documentPackInfo, conditionalOrderPronouncedDocumentPack.getLetterId());
    }

    @Override
    public void sendToApplicant2(final CaseData caseData, final Long caseId) {
        log.info("Notifying applicant 2 that their conditional order application has been granted: {}", caseId);

        EmailTemplateName emailTemplateName =
            caseData.getApplicationType().isSole() ? SOLE_RESPONDENT_CONDITIONAL_ORDER_PRONOUNCED : CITIZEN_CONDITIONAL_ORDER_PRONOUNCED;
        notificationService.sendEmail(
            caseData.getApplicant2EmailAddress(),
            emailTemplateName,
            templateVars(caseData, caseId, caseData.getApplicant2(), caseData.getApplicant1()),
            caseData.getApplicant2().getLanguagePreference(),
            caseId
        );
    }

    @Override
    public void sendToApplicant2Solicitor(CaseData caseData, Long caseId) {

        log.info("Notifying applicant 2 solicitor that their conditional order application has been pronounced: {}", caseId);

        notificationService.sendEmail(
            caseData.getApplicant2().getSolicitor().getEmail(),
            SOLICITOR_CONDITIONAL_ORDER_PRONOUNCED,
            solicitorTemplateVars(caseData, caseId, caseData.getApplicant2()),
            caseData.getApplicant2().getLanguagePreference(),
            caseId
        );
    }

    @Override
    public void sendToApplicant2Offline(final CaseData caseData, final Long caseId) {
        log.info("Sending conditional order letter to applicant 2 for case: {}", caseId);
        Applicant applicant2 = caseData.getApplicant2();
        var documentPackInfo = conditionalOrderPronouncedDocumentPack.getDocumentPack(caseData, applicant2);
        letterPrinter.sendLetters(caseData, caseId, applicant2, documentPackInfo, conditionalOrderPronouncedDocumentPack.getLetterId());
    }

    private Map<String, String> templateVars(final CaseData caseData,
                                             final Long caseId,
                                             final Applicant applicant,
                                             final Applicant partner) {

        final ConditionalOrder conditionalOrder = caseData.getConditionalOrder();
        if (isNull(conditionalOrder)) {
            throw new NotificationTemplateException(format(MISSING_FIELD_MESSAGE, "conditionalOrder", caseId));
        }
        if (isNull(conditionalOrder.getCourt())) {
            throw new NotificationTemplateException(format(MISSING_FIELD_MESSAGE, "coCourt", caseId));
        }
        if (isNull(conditionalOrder.getDateAndTimeOfHearing())) {
            throw new NotificationTemplateException(format(MISSING_FIELD_MESSAGE, "coDateAndTimeOfHearing", caseId));
        }
        if (isNull(conditionalOrder.getGrantedDate())) {
            throw new NotificationTemplateException(format(MISSING_FIELD_MESSAGE, "coGrantedDate", caseId));
        }

        DateTimeFormatter dateTimeFormatter = getDateTimeFormatterForPreferredLanguage(applicant.getLanguagePreference());

        final Map<String, String> templateVars = commonContent.mainTemplateVars(caseData, caseId, applicant, partner);
        templateVars.put(COURT_NAME, conditionalOrder.getCourt().getLabel());
        templateVars.put(DATE_OF_HEARING, conditionalOrder.getDateAndTimeOfHearing().format(dateTimeFormatter));
        templateVars.put(CO_PRONOUNCEMENT_DATE_PLUS_43,
            conditionalOrder.getGrantedDate().plusDays(finalOrderOffsetDays).format(dateTimeFormatter));
        templateVars.put(CO_PRONOUNCEMENT_DATE_PLUS_43_PLUS_3_MONTHS,
            conditionalOrder.getGrantedDate().plusDays(finalOrderOffsetDays)
                .plusMonths(finalOrderRespondentOffsetMonth).format(dateTimeFormatter));
        return templateVars;
    }

    private Map<String, String> solicitorTemplateVars(final CaseData caseData,
                                                      final Long caseId,
                                                      final Applicant applicant) {

        final Map<String, String> templateVars = commonContent.solicitorTemplateVars(caseData, caseId, applicant);
        templateVars.put(APPLICANT1_LABEL, caseData.getApplicationType().isSole() ? APPLICANT : APPLICANT_1);
        templateVars.put(APPLICANT2_LABEL, caseData.getApplicationType().isSole() ? RESPONDENT : APPLICANT_2);
        templateVars.put(UNION_TYPE, commonContent.getUnionType(caseData));
        templateVars.put(CO_PRONOUNCEMENT_DATE_PLUS_43,
            caseData.getConditionalOrder().getGrantedDate().plusDays(finalOrderOffsetDays).format(DATE_TIME_FORMATTER));
        return templateVars;
    }
}
