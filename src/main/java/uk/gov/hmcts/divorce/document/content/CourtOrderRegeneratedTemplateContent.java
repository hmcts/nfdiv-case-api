package uk.gov.hmcts.divorce.document.content;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference;
import uk.gov.hmcts.divorce.divorcecase.util.AddressUtil;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_APPLICATION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_APPLICATION_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.END_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.END_CIVIL_PARTNERSHIP_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.JUDICIAL_SEPARATION_APPLICATION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.JUDICIAL_SEPARATION_APPLICATION_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RECIPIENT_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.THE_APPLICATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED;
import static uk.gov.hmcts.divorce.document.model.DocumentType.FINAL_ORDER_GRANTED;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.notification.FormatUtil.getDateTimeFormatterForPreferredLanguage;

@Component
@RequiredArgsConstructor
@Slf4j
public class CourtOrderRegeneratedTemplateContent {

    private final DocmosisCommonContent docmosisCommonContent;

    public static final String HAS_CERTIFICATE_OF_ENTITLEMENT = "hasCertificateOfEntitlement";
    public static final String HAS_FINAL_ORDER_GRANTED = "hasFinalOrderGranted";
    public static final String HAS_CONDITIONAL_ORDER_GRANTED = "hasConditionalOrderGranted";

    public Map<String, Object> getTemplateContent(CaseData caseData, Long caseId, Applicant applicant) {
        Map<String, Object> templateContent = docmosisCommonContent
                .getBasicDocmosisTemplateContent(applicant.getLanguagePreference());

        templateContent.put(CASE_REFERENCE, formatId(caseId));
        templateContent.put(THE_APPLICATION, getApplicationName(caseData));
        templateContent.put(FIRST_NAME, applicant.getFirstName());
        templateContent.put(LAST_NAME, applicant.getLastName());
        templateContent.put(RECIPIENT_ADDRESS,  AddressUtil.getPostalAddress(applicant.getAddress()));
        templateContent.putAll(regenerateTemplateContent(caseData));
        templateContent.put(DATE, LocalDate.now().format(getDateTimeFormatterForPreferredLanguage(LanguagePreference.ENGLISH)));

        return templateContent;
    }

    public Map<String, String> regenerateTemplateContent(final CaseData data) {
        final Map<String,String> templateContent = new HashMap<>();
        templateContent.put(HAS_CERTIFICATE_OF_ENTITLEMENT,
            data.getConditionalOrder().getCertificateOfEntitlementDocument() != null ? "Yes" : "No");
        templateContent.put(HAS_FINAL_ORDER_GRANTED,
            data.getDocuments().getDocumentGeneratedWithType(FINAL_ORDER_GRANTED).isPresent() ? "Yes" : "No");
        templateContent.put(HAS_CONDITIONAL_ORDER_GRANTED,
            data.getDocuments().getDocumentGeneratedWithType(CONDITIONAL_ORDER_GRANTED).isPresent() ? "Yes" : "No");
        return templateContent;
    }

    private String getApplicationName(CaseData data) {
        boolean prefersWelsh = LanguagePreference.WELSH.equals(
            data.getApplicant1().getLanguagePreference()
        );

        if (data.isJudicialSeparationCase()) {
            return prefersWelsh ? JUDICIAL_SEPARATION_APPLICATION_CY : JUDICIAL_SEPARATION_APPLICATION;
        } else if (data.isDivorce()) {
            return prefersWelsh ? DIVORCE_APPLICATION_CY : DIVORCE_APPLICATION;
        } else {
            return prefersWelsh ? END_CIVIL_PARTNERSHIP_CY : END_CIVIL_PARTNERSHIP;
        }
    }
}
