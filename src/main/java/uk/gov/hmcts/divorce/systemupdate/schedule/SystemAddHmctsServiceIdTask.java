package uk.gov.hmcts.divorce.systemupdate.schedule;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.caseworker.service.CaseFlagsService;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.service.CcdConflictException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;

import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.existsQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Applicant2Approved;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Archived;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingApplicant1Response;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingApplicant2Response;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Draft;
import static uk.gov.hmcts.divorce.divorcecase.model.State.FinalOrderComplete;
import static uk.gov.hmcts.divorce.divorcecase.model.State.NewPaperCase;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Rejected;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Withdrawn;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.STATE;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.SUPPLEMENTARY_DATA;

@Component
@Slf4j
public class SystemAddHmctsServiceIdTask implements Runnable {

    @Autowired
    private CaseFlagsService caseFlagsService;

    @Autowired
    private CcdSearchService ccdSearchService;

    @Autowired
    private IdamService idamService;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    public static final String SERVICE_ID_FIELD = "HMCTSServiceId";

    @Override
    public void run() {
        log.info("Adding HMCTSServiceId on existing cases scheduled task started");

        final User user = idamService.retrieveSystemUpdateUserDetails();
        final String serviceAuth = authTokenGenerator.generate();

        try {
            final BoolQueryBuilder query =
                boolQuery()
                    .mustNot(matchQuery(STATE, Draft))
                    .mustNot(matchQuery(STATE, AwaitingApplicant1Response))
                    .mustNot(matchQuery(STATE, AwaitingApplicant2Response))
                    .mustNot(matchQuery(STATE, Applicant2Approved))
                    .mustNot(matchQuery(STATE, AwaitingPayment))
                    .mustNot(matchQuery(STATE, Withdrawn))
                    .mustNot(matchQuery(STATE, Archived))
                    .mustNot(matchQuery(STATE, Rejected))
                    .mustNot(matchQuery(STATE, NewPaperCase))
                    .mustNot(matchQuery(STATE, FinalOrderComplete))
                    .mustNot(existsQuery(String.format(SUPPLEMENTARY_DATA, SERVICE_ID_FIELD)));

            List<CaseDetails> result = ccdSearchService.searchForAllCasesWithQuery(query, user, serviceAuth,
                Draft, AwaitingApplicant1Response, AwaitingApplicant2Response, Applicant2Approved,
                AwaitingPayment, Withdrawn, Archived, Rejected, NewPaperCase, FinalOrderComplete);

            emptyIfNull(result)
                .stream()
                .forEach(caseDetails -> caseFlagsService.setSupplementaryDataForCaseFlags(caseDetails.getId()));

            log.info("Adding HMCTSServiceId on existing cases scheduled task completed.");
        } catch (final CcdSearchCaseException e) {
            log.error("SystemAddHMCTSServiceIdTask schedule task stopped after search error", e);
        } catch (final CcdConflictException e) {
            log.info("SystemAddHMCTSServiceIdTask schedule task stopping "
                + "due to conflict with another running task"
            );
        }
    }
}
