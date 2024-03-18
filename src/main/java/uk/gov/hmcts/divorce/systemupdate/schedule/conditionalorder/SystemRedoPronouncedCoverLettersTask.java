package uk.gov.hmcts.divorce.systemupdate.schedule.conditionalorder;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.service.CcdConflictException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerRegenerateCourtOrders.CASEWORKER_REGENERATE_COURT_ORDERS;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingFinalOrder;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderPronounced;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemResendCOPronouncedCoverLetter.SYSTEM_RESEND_CO_PRONOUNCED_COVER_LETTER;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.APPLICANT1_OFFLINE;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.APPLICANT2_OFFLINE;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.DATA;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.STATE;

@Component
@Slf4j
/**
 * SystemRedoPronouncedCoverLettersTask introduced as a one off fix for NFDIV-3935Any cases which are in 'ConditionalOrderPronounced' state
 * and where the applicants are offline where some cases had the details missing off the cover letter to offline applicants due to defect
 * The cases picked for this are in resource file but this task will check the state again before sending as some cases have moved on already
 * 'Conditional order pronounced cover letters' should be regenerated with address on the top and sent to respective applicant.
 */
public class SystemRedoPronouncedCoverLettersTask implements Runnable {

    @Autowired
    private CcdUpdateService ccdUpdateService;

    @Autowired
    private CcdSearchService ccdSearchService;

    @Autowired
    private IdamService idamService;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CaseIdChecker caseIdChecker;

    public static final String NOTIFICATION_FLAG = "coPronouncedCoverLetterResent";

    @Override
    public void run() {
        log.info("SystemRedoPronouncedCoverLettersTask started");

        final User user = idamService.retrieveSystemUpdateUserDetails();
        final String serviceAuth = authTokenGenerator.generate();

        try {
            final BoolQueryBuilder query =
                boolQuery()
                    .must(
                        boolQuery()
                            .should(matchQuery(STATE, AwaitingFinalOrder))
                            .should(matchQuery(STATE, ConditionalOrderPronounced))
                            .minimumShouldMatch(1)
                    )
                    .must(
                        boolQuery()
                            .should(
                                boolQuery()
                                    .must(matchQuery(String.format(DATA, APPLICANT1_OFFLINE), YES))
                            )
                            .should(
                                boolQuery()
                                    .must(matchQuery(String.format(DATA, APPLICANT2_OFFLINE), YES))
                            )
                            .minimumShouldMatch(1)
                    )
                    .mustNot(matchQuery(String.format(DATA, NOTIFICATION_FLAG), YES));

            final List<CaseDetails> casesToBeUpdated =
                ccdSearchService.searchForAllCasesWithQuery(query, user, serviceAuth, ConditionalOrderPronounced, AwaitingFinalOrder);

            for (final CaseDetails caseDetails : casesToBeUpdated) {
                triggerRedoCoPronouncedCoverLetterForEligibleCases(user, serviceAuth, caseDetails);
            }

            log.info("SystemRedoPronouncedCoverLettersTask completed.");
        } catch (final CcdSearchCaseException e) {
            log.error("SystemRedoPronouncedCoverLettersTask stopped after search error", e);
        } catch (final CcdConflictException e) {
            log.info("SystemRedoPronouncedCoverLettersTask stopping "
                + "due to conflict with another running task"
            );
        }
    }

    private void triggerRedoCoPronouncedCoverLetterForEligibleCases(User user, String serviceAuth, CaseDetails caseDetails) {
        try {

            if (isCaseEligibleToResendTheCoverLetters(caseDetails.getId())) {
                log.info("Submitting Redo CO Pronounced letter for Case {}", caseDetails.getId());
                ccdUpdateService.submitEvent(caseDetails.getId(), SYSTEM_RESEND_CO_PRONOUNCED_COVER_LETTER, user, serviceAuth);
            }
        } catch (final CcdManagementException e) {
            log.error("Submit event failed for case id: {}, continuing to next case", caseDetails.getId());
        } catch (final IllegalArgumentException e) {
            log.error("Deserialization failed for case id: {}, continuing to next case", caseDetails.getId());
        }
    }

    private boolean isCaseEligibleToResendTheCoverLetters(final Long id) {
        return caseIdChecker.isCaseIdValid(id);
    }
}
