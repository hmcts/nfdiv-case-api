package uk.gov.hmcts.divorce.common.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.caseworker.service.task.SendAosResponseLetterPackToApplicant;
import uk.gov.hmcts.divorce.common.service.task.AddRespondentAnswersLink;
import uk.gov.hmcts.divorce.common.service.task.SendAosNotifications;
import uk.gov.hmcts.divorce.common.service.task.SetSubmissionAndDueDate;
import uk.gov.hmcts.divorce.common.service.task.SetSubmitAosState;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import static uk.gov.hmcts.divorce.divorcecase.task.CaseTaskRunner.caseTasks;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubmitAosService {

    private final SetSubmitAosState setSubmitAosState;
    private final SetSubmissionAndDueDate setSubmissionAndDueDate;
    private final AddRespondentAnswersLink addRespondentAnswersLink;
    private final SendAosNotifications sendAosNotifications;
    private final SendAosResponseLetterPackToApplicant sendAosResponseLetterPackToApplicant;

    public CaseDetails<CaseData, State> submitAos(final CaseDetails<CaseData, State> caseDetails) {
        return caseTasks(
            setSubmitAosState,
            setSubmissionAndDueDate,
            addRespondentAnswersLink
        ).run(caseDetails);
    }

    public CaseDetails<CaseData, State> submitOfflineAos(final CaseDetails<CaseData, State> caseDetails) {
        return caseTasks(
            setSubmitAosState,
            setSubmissionAndDueDate,
            addRespondentAnswersLink
        ).run(caseDetails);
    }

    public CaseDetails<CaseData, State> submitAosNotifications(final CaseDetails<CaseData, State> caseDetails) {
        return caseTasks(
            sendAosNotifications,
            sendAosResponseLetterPackToApplicant
        ).run(caseDetails);
    }
}
