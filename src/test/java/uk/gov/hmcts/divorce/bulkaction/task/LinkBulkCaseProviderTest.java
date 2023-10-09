package uk.gov.hmcts.divorce.bulkaction.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemLinkWithBulkCase.SYSTEM_LINK_WITH_BULK_CASE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class LinkBulkCaseProviderTest {

    @InjectMocks
    private LinkBulkCaseProvider linkBulkCaseProvider;

    @Test
    void shouldReturnSystemLinkWithBulkCaseEventId() {
        assertThat(linkBulkCaseProvider.getEventId()).isEqualTo(SYSTEM_LINK_WITH_BULK_CASE);
    }

    @Test
    void shouldReturnSystemLinkWithBulkCaseCaseTask() {

        final CaseDetails<BulkActionCaseData, BulkActionState> bulkCaseDetails = new CaseDetails<>();
        bulkCaseDetails.setId(TEST_CASE_ID);

        final var caseData = CaseData.builder()
            .finalOrder(FinalOrder.builder().build())
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final CaseTask caseTask = linkBulkCaseProvider.getCaseTask(bulkCaseDetails);

        final CaseDetails<CaseData, State> resultCaseDetails = caseTask.apply(caseDetails);
        final CaseData resultCaseData = resultCaseDetails.getData();
        final ConditionalOrder resultConditionalOrder = resultCaseData.getConditionalOrder();

        assertThat(resultCaseData.getBulkListCaseReferenceLink().getCaseReference()).isEqualTo(TEST_CASE_ID.toString());
    }
}
