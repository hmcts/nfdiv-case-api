package uk.gov.hmcts.divorce.systemupdate.schedule.conditionalorder;

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
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingConditionalOrder;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemApplicant1ApplyForConditionalOrder.SYSTEM_NOTIFY_APPLICANT1_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.DATA;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.DUE_DATE;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.STATE;

@Component
@Slf4j
public class SystemNotifyApplicant1ApplyForConditionalOrder implements Runnable {

    private static final int TWENTY_WEEKS = 20;
    private static final String NOTIFICATION_FLAG = "applicant1NotifiedCanApplyForConditionalOrder";

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
        log.info("Notify Applicant 1 that they can apply for a Conditional Order");

        final User user = idamService.retrieveSystemUpdateUserDetails();
        final String serviceAuthorization = authTokenGenerator.generate();

        try {
            final BoolQueryBuilder query =
                boolQuery()
                    .must(matchQuery(STATE, AwaitingConditionalOrder))
                    .filter(rangeQuery(DUE_DATE).lte(LocalDate.now()))
                    .mustNot(matchQuery(String.format(DATA, NOTIFICATION_FLAG), YesOrNo.YES));

            final List<CaseDetails> casesInAwaitingApplicant2Response =
                ccdSearchService.searchForAllCasesWithQuery(AwaitingConditionalOrder, query, user, serviceAuthorization);

            for (CaseDetails caseDetails : casesInAwaitingApplicant2Response) {
                try {
                    final CaseData caseData = objectMapper.convertValue(caseDetails.getData(), CaseData.class);
                    final LocalDate canApplyForConditionalOrderFrom = caseData.getDueDate().plusWeeks(TWENTY_WEEKS);

                    if (!canApplyForConditionalOrderFrom.isAfter(LocalDate.now())
                        && !caseData.getApplication().hasApplicant1BeenNotifiedCanApplyForConditionalOrder()
                    ) {
                        notifyApplicant1(caseDetails, caseData, user, serviceAuthorization);
                    }
                } catch (final CcdManagementException e) {
                    log.error("Submit event failed for case id: {}, continuing to next case", caseDetails.getId());
                } catch (final IllegalArgumentException e) {
                    log.error("Deserialization failed for case id: {}, continuing to next case", caseDetails.getId());
                }
            }

            log.info("Applicant 1 Apply For Conditional Order scheduled task complete.");
        } catch (final CcdSearchCaseException e) {
            log.error("Applicant 1 Apply For Conditional Order scheduled task stopped after search error", e);
        } catch (final CcdConflictException e) {
            log.info("Remind applicant 2 scheduled task stopping"
                + " due to conflict with another running Remind applicant 2 task"
            );
        }
    }

    private void notifyApplicant1(CaseDetails caseDetails, CaseData caseData, User user, String serviceAuth) {
        log.info(
            "20 weeks has passed since due date for Case id {} - notifying Applicant 1 that they can apply for a Conditional Order",
            caseDetails.getId());

        ccdUpdateService.submitEvent(caseDetails, SYSTEM_NOTIFY_APPLICANT1_CONDITIONAL_ORDER, user, serviceAuth);
    }
}
