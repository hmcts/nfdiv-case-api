package uk.gov.hmcts.divorce.systemupdate.schedule.finalorder;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdConflictException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.User;

import java.util.Collection;

import static java.util.stream.Stream.ofNullable;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingJointFinalOrder;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemNotifyJointApplicantCanSwitchToSoleFinalOrder.SYSTEM_NOTIFY_JOINT_APPLICANT_CAN_SWITCH_TO_SOLE_FINAL_ORDER;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.DATA;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.STATE;

@Component
@Slf4j
public class SystemNotifyJointApplicantCanSwitchToSoleFinalOrderTask implements Runnable {

    public static final String NOTIFICATION_FLAG_FO = "jointApplicantNotifiedCanSwitchToSoleFinalOrder";
    public static final String SUBMIT_EVENT_ERROR = "Submit event failed for case id: {}, continuing to next case";
    public static final String DESERIALIZATION_ERROR = "Deserialization failed for case id: {}, continuing to next case";
    public static final String CCD_SEARCH_ERROR =
        "SystemNotifyJointApplicantCanSwitchToSoleFinalOrderTask scheduled task stopped after search error";
    public static final String CCD_CONFLICT_ERROR =
        "SystemNotifyJointApplicantCanSwitchToSoleFinalOrderTask scheduled task stopping due to conflict with another running task";

    @Autowired
    private CcdSearchService ccdSearchService;

    @Autowired
    private CcdUpdateService ccdUpdateService;

    @Autowired
    private IdamService idamService;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${final_order.reminder_offset_days}")
    private int finalOrderReminderOffsetDays;

    @Override
    public void run() {
        log.info("Remind joint applicant that they can switch to sole at final order stage");
        final User user = idamService.retrieveSystemUpdateUserDetails();
        final String serviceAuthorization = authTokenGenerator.generate();
        try {
            final BoolQueryBuilder query =
                boolQuery()
                    .must(
                        boolQuery()
                            .should(matchQuery(STATE, AwaitingJointFinalOrder))
                            .minimumShouldMatch(1)
                    )
                    .mustNot(matchQuery(String.format(DATA, NOTIFICATION_FLAG_FO), YES));

            ofNullable(ccdSearchService.searchForAllCasesWithQuery(query, user, serviceAuthorization, AwaitingJointFinalOrder))
                .flatMap(Collection::stream)
                .filter(this::isJointFinalOrderOverdue)
                .forEach(caseDetails -> remindJointApplicant(caseDetails, user, serviceAuthorization));

            log.info("SystemNotifyJointApplicantCanSwitchToSoleFinalOrderTask scheduled task complete.");
        } catch (final CcdSearchCaseException e) {
            log.error(CCD_SEARCH_ERROR, e);
        } catch (final CcdConflictException e) {
            log.info(CCD_CONFLICT_ERROR);
        }
    }

    private void remindJointApplicant(CaseDetails caseDetails, User user, String serviceAuth) {
        try {
            log.info(
                "Final order application +{}days elapsed for Case({}) - reminding Joint Applicant they can switch to sole",
                finalOrderReminderOffsetDays, caseDetails.getId()
            );
            ccdUpdateService.submitEvent(caseDetails, SYSTEM_NOTIFY_JOINT_APPLICANT_CAN_SWITCH_TO_SOLE_FINAL_ORDER, user, serviceAuth);

        } catch (final CcdManagementException e) {
            log.error(SUBMIT_EVENT_ERROR, caseDetails.getId());
        } catch (final IllegalArgumentException e) {
            log.error(DESERIALIZATION_ERROR, caseDetails.getId());
        }
    }

    private boolean isJointFinalOrderOverdue(final CaseDetails caseDetails) {

        final CaseData caseData = objectMapper.convertValue(caseDetails.getData(), CaseData.class);
        final FinalOrder finalOrder = caseData.getFinalOrder();

        return finalOrder.shouldEnableSwitchToSoleFoForApplicant1() || finalOrder.shouldEnableSwitchToSoleFoForApplicant2();
    }
}
