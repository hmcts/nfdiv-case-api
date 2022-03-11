package uk.gov.hmcts.divorce.document.content;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import java.util.Map;

@Component
@Slf4j
public class CitizenRespondentAosInvitationTemplateContent {

    @Autowired
    private NoticeOfProceedingContent templateContent;

    //Reuse notice of proceeding template content as it includes all template vars required for citizen respondent template
    public Map<String, Object> apply(final CaseData caseData, final Long ccdCaseReference) {
        return templateContent.apply(caseData, ccdCaseReference);
    }
}
