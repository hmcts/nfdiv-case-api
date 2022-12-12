package uk.gov.hmcts.divorce.systemupdate.schedule.migration;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionCaseTypeConfig;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.idam.client.models.User;

import java.util.List;
import java.util.Objects;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.springframework.util.CollectionUtils.isEmpty;

@Component
@Slf4j
public class CaseCourtHearingMigration implements Migration {

    @Value("${MIGRATE_CASE_COURT_HEARING:false}")
    private boolean migrateCaseCourtHearing;

    @Value("#{'${CASE_COURT_HEARING_REFERENCES:}'.split(',')}")
    private List<Long> caseCourtHearingReferences;

    @Autowired
    private CoreCaseDataApi coreCaseDataApi;

    @Autowired
    private SetCaseCourtHearingBulkAction setCaseCourtHearingBulkAction;

    @Override
    public void apply(final User user, final String serviceAuthorization) {

        final List<Long> allReferences = caseCourtHearingReferences.stream()
            .filter(Objects::nonNull)
            .toList();

        if (migrateCaseCourtHearing && !isEmpty(allReferences)) {
            log.info("Started CaseCourtHearingMigration with references: {}", caseCourtHearingReferences);
            try {
                final List<CaseDetails> caseDetails = searchForCaseCourtHearingCases(allReferences, user, serviceAuthorization);

                log.info("CaseCourtHearingMigration Number of bulk cases {}", caseDetails.size());

                caseDetails
                    .parallelStream()
                    .forEach(caseDetail -> setCaseCourtHearingBulkAction.setCaseCourtHearing(caseDetail, user, serviceAuthorization));

            } catch (final CcdSearchCaseException e) {
                log.error("Case schedule task(CaseCourtHearingMigration) stopped after search error", e);
            }
            log.info("Completed CaseCourtHearingMigration");
        } else {
            log.info("Skipping CaseCourtHearingMigration, MIGRATE_CASE_COURT_HEARING={}, references size: {}",
                migrateCaseCourtHearing,
                allReferences.size());
        }
    }

    private List<CaseDetails> searchForCaseCourtHearingCases(final List<Long> references,
                                                             final User user,
                                                             final String serviceAuthorization) {

        final BoolQueryBuilder referenceQuery = boolQuery();
        references.forEach(reference -> referenceQuery.should(matchQuery("reference", reference)));

        final SearchSourceBuilder sourceBuilder = SearchSourceBuilder
            .searchSource()
            .query(boolQuery().must(referenceQuery))
            .from(0)
            .size(100);

        log.info("CaseCourtHearingMigration searching ES, {}", sourceBuilder);

        final SearchResult searchResult = coreCaseDataApi.searchCases(
            user.getAuthToken(),
            serviceAuthorization,
            BulkActionCaseTypeConfig.CASE_TYPE,
            sourceBuilder.toString());

        log.info("CaseCourtHearingMigration result ES, {}", searchResult);

        return searchResult.getCases();
    }
}
