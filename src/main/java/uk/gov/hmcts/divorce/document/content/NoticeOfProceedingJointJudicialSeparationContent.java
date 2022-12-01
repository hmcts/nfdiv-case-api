package uk.gov.hmcts.divorce.document.content;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CtscContactDetails;
import uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference;
import uk.gov.hmcts.divorce.notification.CommonContent;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONTACT_DIVORCE_JUSTICE_GOV_UK;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONTACT_JUSTICE_GOV_UK_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CTSC_CONTACT_DETAILS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_AND_DISSOLUTION_HEADER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_AND_DISSOLUTION_HEADER_TEXT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_CIVIL_PARTNERSHIP_EMAIL;
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

    public static final String DIVORCE_AND_DISSOLUTION_HEADER_TEXT_CY = "Ysgariadau a Diddymiadau";

    public static final String PHONE_NUMBER_CY = "0300 303 5171";

    @Value("${court.locations.serviceCentre.phoneNumber}")
    private String phoneNumber;

    @Autowired
    private CommonContent commonContent;

    public Map<String, Object> apply(final CaseData caseData,
                                     final Long ccdCaseReference,
                                     Applicant applicant,
                                     Applicant partner) {
        final Map<String, Object> templateContent = new HashMap<>();
        final LanguagePreference applicantLanguagePreference = applicant.getLanguagePreference();

        templateContent.put(CASE_REFERENCE, formatId(ccdCaseReference));
        templateContent.put(FIRST_NAME, applicant.getFirstName());
        templateContent.put(LAST_NAME, applicant.getLastName());
        templateContent.put(ADDRESS, applicant.getPostalAddress());
        templateContent.put(ISSUE_DATE, caseData.getApplication().getIssueDate().format(DATE_TIME_FORMATTER));

        if (null != caseData.getApplication().getReissueDate()) {

            templateContent.put(
                REISSUED_DATE,
                WELSH.equals(applicantLanguagePreference) ? REISSUED_TEXT_CY : REISSUED_TEXT
                    + caseData.getApplication().getReissueDate().format(DATE_TIME_FORMATTER)
            );
        }

        StringBuilder judicialSeparationProceedingsFinalText = new StringBuilder(
            WELSH.equals(applicantLanguagePreference) ? JUDICIAL_SEPARATION_PROCEEDINGS_SUBTEXT_CY : JUDICIAL_SEPARATION_PROCEEDINGS_SUBTEXT
        );
        StringBuilder judicialSeparationFinalText = new StringBuilder(
            WELSH.equals(applicantLanguagePreference) ? JUDICIAL_SEPARATION_SUBTEXT_CY : JUDICIAL_SEPARATION_SUBTEXT
        );
        if (caseData.isDivorce()) {
            if (WELSH.equals(applicantLanguagePreference)) {
                judicialSeparationProceedingsFinalText.append(" " + JUDICIAL_CY);
                judicialSeparationFinalText.append(" " + JUDICIAL_CY);

                templateContent.put(MARRIED_TO_MORE_THAN_ONE_PERSON, MARRIED_TO_MORE_THAN_ONE_PERSON_TEXT_CY);
            } else {
                judicialSeparationProceedingsFinalText.insert(0, JUDICIAL + " ");
                judicialSeparationFinalText.insert(0, JUDICIAL + " ");

                templateContent.put(MARRIED_TO_MORE_THAN_ONE_PERSON, MARRIED_TO_MORE_THAN_ONE_PERSON_TEXT);
            }
        }

        templateContent.put(JUDICIAL_SEPARATION_PROCEEDINGS, judicialSeparationProceedingsFinalText.toString());
        templateContent.put(JUDICIAL_SEPARATION, judicialSeparationFinalText.toString());
        templateContent.put(RELATION, commonContent.getPartner(caseData, partner, applicantLanguagePreference));
        templateContent.put(DIVORCE_AND_DISSOLUTION_HEADER,
            WELSH.equals(applicantLanguagePreference) ? DIVORCE_AND_DISSOLUTION_HEADER_TEXT_CY : DIVORCE_AND_DISSOLUTION_HEADER_TEXT);
        templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP_EMAIL,
            WELSH.equals(applicantLanguagePreference) ? CONTACT_JUSTICE_GOV_UK_CY : CONTACT_DIVORCE_JUSTICE_GOV_UK);

        final var ctscContactDetails = CtscContactDetails
            .builder()
            .phoneNumber(WELSH.equals(applicantLanguagePreference) ? PHONE_NUMBER_CY : phoneNumber)
            .build();

        templateContent.put(CTSC_CONTACT_DETAILS, ctscContactDetails);

        return templateContent;
    }

}
