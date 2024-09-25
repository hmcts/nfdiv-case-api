package uk.gov.hmcts.divorce.notification;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.Organisation;
import uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.config.EmailTemplatesConfig;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference;
import uk.gov.hmcts.divorce.divorcecase.model.RefusalOption;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.document.content.DocmosisCommonContent;
import uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.join;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.FEMALE;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.MALE;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.divorcecase.model.RefusalOption.MORE_INFO;
import static uk.gov.hmcts.divorce.divorcecase.model.RefusalOption.REJECT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.NOT_PROVIDED;
import static uk.gov.hmcts.divorce.notification.FinalOrderNotificationCommonContent.IN_TIME;
import static uk.gov.hmcts.divorce.notification.FinalOrderNotificationCommonContent.IS_OVERDUE;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;

@Component
public class CommonContent {

    public static final String PARTNER = "partner";
    public static final String FIRST_NAME = "first name";
    public static final String LAST_NAME = "last name";
    public static final String NAME = "name";
    public static final String ADDRESS = "address";

    public static final String IS_DIVORCE = "isDivorce";
    public static final String IS_DISSOLUTION = "isDissolution";

    public static final String IS_CONDITIONAL_ORDER = "isConditionalOrder";
    public static final String IS_FINAL_ORDER = "isFinalOrder";

    public static final String IS_REMINDER = "isReminder";
    public static final String YES = "yes";
    public static final String NO = "no";

    public static final String IS_PAID = "isPaid";

    public static final String CREATE_ACCOUNT_LINK = "create account link";
    public static final String SIGN_IN_URL = "signin url";
    public static final String WEBFORM_URL = "webformUrl";
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

    public static final String DUE_DATE = " due date";
    public static final String ISSUE_DATE = " issue date";

    public static final String UNION_TYPE = "union type";

    public static final String COURT_NAME = "court name";
    public static final String COURT_EMAIL = "court email";
    public static final String DATE_OF_HEARING = "date of hearing";
    public static final String TIME_OF_HEARING = "time of hearing";
    public static final String DATE_OF_HEARING_MINUS_SEVEN_DAYS = "date of hearing minus seven days";
    public static final String CO_PRONOUNCEMENT_DATE_PLUS_43 = "CO pronouncement date plus 43 days";
    public static final String CO_PRONOUNCEMENT_DATE_PLUS_43_PLUS_3_MONTHS = "CO pronouncement date plus 43 days plus 3 months";
    public static final String PLUS_21_DUE_DATE = "date email received plus 21 days";
    public static final String DATE_PLUS_14_DAYS = "date plus 14 days";

    public static final String DATE_FINAL_ORDER_ELIGIBLE_FROM_PLUS_3_MONTHS = "date final order eligible from plus 3 months";
    public static final String FINAL_ORDER_OVERDUE_DATE = "finalOrderOverdueDate";

    public static final String IS_SOLE = "isSole";
    public static final String IS_JOINT = "isJoint";

    public static final String DIVORCE = "divorce";
    public static final String DISSOLUTION = "dissolution";
    public static final String DIVORCE_WELSH = "ysgariad";
    public static final String DISSOLUTION_WELSH = "diddymiad";

    public static final String APPLICANT = "Applicant";
    public static final String APPLICANT_1 = "Applicant 1";
    public static final String APPLICANT_2 = "Applicant 2";
    public static final String RESPONDENT = "Respondent";
    public static final String APPLICANT1_LABEL = "applicant1Label";
    public static final String APPLICANT2_LABEL = "applicant2Label";

    public static final String PRONOUNCE_BY_DATE = "pronounceByDate";
    public static final int CO_SUBMISSION_DATE_PLUS_DAYS = 56;

    public static final String DIGITAL_FINAL_ORDER_CERTIFICATE_COPY_FEE = "digitalFinalOrderCertificateCopyFee";

    public static final String SPOUSE = "spouse";
    public static final String SPOUSE_WELSH = "priod";

    public static final String SMART_SURVEY = "smartSurvey";
    public static final String GENERAL_FEE = "generalFee";
    public static final String FINAL_ORDER_FEE = "fee";

