package uk.gov.hmcts.divorce.document.content.templatecontent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.divorce.document.DocumentConstants.FINAL_ORDER_CAN_APPLY_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;

@Component
@Slf4j
public class FinalOrderCanApplyTemplateContent implements TemplateContent {

    @Override
    public List<String> getSupportedTemplates() {
        return List.of(FINAL_ORDER_CAN_APPLY_TEMPLATE_ID);
    }

    @Override
    public Map<String, Object> getTemplateContent(CaseData caseData, Long caseId, Applicant applicant) {
        return Map.of(CASE_REFERENCE, formatId(caseId),
            "applicantFirstName", applicant.getFirstName(),
            "applicantLastName", applicant.getLastName(),
            "applicantAddress", applicant.getCorrespondenceAddressWithoutConfidentialCheck());
    }
}
