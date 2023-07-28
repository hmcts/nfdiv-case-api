package uk.gov.hmcts.divorce.common.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.systemupdate.service.print.ConditionalOrderPronouncedPrinter;

import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2;

@Component
@Slf4j
public class ResendConditionalOrderPronouncedNotification implements ApplicantNotification {

    @Autowired
    private ConditionalOrderPronouncedPrinter conditionalOrderPronouncedPrinter;

    @Override
    public void sendToApplicant1Offline(final CaseData caseData, final Long caseId) {
        if (YES.equals(caseData.getApplicant1().getCoPronouncedCoverLetterRegenerated())) {
            log.info("Resending conditional order letter to applicant 1 for case: {}", caseId);
            conditionalOrderPronouncedPrinter.sendLetter(
                caseData,
                caseId,
                CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1,
                caseData.getApplicant1()
            );
        }
    }

    @Override
    public void sendToApplicant2Offline(final CaseData caseData, final Long caseId) {
        if (YES.equals(caseData.getApplicant2().getCoPronouncedCoverLetterRegenerated())) {
            log.info("Resending conditional order letter to applicant 2 for case: {}", caseId);
            conditionalOrderPronouncedPrinter.sendLetter(
                caseData,
                caseId,
                CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2,
                caseData.getApplicant2()
            );
        }
    }
}
