package uk.gov.hmcts.divorce.report.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.divorce.notification.NotificationService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;

@Service
@RequiredArgsConstructor
@Slf4j
public class DailyReportService {
    private final NotificationService notificationService;
    private final CcdSearchService ccdSearchService;

    public void runReport() {
        //for each state in list Submitted need to search for cases currently in this state and then group by last modified date
        // or last state modified date (awaiting confirmation)
        // then do a count
        //Awaiting Help with Fee Decision
        //Offline Document Received by CW
        //New Paper Case
        //Final Order Requested
        //General Application Received
        //General Consideration Complete
    }
}
