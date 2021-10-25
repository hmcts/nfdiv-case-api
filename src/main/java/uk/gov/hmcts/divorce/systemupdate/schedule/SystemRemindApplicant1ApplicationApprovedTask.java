package uk.gov.hmcts.divorce.systemupdate.schedule;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdConflictException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.User;

import java.time.LocalDate;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Applicant2Approved;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemRemindApplicant1ApplicationReviewed.SYSTEM_REMIND_APPLICANT_1_APPLICATION_REVIEWED;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.DATA;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.DUE_DATE;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.STATE;

@Component
@Slf4j
public class SystemRemindApplicant1ApplicationApprovedTask implements Runnable {

    private static final String NOTIFICATION_FLAG = "applicant1ReminderSent";

    @Autowired
    private CcdSearchService ccdSearchService;

    @Autowired
    private CcdUpdateService ccdUpdateService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IdamService idamService;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Override
    public void run() {
        log.info("Remind applicant 1 application approved scheduled task started");

        final User user = idamService.retrieveSystemUpdateUserDetails();
        final String serviceAuthorization = authTokenGenerator.generate();

        try {
            final BoolQueryBuilder query =
                boolQuery()
                    .must(matchQuery(STATE, Applicant2Approved))
                    .filter(rangeQuery(DUE_DATE).lte(LocalDate.now()))
                    .mustNot(matchQuery(String.format(DATA, NOTIFICATION_FLAG), YesOrNo.YES));

            final List<CaseDetails> casesInAwaitingApplicant1Response =
                ccdSearchService.searchForAllCasesWithQuery(Applicant2Approved, query, user, serviceAuthorization);

            for (final CaseDetails caseDetails : casesInAwaitingApplicant1Response) {
                try {
                    final CaseData caseData = objectMapper.convertValue(caseDetails.getData(), CaseData.class);
                    LocalDate reminderDate = caseData.getDueDate().minusWeeks(1);
                    log.info("Reminder Date for case id: {} is {}", caseDetails.getId(), reminderDate);

                    if (!reminderDate.isAfter(LocalDate.now())
                        && !caseData.getApplication().isApplicant1ReminderSent()
                    ) {
                        notifyApplicant1(caseDetails, caseData, reminderDate, user, serviceAuthorization);
                    } else {
                        log.info("Case id {} not eligible for SystemRemindApplicant1ApplicationApprovedTask as Reminder Date is: {}",
                            caseDetails.getId(), reminderDate);
                    }
                } catch (final CcdManagementException e) {
                    log.error("Submit event failed for case id: {}, continuing to next case", caseDetails.getId());
                } catch (final IllegalArgumentException e) {
                    log.error("Deserialization failed for case id: {}, continuing to next case", caseDetails.getId());
                }
            }

            log.info("Remind applicant 1 application approved scheduled task complete.");
        } catch (final CcdSearchCaseException e) {
            log.error("Remind applicant 1 application approved schedule task stopped after search error", e);
        } catch (final CcdConflictException e) {
            log.info("Remind applicant 1 application approved scheduled task stopping"
                + " due to conflict with another running Remind applicant 1 application approved task"
            );
        }
    }

    private void notifyApplicant1(CaseDetails caseDetails, CaseData caseData, LocalDate reminderDate, User user, String serviceAuth) {
        log.info("Reminder date {} for Case id {} is on/before current date - sending reminder to Applicant 1",
            reminderDate,
            caseDetails.getId()
        );

        ccdUpdateService.submitEvent(caseDetails, SYSTEM_REMIND_APPLICANT_1_APPLICATION_REVIEWED, user, serviceAuth);
    }
}
