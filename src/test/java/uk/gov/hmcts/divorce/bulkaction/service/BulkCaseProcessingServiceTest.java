package uk.gov.hmcts.divorce.bulkaction.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.CaseLink;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.data.BulkListCaseDetails;
import uk.gov.hmcts.divorce.bulkaction.task.BulkCaseTask;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.divorce.bulkaction.ccd.event.SystemUpdateCase.SYSTEM_UPDATE_BULK_CASE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class BulkCaseProcessingServiceTest {

    @Mock
    private CcdUpdateService ccdUpdateService;

    @InjectMocks
    private BulkCaseProcessingService bulkCaseProcessingService;

    @Test
    void shouldUpdateAllBulkCases() {

        final User user = mock(User.class);
        final BulkCaseTask bulkCaseTask = bulkCaseDetails -> bulkCaseDetails;
        final List<ListValue<BulkListCaseDetails>> fullBulkList = getBulkListCaseDetailsListValueForCaseIds("1", "2", "3", "4");

        final BulkActionCaseData bulkActionCaseData = BulkActionCaseData.builder()
            .bulkListCaseDetails(fullBulkList)
            .build();

        final CaseDetails<BulkActionCaseData, BulkActionState> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(bulkActionCaseData);

        bulkCaseProcessingService.updateBulkCase(
            caseDetails,
            bulkCaseTask,
            user,
            SERVICE_AUTHORIZATION);

        verify(ccdUpdateService).submitBulkActionEvent(
            bulkCaseTask,
            TEST_CASE_ID,
            SYSTEM_UPDATE_BULK_CASE,
            user,
            SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldUpdateUnprocessedBulkCases() {

        final User user = mock(User.class);
        final BulkCaseTask bulkCaseTask = bulkCaseDetails -> bulkCaseDetails;
        final List<ListValue<BulkListCaseDetails>> fullBulkList = getBulkListCaseDetailsListValueForCaseIds("1", "2", "3", "4");

        final BulkActionCaseData bulkActionCaseData = BulkActionCaseData.builder()
            .bulkListCaseDetails(fullBulkList)
            .build();

        final CaseDetails<BulkActionCaseData, BulkActionState> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(bulkActionCaseData);

        bulkCaseProcessingService.updateBulkCase(
            caseDetails,
            bulkCaseTask,
            user,
            SERVICE_AUTHORIZATION);

        verify(ccdUpdateService).submitBulkActionEvent(
            bulkCaseTask,
            TEST_CASE_ID,
            SYSTEM_UPDATE_BULK_CASE,
            user,
            SERVICE_AUTHORIZATION);
    }

    private List<ListValue<BulkListCaseDetails>> getBulkListCaseDetailsListValueForCaseIds(final String... caseIds) {
        return Arrays.stream(caseIds)
            .map(this::getBulkListCaseDetailsListValue).collect(toList());
    }

    private ListValue<BulkListCaseDetails> getBulkListCaseDetailsListValue(final String caseId) {
        var bulkListCaseDetails = BulkListCaseDetails.builder().caseReference(CaseLink.builder().caseReference(caseId).build()).build();
        return ListValue.<BulkListCaseDetails>builder().value(bulkListCaseDetails).build();
    }
}
