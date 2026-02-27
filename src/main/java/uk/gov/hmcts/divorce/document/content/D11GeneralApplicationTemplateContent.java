package uk.gov.hmcts.divorce.document.content;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplication;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplicationD11JourneyOptions;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;
import uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static uk.gov.hmcts.divorce.document.DocumentConstants.D11_GENERAL_APPLICATION_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_LABEL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_LABEL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_LABEL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CCD_CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RESPONDENT_LABEL;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.notification.FormatUtil.getDateTimeFormatterForPreferredLanguage;

@Component
@RequiredArgsConstructor
@Slf4j
public class D11GeneralApplicationTemplateContent {

    private final DocmosisCommonContent docmosisCommonContent;

    public static final String APPLICATION_DATE = "applicationDate";
    public static final String CASE_APPLICANT_LABEL = "caseApplicantLabel";
    public static final String CASE_RESPONDENT_OR_APPLICANT2_LABEL = "caseRespondentOrApplicant2Label";
    public static final String GENERAL_APPLICANT_LABEL = "generalApplicantLabel";
    public static final String HEARING_NOT_REQUIRED_DETAILS = "hearingNotRequiredDetails";
    public static final String EVIDENCE_PARTNER_AGREES_REQUIRED = "evidencePartnerAgreesRequired";
    public static final String HAS_UPLOADED_PARTNER_AGREES_DOCS = "hasUploadedPartnerAgreesDocs";
    public static final String PARTNER_DETAILS_CORRECT = "partnerDetailsCorrect";
    public static final String APPLICATION_TYPE = "applicationType";
    public static final String IS_OTHER_APPLICATION_TYPE = "isOtherApplicationType";
    public static final String APPLICATION_TYPE_OTHER_DETAILS = "applicationTypeOtherDetails";
    public static final String APPLICATION_REASON = "applicationReason";
    public static final String HAS_PROVIDED_EVIDENCE = "hasProvidedEvidence";
    public static final String HAS_PROVIDED_STATEMENT = "hasProvidedStatement";
    public static final String STATEMENT_OF_EVIDENCE = "statementOfEvidence";
    public static final String SUPPORTING_EVIDENCE_UPLOADED = "supportingEvidenceUploaded";
    public static final String GENERAL_APPLICANT_FULL_NAME = "generalApplicantFullName";

    private static final String CONFIDENTIAL_PARTNER_PLACEHOLDER = "Their partner's details are confidential";

    public List<String> getSupportedTemplates() {
        return List.of(D11_GENERAL_APPLICATION_TEMPLATE_ID);
    }

    public Map<String, Object> getTemplateContent(
        CaseData caseData, Long caseId,
        Applicant applicant, GeneralApplication generalApplication
    ) {
        LanguagePreference languagePreference = applicant.getLanguagePreference();

        Map<String, Object> templateContent = docmosisCommonContent
            .getBasicDocmosisTemplateContent(languagePreference);

        final DateTimeFormatter dateTimeFormatter = getDateTimeFormatterForPreferredLanguage(languagePreference);
        final Applicant applicant1 = caseData.getApplicant1();
        final Applicant applicant2 = caseData.getApplicant2();
        final boolean isApplicant1 = applicant.equals(applicant1);

        String caseApplicantLabel = getApplicantOrApplicant1Label(caseData.getApplicationType());
        String caseRespondentOrApplicant2Label = getRespondentOrApplicant2Label(caseData.getApplicationType());
        templateContent.put(APPLICANT_1_FULL_NAME, applicant1.getFullName());
        templateContent.put(APPLICANT_2_FULL_NAME, applicant2.getFullName());
        templateContent.put(CCD_CASE_REFERENCE, formatId(caseId));
        templateContent.put(CASE_APPLICANT_LABEL, caseApplicantLabel);
        templateContent.put(CASE_RESPONDENT_OR_APPLICANT2_LABEL, caseRespondentOrApplicant2Label);
        templateContent.put(GENERAL_APPLICANT_LABEL, isApplicant1 ? caseApplicantLabel : caseRespondentOrApplicant2Label);
        templateContent.put(GENERAL_APPLICANT_FULL_NAME, isApplicant1 ? applicant1.getFullName() : applicant2.getFullName());
        templateContent.put(
            APPLICATION_DATE,
            dateTimeFormatter.format(generalApplication.getGeneralApplicationReceivedDate().toLocalDate())
        );
        
        templateContent.putAll(generalApplicationD11Content(
            isApplicant1, caseData
        ));

        return templateContent;
    }

