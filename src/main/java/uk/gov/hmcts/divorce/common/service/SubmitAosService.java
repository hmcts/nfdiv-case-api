package uk.gov.hmcts.divorce.common.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.caseworker.service.task.SendAosResponseLetterPackToApplicant;
import uk.gov.hmcts.divorce.common.service.task.AddRespondentAnswersLink;
import uk.gov.hmcts.divorce.common.service.task.GenerateAosResponseLetterDocument;
import uk.gov.hmcts.divorce.common.service.task.GenerateRespondentAnswersDoc;
import uk.gov.hmcts.divorce.common.service.task.SendAosNotifications;
import uk.gov.hmcts.divorce.common.service.task.SetSubmissionAndDueDate;
import uk.gov.hmcts.divorce.common.service.task.SetSubmitAosState;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import static uk.gov.hmcts.divorce.divorcecase.task.CaseTaskRunner.caseTasks;

@Service
@Slf4j
public class SubmitAosService {

    @Autowired
    private SetSubmitAosState setSubmitAosState;

    @Autowired
    private SetSubmissionAndDueDate setSubmissionAndDueDate;

    @Autowired
    private GenerateRespondentAnswersDoc generateRespondentAnswersDoc;

    @Autowired
    private AddRespondentAnswersLink addRespondentAnswersLink;

    @Autowired
    private SendAosNotifications sendAosNotifications;

    @Autowired
    private GenerateAosResponseLetterDocument generateAosResponseLetterDocument;

    @Autowired
    private SendAosResponseLetterPackToApplicant sendAosResponseLetterPackToApplicant;

    public CaseDetails<CaseData, State> submitAos(final CaseDetails<CaseData, State> caseDetails) {
        return caseTasks(
            setSubmitAosState,
            setSubmissionAndDueDate,
            generateRespondentAnswersDoc,
            addRespondentAnswersLink,
            sendAosNotifications,
            generateAosResponseLetterDocument,
            sendAosResponseLetterPackToApplicant
        ).run(caseDetails);
    }

    public CaseDetails<CaseData, State> submitOfflineAos(final CaseDetails<CaseData, State> caseDetails) {
        return caseTasks(
            setSubmitAosState,
            setSubmissionAndDueDate,
            addRespondentAnswersLink,
            sendCitizenAosNotifications,
            generateAosResponseLetterDocument,
            sendAosResponseLetterPackToApplicant
        ).run(caseDetails);
    }
}
