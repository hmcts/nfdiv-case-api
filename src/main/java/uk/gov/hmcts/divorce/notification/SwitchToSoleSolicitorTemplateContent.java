package uk.gov.hmcts.divorce.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Map;

import static uk.gov.hmcts.divorce.notification.CommonContent.DATE_PLUS_14_DAYS;
import static uk.gov.hmcts.divorce.notification.FormatUtil.getDateTimeFormatterForPreferredLanguage;

@Component
@RequiredArgsConstructor
public class SwitchToSoleSolicitorTemplateContent {

    private final Clock clock;

    private final CommonContent commonContent;

    public static final String APPLICANT_1_NAME = "applicant 1 name";
    public static final String APPLICANT_2_NAME = "applicant 2 name";

    public Map<String, String> templatevars(CaseData caseData, Long caseId, Applicant applicant, Applicant partner) {
        Map<String, String> templateContent = commonContent.mainTemplateVars(caseData, caseId, applicant, partner);

        templateContent.put(APPLICANT_1_NAME, caseData.getApplicant1().getFullName());
        templateContent.put(APPLICANT_2_NAME, caseData.getApplicant2().getFullName());
        templateContent.putAll(commonContent.solicitorTemplateVars(caseData, caseId, applicant));
        templateContent.put(DATE_PLUS_14_DAYS, LocalDate.now(clock).plusDays(14).format(
            getDateTimeFormatterForPreferredLanguage(applicant.getLanguagePreference())));
        return templateContent;
    }
}
