package uk.gov.hmcts.divorce.document.content;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference;
import uk.gov.hmcts.divorce.notification.CommonContent;

import java.util.Map;

import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ISSUE_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RELATION;
import static uk.gov.hmcts.divorce.notification.CommonContent.ADDRESS;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;

@Component
public class NoticeOfProceedingJointJudicialSeparationContent {

    public static final String JUDICIAL_SEPARATION_PROCEEDINGS = "judicialSeparationProceedings";
    public static final String JUDICIAL_SEPARATION = "judicialSeparation";
    public static final String REISSUED_DATE = "ressiuedDate";
    public static final String MARRIED_TO_MORE_THAN_ONE_PERSON = "marriedToMoreThanOnePerson";
    public static final String MARRIED_TO_MORE_THAN_ONE_PERSON_TEXT = "You must tell the court if you’ve been married to more than"
        + " one person during this marriage.";
    public static final String MARRIED_TO_MORE_THAN_ONE_PERSON_TEXT_CY = "Rhaid i chi ddweud wrth y llys os ydych wedi bod yn briod i"
        + " fwy nag un unigolyn yn ystod y briodas hon.";
    public static final String JUDICIAL_SEPARATION_PROCEEDINGS_SUBTEXT = "separation proceedings";
    public static final String JUDICIAL_SEPARATION_SUBTEXT = "separation";
    public static final String JUDICIAL = "judicial";

    public static final String JUDICIAL_SEPARATION_PROCEEDINGS_SUBTEXT_CY = "achos ymwahaniad";
    public static final String JUDICIAL_SEPARATION_SUBTEXT_CY = "ymwahaniad";
    public static final String JUDICIAL_CY = "cyfreithiol";

    public static final String REISSUED_TEXT = "Reissued on: ";
    public static final String REISSUED_TEXT_CY = "Ailgyhoeddwyd ar: ";

    @Autowired
    private CommonContent commonContent;

    @Autowired
    private DocmosisCommonContent docmosisCommonContent;

    public Map<String, Object> apply(final CaseData caseData,
                                     final Long ccdCaseReference,
                                     Applicant applicant,
                                     Applicant partner) {

        final LanguagePreference applicantLanguagePreference = applicant.getLanguagePreference();
        Map<String, Object> templateContent = docmosisCommonContent.getBasicDocmosisTemplateContent(
            applicantLanguagePreference);

        templateContent.put(RELATION, commonContent.getPartner(caseData, partner, applicantLanguagePreference));
        templateContent.put(CASE_REFERENCE, formatId(ccdCaseReference));
        templateContent.put(FIRST_NAME, applicant.getFirstName());
        templateContent.put(LAST_NAME, applicant.getLastName());
        templateContent.put(ADDRESS, applicant.getPostalAddress());
        templateContent.put(ISSUE_DATE, caseData.getApplication().getIssueDate().format(DATE_TIME_FORMATTER));

        if (WELSH.equals(applicantLanguagePreference)) {
            getWelshTemplateContent(templateContent, caseData);
        } else {
            getEnglishTemplateContent(templateContent, caseData);
        }

        return templateContent;
    }

    private void getEnglishTemplateContent(Map<String, Object> templateContent, CaseData caseData) {

        if (null != caseData.getApplication().getReissueDate()) {
            templateContent.put(REISSUED_DATE, REISSUED_TEXT + caseData.getApplication().getReissueDate().format(DATE_TIME_FORMATTER));
        }

        StringBuilder judicialSeparationProceedingsFinalText = new StringBuilder(JUDICIAL_SEPARATION_PROCEEDINGS_SUBTEXT);
        StringBuilder judicialSeparationFinalText = new StringBuilder(JUDICIAL_SEPARATION_SUBTEXT);
        if (caseData.isDivorce()) {
            judicialSeparationProceedingsFinalText.insert(0, JUDICIAL + " ");
            judicialSeparationFinalText.insert(0, JUDICIAL + " ");

            templateContent.put(MARRIED_TO_MORE_THAN_ONE_PERSON, MARRIED_TO_MORE_THAN_ONE_PERSON_TEXT);
        }

        templateContent.put(JUDICIAL_SEPARATION_PROCEEDINGS, judicialSeparationProceedingsFinalText.toString());
        templateContent.put(JUDICIAL_SEPARATION, judicialSeparationFinalText.toString());

    }

    private void getWelshTemplateContent(Map<String, Object> templateContent, CaseData caseData) {

        if (null != caseData.getApplication().getReissueDate()) {
            templateContent.put(REISSUED_DATE, REISSUED_TEXT_CY + caseData.getApplication().getReissueDate().format(DATE_TIME_FORMATTER));
        }

        StringBuilder judicialSeparationProceedingsFinalText = new StringBuilder(JUDICIAL_SEPARATION_PROCEEDINGS_SUBTEXT_CY);
        StringBuilder judicialSeparationFinalText = new StringBuilder(JUDICIAL_SEPARATION_SUBTEXT_CY);
        if (caseData.isDivorce()) {
            judicialSeparationProceedingsFinalText.append(" " + JUDICIAL_CY);
            judicialSeparationFinalText.append(" " + JUDICIAL_CY);

            templateContent.put(MARRIED_TO_MORE_THAN_ONE_PERSON, MARRIED_TO_MORE_THAN_ONE_PERSON_TEXT_CY);
        }

        templateContent.put(JUDICIAL_SEPARATION_PROCEEDINGS, judicialSeparationProceedingsFinalText.toString());
        templateContent.put(JUDICIAL_SEPARATION, judicialSeparationFinalText.toString());

    }

}
