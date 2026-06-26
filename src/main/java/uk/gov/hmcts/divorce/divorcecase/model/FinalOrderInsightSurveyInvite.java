package uk.gov.hmcts.divorce.divorcecase.model;

import lombok.Getter;
import uk.gov.hmcts.divorce.notification.EmailTemplateName;

import java.util.List;

import static uk.gov.hmcts.divorce.notification.EmailTemplateName.FINAL_ORDER_INSIGHT_SURVEY_LAST_REMINDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.FINAL_ORDER_INSIGHT_SURVEY_FIRST_REMINDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.FINAL_ORDER_INSIGHT_SURVEY_INVITE;

@Getter
public enum FinalOrderInsightSurveyInvite {
    FIRST_NOTIFICATION(0, FINAL_ORDER_INSIGHT_SURVEY_INVITE, 1),
    FIRST_REMINDER(1, FINAL_ORDER_INSIGHT_SURVEY_FIRST_REMINDER, 3),
    LAST_REMINDER(2, FINAL_ORDER_INSIGHT_SURVEY_LAST_REMINDER, 6);

    private final int stage;
    private final EmailTemplateName emailTemplateName;
    private final int daysAfterGrantedDate;

    FinalOrderInsightSurveyInvite(int stage, EmailTemplateName emailTemplateName, int daysAfterGrantedDate) {
        this.stage = stage;
        this.emailTemplateName = emailTemplateName;
        this.daysAfterGrantedDate = daysAfterGrantedDate;
    }

    public static final List<FinalOrderInsightSurveyInvite> BY_STAGE =
        List.of(FIRST_NOTIFICATION, FIRST_REMINDER, LAST_REMINDER);
}
