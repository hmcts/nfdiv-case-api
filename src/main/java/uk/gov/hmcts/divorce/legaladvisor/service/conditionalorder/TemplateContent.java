package uk.gov.hmcts.divorce.legaladvisor.service.conditionalorder;

import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import java.util.List;
import java.util.Map;

public interface TemplateContent {
    List<String> getSupportedTemplates();
    Map<String, Object> getTemplateContent(CaseData caseData, Long caseId, Applicant applicant);
}
