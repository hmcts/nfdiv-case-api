package uk.gov.hmcts.divorce.common.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;
import uk.gov.hmcts.divorce.payment.service.PaymentService;

import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingConditionalOrder;
import static uk.gov.hmcts.divorce.divorcecase.model.State.WelshTranslationReview;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.NOT_PROVIDED;
import static uk.gov.hmcts.divorce.notification.CommonContent.DATE_OF_ISSUE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DISPUTED;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DISSOLUTION;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_UNDISPUTED;
import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.CommonContent.SIGN_IN_URL;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.SUBMISSION_RESPONSE_DATE;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_AOS_SUBMITTED_APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_AOS_SUBMITTED_RESPONDENT_SOLICITOR;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_APPLICANT_DISPUTED_AOS_SUBMITTED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_APPLICANT_DISPUTED_AOS_SUBMITTED_CO;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_RESPONDENT_DISPUTED_AOS_SUBMITTED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_RESPONDENT_DISPUTED_AOS_SUBMITTED_CO;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.getDateTimeFormatterForPreferredLanguage;
import static uk.gov.hmcts.divorce.payment.FeesAndPaymentsUtil.formatAmount;
import static uk.gov.hmcts.divorce.payment.service.PaymentService.EVENT_ISSUE;
import static uk.gov.hmcts.divorce.payment.service.PaymentService.KEYWORD_DIVORCE_ANSWERS;
import static uk.gov.hmcts.divorce.payment.service.PaymentService.SERVICE_OTHER;

@Component
@Slf4j
@RequiredArgsConstructor
public class SoleApplicationDisputedNotification implements ApplicantNotification {

    private static final String ISSUE_DATE_PLUS_37_DAYS = "issue date plus 37 days";
    private static final String ISSUE_DATE_PLUS_141_DAYS = "issue date plus 141 days";
    static final String DISPUTED_AOS_FEE = "disputedAOSFee"; //var in notify template

    private final NotificationService notificationService;

    private final CommonContent commonContent;

    private final PaymentService paymentService;

    @Value("${submit_aos.dispute_offset_days}")
    private int disputeDueDateOffsetDays;

    @Value("${submit_aos.disputedAOS_fee}")
    private String disputedAOSFee; //will pull this in from fee service separate task

    @Override
    public void sendToApplicant1(final CaseDetails<CaseData, State> caseDetails) {
        CaseData caseData = caseDetails.getData();
        Long id = caseDetails.getId();
        log.info("Sending AOS disputed notification to applicant for: {}", id);

        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            getState(caseDetails).equals(AwaitingConditionalOrder)
                ? SOLE_APPLICANT_DISPUTED_AOS_SUBMITTED_CO
                : SOLE_APPLICANT_DISPUTED_AOS_SUBMITTED,
            disputedTemplateVars(caseData, id, caseData.getApplicant1(), caseData.getApplicant2()),
            caseData.getApplicant1().getLanguagePreference(),
            id
        );
    }

    @Override
    public void sendToApplicant2(final CaseDetails<CaseData, State> caseDetails) {
        CaseData caseData = caseDetails.getData();
        Long id = caseDetails.getId();
        log.info("Sending AOS disputed notification to Respondent for: {}", id);

        Map<String, String> templateVars = disputedTemplateVars(caseData, id, caseData.getApplicant2(), caseData.getApplicant1());
        templateVars.put(DISPUTED_AOS_FEE,formatAmount(paymentService.getServiceCost(SERVICE_OTHER, EVENT_ISSUE,KEYWORD_DIVORCE_ANSWERS)));
        notificationService.sendEmail(
            caseData.getApplicant2EmailAddress(),
            getState(caseDetails).equals(AwaitingConditionalOrder)
                ? SOLE_RESPONDENT_DISPUTED_AOS_SUBMITTED_CO
                : SOLE_RESPONDENT_DISPUTED_AOS_SUBMITTED,
            templateVars,
            caseData.getApplicant2().getLanguagePreference(),
            id
        );
    }

    @Override
    public void sendToApplicant1Solicitor(final CaseDetails<CaseData, State> caseDetails) {
        CaseData caseData = caseDetails.getData();
        Long id = caseDetails.getId();
        log.info("Sending AOS disputed notification to Applicant Solicitor for: {}", id);

        notificationService.sendEmail(
            caseData.getApplicant1().getSolicitor().getEmail(),
            SOLE_AOS_SUBMITTED_APPLICANT_1_SOLICITOR,
            solicitorTemplateVars(caseData, id, caseData.getApplicant1()),
            caseData.getApplicant1().getLanguagePreference(),
            id
        );
    }

    @Override
    public void sendToApplicant2Solicitor(final CaseDetails<CaseData, State> caseDetails) {
        CaseData caseData = caseDetails.getData();
        Long id = caseDetails.getId();
        log.info("Sending AOS disputed notification to Respondent Solicitor for: {}", id);

        notificationService.sendEmail(
            caseData.getApplicant2().getSolicitor().getEmail(),
            SOLE_AOS_SUBMITTED_RESPONDENT_SOLICITOR,
            solicitorTemplateVars(caseData, id, caseData.getApplicant2()),
            caseData.getApplicant2().getLanguagePreference(),
            id
        );
    }

    private State getState(final CaseDetails<CaseData, State> caseDetails) {
        return WelshTranslationReview.equals(caseDetails.getState())
            ? caseDetails.getData().getApplication().getWelshPreviousState()
            : caseDetails.getState();
    }

    private Map<String, String> disputedTemplateVars(CaseData caseData, Long id, Applicant applicant, Applicant partner) {
        Map<String, String> templateVars = commonContent.mainTemplateVars(caseData, id, applicant, partner);
        templateVars.put(SUBMISSION_RESPONSE_DATE,
            caseData.getApplication().getIssueDate().plusDays(disputeDueDateOffsetDays)
                    .format(getDateTimeFormatterForPreferredLanguage(applicant.getLanguagePreference())));
        return templateVars;
    }

    private Map<String, String> solicitorTemplateVars(CaseData caseData, Long id, Applicant applicant) {
        var templateVars = commonContent.basicTemplateVars(caseData, id, applicant.getLanguagePreference());

        templateVars.put(IS_DIVORCE, caseData.isDivorce() ? YES : NO);
        templateVars.put(IS_DISSOLUTION, !caseData.isDivorce() ? YES : NO);
        templateVars.put(SIGN_IN_URL, commonContent.getProfessionalUsersSignInUrl(id));

        templateVars.put(ISSUE_DATE_PLUS_37_DAYS,
            caseData.getApplication().getIssueDate().plusDays(disputeDueDateOffsetDays).format(DATE_TIME_FORMATTER));
        templateVars.put(ISSUE_DATE_PLUS_141_DAYS, "");
        templateVars.put(DATE_OF_ISSUE, caseData.getApplication().getIssueDate().format(DATE_TIME_FORMATTER));
        templateVars.put(SOLICITOR_NAME, applicant.getSolicitor().getName());
        templateVars.put(
            SOLICITOR_REFERENCE,
            isNotEmpty(applicant.getSolicitor().getReference()) ? applicant.getSolicitor().getReference() : NOT_PROVIDED
        );

        templateVars.put(IS_UNDISPUTED, NO);
        templateVars.put(IS_DISPUTED, YES);

        return templateVars;
    }
}
