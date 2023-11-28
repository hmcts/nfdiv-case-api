package uk.gov.hmcts.divorce.bulkaction.service.filter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.bulkaction.data.BulkListCaseDetails;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;

import java.util.EnumSet;
import java.util.List;

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

        when(ccdSearchService.searchForCases(List.of("1", "2", "3", "4"), user, SERVICE_AUTHORIZATION))
            .thenReturn(asList(
                uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                    .id(TEST_CASE_ID)
                    .state(AwaitingPronouncement.name())
                    .build(),
                uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                    .id(2L)
                    .state(ConditionalOrderPronounced.name())
                    .build(),
                uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                    .id(3L)
                    .state(OfflineDocumentReceived.name())
                    .build(),
                uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                    .id(4L)
                    .state(Archived.name())
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
}
