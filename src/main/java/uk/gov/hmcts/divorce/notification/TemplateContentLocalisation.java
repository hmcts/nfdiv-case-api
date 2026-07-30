package uk.gov.hmcts.divorce.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.common.config.EmailTemplatesConfig;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference;

import java.time.LocalDate;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.MALE;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PHONE_AND_OPENING_TIMES_TEXT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PHONE_AND_OPENING_TIMES_TEXT_CY;
import static uk.gov.hmcts.divorce.notification.CommonContent.CIVIL_PARTNER;
import static uk.gov.hmcts.divorce.notification.CommonContent.CIVIL_PARTNER_CY;
import static uk.gov.hmcts.divorce.notification.CommonContent.CONTACT_TEXT;
import static uk.gov.hmcts.divorce.notification.CommonContent.CONTACT_TEXT_WELSH;
import static uk.gov.hmcts.divorce.notification.CommonContent.DISSOLUTION;
import static uk.gov.hmcts.divorce.notification.CommonContent.DISSOLUTION_WELSH;
import static uk.gov.hmcts.divorce.notification.CommonContent.DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.DIVORCE_WELSH;
import static uk.gov.hmcts.divorce.notification.CommonContent.DO_NOT_REPLY;
import static uk.gov.hmcts.divorce.notification.CommonContent.DO_NOT_REPLY_WELSH;
import static uk.gov.hmcts.divorce.notification.CommonContent.HUSBAND;
import static uk.gov.hmcts.divorce.notification.CommonContent.HUSBAND_CY;
import static uk.gov.hmcts.divorce.notification.CommonContent.IDAM_INACTIVITY_POLICY;
import static uk.gov.hmcts.divorce.notification.CommonContent.IDAM_INACTIVITY_POLICY_CY;
import static uk.gov.hmcts.divorce.notification.CommonContent.SMART_SURVEY;
import static uk.gov.hmcts.divorce.notification.CommonContent.SPOUSE;
import static uk.gov.hmcts.divorce.notification.CommonContent.SPOUSE_WELSH;
import static uk.gov.hmcts.divorce.notification.CommonContent.USER;
import static uk.gov.hmcts.divorce.notification.CommonContent.USER_CY;
import static uk.gov.hmcts.divorce.notification.CommonContent.WEBFORM_CY_URL;
import static uk.gov.hmcts.divorce.notification.CommonContent.WEBFORM_URL;
import static uk.gov.hmcts.divorce.notification.CommonContent.WIFE;
import static uk.gov.hmcts.divorce.notification.CommonContent.WIFE_CY;
import static uk.gov.hmcts.divorce.notification.FormatUtil.getDateTimeFormatterForPreferredLanguage;

@Component
@RequiredArgsConstructor
public class TemplateContentLocalisation {

    private final EmailTemplatesConfig config;

    //Could we set this in commonContent.mainTemplateVars() and avoid calling this method elsewhere?
    public String getWebFormUrl(LanguagePreference languagePreference) {
        return WELSH.equals(languagePreference)
            ? config.getTemplateVars().get(WEBFORM_CY_URL)
            : config.getTemplateVars().get(WEBFORM_URL);
    }

    //Only called in commonContent.solicitorTemplateVars()
    public String getIssueDate(LocalDate issueDate, LanguagePreference languagePreference) {
        return
            issueDate == null ? "" : issueDate.format(
                getDateTimeFormatterForPreferredLanguage(languagePreference));
    }

    //Could we set this in commonContent.mainTemplateVars() and avoid calling this method elsewhere?
    public String getUnionType(CaseData caseData, LanguagePreference applicantLanguagePreference) {
        if (WELSH.equals(applicantLanguagePreference)) {
            return caseData.isDivorce() ? DIVORCE_WELSH : DISSOLUTION_WELSH;
        }

        return caseData.isDivorce() ? DIVORCE : DISSOLUTION;
    }

    //Is this method even required? Original commonContent method was only called in two places.  Seems excessive.
    public String getUnionType(CaseData caseData) {
        return getUnionType(caseData, LanguagePreference.ENGLISH);
    }

    //Could we set this in commonContent.mainTemplateVars() and avoid calling this method elsewhere?
    public String getPartner(CaseData caseData, Applicant partner, LanguagePreference applicantLanguagePreference) {
        if (WELSH.equals(applicantLanguagePreference)) {
            return getPartnerWelshContent(caseData, partner);
        }
        return getPartnerEnglishContent(caseData, partner);
    }

    private String getPartnerEnglishContent(CaseData caseData, Applicant partner) {
        if (caseData.isDivorce()) {
            if (isNull(partner.getGender())) {
                return SPOUSE;
            } else {
                return partner.getGender() == MALE ? HUSBAND : WIFE;
            }
        } else {
            return CIVIL_PARTNER;
        }
    }

    private String getPartnerWelshContent(CaseData caseData, Applicant partner) {
        if (caseData.isDivorce()) {
            if (isNull(partner.getGender())) {
                return SPOUSE_WELSH;
            } else {
                return partner.getGender() == MALE ? HUSBAND_CY : WIFE_CY;
            }
        } else {
            return CIVIL_PARTNER_CY;
        }
    }

    //Could we set this in commonContent.mainTemplateVars() and avoid calling this method elsewhere?
    public String getSmartSurvey(LanguagePreference languagePreference) {
        final String smartSurvey = config.getTemplateVars().get(SMART_SURVEY);
        return smartSurvey + System.lineSeparator() + System.lineSeparator() + "##"
            + (WELSH.equals(languagePreference) ? DO_NOT_REPLY_WELSH : DO_NOT_REPLY);
    }

    //Could we set this in commonContent.mainTemplateVars() and avoid calling this method elsewhere?
    public String getContactWebFormText(LanguagePreference languagePreference) {
        if (languagePreference == WELSH) {
            return CONTACT_TEXT_WELSH + "(" + config.getTemplateVars().get(WEBFORM_CY_URL) + ")";
        } else {
            return CONTACT_TEXT + "(" + config.getTemplateVars().get(WEBFORM_URL) + ")";
        }
    }

    //Could we set this in commonContent.mainTemplateVars() and avoid calling this method elsewhere?
    public String getPhoneAndOpeningTimes(LanguagePreference languagePreference) {
        if (languagePreference == WELSH) {
            return PHONE_AND_OPENING_TIMES_TEXT_CY;
        } else {
            return PHONE_AND_OPENING_TIMES_TEXT;
        }
    }

    //Only called by commonContent.mainTemplateVars()
    public String getUserString(LanguagePreference languagePreference) {
        return languagePreference == WELSH ? USER_CY : USER;
    }

    //Only called by commonContent.mainTemplateVars()
    public String getIdamInactivityPolicy(LanguagePreference languagePreference) {
        return WELSH.equals(languagePreference)
            ? config.getTemplateVars().get(IDAM_INACTIVITY_POLICY_CY)
            : config.getTemplateVars().get(IDAM_INACTIVITY_POLICY);
    }
}
