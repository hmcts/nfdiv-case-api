package uk.gov.hmcts.divorce.systemupdate.schedule.conditionalorder;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
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

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingConditionalOrder;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemJointApplicantsApplyForConditionalOrder.SYSTEM_NOTIFY_APPLICANTS_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.DATA;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.DUE_DATE;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.STATE;

@Component
@Slf4j
public class SystemNotifyApplicantsApplyForConditionalOrderTask implements Runnable {

    public static final String NOTIFICATION_FLAG = "jointApplicantsNotifiedCanApplyForConditionalOrder";
    public static final String SUBMIT_EVENT_ERROR = "Submit event failed for case id: {}, continuing to next case";
    public static final String DESERIALIZATION_ERROR = "Deserialization failed for case id: {}, continuing to next case";
    public static final String CCD_SEARCH_ERROR =
        "SystemNotifyApplicantsApplyForConditionalOrderTask scheduled task stopped after search error";
    public static final String CCD_CONFLICT_ERROR =
        "SystemNotifyApplicantsApplyForConditionalOrderTask scheduled task stopping due to conflict with another running task";

    @Autowired
    private CcdSearchService ccdSearchService;

    @Autowired
    private CcdUpdateService ccdUpdateService;

    @Autowired
    private IdamService idamService;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Override
    public void run() {
        log.info("Notify Joint Applicants that they can apply for a Conditional Order");
        final User user = idamService.retrieveSystemUpdateUserDetails();
        final String serviceAuthorization = authTokenGenerator.generate();
        try {
            final BoolQueryBuilder query =
                boolQuery()
                    .must(matchQuery(STATE, AwaitingConditionalOrder))
                    .filter(rangeQuery(DUE_DATE).lte(LocalDate.now()))
                    .mustNot(matchQuery(String.format(DATA, NOTIFICATION_FLAG), YesOrNo.YES));

            ccdSearchService.searchForAllCasesWithQuery(AwaitingConditionalOrder, query, user, serviceAuthorization)
                    .forEach(caseDetails -> notifyJointApplicants(caseDetails, user, serviceAuthorization));

            log.info("SystemNotifyApplicantsApplyForConditionalOrderTask scheduled task complete.");
        } catch (final CcdSearchCaseException e) {
            log.error(CCD_SEARCH_ERROR, e);
        } catch (final CcdConflictException e) {
            log.info(CCD_CONFLICT_ERROR);
        }
    }

    private void notifyJointApplicants(CaseDetails caseDetails, User user, String serviceAuth) {
        try {
            log.info(
                "20Week holding period elapsed for Case({}) - notifying Joint Applicants they can apply for a Conditional Order",
                caseDetails.getId()
            );
            ccdUpdateService.submitEvent(caseDetails, SYSTEM_NOTIFY_APPLICANTS_CONDITIONAL_ORDER, user, serviceAuth);

        } catch (final CcdManagementException e) {
            log.error(SUBMIT_EVENT_ERROR, caseDetails.getId());
        } catch (final IllegalArgumentException e) {
            log.error(DESERIALIZATION_ERROR, caseDetails.getId());
        }
    }
}
