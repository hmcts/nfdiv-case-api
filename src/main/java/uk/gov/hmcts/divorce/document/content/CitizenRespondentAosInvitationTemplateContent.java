package uk.gov.hmcts.divorce.document.content;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import java.time.LocalDate;
import java.util.Map;

@Component
@Slf4j
public class CitizenRespondentAosInvitationTemplateContent {

    @Autowired
    private RespondentSolicitorAosInvitationTemplateContent templateContent;

    public Map<String, Object> apply(final CaseData caseData,
                                     final Long ccdCaseReference,
                                     final LocalDate createdDate) {

        //TODO: Reusing Respondent Solicitor Template until we know the required content
        return templateContent.apply(caseData, ccdCaseReference, createdDate);
    }
}
