package uk.gov.hmcts.divorce.systemupdate.schedule.migration;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.systemupdate.schedule.migration.predicate.HasAosDraftedEventPredicate;
import uk.gov.hmcts.divorce.systemupdate.service.CcdConflictException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.User;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.existsQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemMigrateCase.SYSTEM_MIGRATE_CASE;

@Component
@Slf4j
public class SetAosIsDraftedToYesMigration implements Migration {

    @Value("${MIGRATE_AOS_IS_DRAFTED:false}")
    private boolean migrateAosIsDrafted;

    @Value("#{'${AOS_IS_DRAFTED_REFERENCES:}'.split(',')}")
    private List<Long> aosIsDraftedReferences;

    @Value("#{'${AOS_IS_DRAFTED_REFERENCES_2:}'.split(',')}")
    private List<Long> aosIsDraftedReferences2;

    @Value("#{'${AOS_IS_DRAFTED_REFERENCES_3:}'.split(',')}")
    private List<Long> aosIsDraftedReferences3;

    @Value("#{'${AOS_IS_DRAFTED_REFERENCES_4:}'.split(',')}")
    private List<Long> aosIsDraftedReferences4;

    @Value("#{'${AOS_IS_DRAFTED_REFERENCES_5:}'.split(',')}")
    private List<Long> aosIsDraftedReferences5;

    @Autowired
    private CcdSearchService ccdSearchService;

    @Autowired
    private CcdUpdateService ccdUpdateService;

    @Autowired
    private HasAosDraftedEventPredicate hasAosDraftedEventPredicate;

    @Override
    public void apply(final User user, final String serviceAuthorization) {

        final List<Long> allReferences = Stream.of(
                aosIsDraftedReferences,
                aosIsDraftedReferences2,
                aosIsDraftedReferences3,
                aosIsDraftedReferences4,
                aosIsDraftedReferences5)
            .flatMap(Collection::stream)
            .filter(Objects::nonNull)
            .distinct()
            .toList();

        if (migrateAosIsDrafted && !isEmpty(allReferences)) {
            log.info("Started SetAosIsDraftedToYesMigration with references size: {}", allReferences);
            try {

                final List<CaseDetails> caseDetails = searchForAosDraftedCases(allReferences, user, serviceAuthorization);

                log.info("SetAosIsDraftedToYesMigration Number of cases {}", caseDetails.size());

                caseDetails
                    .parallelStream()
                    .forEach(caseDetail -> setAosIsDrafted(caseDetail, user, serviceAuthorization));

            } catch (final CcdSearchCaseException e) {
                log.error("Case schedule task(SetAosIsDraftedToYesMigration) stopped after search error", e);
            }
            log.info("Completed SetAosIsDraftedToYesMigration");
        } else {
            log.info("Skipping SetAosIsDraftedToYesMigration, MIGRATE_AOS_IS_DRAFTED={}, references size: {}",
                migrateAosIsDrafted,
                allReferences.size());
        }
    }

    private List<CaseDetails> searchForAosDraftedCases(final List<Long> references,
                                                       final User user,
                                                       final String serviceAuthorization) {

        final Predicate<CaseDetails> hasAosDraftedEvent = hasAosDraftedEventPredicate.hasAosDraftedEvent(user, serviceAuthorization);

        final BoolQueryBuilder referenceQuery = boolQuery();
        references.forEach(reference -> referenceQuery.should(matchQuery("reference", reference)));

        final BoolQueryBuilder query =
            boolQuery()
                .must(referenceQuery)
                .mustNot(existsQuery("data.dateAosSubmitted"))
                .mustNot(existsQuery("data.aosIsDrafted"));

        log.info("SetAosIsDraftedToYesMigration searching ES");

        final List<CaseDetails> searchResult = ccdSearchService
            .searchForAllCasesWithQuery(
                query,
                user,
                serviceAuthorization);

        log.info("SetAosIsDraftedToYesMigration Pre Filter Number of cases {}", searchResult.size());

        return searchResult
            .stream()
            .filter(hasAosDraftedEvent)
            .peek(caseDetails -> log.info("SetAosIsDraftedToYesMigration post filter Case Id: {}", caseDetails.getId()))
            .toList();
    }

    private void setAosIsDrafted(final CaseDetails caseDetails, final User user, final String serviceAuthorization) {

        final Long caseId = caseDetails.getId();

        try {
            caseDetails.getData().put("aosIsDrafted", "Yes");
            ccdUpdateService.submitEvent(caseDetails, SYSTEM_MIGRATE_CASE, user, serviceAuthorization);
            log.info("SetAosIsDraftedToYesMigration Set aosIsDrafted to Yes successfully for case id: {}", caseId);
        } catch (final CcdConflictException e) {
            log.error("SetAosIsDraftedToYesMigration Could not get lock for case id: {}, continuing to next case", caseId);
        } catch (final CcdManagementException e) {
            log.error(
                "SetAosIsDraftedToYesMigration Submit event(after setting aosIsDrafted) failed for case id: {}, continuing to next case",
                caseId);
        }
    }
}
