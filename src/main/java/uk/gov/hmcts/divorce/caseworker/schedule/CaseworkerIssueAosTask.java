package uk.gov.hmcts.divorce.caseworker.schedule;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.caseworker.service.CcdManagementException;
import uk.gov.hmcts.divorce.caseworker.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.caseworker.service.CcdSearchService;
import uk.gov.hmcts.divorce.caseworker.service.CcdUpdateService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;

import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerIssueAos.CASEWORKER_ISSUE_AOS;
import static uk.gov.hmcts.divorce.common.model.State.Issued;

@Component
@Slf4j
public class CaseworkerIssueAosTask {

    @Autowired
    private CcdUpdateService ccdUpdateService;

    @Autowired
    private CcdSearchService ccdSearchService;

    @Scheduled(cron = "${schedule.issue_aos}")
    public void issueAosTask() {

        log.info("Issue AOS Scheduled Task started.");

        try {
            final List<CaseDetails> issuedCases = ccdSearchService.searchForAllCasesWithStateOf(Issued);
            issuedCases.forEach(caseDetails -> {
                try {
                    ccdUpdateService.submitEvent(caseDetails, CASEWORKER_ISSUE_AOS);
                } catch (final CcdManagementException e) {
                    log.info("Submit event failed for Case ID: {}, continuing to next Case", caseDetails.getId());
                }
            });
            log.info("Issue AOS Scheduled Task complete.");
        } catch (final CcdSearchCaseException e) {
            log.info("Issue AOS Schedule Task stopped after search error", e);
        }
    }
}
