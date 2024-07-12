package uk.gov.hmcts.divorce.notification;

import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

public interface ApplicantNotification {

    default void sendToApplicant1(final CaseData caseData, final Long caseId) {
        //No operation
    }

    default void sendToApplicant1Solicitor(final CaseData caseData, final Long caseId) {
        //No operation
    }

    default void sendToApplicant1Offline(final CaseData caseData, final Long caseId) {
        //No operation
    }

    default void sendToApplicant2(final CaseData caseData, final Long caseId) {
        //No operation
    }

    default void sendToApplicant2Solicitor(final CaseData caseData, final Long caseId) {
        //No operation
    }

    default void sendToApplicant2Offline(final CaseData caseData, final Long caseId) {
        //No operation
    }

    default void sendToApplicant1OldSolicitor(final CaseData oldCaseData, final Long caseId) {
        //No operation
    }

    default void sendToApplicant2OldSolicitor(final CaseData oldCaseData, final Long caseId) {
        //No operation
    }
}
