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
import java.time.LocalDateTime;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.existsQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingJointFinalOrder;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemNotifyApplicantPartnerNotAppliedForFinalOrder.SYSTEM_PARTNER_NOT_APPLIED_FOR_FINAL_ORDER;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.DATA;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.FINAL_ORDER_SUBMITTED_DATE;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.STATE;

@Component
@Slf4j
public class SystemNotifyApplicantThatPartnerNotAppliedForFOTask implements Runnable {

    public static final String NOTIFICATION_SENT_FLAG = "finalOrderFirstInTimeNotifiedOtherApplicantNotApplied";
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
        log.info("Notify First In Time Applicant that other applicant not applied for Final Order task started");

        final User user = idamService.retrieveSystemUpdateUserDetails();
        final String serviceAuthorization = authTokenGenerator.generate();

        try {
            final BoolQueryBuilder query = boolQuery()
                .must(matchQuery(STATE, AwaitingJointFinalOrder))
                .must(existsQuery(FINAL_ORDER_SUBMITTED_DATE))
                .mustNot(matchQuery(String.format(DATA, NOTIFICATION_SENT_FLAG), YES));

            final List<CaseDetails> casesInAwaitingJointFinalOrderPartnerNotApplied =
                ccdSearchService.searchForAllCasesWithQuery(query, user, serviceAuthorization, AwaitingJointFinalOrder);

            for (final CaseDetails caseDetails : casesInAwaitingJointFinalOrderPartnerNotApplied) {
                try {
                    final CaseData caseData = objectMapper.convertValue(caseDetails.getData(), CaseData.class);
                    final LocalDate dateFinalOrderSubmittedPlusFourteenDays =
                        caseData.getFinalOrder().getDateFinalOrderSubmitted().plusDays(FOURTEEN_DAYS).toLocalDate();

                    if (dateFinalOrderSubmittedPlusFourteenDays.isBefore(LocalDate.now())
                        && !caseData.getFinalOrder().hasFirstInTimeApplicantBeenNotifiedOtherApplicantHasNotApplied()
                    ) {
                        notifyFirstInTimeApplicant(
                            caseDetails,
                            caseData.getFinalOrder().getDateFinalOrderSubmitted(),
                            user,
                            serviceAuthorization
                        );
                    }
                } catch (final CcdManagementException e) {
                    log.error("Submit event failed for case id: {}, continuing to next case", caseDetails.getId());
                } catch (final IllegalArgumentException e) {
                    log.error("Deserialization failed for case id: {}, continuing to next case", caseDetails.getId());
                }
            }

            log.info("Notify First In Time Applicant that other applicant not applied for Final Order task complete.");

        } catch (final CcdSearchCaseException e) {
            log.error("SystemNotifyWhenSecondInTimeNotAppliedForFO schedule task stopped after search error", e);
        } catch (final CcdConflictException e) {
            log.info("SystemNotifyWhenSecondInTimeNotAppliedForFO schedule task stopping "
                + "due to conflict with another running task"
            );
        }
    }

    private void notifyFirstInTimeApplicant(CaseDetails caseDetails,
                                            LocalDateTime dateFinalOrderSubmitted,
                                            User user,
                                            String serviceAuth) {

        log.info(
            """
            14 days has passed since first in time applicant submitted final order {} for Case id {}
            and the other applicant has not applied for final order - notifying first in time applicant
            """,
            dateFinalOrderSubmitted,
            caseDetails.getId()
        );

        ccdUpdateService.submitEvent(caseDetails.getId(), SYSTEM_PARTNER_NOT_APPLIED_FOR_FINAL_ORDER, user, serviceAuth);
    }
}