    private Map<String, Object>  generalApplicationD11Content(boolean isApplicant1, CaseData caseData) {
        final Map<String, Object> templateContent = new HashMap<>();
        final Applicant applicant = isApplicant1 ? caseData.getApplicant1() : caseData.getApplicant2();
        final Applicant partner = isApplicant1 ? caseData.getApplicant2() : caseData.getApplicant1();
        final InterimApplicationOptions interimApplicationOptions = applicant.getInterimApplicationOptions();
        final GeneralApplicationD11JourneyOptions applicationAnswers = interimApplicationOptions.getGeneralApplicationD11JourneyOptions();
        final boolean isDivorce = caseData.isDivorce();

        templateContent.put(HEARING_NOT_REQUIRED_DETAILS, applicationAnswers.getHearingNotRequired().getLabel());
        templateContent.put(
            EVIDENCE_PARTNER_AGREES_REQUIRED,
            YesOrNo.from(applicationAnswers.evidenceOfPartnerSupportRequired()).getValue()
        );
        templateContent.put(
            HAS_UPLOADED_PARTNER_AGREES_DOCS,
            YesOrNo.from(!YesOrNo.YES.equals(applicationAnswers.getCannotUploadAgreedEvidence())).getValue()
        );
        templateContent.put(
            PARTNER_DETAILS_CORRECT,
            partner.isConfidentialContactDetails() ?
                CONFIDENTIAL_PARTNER_PLACEHOLDER :
                applicationAnswers.getPartnerDetailsCorrect().getValue()
        );
        templateContent.put(APPLICATION_REASON, applicationAnswers.getReason());

        final boolean hasProvidedEvidence = YesOrNo.YES.equals(interimApplicationOptions.getInterimAppsCanUploadEvidence());
        final boolean hasUploadedAllSupportingEvidence = hasProvidedEvidence
            && !YesOrNo.YES.equals(interimApplicationOptions.getInterimAppsCannotUploadDocs());
        templateContent.put(HAS_PROVIDED_EVIDENCE, YesOrNo.from(hasProvidedEvidence).getValue());
        templateContent.put(
            HAS_PROVIDED_STATEMENT,
            YesOrNo.from(hasProvidedEvidence && isNotEmpty(applicationAnswers.getStatementOfEvidence())).getValue()
        );
        templateContent.put(STATEMENT_OF_EVIDENCE, applicationAnswers.getStatementOfEvidence());
        templateContent.put(SUPPORTING_EVIDENCE_UPLOADED, YesOrNo.from(hasUploadedAllSupportingEvidence).getValue());

        final GeneralApplicationType applicationType = applicationAnswers.getType();
        templateContent.put(
                APPLICATION_TYPE,
                docmosisCommonContent.getGeneralApplicationTypeLabel(applicationType, isDivorce)
        );
        templateContent.put(
            IS_OTHER_APPLICATION_TYPE, YesOrNo.from(GeneralApplicationType.OTHER.equals(applicationType)).getValue()
        );
        templateContent.put(APPLICATION_TYPE_OTHER_DETAILS, applicationAnswers.getTypeOtherDetails());


        return templateContent;
    }

    private String getApplicantOrApplicant1Label(ApplicationType applicationType) {
        return ApplicationType.JOINT_APPLICATION.equals(applicationType)
            ? APPLICANT_1_LABEL
            : APPLICANT_LABEL;
    }

    private String getRespondentOrApplicant2Label(ApplicationType applicationType) {
        return ApplicationType.JOINT_APPLICATION.equals(applicationType)
            ? APPLICANT_2_LABEL
            : RESPONDENT_LABEL;
    }
}
