package uk.gov.hmcts.divorce.caseworker.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateFinalOrder;
import uk.gov.hmcts.divorce.caseworker.service.task.SendFinalOrderGrantedNotifications;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import static uk.gov.hmcts.divorce.divorcecase.task.CaseTaskRunner.caseTasks;

@Service
@Slf4j
public class GrantFinalOrderService {

    @Autowired
    private GenerateFinalOrder generateFinalOrder;

    @Autowired
    private SendFinalOrderGrantedNotifications sendFinalOrderGrantedNotifications;

    public CaseDetails<CaseData, State> process(final CaseDetails<CaseData, State> caseDetails) {
        return caseTasks(
            generateFinalOrder,
            sendFinalOrderGrantedNotifications
        ).run(caseDetails);
    }
}
