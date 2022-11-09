package uk.gov.hmcts.divorce.systemupdate.schedule;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
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
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingJointFinalOrder;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemNotifyApplicant1CanSwitchToSoleAfterIntentionFO.SYSTEM_APPLICANT_SWITCH_TO_SOLE_AFTER_INTENTION;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.DATA;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.STATE;

@Component
@Slf4j
public class SystemNotifyApplicant1CanSwitchToSoleAfterIntentionFOTask implements Runnable {

    public static final String NOTIFICATION_SENT_FLAG = "finalOrderApplicantNotifiedCanSwitchToSoleAfterIntention";
    public static final String APP_1_INTENDED_TO_SWITCH_TO_SOLE = "doesApplicant1IntendToSwitchToSole";
    private static final int FOURTEEN_DAYS = 14;

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

    @Override
    public void run() {
        log.info("Notify applicant they can switch to sole 14 days after intention final order task started");

        final User user = idamService.retrieveSystemUpdateUserDetails();
        final String serviceAuthorization = authTokenGenerator.generate();

        try {
            final BoolQueryBuilder query = boolQuery()
                .must(matchQuery(STATE, AwaitingJointFinalOrder))
                .must(matchQuery(String.format(DATA, APP_1_INTENDED_TO_SWITCH_TO_SOLE), YES))
                .mustNot(matchQuery(String.format(DATA, NOTIFICATION_SENT_FLAG), YES));

            final List<CaseDetails> casesWithApplicant1IntendedSwitchToSole =
                ccdSearchService.searchForAllCasesWithQuery(query, user, serviceAuthorization, AwaitingJointFinalOrder);

            for (final CaseDetails caseDetails : casesWithApplicant1IntendedSwitchToSole) {
                try {
                    final CaseData caseData = objectMapper.convertValue(caseDetails.getData(), CaseData.class);
                    final LocalDate dateFinalOrderIntendsSwitchToSoleNowEligible =
                        caseData.getFinalOrder().getDateApplicant1DeclaredIntentionToSwitchToSoleFo().plusDays(FOURTEEN_DAYS);

                    if (dateFinalOrderIntendsSwitchToSoleNowEligible.isBefore(LocalDate.now())
                        && !caseData.getFinalOrder().hasApplicant1BeenNotifiedTheyCanContinueSwitchToSoleFO()) {

                        log.info(
                            """
                            14 days has passed since applicant intended to switch to sole for final order on {} for Case id {}
                            - notifying them they can continue and switch to sole.
                            """,
                            caseData.getFinalOrder().getDateApplicant1DeclaredIntentionToSwitchToSoleFo(),
                            caseDetails.getId()
                        );

                        ccdUpdateService.submitEvent(
                            caseDetails,
                            SYSTEM_APPLICANT_SWITCH_TO_SOLE_AFTER_INTENTION,
                            user,
                            serviceAuthorization);
                    }
                } catch (final CcdManagementException e) {
                    log.error("Submit event failed for case id: {}, continuing to next case", caseDetails.getId());
                } catch (final IllegalArgumentException e) {
                    log.error("Deserialization failed for case id: {}, continuing to next case", caseDetails.getId());
                }
            }

            log.info("Notify applicant they can switch to sole after intention final order task complete.");

        } catch (final CcdSearchCaseException e) {
            log.error("SystemNotifyApplicant1CanSwitchToSoleAfterIntentionFO schedule task stopped after search error", e);
        } catch (final CcdConflictException e) {
            log.info("SystemNotifyApplicant1CanSwitchToSoleAfterIntentionFO schedule task stopping "
                + "due to conflict with another running task"
            );
        }
    }
}
