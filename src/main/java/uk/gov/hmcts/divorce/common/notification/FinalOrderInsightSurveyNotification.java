package uk.gov.hmcts.divorce.common.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrderInsightSurveyInvite;
import uk.gov.hmcts.divorce.divorcecase.model.NoticeOfChange.WhichApplicant;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.EmailTemplateName;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class FinalOrderInsightSurveyNotification implements ApplicantNotification {

    private final CommonContent commonContent;
    private final NotificationService notificationService;

    public static final String INSIGHT_SURVEY_URL_VARIABLE = "insightSurveyUrl";
    public static final String INSIGHT_SURVEY_URL_VALUE = "https://www.smartsurvey.co.uk/t/onlinedivorceservice/";
    public static final String YOUR_DATA_URL_VARIABLE = "yourDataUrl";
    public static final String YOUR_DATA_URL_VALUE =
        "https://www.gov.uk/government/organisations/hm-courts-and-tribunals-service/about/personal-information-charter";

    private static final Map<String, String> INSIGHT_SURVEY_VARIABLES = Map.of(
        INSIGHT_SURVEY_URL_VARIABLE, INSIGHT_SURVEY_URL_VALUE,
        YOUR_DATA_URL_VARIABLE, YOUR_DATA_URL_VALUE
    );

    @Override
    public void sendToApplicant1(final CaseData caseData, final Long id) {
        log.info("Sending final order survey notification to applicant 1: {}", id);

        sendCitizenNotification(caseData, id, WhichApplicant.APPLICANT_1);
    }

    @Override
    public void sendToApplicant2(final CaseData caseData, final Long id) {
        sendCitizenNotification(caseData, id, WhichApplicant.APPLICANT_2);
    }

    private void sendCitizenNotification(final CaseData data, final long caseId, final WhichApplicant whichApplicant) {
        final boolean isApplicant1 = WhichApplicant.APPLICANT_1.equals(whichApplicant);
        final Applicant applicant = isApplicant1 ? data.getApplicant1() : data.getApplicant2();
        final Applicant partner = isApplicant1 ? data.getApplicant2() : data.getApplicant1();

        final FinalOrder finalOrder = data.getFinalOrder();
        final List<FinalOrderInsightSurveyInvite> inviteStages = FinalOrderInsightSurveyInvite.BY_STAGE;

        final int notificationsSent = finalOrder.getFinalOrderInsightSurveyStage();
        if (notificationsSent >= inviteStages.size()) {
            log.info("Aborting final order insight survey notification for {}, all stages have been sent.", caseId);
            return;
        }

        final FinalOrderInsightSurveyInvite inviteStage = FinalOrderInsightSurveyInvite.BY_STAGE.get(notificationsSent);
        final LocalDateTime earliestNotificationDate = finalOrder.getGrantedDate().plusDays(inviteStage.getDaysAfterGrantedDate());
        if (earliestNotificationDate.isAfter(LocalDateTime.now())) {
            log.info("Aborting final order insight survey notification for {}, not eligible for next stage yet.", caseId);
            return;
        }

        final EmailTemplateName emailTemplate = inviteStage.getEmailTemplateName();
        log.info("Sending final order insight survey for {}, stage: {}, party: {}", caseId, emailTemplate, whichApplicant.getLabel());

        final Map<String, String> templateVars = commonContent.mainTemplateVars(data, caseId, applicant, partner);

        templateVars.putAll(INSIGHT_SURVEY_VARIABLES);

        notificationService.sendEmail(
            applicant.getEmail(),
            emailTemplate,
            templateVars,
            applicant.getLanguagePreference(),
            caseId
        );
    }
}
