package uk.gov.hmcts.divorce.document.content;

import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import java.util.Map;

public interface ConditionalOrderRefusedTemplateContent {
    Map<String, Object> apply(final CaseData caseData, final Long ccdCaseReference);
}
