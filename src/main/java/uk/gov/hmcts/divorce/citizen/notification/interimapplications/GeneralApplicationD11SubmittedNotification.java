package uk.gov.hmcts.divorce.citizen.notification.interimapplications;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplication;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralParties;
import uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference;
import uk.gov.hmcts.divorce.divorcecase.model.ServicePaymentMethod;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.GeneralApplicationNotification;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE;
import static uk.gov.hmcts.divorce.notification.CommonContent.MADE_PAYMENT;
import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.CommonContent.SUBMISSION_RESPONSE_DATE;
import static uk.gov.hmcts.divorce.notification.CommonContent.USED_HELP_WITH_FEES;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.GENERAL_APPLICATION_D11_AWAITING_DOCUMENTS;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.GENERAL_APPLICATION_D11_SUBMITTED;
import static uk.gov.hmcts.divorce.notification.FormatUtil.getDateTimeFormatterForPreferredLanguage;

@Component
@Slf4j
@RequiredArgsConstructor
public class GeneralApplicationD11SubmittedNotification implements GeneralApplicationNotification {

    @Value("${interim_application.response_offset_days}")
    private long interimApplicationResponseOffsetDays;

    private final NotificationService notificationService;
    private final CommonContent commonContent;

    private static final String HAS_PRONOUNCEMENT_GUIDANCE = "hasPronouncementGuidance";
    private static final String EMAIL_BODY_LINE1 = "emailBodyLine1";
    private static final String GEN_APP_SUBMITTED_EN = "Your general application has been submitted";
    private static final String GEN_APP_SUBMITTED_CY = "Your general application has been submitted";

    public void sendToApplicant(final CaseData caseData, final Long caseId, GeneralApplication generalApplication) {
        final GeneralParties generalParties = generalApplication.getGeneralApplicationParty();

        log.info("Sending d11 general application submitted notification to {} on case id {}", generalParties.getLabel(), caseId);

        final boolean isApplicant1 = GeneralParties.APPLICANT.equals(generalParties);
        final Applicant applicant = isApplicant1 ? caseData.getApplicant1() : caseData.getApplicant2();
        final Applicant partner = isApplicant1 ? caseData.getApplicant2() : caseData.getApplicant1();
        boolean awaitingDocuments = YesOrNo.NO.equals(generalApplication.getGeneralApplicationDocsUploadedPreSubmission());

        final boolean wasAwaitingPronouncement = State.AwaitingPronouncement.equals(caseData.getApplication().getPreviousState());
        final boolean isSoleRespondent = !isApplicant1 && ApplicationType.SOLE_APPLICATION.equals(caseData.getApplicationType());
        final boolean hasPronouncementGuidance = wasAwaitingPronouncement && !isSoleRespondent;

        Map<String, String> templateVars = genAppTemplateVars(caseData, caseId, applicant, partner, generalApplication);

        templateVars.put(HAS_PRONOUNCEMENT_GUIDANCE, hasPronouncementGuidance ? YES : NO);

        notificationService.sendEmail(
            applicant.getEmail(),
            awaitingDocuments ? GENERAL_APPLICATION_D11_AWAITING_DOCUMENTS : GENERAL_APPLICATION_D11_SUBMITTED,
            templateVars,
            applicant.getLanguagePreference(),
            caseId
        );
    }

    private Map<String, String> genAppTemplateVars(
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

            templateVars.put(SUBMISSION_RESPONSE_DATE, responseDate.format(dateTimeFormatter));
        } else {
            templateVars.put(SUBMISSION_RESPONSE_DATE, "");
        }

        templateVars.put(EMAIL_BODY_LINE1, getContentBasedOnGenAppType(generalApplication.getGeneralApplicationType(),
            applicant.getLanguagePreference(), caseData.isDivorce()));

        return templateVars;
    }

    private String getContentBasedOnGenAppType(GeneralApplicationType generalApplicationType,
                                               LanguagePreference languagePreference,
                                               boolean isDivorce) {
        Map<GeneralApplicationType, String> translationMap =
            languagePreference == LanguagePreference.WELSH ? getWelshTranslationMap(isDivorce) : getEnglishTranslationMap(isDivorce);

        return translationMap.getOrDefault(
            generalApplicationType,
            languagePreference == LanguagePreference.WELSH
                ? GEN_APP_SUBMITTED_CY
                : GEN_APP_SUBMITTED_EN
        );
    }

    private Map<GeneralApplicationType, String> getEnglishTranslationMap(boolean isDivorce) {
        String divorceOrDissolution = isDivorce ? "divorce" : "dissolution";
        String marriageCertificate = isDivorce ? MARRIAGE : CIVIL_PARTNERSHIP;
        return Map.of(
            GeneralApplicationType.AMEND_APPLICATION,
                String.format("Your application to amend your %s case has been submitted", divorceOrDissolution),
            GeneralApplicationType.EXPEDITE,
                String.format("Your application to complete your %s case more quickly has been submitted",
                        divorceOrDissolution),
            GeneralApplicationType.EXTEND,
                String.format("Your application to get more time to serve your %s case has been submitted",
                        divorceOrDissolution),
            GeneralApplicationType.ISSUE_DIVORCE_WITHOUT_CERT,
                String.format("Your application to continue your %s case without a %s certificate has been submitted",
                        divorceOrDissolution,
                marriageCertificate),
            GeneralApplicationType.DELAY,
                String.format("Your application to delay or pause your %s case has been submitted",
                        divorceOrDissolution),
            GeneralApplicationType.OTHER,
                String.format("Your general application relating to your %s case has been submitted",
                        divorceOrDissolution),
            GeneralApplicationType.WITHDRAW_POST_ISSUE,
                String.format("Your application to withdraw your %s case has been submitted", divorceOrDissolution)
        );
    }

    private Map<GeneralApplicationType, String> getWelshTranslationMap(boolean isDivorce) {
        return getEnglishTranslationMap(isDivorce);
    }
}
