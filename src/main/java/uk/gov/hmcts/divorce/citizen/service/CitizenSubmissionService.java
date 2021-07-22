package uk.gov.hmcts.divorce.citizen.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.divorce.citizen.notification.ApplicationOutstandingActionNotification;
import uk.gov.hmcts.divorce.citizen.notification.ApplicationSubmittedNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static uk.gov.hmcts.divorce.divorcecase.model.Application.SUBMISSION_RESPONSE_DAYS;

@Service
@Slf4j
public class CitizenSubmissionService {

    @Autowired
    private Clock clock;

    @Autowired
    private ApplicationOutstandingActionNotification outstandingActionNotification;

    @Autowired
    private ApplicationSubmittedNotification notification;

    public CaseData submit(final CaseData caseData, final Long caseId) {
        final CaseData data = caseData.toBuilder().build();
        data.getApplication().setDateSubmitted(LocalDateTime.now(clock));
        data.setDueDate(LocalDate.now(clock).plusDays(SUBMISSION_RESPONSE_DAYS));

        if (data.getApplication().hasAwaitingDocuments()) {
            outstandingActionNotification.send(data, caseId);
        }

        notification.send(data, caseId);

        log.info("Case {} submitted", caseId);

        return data;
    }
}
