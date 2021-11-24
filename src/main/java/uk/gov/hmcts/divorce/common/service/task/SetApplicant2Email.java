package uk.gov.hmcts.divorce.common.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;

import static org.apache.groovy.parser.antlr4.util.StringUtils.isEmpty;

@Component
@Slf4j
public class SetApplicant2Email implements CaseTask {

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {

        final CaseData caseData = caseDetails.getData();
        final String applicant2InviteEmailAddress = caseData.getCaseInvite().getApplicant2InviteEmailAddress();
        final Applicant applicant2 = caseData.getApplicant2();

        if (!isEmpty(applicant2InviteEmailAddress) && isEmpty(applicant2.getEmail())) {
            applicant2.setEmail(applicant2InviteEmailAddress);
        }

        return caseDetails;
    }
}
