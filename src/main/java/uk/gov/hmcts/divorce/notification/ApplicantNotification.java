package uk.gov.hmcts.divorce.notification;

import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

public abstract class ApplicantNotification {

    public boolean alreadySentToApplicant1;
    public boolean alreadySentToApplicant2;
    public boolean alreadySentToApplicant1Solicitor;
    public boolean alreadySentToApplicant2Solicitor;
    public boolean alreadySentToApplicant1Offline;
    public boolean alreadySentToApplicant2Offline;


    public abstract void sendToApplicant1(final CaseData caseData, final Long caseId);

    public abstract void sendToApplicant1Solicitor(final CaseData caseData, final Long caseId);

    public abstract void sendToApplicant1Offline(final CaseData caseData, final Long caseId);

    public abstract void sendToApplicant2(final CaseData caseData, final Long caseId);

    public abstract void sendToApplicant2Solicitor(final CaseData caseData, final Long caseId);

    public abstract void sendToApplicant2Offline(final CaseData caseData, final Long caseId);
}
