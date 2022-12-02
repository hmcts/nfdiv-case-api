package uk.gov.hmcts.divorce.document.content;

import com.launchdarkly.shaded.kotlin.jvm.internal.PackageReference;
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
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COURTS_AND_TRIBUNALS_SERVICE_HEADER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COURTS_AND_TRIBUNALS_SERVICE_HEADER_JS_TEXT_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CTSC_CONTACT_DETAILS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_AND_DISSOLUTION_HEADER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_AND_DISSOLUTION_HEADER_TEXT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_CIVIL_PARTNERSHIP_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ISSUE_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PHONE_AND_OPENING_TIMES;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PHONE_AND_OPENING_TIMES_JS_TEXT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PHONE_AND_OPENING_TIMES_TEXT_CY;
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

    @Value("${court.locations.serviceCentre.poBox}")
    private String poBox;

    @Value("${court.locations.serviceCentre.postCode}")
    private String postCode;

    @Value("${court.locations.serviceCentre.town}")
    private String town;

    @Autowired
    private CommonContent commonContent;

    public Map<String, Object> apply(final CaseData caseData,
                                     final Long ccdCaseReference,
                                     Applicant applicant,
                                     Applicant partner) {
        Map<String, Object> templateContent = new HashMap<>();
        final LanguagePreference applicantLanguagePreference = applicant.getLanguagePreference();

        templateContent.put(RELATION, commonContent.getPartner(caseData, partner, applicantLanguagePreference));
        templateContent.put(CASE_REFERENCE, formatId(ccdCaseReference));
        templateContent.put(FIRST_NAME, applicant.getFirstName());
        templateContent.put(LAST_NAME, applicant.getLastName());
        templateContent.put(ADDRESS, applicant.getPostalAddress());
        templateContent.put(ISSUE_DATE, caseData.getApplication().getIssueDate().format(DATE_TIME_FORMATTER));

        final var ctscContactDetails = CtscContactDetails
            .builder()
            .postcode(postCode)
            .town(town)
            .build();

        if (WELSH.equals(applicantLanguagePreference)) {
            getWelshTemplateContent(templateContent, caseData);
            ctscContactDetails.setPoBox("Blwch Post 13226");
        } else {
            getEnglishTemplateContent(templateContent, caseData);
            ctscContactDetails.setPoBox(poBox);
        }

        templateContent.put(CTSC_CONTACT_DETAILS, ctscContactDetails);

        return templateContent;
    }

    private Map<String, Object> getEnglishTemplateContent(Map<String, Object> templateContent, CaseData caseData) {

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
        templateContent.put(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT);
        templateContent.put(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT);
        templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP_EMAIL, CONTACT_DIVORCE_JUSTICE_GOV_UK);
        templateContent.put(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_JS_TEXT);

        return templateContent;
    }

    private Map<String, Object> getWelshTemplateContent(Map<String, Object> templateContent, CaseData caseData) {

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
        templateContent.put(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT_CY);
        templateContent.put(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_JS_TEXT_CY);
        templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP_EMAIL, CONTACT_JUSTICE_GOV_UK_CY);
        templateContent.put(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT_CY);

        return templateContent;
    }

}
