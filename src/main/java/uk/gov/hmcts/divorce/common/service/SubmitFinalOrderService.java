package uk.gov.hmcts.divorce.common.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.common.service.task.ProgressFinalOrderState;
import uk.gov.hmcts.divorce.common.service.task.SetFinalOrderFieldsAsApplicant1;
import uk.gov.hmcts.divorce.common.service.task.SetFinalOrderFieldsAsApplicant2;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTaskRunner;

@Service
public class SubmitFinalOrderService {

    @Autowired
    private SetFinalOrderFieldsAsApplicant1 setFinalOrderFieldsAsApplicant1;

    @Autowired
    private SetFinalOrderFieldsAsApplicant2 setFinalOrderFieldsAsApplicant2;

    @Autowired
    private ProgressFinalOrderState progressFinalOrderState;

    public CaseDetails<CaseData, State> submitFinalOrderAsApplicant1(final CaseDetails<CaseData, State> caseDetails) {

        return CaseTaskRunner.caseTasks(
            setFinalOrderFieldsAsApplicant1,
            progressFinalOrderState
        ).run(caseDetails);
    }

    public CaseDetails<CaseData, State> submitFinalOrderAsApplicant2(final CaseDetails<CaseData, State> caseDetails) {

        return CaseTaskRunner.caseTasks(
            setFinalOrderFieldsAsApplicant2,
            progressFinalOrderState
        ).run(caseDetails);
    }
}
