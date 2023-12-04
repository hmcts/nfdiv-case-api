package uk.gov.hmcts.divorce.systemupdate.schedule.conditionalorder;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments;
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
import static uk.gov.hmcts.divorce.divorcecase.model.ContactDetailsType.PRIVATE;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderPronounced;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemResendCOPronouncedCoverLetter.SYSTEM_RESEND_CO_PRONOUNCED_COVER_LETTER;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.APPLICANT1_OFFLINE;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.APPLICANT1_PRIVATE_CONTACT;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.APPLICANT2_OFFLINE;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.APPLICANT2_PRIVATE_CONTACT;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.DATA;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.STATE;

@Component
@Slf4j
/**
 * Any cases which are in 'ConditionalOrderPronounced' state and where the applicants are offline and have contact details private
 * 'Conditional order pronounced cover letters' should be regenerated with address on the top and sent to respective applicant.
 */
public class SystemResendCOPronouncedCoverLettersTask implements Runnable {

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

    public static final String NOTIFICATION_FLAG = "coPronouncedCoverLetterResent";

    @Override
    public void run() {
        log.info("SystemResendCOPronouncedCoverLettersTask started");

        final User user = idamService.retrieveSystemUpdateUserDetails();
        final String serviceAuth = authTokenGenerator.generate();

        try {
            final BoolQueryBuilder query =
                boolQuery()
                    .must(matchQuery(STATE, ConditionalOrderPronounced))
                    .must(
                        boolQuery()
                            .should(
                                boolQuery()
                                    .must(matchQuery(String.format(DATA, APPLICANT1_OFFLINE), YES))
                                    .must(matchQuery(String.format(DATA, APPLICANT1_PRIVATE_CONTACT), PRIVATE.getType()))
                            )
                            .should(
                                boolQuery()
                                    .must(matchQuery(String.format(DATA, APPLICANT2_OFFLINE), YES))
                                    .must(matchQuery(String.format(DATA, APPLICANT2_PRIVATE_CONTACT), PRIVATE.getType()))
                            )
                            .minimumShouldMatch(1)
                    )
                    .mustNot(matchQuery(String.format(DATA, NOTIFICATION_FLAG), YES));

            final List<CaseDetails> casesToBeUpdated =
                ccdSearchService.searchForAllCasesWithQuery(query, user, serviceAuth, ConditionalOrderPronounced);

            for (final CaseDetails caseDetails : casesToBeUpdated) {
                triggerResendCoPronouncedCoverLetterForEligibleCases(user, serviceAuth, caseDetails);
            }

            log.info("SystemResendCOPronouncedCoverLettersTask completed.");
        } catch (final CcdSearchCaseException e) {
            log.error("SystemResendCOPronouncedCoverLettersTask stopped after search error", e);
        } catch (final CcdConflictException e) {
            log.info("SystemResendCOPronouncedCoverLettersTask stopping "
                + "due to conflict with another running task"
            );
        }
    }

    private void triggerResendCoPronouncedCoverLetterForEligibleCases(User user, String serviceAuth, CaseDetails caseDetails) {
        try {

            final CaseData caseData = objectMapper.convertValue(caseDetails.getData(), CaseData.class);

            if (isCaseEligibleToResendTheCoverLetters(caseData)) {
                log.info("Submitting Resend CO Pronounced letter for Case {}", caseDetails.getId());
                ccdUpdateService.submitEvent(caseDetails.getId(), SYSTEM_RESEND_CO_PRONOUNCED_COVER_LETTER, user, serviceAuth);
            }
        } catch (final CcdManagementException e) {
            log.error("Submit event failed for case id: {}, continuing to next case", caseDetails.getId());
        } catch (final IllegalArgumentException e) {
            log.error("Deserialization failed for case id: {}, continuing to next case", caseDetails.getId());
        }
    }

    private boolean isCaseEligibleToResendTheCoverLetters(final CaseData caseData) {
        CaseDocuments caseDocuments = caseData.getDocuments();

        if (caseData.getApplicant1().isApplicantOffline()
            && caseData.getApplicant1().isConfidentialContactDetails()
            && !caseDocuments.isGivenDocumentUnderConfidentialList(CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1)) {
            return true;
        }

        return caseData.getApplicant2().isApplicantOffline()
            && caseData.getApplicant2().isConfidentialContactDetails()
            && !caseDocuments.isGivenDocumentUnderConfidentialList(CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2);
    }
}