    @Autowired
    private DocmosisCommonContent docmosisCommonContent;

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
        templateVars.put(PARTNER, getPartner(caseData, partner, applicant.getLanguagePreference()));
        templateVars.put(COURT_EMAIL,
            config.getTemplateVars().get(caseData.isDivorce() ? DIVORCE_COURT_EMAIL : DISSOLUTION_COURT_EMAIL));
        templateVars.put(SIGN_IN_URL, getSignInUrl(caseData));
        templateVars.put(WEBFORM_URL, config.getTemplateVars().get(WEBFORM_URL));
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

    public Map<String, String> solicitorTemplateVarsPreIssue(CaseData data, Long id, Applicant applicant) {
        Map<String, String> templateVars = basicTemplateVars(data, id);
        templateVars.put(SOLICITOR_NAME, applicant.getSolicitor().getName());
        templateVars.put(SOLICITOR_REFERENCE,
            isNotEmpty(applicant.getSolicitor().getReference())
                ? applicant.getSolicitor().getReference()
                : NOT_PROVIDED);
        templateVars.put(APPLICANT_1_FULL_NAME, data.getApplicant1().getFullName());
        templateVars.put(APPLICANT_2_FULL_NAME, data.getApplicant2().getFullName());
        templateVars.put(SIGN_IN_URL, getProfessionalUsersSignInUrl(id));
        return templateVars;
    }

    public Map<String, String> solicitorTemplateVars(CaseData data, Long id, Applicant applicant) {
        Map<String, String> templateVars = solicitorTemplateVarsPreIssue(data, id, applicant);
        templateVars.put(DocmosisTemplateConstants.ISSUE_DATE, data.getApplication().getIssueDate().format(DATE_TIME_FORMATTER));
        return templateVars;
    }

    public Map<String, String> getCoRefusedSolicitorTemplateVars(CaseData caseData, Long caseId, Applicant applicant,
                                                                 RefusalOption refusalOption) {
        final Map<String, String> templateVars = solicitorTemplateVars(caseData, caseId, applicant);

        boolean isSole = caseData.getApplicationType().isSole();

        templateVars.put("moreInfo", MORE_INFO.equals(refusalOption) ? YES : NO);
        templateVars.put("amendApplication", REJECT.equals(refusalOption) ? YES : NO);
        templateVars.put(IS_JOINT, isSole ? NO : YES);
        templateVars.put(APPLICANT1_LABEL, isSole ? APPLICANT : APPLICANT_1);
        templateVars.put(APPLICANT2_LABEL, isSole ? RESPONDENT : APPLICANT_2);

        return templateVars;
    }

    public String getUnionType(CaseData caseData, LanguagePreference applicantLanguagePreference) {
        if (WELSH.equals(applicantLanguagePreference)) {
            return caseData.isDivorce() ? DIVORCE_WELSH : DISSOLUTION_WELSH;
        }

        return caseData.isDivorce() ? DIVORCE : DISSOLUTION;
    }

    public String getUnionType(CaseData caseData) {
        return caseData.isDivorce() ? DIVORCE : DISSOLUTION;
    }

    public String getPartner(CaseData caseData, Applicant partner, LanguagePreference applicantLanguagePreference) {
        if (WELSH.equals(applicantLanguagePreference)) {
            return getPartnerWelshContent(caseData, partner);
        }

        return getPartner(caseData, partner);
    }

