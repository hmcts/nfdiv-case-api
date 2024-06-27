package uk.gov.hmcts.divorce.caseworker.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import java.time.Clock;
import java.time.LocalDateTime;

import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingDocuments;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;

@Component
public class CaseworkerHwfApplicationAndPaymentHelper {
    @Autowired
    private Clock clock;

    public State getState(CaseData caseData) {
        if (caseData.getApplicationType().isSole()
            && NO.equals(caseData.getApplication().getApplicant1KnowsApplicant2Address())
            && YES.equals(caseData.getApplication().getApplicant1WantsToHavePapersServedAnotherWay())) {
            return AwaitingDocuments;
        } else {
            return Submitted;
        }
    }

    public CaseData setDateSubmittedAndDueDate(CaseData caseData) {

        if (null == caseData.getApplication().getDateSubmitted()) {
            caseData.getApplication().setDateSubmitted(LocalDateTime.now(clock));
        }

        if (null == caseData.getDueDate()) {
            caseData.setDueDate(caseData.getApplication().getDateOfSubmissionResponse());
        }

        return caseData;
    }
}
