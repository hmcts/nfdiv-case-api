package uk.gov.hmcts.divorce.caseworker.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateD10Form;
import uk.gov.hmcts.divorce.caseworker.service.task.SendApplicationIssueNotifications;
import uk.gov.hmcts.divorce.caseworker.service.task.SetDueDateAfterIssue;
import uk.gov.hmcts.divorce.caseworker.service.task.SetIssueDate;
import uk.gov.hmcts.divorce.caseworker.service.task.SetNoticeOfProceedingDetailsForRespondent;
import uk.gov.hmcts.divorce.caseworker.service.task.SetPostIssueState;
import uk.gov.hmcts.divorce.caseworker.service.task.SetServiceType;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.print.LetterPrinter;
import uk.gov.hmcts.divorce.document.print.documentpack.NoticeOfProceedingDocumentPack;
import uk.gov.hmcts.divorce.document.task.DivorceApplicationRemover;
import uk.gov.hmcts.divorce.systemupdate.service.task.GenerateD84Form;

import static uk.gov.hmcts.divorce.divorcecase.task.CaseTaskRunner.caseTasks;

@RequiredArgsConstructor
@Service
public class IssueApplicationService {

    private final SetPostIssueState setPostIssueState;
    private final DivorceApplicationRemover divorceApplicationRemover;
    private final SetNoticeOfProceedingDetailsForRespondent setNoticeOfProceedingDetailsForRespondent;
    private final SendApplicationIssueNotifications sendApplicationIssueNotifications;
    private final SetDueDateAfterIssue setDueDateAfterIssue;
    private final GenerateD10Form generateD10Form;
    private final GenerateD84Form generateD84Form;
    private final SetServiceType setServiceType;
    private final SetIssueDate setIssueDate;
    private final LetterPrinter letterPrinter;
    private final NoticeOfProceedingDocumentPack noticeOfProceedingDocumentPack;

    public CaseDetails<CaseData, State> issueApplication(final CaseDetails<CaseData, State> caseDetails) {
        return caseTasks(
            setServiceType,
            setIssueDate,
            setPostIssueState,
            setDueDateAfterIssue,
            setNoticeOfProceedingDetailsForRespondent,
            divorceApplicationRemover,
            generateD10Form,
            generateD84Form
        ).run(caseDetails);
    }

    public void sendNotifications(final CaseDetails<CaseData, State> caseDetails) {

        CaseData caseData = caseDetails.getData();
        Applicant applicant1 = caseData.getApplicant1();
        Applicant applicant2 = caseData.getApplicant2();

        letterPrinter.sendLetters(caseData, caseDetails.getId(), applicant2,
                noticeOfProceedingDocumentPack.getDocumentPack(caseData, applicant2),
                noticeOfProceedingDocumentPack.getLetterId());

        letterPrinter.sendLetters(caseData, caseDetails.getId(), applicant1,
                noticeOfProceedingDocumentPack.getDocumentPack(caseData, applicant1),
                noticeOfProceedingDocumentPack.getLetterId());

        caseTasks(
            sendApplicationIssueNotifications
        ).run(caseDetails);
    }
}
