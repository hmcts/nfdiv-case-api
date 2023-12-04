package uk.gov.hmcts.divorce.document.content.templatecontent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.systemupdate.service.task.GenerateCertificateOfEntitlementHelper;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.divorce.document.DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_OFFLINE_RESPONDENT_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_JS_COVER_LETTER_TEMPLATE_ID;

@Component
@RequiredArgsConstructor
@Slf4j
public class CertificateOfEntitlementCoverLetterTemplateContent implements TemplateContent {
    private final GenerateCertificateOfEntitlementHelper generateCertificateOfEntitlementHelper;

    @Override
    public List<String> getSupportedTemplates() {
        return List.of(CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_TEMPLATE_ID,
                CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_OFFLINE_RESPONDENT_TEMPLATE_ID,
                CERTIFICATE_OF_ENTITLEMENT_JS_COVER_LETTER_TEMPLATE_ID);
    }

    @Override
    public Map<String, Object> getTemplateContent(CaseData caseData, Long caseId, Applicant applicant) {
        return generateCertificateOfEntitlementHelper.getTemplateContent(
                caseData, caseId, applicant, caseData.getApplicant2());
    }
}
