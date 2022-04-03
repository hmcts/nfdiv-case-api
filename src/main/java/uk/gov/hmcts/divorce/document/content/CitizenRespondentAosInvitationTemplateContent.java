package uk.gov.hmcts.divorce.document.content;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import java.util.Map;

@Component
@Slf4j
public class CitizenRespondentAosInvitationTemplateContent {

    public static final String APPLICANT_2_ADDRESS = "applicant2Address";

    @Autowired
    private NoticeOfProceedingContent noticeOfProceedingContent;

    //Reuse notice of proceeding template content as it includes all template vars required for citizen respondent template
    public Map<String, Object> apply(final CaseData caseData, final Long ccdCaseReference) {
        Map<String, Object> respondentAosTemplateContent = noticeOfProceedingContent.apply(caseData, ccdCaseReference);
        respondentAosTemplateContent.put(APPLICANT_2_ADDRESS, caseData.getApplicant2().getPostalAddress());
        return respondentAosTemplateContent;
    }
}
