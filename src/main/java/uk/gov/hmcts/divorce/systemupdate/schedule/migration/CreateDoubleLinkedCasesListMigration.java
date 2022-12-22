package uk.gov.hmcts.divorce.systemupdate.schedule.migration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.reform.idam.client.models.User;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Optional.ofNullable;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;

@Component
@Slf4j
public class CreateDoubleLinkedCasesListMigration implements Migration {

    @Value("${MIGRATE_CREATE_DOUBLE_LINKED_LIST:false}")
    private boolean migrateCreateDoubleLinkedList;

    @Autowired
    private CcdSearchService ccdSearchService;

    @Override
    public void apply(final User user, final String serviceAuthorization) {

        if (migrateCreateDoubleLinkedList) {

            log.info("Started CreateDoubleLinkedCasesListMigration");

            final Map<String, Set<Long>> caseToBulkCasesMap = new HashMap<>();

            try {

                final List<CaseDetails<BulkActionCaseData, BulkActionState>> bulkCases = ccdSearchService
                    .searchForBulkCases(user, serviceAuthorization, boolQuery());

                log.info("CreateDoubleLinkedCasesListMigration Number of bulk cases {}", bulkCases.size());

                bulkCases.forEach(bulkDetails -> {
                    final BulkActionCaseData bulkCase = bulkDetails.getData();
                    final Long bulkCaseId = bulkDetails.getId();

                    ofNullable(bulkCase.getBulkListCaseDetails())
                        .ifPresent(list -> list
                            .forEach(value -> {
                                final String caseReference = value.getValue().getCaseReference().getCaseReference();
                                final Set<Long> bulkCaseReferences = caseToBulkCasesMap
                                    .computeIfAbsent(caseReference, s -> new HashSet<>());
                                bulkCaseReferences.add(bulkCaseId);
                            }));
                });

                caseToBulkCasesMap.entrySet().stream()
                    .filter(stringSetEntry -> stringSetEntry.getValue().size() > 1)
                    .forEach(stringSetEntry -> log.info(
                        "Case Id: {} linked in bulk Cases: {}",
                        stringSetEntry.getKey(),
                        stringSetEntry.getValue()));

            } catch (final CcdSearchCaseException e) {
                log.error("Case schedule task(CreateDoubleLinkedCasesListMigration) stopped after search error", e);
            }
            log.info("Completed CreateDoubleLinkedCasesListMigration");
        } else {
            log.info("Skipping CreateDoubleLinkedCasesListMigration, MIGRATE_CREATE_DOUBLE_LINKED_LIST={}",
                migrateCreateDoubleLinkedList);
        }

    }
}
