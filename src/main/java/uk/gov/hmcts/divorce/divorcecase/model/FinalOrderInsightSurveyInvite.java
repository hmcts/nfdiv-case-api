package uk.gov.hmcts.divorce.divorcecase.model;

import lombok.Getter;
import uk.gov.hmcts.divorce.notification.EmailTemplateName;

import java.util.List;

import static uk.gov.hmcts.divorce.notification.EmailTemplateName.INSIGHT_SURVEY_FINAL_REMINDER_FINAL_ORDER_COMPLETE;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.INSIGHT_SURVEY_FIRST_REMINDER_FINAL_ORDER_COMPLETE;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.INSIGHT_SURVEY_INVITE_FINAL_ORDER_COMPLETE;

@Getter
public enum FinalOrderInsightSurveyInvite {
    FIRST_NOTIFICATION(0, INSIGHT_SURVEY_INVITE_FINAL_ORDER_COMPLETE, 1),
    FIRST_REMINDER(1, INSIGHT_SURVEY_FIRST_REMINDER_FINAL_ORDER_COMPLETE, 3),
    FINAL_REMINDER(2, INSIGHT_SURVEY_FINAL_REMINDER_FINAL_ORDER_COMPLETE, 6);

    private final int stage;
    private final EmailTemplateName emailTemplateName;
    private final int daysAfterGrantedDate;

    FinalOrderInsightSurveyInvite(int stage, EmailTemplateName emailTemplateName, int daysAfterGrantedDate) {
        this.stage = stage;
        this.emailTemplateName = emailTemplateName;
        this.daysAfterGrantedDate = daysAfterGrantedDate;
    }

    public static final List<FinalOrderInsightSurveyInvite> BY_STAGE =
        List.of(FIRST_NOTIFICATION, FIRST_REMINDER, FINAL_REMINDER);
}
