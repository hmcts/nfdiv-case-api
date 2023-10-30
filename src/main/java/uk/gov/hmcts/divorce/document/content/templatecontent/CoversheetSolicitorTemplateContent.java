package uk.gov.hmcts.divorce.document.content.templatecontent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_APPLICANT2_SOLICITOR;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;

@Component
@Slf4j
public class CoversheetSolicitorTemplateContent implements TemplateContent {

    @Override
    public List<String> getSupportedTemplates() {
        return List.of(COVERSHEET_APPLICANT2_SOLICITOR);
    }

    @Override
    public Map<String, Object> getTemplateContent(CaseData caseData, Long caseId, Applicant applicant) {
        return apply(caseId, applicant);
    }

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
