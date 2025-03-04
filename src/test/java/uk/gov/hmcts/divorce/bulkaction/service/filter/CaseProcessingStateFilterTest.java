package uk.gov.hmcts.divorce.bulkaction.service.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.bulkaction.data.BulkListCaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.time.LocalDate;
import java.time.Month;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Archived;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPronouncement;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderPronounced;
import static uk.gov.hmcts.divorce.divorcecase.model.State.OfflineDocumentReceived;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getBulkListCaseDetailsListValue;

@ExtendWith(MockitoExtension.class)
class CaseProcessingStateFilterTest {

    @Mock
    private CcdSearchService ccdSearchService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private CaseProcessingStateFilter caseProcessingStateFilter;

    @Test
    void shouldFilterBulkListCaseDetailsIntoProcessableCasesAndErroredCasesAndProcessedCasesLists() {

        final var user = mock(User.class);
        final var bulkListCaseDetailsListValue1 = getBulkListCaseDetailsListValue("1");
        final var bulkListCaseDetailsListValue2 = getBulkListCaseDetailsListValue("2");
        final var bulkListCaseDetailsListValue3 = getBulkListCaseDetailsListValue("3");
        final var bulkListCaseDetailsListValue4 = getBulkListCaseDetailsListValue("4");

        final List<ListValue<BulkListCaseDetails>> bulkListCaseDetails = List.of(
            bulkListCaseDetailsListValue1,
            bulkListCaseDetailsListValue2,
            bulkListCaseDetailsListValue3,
            bulkListCaseDetailsListValue4
        );

        Map<String, Object> caseData = new HashMap<>();
        caseData.put("dueDate", LocalDate.of(2022, 1, 1));

        when(objectMapper.convertValue(caseData, CaseData.class))
                .thenReturn(CaseData.builder().dueDate(LocalDate.of(2022, 1, 1)).build());

        when(ccdSearchService.searchForCases(List.of("1", "2", "3", "4"), user, SERVICE_AUTHORIZATION))
            .thenReturn(asList(
                uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                    .id(TEST_CASE_ID)
                    .state(AwaitingPronouncement.name())
                    .data(caseData)
                    .build(),
                uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                    .id(2L)
                    .state(ConditionalOrderPronounced.name())
                    .data(caseData)
                    .build(),
                uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                    .id(3L)
                    .state(OfflineDocumentReceived.name())
                    .data(caseData)
                    .build(),
                uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                    .id(4L)
                    .state(Archived.name())
                    .data(caseData)
                    .build())
            );

        final CaseFilterProcessingState caseFilterProcessingState = caseProcessingStateFilter.filterProcessingState(
            bulkListCaseDetails,
            user,
            SERVICE_AUTHORIZATION,
            EnumSet.of(AwaitingPronouncement, OfflineDocumentReceived),
            EnumSet.of(ConditionalOrderPronounced));

        assertThat(caseFilterProcessingState.getUnprocessedCases()).isEqualTo(
            List.of(bulkListCaseDetailsListValue1, bulkListCaseDetailsListValue3));
        assertThat(caseFilterProcessingState.getErroredCases()).isEqualTo(List.of(bulkListCaseDetailsListValue4));
        assertThat(caseFilterProcessingState.getProcessedCases()).isEqualTo(List.of(bulkListCaseDetailsListValue2));
    }

    @Test
    void shouldIdentifyCasesWithFinalOrdersAsProcessed() {
        final var user = mock(User.class);
        final var bulkListCaseDetailsListValue1 = getBulkListCaseDetailsListValue("1");

        final List<ListValue<BulkListCaseDetails>> bulkListCaseDetails = List.of(
            bulkListCaseDetailsListValue1
        );

        CaseDetails completedCaseDetails = uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
            .id(1L)
            .state(OfflineDocumentReceived.name())
            .data(Map.of("finalOrderGrantedDate", "2022-12-01"))
            .build();

        when(ccdSearchService.searchForCases(List.of("1"), user, SERVICE_AUTHORIZATION))
            .thenReturn(asList(completedCaseDetails));

        when(objectMapper.convertValue(completedCaseDetails.getData(), CaseData.class)).thenReturn(
            CaseData.builder().finalOrder(
                FinalOrder.builder()
                    .grantedDate(LocalDate.of(2022, Month.DECEMBER, 1).atStartOfDay())
                    .build()
            ).build()
        );

        final CaseFilterProcessingState caseFilterProcessingState = caseProcessingStateFilter.filterProcessingState(
            bulkListCaseDetails,
            user,
            SERVICE_AUTHORIZATION,
            EnumSet.of(AwaitingPronouncement, OfflineDocumentReceived),
            EnumSet.of(ConditionalOrderPronounced));

        assertThat(caseFilterProcessingState.getUnprocessedCases()).isEqualTo(Collections.emptyList());
        assertThat(caseFilterProcessingState.getErroredCases()).isEqualTo(Collections.emptyList());
        assertThat(caseFilterProcessingState.getProcessedCases()).isEqualTo(List.of(bulkListCaseDetailsListValue1));
    }

    @Test
    void shouldFilterBulkListCaseDetailsIntoProcessableCasesWhenAlreadyPronouncedAndStateIsOfflineDocumentReceived() {
        final var user = mock(User.class);
        final var bulkListCaseDetailsListValue1 = getBulkListCaseDetailsListValue("1");

        final List<ListValue<BulkListCaseDetails>> bulkListCaseDetails = List.of(
                bulkListCaseDetailsListValue1
        );

        CaseDetails completedCaseDetails = uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                .id(1L)
                .state(OfflineDocumentReceived.name())
                .data(Map.of("dateFinalOrderEligibleFrom", "2022-12-01"))
                .build();

        when(ccdSearchService.searchForCases(List.of("1"), user, SERVICE_AUTHORIZATION))
                .thenReturn(asList(completedCaseDetails));

        when(objectMapper.convertValue(completedCaseDetails.getData(), CaseData.class)).thenReturn(
                CaseData.builder().finalOrder(
                        FinalOrder.builder()
                                .grantedDate(LocalDate.of(2022, Month.DECEMBER, 1).atStartOfDay())
                                .build()
                ).build()
        );

        final CaseFilterProcessingState caseFilterProcessingState = caseProcessingStateFilter.filterProcessingState(
                bulkListCaseDetails,
                user,
                SERVICE_AUTHORIZATION,
                EnumSet.of(AwaitingPronouncement, OfflineDocumentReceived),
                EnumSet.of(ConditionalOrderPronounced));

        assertThat(caseFilterProcessingState.getUnprocessedCases()).isEqualTo(Collections.emptyList());
        assertThat(caseFilterProcessingState.getErroredCases()).isEqualTo(Collections.emptyList());
        assertThat(caseFilterProcessingState.getProcessedCases()).isEqualTo(List.of(bulkListCaseDetailsListValue1));
    }
}
