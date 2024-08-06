package uk.gov.hmcts.divorce.systemupdate.schedule;

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
import static uk.gov.hmcts.divorce.systemupdate.event.SystemRegenerateJsCitizenAosResponseCoverLetter.SYSTEM_REGEN_JS_CITIZEN_AOS_RESPONSE_COVER_LETTER;

@Component
@RequiredArgsConstructor
@Slf4j
/**
 * SystemRegenerateJsCitizenAosResponseLetterTask introduced as a one off fix for NFDIV-4237.
 * Any js cases sent an AoS pack to unrepresented citizens since Nov 2023, used the solicitor template for the cover letter which resulted
 * in a blank address line.
 * 'JS AoS Response pack cover letters' should be regenerated with address on the top and sent to the respective applicant.
 */
public class SystemRegenerateJsCitizenAosResponseLetterTask implements Runnable {

    private final CcdUpdateService ccdUpdateService;

    private final CcdSearchService ccdSearchService;

    private final IdamService idamService;

    private final AuthTokenGenerator authTokenGenerator;

    private final TaskHelper taskHelper;

    @Override
    public void run() {
        log.info("SystemRegenerateJsCitizenAosResponseCoverLetterTask started");
        try {
            final User user = idamService.retrieveSystemUpdateUserDetails();
            final String serviceAuth = authTokenGenerator.generate();

            List<Long> caseIds = taskHelper.loadCaseIds("NFDIV-4237.csv");
            final BoolQueryBuilder query = boolQuery()
                .filter(QueryBuilders.termsQuery("reference", caseIds));

            final List<CaseDetails> caseList =
                ccdSearchService.searchForAllCasesWithQuery(query, user, serviceAuth);
            for (int i = 0; i < caseList.size(); i++) {
                CaseDetails caseDetails = caseList.get(i);
                triggerRegenJsCitizenAosResponseCoverLetterForEligibleCases(user, serviceAuth, caseDetails);
            }

            log.info("SystemRegenerateJsCitizenAosResponseCoverLetterTask completed.");
        } catch (final CcdSearchCaseException e) {
            taskHelper.logError(
                "SystemRegenerateJsCitizenAosResponseCoverLetterTask stopped after search error",
                null,
                e
            );
        } catch (final CcdConflictException e) {
            taskHelper.logError(
                "SystemRegenerateJsCitizenAosResponseCoverLetterTask stopping due to conflict with another running task",
                null,
                e
            );
        } catch (IOException e) {
            taskHelper.logError("SystemRegenerateJsCitizenAosResponseCoverLetterTask stopped after file read error", null, e);
        }
    }

    private void triggerRegenJsCitizenAosResponseCoverLetterForEligibleCases(User user, String serviceAuth, CaseDetails caseDetails) {
        try {
            log.info("Submitting Regenerate JS Citizen AoS Response letter for Case {}", caseDetails.getId());
            ccdUpdateService.submitEvent(caseDetails.getId(),
                SYSTEM_REGEN_JS_CITIZEN_AOS_RESPONSE_COVER_LETTER, user, serviceAuth);
        } catch (final CcdManagementException e) {
            taskHelper.logError("Submit event failed for case id: {}, continuing to next case", caseDetails.getId(), e);
        } catch (final IllegalArgumentException e) {
            taskHelper.logError("Deserialization failed for case id: {}, continuing to next case", caseDetails.getId(), e);
        }
    }
}
