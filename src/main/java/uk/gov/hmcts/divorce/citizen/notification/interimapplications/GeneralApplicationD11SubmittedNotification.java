package uk.gov.hmcts.divorce.citizen.notification.interimapplications;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplication;
import uk.gov.hmcts.divorce.divorcecase.model.ServicePaymentMethod;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE;
import static uk.gov.hmcts.divorce.notification.CommonContent.MADE_PAYMENT;
import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.CommonContent.USED_HELP_WITH_FEES;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.GENERAL_APPLICATION_D11_AWAITING_DOCUMENTS;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.GENERAL_APPLICATION_D11_SUBMITTED;
import static uk.gov.hmcts.divorce.notification.FormatUtil.getDateTimeFormatterForPreferredLanguage;

@Component
@Slf4j
@RequiredArgsConstructor
public class GeneralApplicationD11SubmittedNotification implements ApplicantNotification {

    @Value("${interim_application.response_offset_days}")
    private long interimApplicationResponseOffsetDays;

    private final NotificationService notificationService;
    private final CommonContent commonContent;

    public void sendToApplicant1(final CaseData caseData, final Long caseId, GeneralApplication generalApplication) {
        log.info("Sending d11 general application submitted notification to applicant 1 on case id {}", caseId);

        boolean awaitingDocuments = YesOrNo.NO.equals(generalApplication.getGeneralApplicationDocsUploadedPreSubmission());

        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            awaitingDocuments ? GENERAL_APPLICATION_D11_AWAITING_DOCUMENTS : GENERAL_APPLICATION_D11_SUBMITTED,
            templateVars(caseData, caseId, caseData.getApplicant1(), caseData.getApplicant2(), generalApplication),
            caseData.getApplicant1().getLanguagePreference(),
            caseId
        );
    }

    private Map<String, String> templateVars(
        CaseData caseData, Long id, Applicant applicant, Applicant partner,
        GeneralApplication generalApplication
    ) {

        Map<String, String> templateVars = commonContent.mainTemplateVars(caseData, id, applicant, partner);

        boolean madePayment = ServicePaymentMethod.FEE_PAY_BY_CARD.equals(
            generalApplication.getGeneralApplicationFee().getPaymentMethod()
        );

        templateVars.put(USED_HELP_WITH_FEES, !madePayment ? YES : NO);
        templateVars.put(MADE_PAYMENT, madePayment ? YES : NO);

        if (madePayment) {
            DateTimeFormatter dateTimeFormatter = getDateTimeFormatterForPreferredLanguage(applicant.getLanguagePreference());
            LocalDate submittedDate = generalApplication.getGeneralApplicationFee().getDateOfPayment();
            LocalDate responseDate = submittedDate.plusDays(interimApplicationResponseOffsetDays);

            templateVars.put(DATE, responseDate.format(dateTimeFormatter));
        }

        return templateVars;
    }
}
