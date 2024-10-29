package uk.gov.hmcts.divorce.caseworker.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseInvite;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;

import java.time.Clock;

@Component
@Slf4j
public class SetCaseInviteApplicant2 implements CaseTask {

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> details) {

        final CaseData data = details.getData();
        CaseInvite invite = CaseInvite.builder()
            .applicant2InviteEmailAddress(data.getApplicant2().getEmail())
            .build()
            .generateAccessCode();
        data.setCaseInvite(invite);

        return details;
    }
}
