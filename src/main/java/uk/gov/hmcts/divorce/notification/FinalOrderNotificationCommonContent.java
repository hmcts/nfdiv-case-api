package uk.gov.hmcts.divorce.notification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.FormatUtil.getDateTimeFormatterForPreferredLanguage;

@Component
public class FinalOrderNotificationCommonContent {

    public static final String WILL_BE_CHECKED_WITHIN_2_DAYS = "will be checked within 2 days";
    public static final String WILL_BE_CHECKED_WITHIN_14_DAYS = "will be checked within 14 days";
    public static final String NOW_PLUS_14_DAYS = "now plus 14 days";
    public static final String IS_REMINDER = "isReminder";
    public static final String IN_TIME = "inTime";
    public static final String IS_OVERDUE = "isOverdue";
    private static final int FINAL_ORDER_OFFSET_DAYS = 14;

    public static final String DELAY_REASON_STATIC_CONTENT = "They applied more than 12 months after the conditional order "
        + "was made and gave the following reason:\n%s";

    @Autowired
    private CommonContent commonContent;

    @Autowired
    private Clock clock;

    public Map<String, String> jointApplicantTemplateVars(CaseData caseData, Long id, Applicant applicant, Applicant partner,
                                                          boolean isReminder) {
        Map<String, String> templateVars =
            commonContent.mainTemplateVars(caseData, id, applicant, partner);

        templateVars.put(NOW_PLUS_14_DAYS, isReminder
            ? getNowPlus14Days(applicant, caseData.getFinalOrder())
            : getNowPlus14Days(applicant));

        templateVars.put(IS_REMINDER, isReminder ? YES : NO);

        return templateVars;
    }

    public Map<String, String> applicant2SolicitorTemplateVars(CaseData caseData, Long id) {
        Map<String, String> templateVars = commonContent.solicitorTemplateVars(caseData, id, caseData.getApplicant2());

        commonContent.setIsDivorceAndIsDissolutionVariables(caseData, templateVars);

        commonContent.setOverdueAndInTimeVariables(caseData, templateVars);

        return templateVars;
    }

    public String getNowPlus14Days(Applicant applicant) {
        return LocalDate.now(clock).plusDays(FINAL_ORDER_OFFSET_DAYS)
            .format(getDateTimeFormatterForPreferredLanguage(applicant.getLanguagePreference()));
    }

    public String getNowPlus14Days(Applicant applicant, FinalOrder finalOrder) {
        return finalOrder.getDateFinalOrderSubmitted() != null
            ? finalOrder.getDateFinalOrderSubmitted().plusDays(FINAL_ORDER_OFFSET_DAYS)
            .format(getDateTimeFormatterForPreferredLanguage(applicant.getLanguagePreference()))
            : "";
    }

    public static String getPartnerDelayReason(final YesOrNo isFinalOrderOverdue, final String partnerDelayReason) {
        return YesOrNo.YES.equals(isFinalOrderOverdue)
            ? Optional.ofNullable(partnerDelayReason)
            .map(DELAY_REASON_STATIC_CONTENT::formatted)
            .orElse(DELAY_REASON_STATIC_CONTENT.formatted(EMPTY))
            : EMPTY;
    }
}