    public String getPartner(CaseData caseData, Applicant partner) {
        if (caseData.isDivorce()) {
            if (isNull(partner.getGender())) {
                return SPOUSE;
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
                return SPOUSE_WELSH;
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
        templateVars.put(IS_SOLE, jointApplication ? NO : YES);
        templateVars.put(HUSBAND_JOINT, jointApplication
            && caseData.isDivorce()
            && MALE.equals(partner.getGender())
            ? YES : NO);
        templateVars.put(WIFE_JOINT, jointApplication
            && caseData.isDivorce()
            && FEMALE.equals(partner.getGender())
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

    public Map<String, Object> templateContentCanApplyForCoOrFo(final CaseData caseData,
                                                                final Long caseId,
                                                                final Applicant applicant,
                                                                final Applicant partner, final LocalDate date) {

        final Map<String, Object> templateContent = docmosisCommonContent.getBasicDocmosisTemplateContent(
            applicant.getLanguagePreference());

        templateContent.put(CASE_REFERENCE, caseId != null ? formatId(caseId) : null);

        templateContent.put("firstName", applicant.getFirstName());
        templateContent.put("lastName", applicant.getLastName());
        templateContent.put(ADDRESS, applicant.getCorrespondenceAddressWithoutConfidentialCheck());
        templateContent.put(PARTNER, getPartner(caseData, partner, applicant.getLanguagePreference()));
        templateContent.put(DATE, date);

        templateContent.put(IS_JOINT, !caseData.getApplicationType().isSole());
        templateContent.put(IS_DIVORCE, caseData.isDivorce());

        return templateContent;
    }

    public String getSmartSurvey() {
        return config.getTemplateVars().get(SMART_SURVEY);
    }

    public Map<String, String> nocCitizenTemplateVars(final Long caseId,
                                                      final Applicant applicant) {
        Map<String, String> templateVars = new HashMap<>();
        templateVars.put(APPLICATION_REFERENCE, caseId != null ? formatId(caseId) : null);
        templateVars.put(FIRST_NAME, applicant.getFirstName());
        templateVars.put(LAST_NAME, applicant.getLastName());
        String organisationName = Optional.ofNullable(applicant.getSolicitor())
            .map(Solicitor::getOrganisationPolicy)
            .map(OrganisationPolicy::getOrganisation)
            .map(Organisation::getOrganisationName)
            .orElse(null);

        if (StringUtils.isNotEmpty(applicant.getSolicitor().getFirmName())) {
            templateVars.put(SOLICITOR_FIRM, applicant.getSolicitor().getFirmName());
        } else if (organisationName != null) {
            templateVars.put(SOLICITOR_FIRM, organisationName);
        } else {
            templateVars.put(SOLICITOR_FIRM, applicant.getSolicitor().getName());
        }
        templateVars.put(SMART_SURVEY, getSmartSurvey());
        return templateVars;
    }

    public Map<String, String> nocSolsTemplateVars(final Long caseId,
                                                   final Applicant applicant) {
        Map<String, String> templateVars = new HashMap<>();
        templateVars.put(APPLICATION_REFERENCE, caseId != null ? formatId(caseId) : null);
        templateVars.put(NAME, applicant.getSolicitor().getName());
        templateVars.put(SOLICITOR_REFERENCE,
            isNotEmpty(applicant.getSolicitor().getReference())
                ? applicant.getSolicitor().getReference()
                : NOT_PROVIDED);
        templateVars.put(SMART_SURVEY, getSmartSurvey());
        return templateVars;
    }

    public Map<String, String> nocOldSolsTemplateVars(final Long caseId,
                                                      final Applicant beforeApplicant) {

        // note: it's the beforeApplicant needs to be passed in to get the old sols
        // this can get improved once we are saving noc info out
        Map<String, String> templateVars = new HashMap<>();
        templateVars.put(APPLICATION_REFERENCE, caseId != null ? formatId(caseId) : null);
        templateVars.put(NAME, beforeApplicant.getSolicitor().getName());
        templateVars.put(APPLICANT_NAME, beforeApplicant.getFullName());
        templateVars.put(SMART_SURVEY, getSmartSurvey());
        return templateVars;
    }

    public void setOverdueAndInTimeVariables(CaseData caseData, Map<String, String> templateVars) {
        if (YesOrNo.YES.equals(caseData.getFinalOrder().getIsFinalOrderOverdue())) {
            templateVars.put(IS_OVERDUE, YES);
            templateVars.put(IN_TIME, NO);
        } else {
            templateVars.put(IS_OVERDUE, NO);
            templateVars.put(IN_TIME, YES);
        }
    }

    public void setIsDivorceAndIsDissolutionVariables(CaseData caseData, Map<String, String> templateVars) {
        templateVars.put(IS_DIVORCE, caseData.isDivorce() ? YES : NO);
        templateVars.put(IS_DISSOLUTION, !caseData.isDivorce() ? YES : NO);
    }
}
