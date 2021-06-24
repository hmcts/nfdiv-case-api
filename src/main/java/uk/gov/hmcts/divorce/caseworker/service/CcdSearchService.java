package uk.gov.hmcts.divorce.caseworker.service;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.divorce.common.model.State;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.idam.client.models.User;

import java.util.ArrayList;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.search.sort.SortOrder.ASC;
import static uk.gov.hmcts.divorce.ccd.NoFaultDivorce.CASE_TYPE;

@Service
@Slf4j
public class CcdSearchService {

    public static final int PAGE_SIZE = 100;

    @Autowired
    private CoreCaseDataApi coreCaseDataApi;

    @Autowired
    private IdamService idamService;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    public SearchResult searchForCaseWithStateOf(final State state, final int from, final int size) {

        final User caseWorkerDetails = idamService.retrieveCaseWorkerDetails();

        final SearchSourceBuilder sourceBuilder = SearchSourceBuilder
            .searchSource()
            .sort("data.issueDate", ASC)
            .query(boolQuery().must(matchQuery("state", state)))
            .from(from)
            .size(size);

        return coreCaseDataApi.searchCases(
            caseWorkerDetails.getAuthToken(),
            authTokenGenerator.generate(),
            CASE_TYPE,
            sourceBuilder.toString());
    }

    public List<CaseDetails> searchForAllCasesWithStateOf(final State state) {

        final List<CaseDetails> allCaseDetails = new ArrayList<>();
        int from = 0;
        int totalResults = PAGE_SIZE;

        try {
            while (totalResults == PAGE_SIZE) {
                final SearchResult searchResult = searchForCaseWithStateOf(state, from, PAGE_SIZE);

                allCaseDetails.addAll(searchResult.getCases());

                from += PAGE_SIZE;
                totalResults = searchResult.getTotal();
            }
        } catch (final FeignException e) {

            final String message = String.format("Failed to complete search for Cases with state of %s", state);
            log.info(message, e);
            throw new CcdSearchCaseException(message, e);
        }

        return allCaseDetails;
    }
}
