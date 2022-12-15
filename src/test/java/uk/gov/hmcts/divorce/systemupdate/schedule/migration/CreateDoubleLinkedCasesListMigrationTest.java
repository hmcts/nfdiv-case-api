package uk.gov.hmcts.divorce.systemupdate.schedule.migration;

import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.data.BulkListCaseDetails;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getBulkListCaseDetailsListValue;

@ExtendWith(MockitoExtension.class)
class CreateDoubleLinkedCasesListMigrationTest {

    @Mock
    private Logger logger;

    @Mock
    private CcdSearchService ccdSearchService;

    @InjectMocks
    private CreateDoubleLinkedCasesListMigration createDoubleLinkedCasesListMigration;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserDetails.builder().build());
    }

    @Test
    void shouldLogAllDoubleLinkedCasesToBulkCases() {

        setField(createDoubleLinkedCasesListMigration, "migrateCreateDoubleLinkedList", true);

        final ListValue<BulkListCaseDetails> bulkListCaseDetailsListValue1 = getBulkListCaseDetailsListValue("1");
        final ListValue<BulkListCaseDetails> bulkListCaseDetailsListValue2 = getBulkListCaseDetailsListValue("2");
        final ListValue<BulkListCaseDetails> bulkListCaseDetailsListValue3 = getBulkListCaseDetailsListValue("3");
        final ListValue<BulkListCaseDetails> bulkListCaseDetailsListValue4 = getBulkListCaseDetailsListValue("4");
        final ListValue<BulkListCaseDetails> bulkListCaseDetailsListValue5 = getBulkListCaseDetailsListValue("5");
        final ListValue<BulkListCaseDetails> bulkListCaseDetailsListValue6 = getBulkListCaseDetailsListValue("6");

        final List<ListValue<BulkListCaseDetails>> caseDetailsList1
            = List.of(bulkListCaseDetailsListValue1, bulkListCaseDetailsListValue2);
        final List<ListValue<BulkListCaseDetails>> caseDetailsList2
            = List.of(bulkListCaseDetailsListValue3, bulkListCaseDetailsListValue2);
        final List<ListValue<BulkListCaseDetails>> caseDetailsList3
            = List.of(bulkListCaseDetailsListValue4, bulkListCaseDetailsListValue5);
        final List<ListValue<BulkListCaseDetails>> caseDetailsList4
            = List.of(bulkListCaseDetailsListValue6, bulkListCaseDetailsListValue4);

        final CaseDetails<BulkActionCaseData, BulkActionState> bulkCaseDetails1 = new CaseDetails<>();
        bulkCaseDetails1.setId(1L);
        bulkCaseDetails1.setData(BulkActionCaseData.builder()
            .bulkListCaseDetails(caseDetailsList1)
            .build());

        final CaseDetails<BulkActionCaseData, BulkActionState> bulkCaseDetails2 = new CaseDetails<>();
        bulkCaseDetails2.setId(2L);
        bulkCaseDetails2.setData(BulkActionCaseData.builder()
            .bulkListCaseDetails(caseDetailsList2)
            .build());

        final CaseDetails<BulkActionCaseData, BulkActionState> bulkCaseDetails3 = new CaseDetails<>();
        bulkCaseDetails3.setId(3L);
        bulkCaseDetails3.setData(BulkActionCaseData.builder()
            .bulkListCaseDetails(caseDetailsList3)
            .build());

        final CaseDetails<BulkActionCaseData, BulkActionState> bulkCaseDetails4 = new CaseDetails<>();
        bulkCaseDetails4.setId(4L);
        bulkCaseDetails4.setData(BulkActionCaseData.builder()
            .bulkListCaseDetails(caseDetailsList4)
            .build());

        final List<CaseDetails<BulkActionCaseData, BulkActionState>> caseDetailsList
            = asList(bulkCaseDetails1, bulkCaseDetails2, bulkCaseDetails3, bulkCaseDetails4);

        when(ccdSearchService.searchForBulkCases(user, SERVICE_AUTHORIZATION, boolQuery()))
            .thenReturn(caseDetailsList);

        createDoubleLinkedCasesListMigration.apply(user, SERVICE_AUTHORIZATION);

        verify(logger).info("Case Id: {} linked in bulk Cases: {}", "2", Set.of(1L, 2L));
        verify(logger).info("Case Id: {} linked in bulk Cases: {}", "4", Set.of(3L, 4L));
    }

    @Test
    void shouldHandleNullCaseList() {

        setField(createDoubleLinkedCasesListMigration, "migrateCreateDoubleLinkedList", true);

        final CaseDetails<BulkActionCaseData, BulkActionState> bulkCaseDetails = new CaseDetails<>();
        bulkCaseDetails.setId(1L);
        bulkCaseDetails.setData(BulkActionCaseData.builder()
            .bulkListCaseDetails(null)
            .build());

        final List<CaseDetails<BulkActionCaseData, BulkActionState>> caseDetailsList
            = List.of(bulkCaseDetails);

        when(ccdSearchService.searchForBulkCases(user, SERVICE_AUTHORIZATION, boolQuery()))
            .thenReturn(caseDetailsList);

        try {
            createDoubleLinkedCasesListMigration.apply(user, SERVICE_AUTHORIZATION);
        } catch (final Exception e) {
            fail(String.format("Exception has been thrown: %s", e.getMessage()));
        }
    }

    @Test
    void shouldDoNothingAndLogErrorIfSearchFails() {

        setField(createDoubleLinkedCasesListMigration, "migrateCreateDoubleLinkedList", true);

        final CcdSearchCaseException exception =
            new CcdSearchCaseException("Failed to search cases", mock(FeignException.class));
        when(ccdSearchService.searchForBulkCases(user, SERVICE_AUTHORIZATION, boolQuery()))
            .thenThrow(exception);

        createDoubleLinkedCasesListMigration.apply(user, SERVICE_AUTHORIZATION);

        verify(logger)
            .error("Case schedule task(CreateDoubleLinkedCasesListMigration) stopped after search error", exception);
    }

    @Test
    void shouldSkipProcessingIfEnvironmentSwitchIsSetToFalse() {

        setField(createDoubleLinkedCasesListMigration, "migrateCreateDoubleLinkedList", false);

        createDoubleLinkedCasesListMigration.apply(user, SERVICE_AUTHORIZATION);

        verify(logger).info("Skipping CreateDoubleLinkedCasesListMigration, MIGRATE_CREATE_DOUBLE_LINKED_LIST={}", false);
        verifyNoInteractions(ccdSearchService);
    }
}