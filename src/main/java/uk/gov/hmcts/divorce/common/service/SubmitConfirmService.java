package uk.gov.hmcts.divorce.common.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.common.service.task.SetServiceConfirmed;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.solicitor.service.task.SetConfirmServiceDueDate;
import uk.gov.hmcts.divorce.solicitor.service.task.SetConfirmServiceState;

import static uk.gov.hmcts.divorce.divorcecase.task.CaseTaskRunner.caseTasks;

@Service
@Slf4j
@RequiredArgsConstructor
public class SubmitConfirmService {
    private final SetConfirmServiceDueDate setConfirmServiceDueDate;

    private final SetServiceConfirmed setServiceConfirmed;

    private final SetConfirmServiceState setConfirmServiceState;

    public CaseDetails<CaseData, State> submitConfirmService(final CaseDetails<CaseData, State> caseDetails) {
        return caseTasks(
            setConfirmServiceDueDate,
            setServiceConfirmed,
            setConfirmServiceState
        ).run(caseDetails);
    }
}
