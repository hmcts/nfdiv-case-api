package uk.gov.hmcts.divorce.document.content.templatecontent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.divorce.document.DocumentConstants.REQUEST_FOR_INFORMATION_PARTNER_RESPONSE_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.REQUEST_FOR_INFORMATION_PARTNER_SOLICITOR_RESPONSE_LETTER_TEMPLATE_ID;

@Component
@RequiredArgsConstructor
@Slf4j
public class RequestForInformationPartnerResponseTemplateContent implements TemplateContent {

    private final RequestForInformationResponseTemplateContent requestForInformationResponseTemplateContent;

    @Override
    public List<String> getSupportedTemplates() {
        return List.of(
            REQUEST_FOR_INFORMATION_PARTNER_RESPONSE_LETTER_TEMPLATE_ID,
            REQUEST_FOR_INFORMATION_PARTNER_SOLICITOR_RESPONSE_LETTER_TEMPLATE_ID
        );
    }

    @Override
    public Map<String, Object> getTemplateContent(CaseData caseData, Long caseId, Applicant applicant) {
        return requestForInformationResponseTemplateContent.getTemplateContent(caseData, caseId, applicant);
    }
}
