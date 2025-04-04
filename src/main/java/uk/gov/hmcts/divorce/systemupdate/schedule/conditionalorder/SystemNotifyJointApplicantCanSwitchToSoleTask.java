package uk.gov.hmcts.divorce.systemupdate.schedule.conditionalorder;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.schedule.AbstractTaskEventSubmit;
import uk.gov.hmcts.divorce.systemupdate.service.CcdConflictException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Collection;

import static java.util.stream.Stream.ofNullable;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderPending;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemNotifyJointApplicantCanSwitchToSole.SYSTEM_NOTIFY_JOINT_APPLICANT_CAN_SWITCH_TO_SOLE;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.DATA;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.STATE;

@Component
@Slf4j
public class SystemNotifyJointApplicantCanSwitchToSoleTask extends AbstractTaskEventSubmit {

    public static final String NOTIFICATION_FLAG = "jointApplicantNotifiedCanSwitchToSole";
    public static final String CCD_SEARCH_ERROR =
        "SystemNotifyJointApplicantCanSwitchToSoleTask scheduled task stopped after search error";
    public static final String CCD_CONFLICT_ERROR =
        "SystemNotifyJointApplicantCanSwitchToSoleTask scheduled task stopping due to conflict with another running task";

    @Autowired
    private CcdSearchService ccdSearchService;

    @Autowired
    private IdamService idamService;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${submit_co.reminder_offset_days}")
    private int submitCOrderReminderOffsetDays;

    @Override
    public void run() {
        log.info("Remind joint applicant that they can switch to sole");
        final User user = idamService.retrieveSystemUpdateUserDetails();
        final String serviceAuthorization = authTokenGenerator.generate();
        try {
            final BoolQueryBuilder query =
                boolQuery()
                    .must(
                        boolQuery()
                            .should(matchQuery(STATE, ConditionalOrderPending))
                            .minimumShouldMatch(1)
                    )
                    .mustNot(matchQuery(String.format(DATA, NOTIFICATION_FLAG), YES));

            ofNullable(ccdSearchService.searchForAllCasesWithQuery(query, user, serviceAuthorization, ConditionalOrderPending))
                .flatMap(Collection::stream)
                .filter(this::isJointConditionalOrderOverdue)
                .forEach(caseDetails -> remindJointApplicant(caseDetails.getId(), user, serviceAuthorization));

            log.info("SystemNotifyJointApplicantCanSwitchToSoleTask scheduled task complete.");
        } catch (final CcdSearchCaseException e) {
            log.error(CCD_SEARCH_ERROR, e);
        } catch (final CcdConflictException e) {
            log.info(CCD_CONFLICT_ERROR);
        }
    }

    private void remindJointApplicant(Long caseId, User user, String serviceAuth) {
        log.info(
            "Conditional order application +{}days elapsed for Case({}) - reminding Joint Applicant they can switch to sole",
            submitCOrderReminderOffsetDays, caseId
        );
        submitEvent(caseId, SYSTEM_NOTIFY_JOINT_APPLICANT_CAN_SWITCH_TO_SOLE, user, serviceAuth);
    }

    private boolean isJointConditionalOrderOverdue(final CaseDetails caseDetails) {

        final CaseData caseData = objectMapper.convertValue(caseDetails.getData(), CaseData.class);
        final ConditionalOrder conditionalOrder = caseData.getConditionalOrder();

        return conditionalOrder.shouldEnableSwitchToSoleCoForApplicant1()
            || conditionalOrder.shouldEnableSwitchToSoleCoForApplicant2();

    }
}
