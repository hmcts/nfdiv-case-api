package uk.gov.hmcts.divorce.systemupdate.schedule.migration;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.systemupdate.service.CcdConflictException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.User;

import java.util.List;
import java.util.function.Predicate;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.existsQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AosDrafted;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AosOverdue;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAos;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingBailiffReferral;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingDocuments;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingGeneralConsideration;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingGeneralReferralPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingService;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingServiceConsideration;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingServicePayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.GeneralApplicationReceived;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Holding;
import static uk.gov.hmcts.divorce.divorcecase.model.State.IssuedToBailiff;
import static uk.gov.hmcts.divorce.divorcecase.model.State.OfflineDocumentReceived;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemMigrateCase.SYSTEM_MIGRATE_CASE;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.STATE;

@Component
@Slf4j
public class SetAosIsDraftedToYesMigration implements Migration {

    @Value("${MIGRATE_AOS_IS_DRAFTED:false}")
    private boolean migrateAosIsDrafted;

    @Autowired
    private CcdSearchService ccdSearchService;

    @Autowired
    private CcdUpdateService ccdUpdateService;

    @Autowired
    private HasAosDraftedEventPredicate hasAosDraftedEventPredicate;

    @Override
    public void apply(final User user, final String serviceAuthorization) {

        if (migrateAosIsDrafted) {
            log.info("Started SetAosIsDraftedToYesMigration");
            try {

                final List<CaseDetails> caseDetails = searchForAosDraftedCases(user, serviceAuthorization);

                log.info("SetAosIsDraftedToYesMigration Number of cases {}", caseDetails.size());

                caseDetails
                    .parallelStream()
                    .forEach(caseDetail -> setAosIsDrafted(caseDetail, user, serviceAuthorization));

            } catch (final CcdSearchCaseException e) {
                log.error("Case schedule task(SetAosIsDraftedToYesMigration) stopped after search error", e);
            }
            log.info("Completed SetAosIsDraftedToYesMigration");
        } else {
            log.info("Skipping SetAosIsDraftedToYesMigration, MIGRATE_AOS_IS_DRAFTED=false");
        }
    }

    private List<CaseDetails> searchForAosDraftedCases(final User user, final String serviceAuthorization) {

        final Predicate<CaseDetails> hasAosDraftedEvent = hasAosDraftedEventPredicate.hasAosDraftedEvent(user, serviceAuthorization);
        final BoolQueryBuilder query =
            boolQuery()
                .must(
                    boolQuery()
                        .should(matchQuery(STATE, AosDrafted))
                        .should(matchQuery(STATE, AosOverdue))
                        .should(matchQuery(STATE, OfflineDocumentReceived))
                        .should(matchQuery(STATE, AwaitingAos))
                        .should(matchQuery(STATE, GeneralApplicationReceived))
                        .should(matchQuery(STATE, AwaitingGeneralReferralPayment))
                        .should(matchQuery(STATE, Holding))
                        .should(matchQuery(STATE, AwaitingDocuments))
                        .should(matchQuery(STATE, AwaitingBailiffReferral))
                        .should(matchQuery(STATE, AwaitingServicePayment))
                        .should(matchQuery(STATE, AwaitingServiceConsideration))
                        .should(matchQuery(STATE, IssuedToBailiff))
                        .should(matchQuery(STATE, AwaitingService))
                        .should(matchQuery(STATE, AwaitingGeneralConsideration)))
                .mustNot(existsQuery("data.dateAosSubmitted"))
                .mustNot(existsQuery("data.aosIsDrafted"));

        log.info("SetAosIsDraftedToYesMigration searching ES");

        final List<CaseDetails> searchResult = ccdSearchService
            .searchForAllCasesWithQuery(
                query,
                user,
                serviceAuthorization,
                AosDrafted,
                AosOverdue,
                OfflineDocumentReceived,
                AwaitingAos,
                GeneralApplicationReceived,
                AwaitingGeneralReferralPayment,
                Holding,
                AwaitingDocuments,
                AwaitingBailiffReferral,
                AwaitingServicePayment,
                AwaitingServiceConsideration,
                IssuedToBailiff,
                AwaitingService,
                AwaitingGeneralConsideration);

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
