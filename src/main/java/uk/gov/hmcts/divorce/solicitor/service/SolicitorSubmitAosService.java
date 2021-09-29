package uk.gov.hmcts.divorce.solicitor.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.solicitor.service.task.AddRespondentAnswersLink;
import uk.gov.hmcts.divorce.solicitor.service.task.GenerateRespondentAnswersDoc;
import uk.gov.hmcts.divorce.solicitor.service.task.SetSubmissionAndDueDate;
import uk.gov.hmcts.divorce.solicitor.service.task.SetSubmitAosState;

import static uk.gov.hmcts.divorce.divorcecase.task.CaseTaskRunner.caseTasks;

@Service
@Slf4j
public class SolicitorSubmitAosService {

    @Autowired
    private SetSubmitAosState setSubmitAosState;

    @Autowired
    private SetSubmissionAndDueDate setSubmissionAndDueDate;

    @Autowired
    private GenerateRespondentAnswersDoc generateRespondentAnswersDoc;

    @Autowired
    private AddRespondentAnswersLink addRespondentAnswersLink;

    public CaseDetails<CaseData, State> submitAos(final CaseDetails<CaseData, State> caseDetails) {
        return caseTasks(
            setSubmitAosState,
            setSubmissionAndDueDate,
            generateRespondentAnswersDoc,
            addRespondentAnswersLink
        ).run(caseDetails);
    }
}
