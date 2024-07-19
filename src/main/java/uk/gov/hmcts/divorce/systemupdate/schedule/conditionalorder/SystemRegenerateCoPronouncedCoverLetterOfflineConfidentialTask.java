package uk.gov.hmcts.divorce.systemupdate.schedule.conditionalorder;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.service.CcdConflictException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingFinalOrder;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderPronounced;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemRedoPronouncedCoverLetter.SYSTEM_REDO_PRONOUNCED_COVER_LETTER;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.DATA;

@Component
@Slf4j
/**
 * SystemRegenerateCoPronouncedCoverLetterOfflineConfidentialTask introduced as a one off fix for NFDIV-4142.
 * Any cases which were co granted between the 18th June and 4th July, where at least one applicant is offline and confidential, would have
 * had a blank address on their co pronounced cover letter due to a defect.
 * 'Conditional order pronounced cover letters' should be regenerated with address on the top and sent to the respective applicant.
 */
public class SystemRegenerateCoPronouncedCoverLetterOfflineConfidentialTask implements Runnable {

    @Autowired
    private CcdUpdateService ccdUpdateService;

    @Autowired
    private CcdSearchService ccdSearchService;

    @Autowired
    private IdamService idamService;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    public static final String NOTIFICATION_FLAG = "coPronouncedForceConfidentialCoverLetterResentAgain";

    @Override
    public void run() {
        log.info("SystemRedoPronouncedCoverLettersTask started");
        try {
            final User user = idamService.retrieveSystemUpdateUserDetails();
            final String serviceAuth = authTokenGenerator.generate();

            List<Long> caseIds = loadCaseIds();
            final BoolQueryBuilder query =
                boolQuery()
                    .filter(QueryBuilders.termsQuery("reference", caseIds))
                    .mustNot(matchQuery(String.format(DATA, NOTIFICATION_FLAG), YES));

            final List<CaseDetails> casesToBeUpdated =
                ccdSearchService.searchForAllCasesWithQuery(query, user, serviceAuth, ConditionalOrderPronounced, AwaitingFinalOrder);
            int maxCasesToUpdate = Math.min(casesToBeUpdated.size(), 50); // Limit to 50 cases or the size of casesToBeUpdated
            for (int i = 0; i < maxCasesToUpdate; i++) {
                CaseDetails caseDetails = casesToBeUpdated.get(i);
                triggerRedoCoPronouncedCoverLetterForEligibleCases(user, serviceAuth, caseDetails);
            }

            log.info("SystemRegenerateCoPronouncedCoverLetterOfflineConfidentialTask completed.");
        } catch (final CcdSearchCaseException e) {
            logError("SystemRegenerateCoPronouncedCoverLetterOfflineConfidentialTask stopped after search error", null, e);
        } catch (final CcdConflictException e) {
            logError(
                "SystemRegenerateCoPronouncedCoverLetterOfflineConfidentialTask stopping due to conflict with another running task",
                null,
                e
            );
        } catch (IOException e) {
            logError("SystemRegenerateCoPronouncedCoverLetterOfflineConfidentialTask stopped after file read error", null, e);
        }
    }

    private void triggerRedoCoPronouncedCoverLetterForEligibleCases(User user, String serviceAuth, CaseDetails caseDetails) {
        try {
            log.info("Submitting Regenerate CO Pronounced letter for Case {}", caseDetails.getId());
            ccdUpdateService.submitEvent(caseDetails.getId(), SYSTEM_REDO_PRONOUNCED_COVER_LETTER, user, serviceAuth);
        } catch (final CcdManagementException e) {
            logError("Submit event failed for case id: {}, continuing to next case", caseDetails.getId(), e);
        } catch (final IllegalArgumentException e) {
            logError("Deserialization failed for case id: {}, continuing to next case", caseDetails.getId(), e);
        }
    }

    public List<Long> loadCaseIds() throws IOException {
        ClassPathResource resource = new ClassPathResource("relevant_ids_changes.txt");
        String content = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        StringTokenizer tokenizer = new StringTokenizer(content, ",");
        List<Long> idList = new ArrayList<>();

        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken().trim(); // Trim whitespace from each token
            idList.add(Long.valueOf(token));
        }

        return idList;
    }

    public void logError(String message, Long arg, Exception e) {
        log.error(message, arg, e);
    }
}
