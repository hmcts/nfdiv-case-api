package uk.gov.hmcts.divorce.notification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.common.config.EmailTemplatesConfig;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import java.util.HashMap;
import java.util.Map;

import static java.lang.String.join;
import static java.util.Objects.isNull;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.FEMALE;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.MALE;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;

@Component
public class CommonContent {

    public static final String PARTNER = "partner";
    public static final String FIRST_NAME = "first name";
    public static final String LAST_NAME = "last name";

    public static final String IS_DIVORCE = "isDivorce";
    public static final String IS_DISSOLUTION = "isDissolution";

    public static final String IS_REMINDER = "isReminder";
    public static final String YES = "yes";
    public static final String NO = "no";

    public static final String IS_PAID = "isPaid";

    public static final String CREATE_ACCOUNT_LINK = "create account link";
    public static final String SIGN_IN_URL = "signin url";
    public static final String SIGN_IN_DIVORCE_URL = "signInDivorceUrl";
    public static final String SIGN_IN_DISSOLUTION_URL = "signInDissolutionUrl";
    public static final String SIGN_IN_PROFESSIONAL_USERS_URL = "signInProfessionalUsersUrl";
    public static final String DIVORCE_COURT_EMAIL = "divorceCourtEmail";
    public static final String DISSOLUTION_COURT_EMAIL = "dissolutionCourtEmail";

    public static final String SUBMISSION_RESPONSE_DATE = "date of response";
    public static final String APPLICATION_REFERENCE = "reference number";
    public static final String IS_UNDISPUTED = "isUndisputed";
    public static final String IS_DISPUTED = "isDisputed";
    public static final String DATE_OF_ISSUE = "date of issue";

    public static final String ACCESS_CODE = "access code";

    public static final String APPLICANT_NAME = "applicant name";
    public static final String RESPONDENT_NAME = "respondent name";
    public static final String SOLICITOR_NAME = "solicitor name";
    public static final String SOLICITOR_REFERENCE = "solicitor reference";
    public static final String SOLICITOR_FIRM = "solicitor firm";

    public static final String REVIEW_DEADLINE_DATE = "review deadline date";

    public static final String JOINT_CONDITIONAL_ORDER = "joint conditional order";
    public static final String HUSBAND_JOINT = "husbandJoint";
    public static final String WIFE_JOINT = "wifeJoint";
    public static final String CIVIL_PARTNER_JOINT = "civilPartnerJoint";

    public static final String ISSUE_DATE = " issue date";

    public static final String COURT_NAME = "court name";
    public static final String COURT_EMAIL = "court email";
    public static final String DATE_OF_HEARING = "date of hearing";
    public static final String TIME_OF_HEARING = "time of hearing";
    public static final String DATE_OF_HEARING_MINUS_SEVEN_DAYS = "date of hearing minus seven days";
    public static final String CO_PRONOUNCEMENT_DATE_PLUS_43 = "CO pronouncement date plus 43 days";

    public static final String DATE_FINAL_ORDER_ELIGIBLE_FROM_PLUS_3_MONTHS = "date final order eligible from plus 3 months";

    public static final String IS_SOLE = "isSole";
    public static final String IS_JOINT = "isJoint";

    @Autowired
    private EmailTemplatesConfig config;

    public Map<String, String> mainTemplateVars(final CaseData caseData,
                                                final Long id,
                                                final Applicant applicant,
                                                final Applicant partner) {
        Map<String, String> templateVars = new HashMap<>();
        templateVars.put(APPLICATION_REFERENCE, id != null ? formatId(id) : null);
        templateVars.put(IS_DIVORCE, caseData.isDivorce() ? YES : NO);
        templateVars.put(IS_DISSOLUTION, !caseData.isDivorce() ? YES : NO);
        templateVars.put(FIRST_NAME, applicant.getFirstName());
        templateVars.put(LAST_NAME, applicant.getLastName());
        templateVars.put(PARTNER, getPartner(caseData, partner));
        templateVars.put(COURT_EMAIL,
            config.getTemplateVars().get(caseData.isDivorce() ? DIVORCE_COURT_EMAIL : DISSOLUTION_COURT_EMAIL));
        templateVars.put(SIGN_IN_URL, getSignInUrl(caseData));
        return templateVars;
    }

