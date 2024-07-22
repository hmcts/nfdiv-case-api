package uk.gov.hmcts.divorce.systemupdate.schedule.conditionalorder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
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

import java.io.IOException;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemRegenerateCoPronouncedCoverLetterOfflineConfidential.SYSTEM_REGEN_CO_PRONOUNCED_COVER_LETTER_OFFLINE_CONFIDENTIAL;

@Component
@RequiredArgsConstructor
@Slf4j
/**
 * SystemRegenerateCoPronouncedCoverLetterOfflineConfidentialTask introduced as a one off fix for NFDIV-4142.
 * Any cases which were co granted between the 18th June and 4th July, where at least one applicant is offline and confidential, would have
 * had a blank address on their co pronounced cover letter due to a defect.
 * 'Conditional order pronounced cover letters' should be regenerated with address on the top and sent to the respective applicant.
 */
public class SystemRegenerateCoPronouncedCoverLetterOfflineConfidentialTask implements Runnable {

    private final CcdUpdateService ccdUpdateService;

    private final CcdSearchService ccdSearchService;

    private final IdamService idamService;

    private final AuthTokenGenerator authTokenGenerator;

    private final SystemRedoPronouncedCoverLettersTask oldTask;

    @Override
    public void run() {
        log.info("SystemRegenerateCoPronouncedCoverLetterOfflineConfidentialTask started");
        try {
            final User user = idamService.retrieveSystemUpdateUserDetails();
            final String serviceAuth = authTokenGenerator.generate();

            List<Long> caseIds = oldTask.loadCaseIds();
            final BoolQueryBuilder query = boolQuery()
                .filter(QueryBuilders.termsQuery("reference", caseIds));

            final List<CaseDetails> caseList =
                ccdSearchService.searchForAllCasesWithQuery(query, user, serviceAuth);
            for (int i = 0; i < caseList.size(); i++) {
                CaseDetails caseDetails = caseList.get(i);
                triggerRegenCoPronouncedCoverLetterForEligibleCases(user, serviceAuth, caseDetails);
            }

            log.info("SystemRegenerateCoPronouncedCoverLetterOfflineConfidentialTask completed.");
        } catch (final CcdSearchCaseException e) {
            oldTask.logError(
                "SystemRegenerateCoPronouncedCoverLetterOfflineConfidentialTask stopped after search error",
                null,
                e
            );
        } catch (final CcdConflictException e) {
            oldTask.logError(
                "SystemRegenerateCoPronouncedCoverLetterOfflineConfidentialTask stopping due to conflict with another running task",
                null,
                e
            );
        } catch (IOException e) {
            oldTask.logError("SystemRegenerateCoPronouncedCoverLetterOfflineConfidentialTask stopped after file read error", null, e);
        }
    }

    private void triggerRegenCoPronouncedCoverLetterForEligibleCases(User user, String serviceAuth, CaseDetails caseDetails) {
        try {
            log.info("Submitting Regenerate CO Pronounced letter for Case {}", caseDetails.getId());
            ccdUpdateService.submitEvent(caseDetails.getId(),
                SYSTEM_REGEN_CO_PRONOUNCED_COVER_LETTER_OFFLINE_CONFIDENTIAL, user, serviceAuth);
        } catch (final CcdManagementException e) {
            oldTask.logError("Submit event failed for case id: {}, continuing to next case", caseDetails.getId(), e);
        } catch (final IllegalArgumentException e) {
            oldTask.logError("Deserialization failed for case id: {}, continuing to next case", caseDetails.getId(), e);
        }
    }
}
