package uk.gov.hmcts.divorce.solicitor.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.task.DivorceApplicationRemover;
import uk.gov.hmcts.divorce.solicitor.service.task.DivorceApplicationDraft;

import static uk.gov.hmcts.divorce.divorcecase.task.CaseTaskRunner.caseTasks;

@Service
@Slf4j
public class SolicitorUpdateApplicationService {

    @Autowired
    private DivorceApplicationRemover divorceApplicationRemover;

    @Autowired
    private DivorceApplicationDraft divorceApplicationDraft;

    public CaseDetails<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> caseDetails) {

        return caseTasks(
            divorceApplicationRemover,
            divorceApplicationDraft
        ).run(caseDetails);
    }
}