    public Map<String, String> basicTemplateVars(final CaseData caseData, final Long caseId) {

        final Map<String, String> templateVars = new HashMap<>();
        final Applicant applicant = caseData.getApplicant1();
        final Applicant respondent = caseData.getApplicant2();

        templateVars.put(APPLICANT_NAME, join(" ", applicant.getFirstName(), applicant.getLastName()));
        templateVars.put(RESPONDENT_NAME, join(" ", respondent.getFirstName(), respondent.getLastName()));
        templateVars.put(APPLICATION_REFERENCE, formatId(caseId));
        templateVars.put(COURT_EMAIL,
            config.getTemplateVars().get(caseData.isDivorce() ? DIVORCE_COURT_EMAIL : DISSOLUTION_COURT_EMAIL));

        return templateVars;
    }

    public String getPartner(CaseData caseData, Applicant partner) {
        if (caseData.isDivorce()) {
            if (isNull(partner.getGender())) {
                return "spouse";
            } else {
                return partner.getGender() == MALE ? "husband" : "wife";
            }
        } else {
            return "civil partner";
        }
    }

    public String getPartnerWelshContent(CaseData caseData, Applicant partner) {
        if (caseData.isDivorce()) {
            if (isNull(partner.getGender())) {
                return "priod";
            } else {
                return partner.getGender() == MALE ? "gŵr" : "gwraig";
            }
        } else {
            return "partner sifil";
        }
    }

    public Map<String, String> conditionalOrderTemplateVars(final CaseData caseData,
                                                            final Long id,
                                                            final Applicant applicant,
                                                            final Applicant partner) {
        final Map<String, String> templateVars = mainTemplateVars(caseData, id, applicant, partner);
        final boolean jointApplication = !caseData.getApplicationType().isSole();

        templateVars.put(JOINT_CONDITIONAL_ORDER, jointApplication ? YES : NO);
        templateVars.put(HUSBAND_JOINT, jointApplication
            && caseData.isDivorce()
            && partner.getGender().equals(MALE)
            ? YES : NO);
        templateVars.put(WIFE_JOINT, jointApplication
            && caseData.isDivorce()
            && partner.getGender().equals(FEMALE)
            ? YES : NO);
        templateVars.put(CIVIL_PARTNER_JOINT, jointApplication
            && !caseData.isDivorce()
            ? YES : NO);

        return templateVars;
    }

    public String getSignInUrl(CaseData caseData) {
        return config.getTemplateVars().get(caseData.isDivorce() ? SIGN_IN_DIVORCE_URL : SIGN_IN_DISSOLUTION_URL);
    }

    public String getProfessionalUsersSignInUrl(Long caseId) {
        return config.getTemplateVars().get(SIGN_IN_PROFESSIONAL_USERS_URL) + caseId;
    }

    public Map<String, String> addWelshPartnerContentIfApplicant1PrefersWelsh(final Map<String, String> templateContent,
                                                                              final CaseData caseData) {
        return addWelshPartnerContentIfApplicantPrefersWelsh(
            templateContent,
            caseData,
            caseData.getApplicant1(),
            caseData.getApplicant2());
    }

    public Map<String, String> addWelshPartnerContentIfApplicant2PrefersWelsh(final Map<String, String> templateContent,
                                                                              final CaseData caseData) {
        return addWelshPartnerContentIfApplicantPrefersWelsh(
            templateContent,
            caseData,
            caseData.getApplicant2(),
            caseData.getApplicant1());
    }

    private Map<String, String> addWelshPartnerContentIfApplicantPrefersWelsh(final Map<String, String> templateContent,
                                                                              final CaseData caseData,
                                                                              final Applicant applicant,
                                                                              final Applicant partner) {
        if (WELSH == applicant.getLanguagePreference()) {
            templateContent.put(PARTNER, getPartnerWelshContent(caseData, partner));
        }

        return templateContent;
    }
}
