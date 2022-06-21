package uk.gov.hmcts.divorce.document.content;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import java.time.Clock;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CCD_CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_APPLICATION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_APPLICATION_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_DISSOLUTION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ENDING_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ENDING_CIVIL_PARTNERSHIP_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.END_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.END_CIVIL_PARTNERSHIP_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.THE_APPLICATION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.THE_DIVORCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.THE_DIVORCE_CY;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;

@Component
public class BailiffApprovedOrderContent {

    @Autowired
    private Clock clock;

    public Map<String, Object> apply(final CaseData caseData, final Long ccdCaseReference) {

        final Map<String, Object> templateContent = new HashMap<>();
        final var applicant1 = caseData.getApplicant1();
        final var applicant2 = caseData.getApplicant2();
        final var applicant1LanguagePreference = applicant1.getLanguagePreference();

        if (caseData.getDivorceOrDissolution().isDivorce()) {
            templateContent.put(
                THE_APPLICATION, WELSH.equals(applicant1LanguagePreference)
                    ? DIVORCE_APPLICATION_CY
                    : DIVORCE_APPLICATION
            );
            templateContent.put(
                DIVORCE_OR_DISSOLUTION, WELSH.equals(applicant1LanguagePreference)
                    ? THE_DIVORCE_CY
                    : THE_DIVORCE
            );
        } else {
            templateContent.put(
                THE_APPLICATION, WELSH.equals(applicant1LanguagePreference)
                    ? END_CIVIL_PARTNERSHIP_CY
                    : END_CIVIL_PARTNERSHIP
            );
            templateContent.put(
                DIVORCE_OR_DISSOLUTION, WELSH.equals(applicant1LanguagePreference)
                    ? ENDING_CIVIL_PARTNERSHIP_CY
                    : ENDING_CIVIL_PARTNERSHIP
            );
        }

        templateContent.put(CCD_CASE_REFERENCE, ccdCaseReference);
        templateContent.put(DATE, LocalDate.now(clock).format(DATE_TIME_FORMATTER));

        templateContent.put(APPLICANT_1_FIRST_NAME, applicant1.getFirstName());
        templateContent.put(APPLICANT_1_LAST_NAME, applicant1.getLastName());
        templateContent.put(APPLICANT_2_FIRST_NAME, applicant2.getFirstName());
        templateContent.put(APPLICANT_2_LAST_NAME, applicant2.getLastName());

        return templateContent;
    }
}
