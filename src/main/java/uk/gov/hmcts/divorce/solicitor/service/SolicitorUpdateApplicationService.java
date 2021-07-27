package uk.gov.hmcts.divorce.solicitor.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.solicitor.service.task.MiniApplicationDraft;
import uk.gov.hmcts.divorce.solicitor.service.task.MiniApplicationRemover;

import static uk.gov.hmcts.divorce.divorcecase.task.CaseTaskRunner.caseTasks;

@Service
@Slf4j
public class SolicitorUpdateApplicationService {

    @Autowired
    private MiniApplicationRemover miniApplicationRemover;

    @Autowired
    private MiniApplicationDraft miniApplicationDraft;

    public CaseDetails<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> caseDetails) {

        return caseTasks(
            miniApplicationRemover,
            miniApplicationDraft
        ).run(caseDetails);
    }
}
