package uk.gov.hmcts.divorce.document.content;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;

@Component
@Slf4j
public class CoversheetTemplateContent {

    private static final String APPLICANT_2_POSTAL_ADDRESS = "applicant2Address";

    public Map<String, Object> apply(final CaseData caseData,
                                     final Long ccdCaseReference) {
        final Map<String, Object> templateContent = new HashMap<>();
        templateContent.put(CASE_REFERENCE, formatId(ccdCaseReference));
        templateContent.put(APPLICANT_2_FIRST_NAME, caseData.getApplicant2().getFirstName());
        templateContent.put(APPLICANT_2_LAST_NAME, caseData.getApplicant2().getLastName());
        templateContent.put(APPLICANT_2_POSTAL_ADDRESS, caseData.getApplicant2().getPostalAddress());
        return templateContent;
    }
}
