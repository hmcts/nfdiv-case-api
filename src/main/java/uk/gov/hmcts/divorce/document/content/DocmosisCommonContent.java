package uk.gov.hmcts.divorce.document.content;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CtscContactDetails;
import uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;

import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_OR_APPLICANT1;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONTACT_DIVORCE_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONTACT_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COURTS_AND_TRIBUNALS_SERVICE_HEADER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CTSC_CONTACT_DETAILS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_AND_DISSOLUTION_HEADER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_AND_DISSOLUTION_HEADER_TEXT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_AND_DISSOLUTION_HEADER_TEXT_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_APPLICATION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_APPLICATION_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.END_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.END_CIVIL_PARTNERSHIP_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.FAMILY_COURT_LOGO;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.HMCTS_LOGO;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.NOT_PROVIDED;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.NOT_PROVIDED_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.NOT_REPRESENTED;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.NOT_REPRESENTED_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PHONE_AND_OPENING_TIMES;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PHONE_AND_OPENING_TIMES_TEXT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PHONE_AND_OPENING_TIMES_TEXT_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RESPONDENT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RESPONDENT_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RESPONDENT_OR_APPLICANT2;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.APPLICANT_1_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_JOINT;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;

@Component
public class DocmosisCommonContent {

    @Value("${court.locations.serviceCentre.serviceCentreName}")
    private String serviceCentre;

    @Value("${court.locations.serviceCentre.centreName}")
    private String centreName;

    @Value("${court.locations.serviceCentre.poBox}")
    private String poBox;

    @Value("${court.locations.serviceCentre.town}")
    private String town;

    @Value("${court.locations.serviceCentre.postCode}")
    private String postcode;

    @Value("${court.locations.serviceCentre.email}")
    private String email;

    @Value("${court.locations.serviceCentre.phoneNumber}")
    private String phoneNumber;

    public Map<String, Object> getBasicDocmosisTemplateContent(LanguagePreference languagePreference) {
        Map<String, Object> templateContent = new HashMap<>();

        if (ENGLISH.equals(languagePreference)) {
            templateContent.put(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT);
            templateContent.put(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT);
            templateContent.put(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL);
            templateContent.put(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT);
            templateContent.put(HMCTS_LOGO,  "[userImage:hmcts_logo_nfd_en.png]");
            templateContent.put(FAMILY_COURT_LOGO, "[userImage:family_court_logo_nfd_en.png]");
        } else {
            templateContent.put(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT_CY);
            templateContent.put(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT_CY);
            templateContent.put(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL);
            templateContent.put(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT_CY);
            templateContent.put(HMCTS_LOGO,  "[userImage:hmcts_logo_nfd_cy.png]");
            templateContent.put(FAMILY_COURT_LOGO, "[userImage:family_court_logo_nfd_cy.png]");
        }

        final var ctscContactDetails = CtscContactDetails
            .builder()
            .centreName(centreName)
            .serviceCentre(serviceCentre)
            .poBox(poBox)
            .town(town)
            .emailAddress(email)
            .postcode(postcode)
            .phoneNumber(phoneNumber)
            .build();

        templateContent.put(CTSC_CONTACT_DETAILS, ctscContactDetails);

        return templateContent;
    }

    public Map<String, Object> getBasicSolicitorTemplateContent(final CaseData caseData,
                                                                final Long caseId,
                                                                boolean isApplicantSolicitor,
                                                                final LanguagePreference languagePreference) {

        Map<String, Object> templateContent = getBasicDocmosisTemplateContent(languagePreference);

        Applicant applicant1 = caseData.getApplicant1();
        Applicant applicant2 = caseData.getApplicant2();
        Solicitor applicant1Solicitor = applicant1.getSolicitor();
        Solicitor applicant2Solicitor = applicant2.getSolicitor();
        boolean isJoint = !caseData.getApplicationType().isSole();

        templateContent.put(CASE_REFERENCE, formatId(caseId));
        templateContent.put(APPLICANT_1_FIRST_NAME, applicant1.getFirstName());
        templateContent.put(APPLICANT_1_LAST_NAME, applicant1.getLastName());
        templateContent.put(APPLICANT_2_FIRST_NAME, applicant2.getFirstName());
        templateContent.put(APPLICANT_2_LAST_NAME, applicant2.getLastName());
        templateContent.put(APPLICANT_OR_APPLICANT1, getApplicantOrApplicant1(caseData, languagePreference));
        templateContent.put(RESPONDENT_OR_APPLICANT2, getRespondentOrApplicant2(caseData, languagePreference));
        templateContent.put(IS_JOINT, isJoint);
        templateContent.put(IS_DIVORCE, caseData.isDivorce());
        templateContent.put(APPLICANT_1_SOLICITOR_NAME, getSolicitorName(applicant1, applicant1Solicitor, languagePreference));
        templateContent.put(APPLICANT_2_SOLICITOR_NAME, getSolicitorName(applicant2, applicant2Solicitor, languagePreference));
        templateContent.put(SOLICITOR_NAME, isApplicantSolicitor ? applicant1Solicitor.getName() : applicant2Solicitor.getName());
        templateContent.put(SOLICITOR_ADDRESS, isApplicantSolicitor
            ? applicant1Solicitor.getFirmAndAddress()
            : applicant2Solicitor.getFirmAndAddress());
        templateContent.put(
            SOLICITOR_REFERENCE,
            isApplicantSolicitor
                ? getSolicitorReference(applicant1Solicitor, languagePreference)
                : getSolicitorReference(applicant2Solicitor, languagePreference)
        );

        return templateContent;
    }

    public String getSolicitorName(Applicant applicant, Solicitor solicitor, LanguagePreference languagePreference) {
        String notRepresented = WELSH.equals(languagePreference) ? NOT_REPRESENTED_CY : NOT_REPRESENTED;
        return applicant.isRepresented() ? solicitor.getName() : notRepresented;
    }

    public String getSolicitorReference(Solicitor solicitor, LanguagePreference languagePreference) {
        String notProvided = WELSH.equals(languagePreference) ? NOT_PROVIDED_CY : NOT_PROVIDED;
        return isNotEmpty(solicitor.getReference()) ? solicitor.getReference() : notProvided;
    }

    public String getApplicantOrApplicant1(CaseData caseData, LanguagePreference languagePreference) {
        final boolean isSole = caseData.getApplicationType().isSole();
        if (WELSH.equals(languagePreference)) {
            return isSole ? APPLICANT_CY : APPLICANT_1_CY;
        } else {
            return isSole ? APPLICANT : APPLICANT_1;
        }
    }

    public String getRespondentOrApplicant2(CaseData caseData, LanguagePreference languagePreference) {
        final boolean isSole = caseData.getApplicationType().isSole();
        if (WELSH.equals(languagePreference)) {
            return isSole ? RESPONDENT_CY : APPLICANT_2_CY;
        } else {
            return isSole ? RESPONDENT : APPLICANT_2;
        }
    }

    public String getApplicationType(LanguagePreference languagePreference, CaseData caseData) {
        if (LanguagePreference.WELSH.equals(languagePreference)) {
            return caseData.isDivorce() ? DIVORCE_APPLICATION_CY : END_CIVIL_PARTNERSHIP_CY;
        } else {
            return caseData.isDivorce() ? DIVORCE_APPLICATION : END_CIVIL_PARTNERSHIP;
        }
    }
}
