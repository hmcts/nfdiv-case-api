package uk.gov.hmcts.divorce.notification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.NOT_PROVIDED;
import static uk.gov.hmcts.divorce.notification.CommonContent.DATE_PLUS_14_DAYS;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;

@Component
public class SwitchToSoleSolicitorTemplateContent {

    @Autowired
    Clock clock;

    @Autowired
    CommonContent commonContent;

    public static final String APPLICANT_1_NAME = "applicant 1 name";
    public static final String APPLICANT_2_NAME = "applicant 2 name";

    public Map<String, String> templatevars(CaseData caseData, Long caseId, Applicant applicant, Applicant partner) {
        Map<String, String> templateContent = commonContent.mainTemplateVars(caseData, caseId, applicant, partner);

        templateContent.put(APPLICANT_1_NAME, caseData.getApplicant1().getFullName());
        templateContent.put(APPLICANT_2_NAME, caseData.getApplicant2().getFullName());
        templateContent.put(SOLICITOR_REFERENCE,
            isNotEmpty(applicant.getSolicitor().getReference())
                ? applicant.getSolicitor().getReference()
                : NOT_PROVIDED);
        templateContent.put(SOLICITOR_NAME, applicant.getSolicitor().getName());
        templateContent.put(DATE_PLUS_14_DAYS, LocalDate.now(clock).plusDays(14).format(DATE_TIME_FORMATTER));

        return templateContent;
    }
}
