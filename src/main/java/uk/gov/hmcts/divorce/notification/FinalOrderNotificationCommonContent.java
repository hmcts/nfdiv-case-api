package uk.gov.hmcts.divorce.notification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Map;

import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.FormatUtil.getDateTimeFormatterForPreferredLanguage;

@Component
public class FinalOrderNotificationCommonContent {

    public static final String WILL_BE_CHECKED_WITHIN_2_DAYS = "will be checked within 2 days";
    public static final String WILL_BE_CHECKED_WITHIN_14_DAYS = "will be checked within 14 days";
    public static final String NOW_PLUS_14_DAYS = "now plus 14 days";
    public static final String IS_REMINDER = "isReminder";

    @Autowired
    private CommonContent commonContent;

    @Autowired
    private Clock clock;

    public Map<String, String> jointApplicantTemplateVars(CaseData caseData, Long id, Applicant applicant, Applicant partner,
                                                          boolean isReminder) {
        Map<String, String> templateVars =
            commonContent.mainTemplateVars(caseData, id, applicant, partner);

        templateVars.put(NOW_PLUS_14_DAYS, getNowPlus14Days(applicant));
        templateVars.put(IS_REMINDER, isReminder ? YES : NO);

        return templateVars;
    }

    public String getNowPlus14Days(Applicant applicant) {
        return LocalDate.now(clock).plusDays(14)
            .format(getDateTimeFormatterForPreferredLanguage(applicant.getLanguagePreference()));
    }
}
