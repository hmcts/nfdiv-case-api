package uk.gov.hmcts.divorce.citizen.notification.interimapplications;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.time.format.DateTimeFormatter;
import java.util.Map;

import static uk.gov.hmcts.divorce.notification.CommonContent.MADE_PAYMENT;
import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.CommonContent.SUBMISSION_RESPONSE_DATE;
import static uk.gov.hmcts.divorce.notification.CommonContent.USED_HELP_WITH_FEES;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.ALTERNATIVE_SERVICE_APPLICATION_AWAITING_DOCUMENTS;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.ALTERNATIVE_SERVICE_APPLICATION_SUBMITTED;
import static uk.gov.hmcts.divorce.notification.FormatUtil.getDateTimeFormatterForPreferredLanguage;

@Component
@Slf4j
@RequiredArgsConstructor
public class AlternativeServiceApplicationSubmittedNotification implements ApplicantNotification {

    private final NotificationService notificationService;
    private final CommonContent commonContent;

    @Value("${interim_application.response_offset_days}")
    private long applicationResponseOffsetDays;

    public static final String MULTIPLE_WAYS_SELECTED = "multipleWays";
    public static final String DIFFERENT_WAY_SELECTED = "differentWay";
    public static final String OPTIONAL_PARTNER_LABEL = "partnerOptional";

    @Override
    public void sendToApplicant1(final CaseData caseData, final Long caseId) {
        log.info("Sending alternative service application submitted notification to applicant 1 on case id {}", caseId);

        AlternativeService serviceApplication = caseData.getAlternativeService();
        boolean awaitingDocuments = YesOrNo.NO.equals(serviceApplication.getServiceApplicationDocsUploadedPreSubmission());

        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            awaitingDocuments ? ALTERNATIVE_SERVICE_APPLICATION_AWAITING_DOCUMENTS : ALTERNATIVE_SERVICE_APPLICATION_SUBMITTED,
            templateVars(caseData, caseId, caseData.getApplicant1(), caseData.getApplicant2()),
            caseData.getApplicant1().getLanguagePreference(),
            caseId
        );
    }

    private Map<String, String> templateVars(CaseData caseData, Long id, Applicant applicant, Applicant partner) {
        Map<String, String> templateVars = commonContent.mainTemplateVars(caseData, id, applicant, partner);

        AlternativeService serviceApplication = caseData.getAlternativeService();
        boolean madePayment = YesOrNo.YES.equals(serviceApplication.getAlternativeServiceFeeRequired());
        DateTimeFormatter dateTimeFormatter = getDateTimeFormatterForPreferredLanguage(applicant.getLanguagePreference());

        String responseDate = serviceApplication.getReceivedServiceApplicationDate()
            .plusDays(applicationResponseOffsetDays)
            .format(dateTimeFormatter);
        templateVars.put(MADE_PAYMENT, madePayment ? YES : NO);
        templateVars.put(USED_HELP_WITH_FEES, !madePayment ? YES : NO);
        templateVars.put(SUBMISSION_RESPONSE_DATE, madePayment ? responseDate : "");

        return templateVars;
    }
}
