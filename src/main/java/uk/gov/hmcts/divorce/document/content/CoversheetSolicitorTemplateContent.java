package uk.gov.hmcts.divorce.document.content;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;

@Component
@Slf4j
public class CoversheetSolicitorTemplateContent {

    public Map<String, Object> apply(final CaseData caseData, final Long ccdCaseReference) {
        return this.apply(ccdCaseReference, caseData.getApplicant2());
    }

    public Map<String, Object> apply(final Long ccdCaseReference, final Applicant applicant) {
        final Map<String, Object> templateContent = new HashMap<>();
        templateContent.put(CASE_REFERENCE, formatId(ccdCaseReference));
        templateContent.put(SOLICITOR_NAME, applicant.getSolicitor().getName());
        templateContent.put(SOLICITOR_ADDRESS, applicant.getSolicitor().getAddress());
        return templateContent;
    }
}
