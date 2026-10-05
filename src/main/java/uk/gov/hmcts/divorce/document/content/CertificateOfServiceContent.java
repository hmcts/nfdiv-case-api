package uk.gov.hmcts.divorce.document.content;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import java.time.Clock;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.document.content.BailiffServiceApplicationTemplateContent.CONFIDENTIAL_ADDRESS_CY;
import static uk.gov.hmcts.divorce.document.content.BailiffServiceApplicationTemplateContent.CONFIDENTIAL_ADDRESS_EN;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CCD_CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_APPLICATION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_APPLICATION_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_DISSOLUTION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DOCUMENTS_ISSUED_ON;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.END_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.END_CIVIL_PARTNERSHIP_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PETITIONER_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RESPONDENT_FULL_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;

@Component
@Slf4j
@RequiredArgsConstructor
public class CertificateOfServiceContent {

    private final Clock clock;

    public Map<String, Object> apply(final CaseData caseData, final Long ccdCaseReference) {

        final Map<String, Object> templateContent = new HashMap<>();

        log.info("For ccd case reference {} and type(divorce/dissolution) {} ", ccdCaseReference, caseData.getDivorceOrDissolution());

        templateContent.put(CCD_CASE_REFERENCE, ccdCaseReference);
        templateContent.put(PETITIONER_FULL_NAME, caseData.getApplicant1().getFullName());
        templateContent.put(RESPONDENT_FULL_NAME, caseData.getApplicant2().getFullName());
        templateContent.put(DOCUMENTS_ISSUED_ON, LocalDate.now(clock).format(DATE_TIME_FORMATTER));
        templateContent.put(IS_DIVORCE, caseData.isDivorce() ? YES.getValue() : NO.getValue());

        var applicant1LanguagePreference = caseData.getApplicant1().getLanguagePreference();

        if (caseData.getDivorceOrDissolution().isDivorce()) {
            templateContent.put(
                DIVORCE_OR_DISSOLUTION, WELSH.equals(applicant1LanguagePreference)
                    ? DIVORCE_APPLICATION_CY
                    : DIVORCE_APPLICATION
            );
        } else {
            templateContent.put(
                DIVORCE_OR_DISSOLUTION, WELSH.equals(applicant1LanguagePreference)
                    ? END_CIVIL_PARTNERSHIP_CY
                    : END_CIVIL_PARTNERSHIP
            );
        }

        if (caseData.getApplicant2().isConfidentialContactDetails()) {
            templateContent.put(
                APPLICANT_2_ADDRESS, WELSH.equals(applicant1LanguagePreference)
                    ? CONFIDENTIAL_ADDRESS_CY
                    : CONFIDENTIAL_ADDRESS_EN);
        } else {
            templateContent.put(
                APPLICANT_2_ADDRESS, caseData.getApplicant2().getCorrespondenceAddress());
        }

        return templateContent;
    }
}
