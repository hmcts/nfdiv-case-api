package uk.gov.hmcts.divorce.document.content;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference;
import uk.gov.hmcts.divorce.divorcecase.util.AddressUtil;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Map;

import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_APPLICATION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_APPLICATION_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.END_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.END_CIVIL_PARTNERSHIP_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.JUDICIAL_SEPARATION_APPLICATION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.JUDICIAL_SEPARATION_APPLICATION_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RECIPIENT_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RECIPIENT_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.THE_APPLICATION;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaperApplicationReceivedTemplateContent {

    private final DocmosisCommonContent docmosisCommonContent;
    private final Clock clock;

    public static final String DATE_OF_RESPONSE = "dateOfResponse";
    private static final int SUBMISSION_RESPONSE_DAYS = 28;

    public Map<String, Object> getTemplateContent(CaseData caseData, Long caseId, Applicant applicant) {
        Map<String, Object> templateContent = docmosisCommonContent
                .getBasicDocmosisTemplateContent(applicant.getLanguagePreference());

        templateContent.put(CASE_REFERENCE, formatId(caseId));
        templateContent.put(RECIPIENT_NAME,  applicant.getFullName());
        templateContent.put(RECIPIENT_ADDRESS,  AddressUtil.getPostalAddress(applicant.getAddress()));
        templateContent.put(IS_DIVORCE, caseData.isDivorce());
        templateContent.put(DATE_OF_RESPONSE, LocalDate.now(clock)
            .plusDays(SUBMISSION_RESPONSE_DAYS).format(DATE_TIME_FORMATTER));
        templateContent.put(THE_APPLICATION, getApplicationName(caseData));

        return templateContent;
    }

    private String getApplicationName(CaseData data) {
        boolean prefersWelsh = LanguagePreference.WELSH.equals(
            data.getApplicant1().getLanguagePreference()
        );

        if (data.isJudicialSeparationCase()) {
            return prefersWelsh ? JUDICIAL_SEPARATION_APPLICATION_CY : JUDICIAL_SEPARATION_APPLICATION;
        } else if (data.isDivorce()) {
            return prefersWelsh ? DIVORCE_APPLICATION_CY : DIVORCE_APPLICATION;
        } else {
            return prefersWelsh ? END_CIVIL_PARTNERSHIP_CY : END_CIVIL_PARTNERSHIP;
        }
    }
}
