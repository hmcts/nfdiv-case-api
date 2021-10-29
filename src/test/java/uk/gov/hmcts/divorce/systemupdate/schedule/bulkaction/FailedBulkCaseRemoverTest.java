package uk.gov.hmcts.divorce.systemupdate.schedule.bulkaction;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.CaseLink;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.data.BulkListCaseDetails;
import uk.gov.hmcts.divorce.systemupdate.convert.CaseDetailsConverter;
import uk.gov.hmcts.divorce.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.User;

import java.util.List;
import java.util.Map;

import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.bulkaction.ccd.event.SystemRemoveFailedCases.SYSTEM_REMOVE_FAILED_CASES;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;

@ExtendWith(MockitoExtension.class)
class FailedBulkCaseRemoverTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Mock
    private CcdUpdateService ccdUpdateService;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @InjectMocks
    private FailedBulkCaseRemover failedBulkCaseRemover;

    @Test
    void shouldRemoveGivenCaseIdsFromBulkCaseListAndUpdateBulkCase() {

        final List<Long> failedCaseIds = List.of(2L, 4L);
        final List<ListValue<BulkListCaseDetails>> listValues = getBulkListCaseDetailsListValueForCaseIds("1", "2", "3", "4", "5");
        final BulkActionCaseData bulkActionCaseData = BulkActionCaseData.builder()
            .bulkListCaseDetails(listValues)
            .build();
        final Map<String, Object> bulkActionCaseDataMap = objectMapper.convertValue(bulkActionCaseData, new TypeReference<>() {
        });
        final CaseDetails bulkCaseDetails = CaseDetails.builder()
            .id(1L)
            .data(bulkActionCaseDataMap)
            .build();
        final uk.gov.hmcts.ccd.sdk.api.CaseDetails<BulkActionCaseData, BulkActionState> resultCaseDetails =
            convertFromReformModelToBulkActionCaseDetails(bulkCaseDetails);
        final User user = mock(User.class);

        when(caseDetailsConverter.convertToBulkActionCaseDetailsFromReformModel(bulkCaseDetails)).thenReturn(resultCaseDetails);

        failedBulkCaseRemover.removeFailedCasesFromBulkListCaseDetails(
            failedCaseIds,
            bulkCaseDetails,
            user,
            SERVICE_AUTHORIZATION);

        final List<ListValue<BulkListCaseDetails>> resultBulkCaseList = resultCaseDetails.getData().getBulkListCaseDetails();
        assertThat(resultBulkCaseList).hasSize(3);
        assertThat(resultBulkCaseList.get(0).getValue().getCaseReference().getCaseReference()).isEqualTo("1");
        assertThat(resultBulkCaseList.get(1).getValue().getCaseReference().getCaseReference()).isEqualTo("3");
        assertThat(resultBulkCaseList.get(2).getValue().getCaseReference().getCaseReference()).isEqualTo("5");

        verify(ccdUpdateService).submitBulkActionEvent(
            resultCaseDetails,
            SYSTEM_REMOVE_FAILED_CASES,
            user,
            SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldNotDoAnythingIfCaseIdsToRemoveListIsEmpty() {

        final User user = mock(User.class);

        final CaseDetails bulkCaseDetails = CaseDetails.builder()
            .id(1L)
            .build();

        failedBulkCaseRemover.removeFailedCasesFromBulkListCaseDetails(
            emptyList(),
            bulkCaseDetails,
            user,
            SERVICE_AUTHORIZATION);

        verifyNoInteractions(caseDetailsConverter, ccdUpdateService);
    }

    @Test
    void shouldNotThrowExceptionAndCompleteNormallyIfCcdManagementExceptionIfThrown() {

        final List<Long> failedCaseIds = List.of(2L, 4L);
        final List<ListValue<BulkListCaseDetails>> listValues = getBulkListCaseDetailsListValueForCaseIds("1", "2", "3", "4", "5");
        final BulkActionCaseData bulkActionCaseData = BulkActionCaseData.builder()
            .bulkListCaseDetails(listValues)
            .build();
        final Map<String, Object> bulkActionCaseDataMap = objectMapper.convertValue(bulkActionCaseData, new TypeReference<>() {
        });
        final CaseDetails bulkCaseDetails = CaseDetails.builder()
            .id(1L)
            .data(bulkActionCaseDataMap)
            .build();
        final uk.gov.hmcts.ccd.sdk.api.CaseDetails<BulkActionCaseData, BulkActionState> resultCaseDetails =
            convertFromReformModelToBulkActionCaseDetails(bulkCaseDetails);
        final User user = mock(User.class);

        when(caseDetailsConverter.convertToBulkActionCaseDetailsFromReformModel(bulkCaseDetails)).thenReturn(resultCaseDetails);

        doThrow(new CcdManagementException("Message", null))
            .when(ccdUpdateService).submitBulkActionEvent(
                resultCaseDetails,
                SYSTEM_REMOVE_FAILED_CASES,
                user,
                SERVICE_AUTHORIZATION);

        try {
            failedBulkCaseRemover.removeFailedCasesFromBulkListCaseDetails(
                failedCaseIds,
                bulkCaseDetails,
                user,
                SERVICE_AUTHORIZATION);
        } catch (Exception e) {
            fail("No exception should be thrown");
        }
    }

    private List<ListValue<BulkListCaseDetails>> getBulkListCaseDetailsListValueForCaseIds(final String... caseIds) {
        return stream(caseIds)
            .map(this::getBulkListCaseDetailsListValue).collect(toList());
    }

    private ListValue<BulkListCaseDetails> getBulkListCaseDetailsListValue(final String caseId) {
        var bulkListCaseDetails = BulkListCaseDetails.builder()
            .caseReference(CaseLink.builder()
                .caseReference(caseId)
                .build())
            .build();
        return ListValue.<BulkListCaseDetails>builder().value(bulkListCaseDetails).build();
    }

    private uk.gov.hmcts.ccd.sdk.api.CaseDetails<BulkActionCaseData, BulkActionState> convertFromReformModelToBulkActionCaseDetails(
        final CaseDetails caseDetails) {

        return objectMapper.convertValue(caseDetails, new TypeReference<>() {
        });
    }
}