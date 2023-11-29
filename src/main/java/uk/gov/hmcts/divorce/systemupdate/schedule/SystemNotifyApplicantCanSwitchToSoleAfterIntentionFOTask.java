package uk.gov.hmcts.divorce.systemupdate.schedule;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.service.CcdConflictException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.time.LocalDate;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingJointFinalOrder;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemNotifyApplicantCanSwitchToSoleAfterIntentionFO.SYSTEM_APPLICANT_SWITCH_TO_SOLE_AFTER_INTENTION;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.DATA;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.STATE;

@Component
@Slf4j
public class SystemNotifyApplicantCanSwitchToSoleAfterIntentionFOTask implements Runnable {

    public static final String APP_1_NOTIFICATION_SENT_FLAG = "finalOrderApplicant1NotifiedCanSwitchToSoleAfterIntention";
    public static final String APP_2_NOTIFICATION_SENT_FLAG = "finalOrderApplicant2NotifiedCanSwitchToSoleAfterIntention";
    private static final int FOURTEEN_DAYS = 14;
    public static final String APP_1_INTENDED_TO_SWITCH_TO_SOLE = "doesApplicant1IntendToSwitchToSole";
    public static final String APP_2_INTENDED_TO_SWITCH_TO_SOLE = "doesApplicant2IntendToSwitchToSole";

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CcdUpdateService ccdUpdateService;

    @Autowired
    private IdamService idamService;

    @Autowired
    private CcdSearchService ccdSearchService;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;


    @Override
    public void run() {
        log.info("Notify applicant they can switch to sole 14 days after intention final order task started");

        final String serviceAuthorization = authTokenGenerator.generate();
        final User user = idamService.retrieveSystemUpdateUserDetails();

        try {
            final BoolQueryBuilder query = boolQuery()
                .must(matchQuery(STATE, AwaitingJointFinalOrder))
                .must(boolQuery()
                    .should(matchQuery(String.format(DATA, APP_1_INTENDED_TO_SWITCH_TO_SOLE), YES))
                    .should(matchQuery(String.format(DATA, APP_2_INTENDED_TO_SWITCH_TO_SOLE), YES))
                    .minimumShouldMatch(1)
                )
                .mustNot(matchQuery(String.format(DATA, APP_1_NOTIFICATION_SENT_FLAG), YES))
                .mustNot(matchQuery(String.format(DATA, APP_2_NOTIFICATION_SENT_FLAG), YES));

            final List<CaseDetails> casesWithApplicantIntendedSwitchToSole =
                ccdSearchService.searchForAllCasesWithQuery(query, user, serviceAuthorization, AwaitingJointFinalOrder);

            for (final CaseDetails caseDetails : casesWithApplicantIntendedSwitchToSole) {
                try {
                    final CaseData caseData = objectMapper.convertValue(caseDetails.getData(), CaseData.class);

                    if (caseEligibleForNotification(caseData)) {

                        log.info(
                            """
                            14 days has passed since applicant intended to switch to sole for final order for Case id {}
                            - notifying them they can continue and switch to sole.
                            """,
                            caseDetails.getId()
                        );

                        ccdUpdateService.submitEvent(
                            caseDetails.getId(),
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
            log.error("SystemNotifyApplicantCanSwitchToSoleAfterIntentionFO schedule task stopped after search error", e);
        } catch (final CcdConflictException e) {
            log.info("SystemNotifyApplicantCanSwitchToSoleAfterIntentionFO schedule task stopping "
                + "due to conflict with another running task"
            );
        }
    }

    private boolean caseEligibleForNotification(CaseData caseData) {

        // Initialise to date in the future. If intention date is null, this date will stay in
        // the future and not give false positives during the boolean check at bottom of function.
        LocalDate dateFinalOrderApp1IntendsSwitchToSoleNowEligible = LocalDate.now().plusDays(1);
        LocalDate dateFinalOrderApp2IntendsSwitchToSoleNowEligible = LocalDate.now().plusDays(1);

        final LocalDate dateApplicant1DeclaredIntentionToSwitchToSoleFo
            = caseData.getFinalOrder().getDateApplicant1DeclaredIntentionToSwitchToSoleFo();

        final LocalDate dateApplicant2DeclaredIntentionToSwitchToSoleFo
            = caseData.getFinalOrder().getDateApplicant2DeclaredIntentionToSwitchToSoleFo();

        if (dateApplicant1DeclaredIntentionToSwitchToSoleFo != null) {
            dateFinalOrderApp1IntendsSwitchToSoleNowEligible =
                dateApplicant1DeclaredIntentionToSwitchToSoleFo.plusDays(FOURTEEN_DAYS);
        }

        if (dateApplicant2DeclaredIntentionToSwitchToSoleFo != null) {
            dateFinalOrderApp2IntendsSwitchToSoleNowEligible =
                dateApplicant2DeclaredIntentionToSwitchToSoleFo.plusDays(FOURTEEN_DAYS);
        }

        boolean shouldSendToApp1 = dateFinalOrderApp1IntendsSwitchToSoleNowEligible.isBefore(LocalDate.now())
            && !caseData.getFinalOrder().hasApplicant1BeenNotifiedTheyCanContinueSwitchToSoleFO();

        boolean shouldSendToApp2 = dateFinalOrderApp2IntendsSwitchToSoleNowEligible.isBefore(LocalDate.now())
            && !caseData.getFinalOrder().hasApplicant2BeenNotifiedTheyCanContinueSwitchToSoleFO();

        return shouldSendToApp1 || shouldSendToApp2;
    }
}
