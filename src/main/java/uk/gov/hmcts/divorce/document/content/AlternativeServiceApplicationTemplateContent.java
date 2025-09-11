package uk.gov.hmcts.divorce.document.content;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference;
import uk.gov.hmcts.divorce.divorcecase.model.interimapplications.AlternativeServiceDifferentWays;
import uk.gov.hmcts.divorce.divorcecase.model.interimapplications.AlternativeServiceJourneyOptions;
import uk.gov.hmcts.divorce.divorcecase.model.interimapplications.AlternativeServiceMethod;
import uk.gov.hmcts.divorce.divorcecase.model.interimapplications.InterimApplicationOptions;
import uk.gov.hmcts.divorce.document.content.templatecontent.TemplateContent;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.divorce.document.DocumentConstants.ALTERNATIVE_SERVICE_APPLICATION_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ALTERNATIVE_SERVICE_BY_OTHER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ALTERNATIVE_SERVICE_BY_SOCIAL_MEDIA;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ALTERNATIVE_SERVICE_BY_TEXT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ALTERNATIVE_SERVICE_BY_WHATSAPP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ALTERNATIVE_SERVICE_EVIDENCE_DETAILS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ALTERNATIVE_SERVICE_EVIDENCE_UPLOADED;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ALTERNATIVE_SERVICE_METHOD_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ALTERNATIVE_SERVICE_METHOD_EMAIL_AND_DIFFERENT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ALTERNATIVE_SERVICE_PARTNER_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ALTERNATIVE_SERVICE_PARTNER_OTHER_DETAILS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ALTERNATIVE_SERVICE_PARTNER_PHONE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ALTERNATIVE_SERVICE_PARTNER_SOCIAL_DETAILS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ALTERNATIVE_SERVICE_PARTNER_WA_NUM;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ALTERNATIVE_SERVICE_REASON_FOR_APPLYING;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CCD_CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_DISSOLUTION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SERVICE_APPLICATION_RECEIVED_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.STATEMENT_OF_TRUTH;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.notification.FormatUtil.getDateTimeFormatterForPreferredLanguage;

@Component
@RequiredArgsConstructor
public class AlternativeServiceApplicationTemplateContent implements TemplateContent {

    private final DocmosisCommonContent docmosisCommonContent;

    private static final String TRUE = "true";
    private static final String FALSE = "false";

    @Override
    public List<String> getSupportedTemplates() {
        return List.of(
            ALTERNATIVE_SERVICE_APPLICATION_TEMPLATE_ID
        );
    }

    @Override
    public Map<String, Object> getTemplateContent(CaseData caseData, Long caseId, Applicant applicant) {
        Map<String, Object> templateContent = docmosisCommonContent
            .getBasicDocmosisTemplateContent(applicant.getLanguagePreference());

        AlternativeService alternativeService = caseData.getAlternativeService();
        InterimApplicationOptions interimApplicationOptions = applicant.getInterimApplicationOptions();
        LanguagePreference languagePreference = applicant.getLanguagePreference();
        AlternativeServiceJourneyOptions applicationAnswers =
                applicant.getInterimApplicationOptions().getAlternativeServiceJourneyOptions();
        DateTimeFormatter dateTimeFormatter = getDateTimeFormatterForPreferredLanguage(languagePreference);

        templateContent.put(APPLICANT_1_FULL_NAME, applicant.getFullName());
        templateContent.put(APPLICANT_2_FULL_NAME, caseData.getApplicant2().getFullName());
        templateContent.put(CCD_CASE_REFERENCE, formatId(caseId));
        templateContent.put(
            SERVICE_APPLICATION_RECEIVED_DATE, dateTimeFormatter.format(alternativeService.getReceivedServiceApplicationDate())
        );
        templateContent.put(ALTERNATIVE_SERVICE_REASON_FOR_APPLYING, applicationAnswers.getAltServiceReasonForApplying());
        templateContent.put(ALTERNATIVE_SERVICE_METHOD_EMAIL,
            applicationAnswers.getAltServiceMethod() == AlternativeServiceMethod.EMAIL ? TRUE : FALSE);
        templateContent.put(ALTERNATIVE_SERVICE_METHOD_EMAIL_AND_DIFFERENT,
            applicationAnswers.getAltServiceMethod() == AlternativeServiceMethod.EMAIL_AND_DIFFERENT ? TRUE : FALSE);
        templateContent.put(ALTERNATIVE_SERVICE_BY_TEXT, applicationAnswers.getAltServiceDifferentWays() != null
            && applicationAnswers.getAltServiceDifferentWays().contains(AlternativeServiceDifferentWays.TEXT_MESSAGE) ? TRUE : FALSE);
        templateContent.put(ALTERNATIVE_SERVICE_BY_WHATSAPP, applicationAnswers.getAltServiceDifferentWays() != null
            && applicationAnswers.getAltServiceDifferentWays().contains(AlternativeServiceDifferentWays.WHATSAPP) ? TRUE : FALSE);
        templateContent.put(ALTERNATIVE_SERVICE_BY_SOCIAL_MEDIA, applicationAnswers.getAltServiceDifferentWays() != null
            && applicationAnswers.getAltServiceDifferentWays().contains(AlternativeServiceDifferentWays.SOCIAL_MEDIA) ? TRUE : FALSE);
        templateContent.put(ALTERNATIVE_SERVICE_BY_OTHER, applicationAnswers.getAltServiceDifferentWays() != null
            && applicationAnswers.getAltServiceDifferentWays().contains(AlternativeServiceDifferentWays.OTHER) ? TRUE : FALSE);
        templateContent.put(ALTERNATIVE_SERVICE_PARTNER_EMAIL, applicationAnswers.getAltServicePartnerEmail());
        templateContent.put(ALTERNATIVE_SERVICE_PARTNER_PHONE, applicationAnswers.getAltServicePartnerPhone());
        templateContent.put(ALTERNATIVE_SERVICE_PARTNER_WA_NUM, applicationAnswers.getAltServicePartnerWANum());
        templateContent.put(ALTERNATIVE_SERVICE_PARTNER_SOCIAL_DETAILS, applicationAnswers.getAltServicePartnerSocialDetails());
        templateContent.put(ALTERNATIVE_SERVICE_PARTNER_OTHER_DETAILS, applicationAnswers.getAltServicePartnerOtherDetails());
        templateContent.put(ALTERNATIVE_SERVICE_EVIDENCE_DETAILS, applicationAnswers.getAltServiceMethodJustification());
        templateContent.put(ALTERNATIVE_SERVICE_EVIDENCE_UPLOADED,
            YesOrNo.YES.equals(interimApplicationOptions.getInterimAppsCanUploadEvidence()));
        templateContent.put(DIVORCE_OR_DISSOLUTION, docmosisCommonContent.getApplicationType(languagePreference, caseData));
        templateContent.put(STATEMENT_OF_TRUTH, LanguagePreference.WELSH.equals(languagePreference) ? "Ydw" : "Yes");

        return templateContent;
    }
}
