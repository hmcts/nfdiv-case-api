package uk.gov.hmcts.divorce.document.content;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.DeemedServiceJourneyOptions;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;
import uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference;
import uk.gov.hmcts.divorce.document.content.templatecontent.TemplateContent;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.divorce.document.DocumentConstants.DEEMED_SERVICE_APPLICATION_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CCD_CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DEEMED_EVIDENCE_DETAILS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DEEMED_EVIDENCE_UPLOADED;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DEEMED_NO_EVIDENCE_STATEMENT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_APPLICATION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_APPLICATION_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_DISSOLUTION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.END_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.END_CIVIL_PARTNERSHIP_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SERVICE_APPLICATION_RECEIVED_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.STATEMENT_OF_TRUTH;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.notification.FormatUtil.getDateTimeFormatterForPreferredLanguage;

@Component
@RequiredArgsConstructor
public class DeemedServiceApplicationTemplateContent implements TemplateContent {

    private final DocmosisCommonContent docmosisCommonContent;

    @Override
    public List<String> getSupportedTemplates() {
        return List.of(
            DEEMED_SERVICE_APPLICATION_TEMPLATE_ID
        );
    }

    @Override
    public Map<String, Object> getTemplateContent(CaseData caseData, Long caseId, Applicant applicant) {
        Map<String, Object> templateContent = docmosisCommonContent
            .getBasicDocmosisTemplateContent(applicant.getLanguagePreference());

        AlternativeService alternativeService = caseData.getAlternativeService();
        InterimApplicationOptions interimApplicationOptions = applicant.getInterimApplicationOptions();
        DeemedServiceJourneyOptions applicationAnswers = applicant.getInterimApplicationOptions().getDeemedServiceJourneyOptions();
        LanguagePreference languagePreference = applicant.getLanguagePreference();
        DateTimeFormatter dateTimeFormatter = getDateTimeFormatterForPreferredLanguage(languagePreference);

        templateContent.put(APPLICANT_1_FULL_NAME, applicant.getFullName());
        templateContent.put(APPLICANT_2_FULL_NAME, caseData.getApplicant2().getFullName());
        templateContent.put(CCD_CASE_REFERENCE, formatId(caseId));
        templateContent.put(
            SERVICE_APPLICATION_RECEIVED_DATE, dateTimeFormatter.format(alternativeService.getReceivedServiceApplicationDate())
        );
        templateContent.put(DEEMED_EVIDENCE_UPLOADED, YesOrNo.YES.equals(interimApplicationOptions.getInterimAppsCanUploadEvidence()));
        templateContent.put(DEEMED_EVIDENCE_DETAILS, applicationAnswers.getDeemedEvidenceDetails());
        templateContent.put(DEEMED_NO_EVIDENCE_STATEMENT, applicationAnswers.getDeemedNoEvidenceStatement());
        templateContent.put(DIVORCE_OR_DISSOLUTION, getApplicationType(languagePreference, caseData));
        templateContent.put(STATEMENT_OF_TRUTH, LanguagePreference.WELSH.equals(languagePreference) ? "Ydw" : "Yes");

        return templateContent;
    }

    private String getApplicationType(LanguagePreference languagePreference, CaseData caseData) {
        return LanguagePreference.WELSH.equals(languagePreference)
            ? caseData.isDivorce() ? DIVORCE_APPLICATION_CY : END_CIVIL_PARTNERSHIP_CY
            : caseData.isDivorce() ? DIVORCE_APPLICATION : END_CIVIL_PARTNERSHIP;
    }
}
