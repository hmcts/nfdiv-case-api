package uk.gov.hmcts.divorce.bulkaction.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.CaseLink;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.bulkaction.data.BulkListCaseDetails;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.idam.client.models.User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.feignException;

@ExtendWith(MockitoExtension.class)
class CaseTriggerServiceTest {

    @Mock
    private CcdUpdateService ccdUpdateService;

    @InjectMocks
    private CaseTriggerService caseTriggerService;

    @Test
    void shouldReturnSuccessfulTriggerResult() {

        final var bulkListCaseDetails = BulkListCaseDetails.builder()
            .caseReference(CaseLink.builder()
                .caseReference("1")
                .build())
            .build();

        final var bulkListCaseDetailsListValue = ListValue.<BulkListCaseDetails>builder()
            .value(bulkListCaseDetails).build();

        final CaseTask caseTask = caseDetails -> caseDetails;
        final User user = mock(User.class);

        final CaseTriggerService.TriggerResult triggerResult = caseTriggerService.caseTrigger(
            bulkListCaseDetailsListValue,
            "Event Id",
            caseTask,
            user,
            SERVICE_AUTHORIZATION);

        assertThat(triggerResult.isProcessed()).isTrue();
        assertThat(triggerResult.getListValueBulkListCaseDetails().getValue()).isEqualTo(bulkListCaseDetails);

        verify(ccdUpdateService).submitEventWithRetry(
            "1",
            "Event Id",
            caseTask,
            user,
            SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldReturnFailedTriggerResultIfCcdUpdateServiceThrowException() {

        final var bulkListCaseDetails = BulkListCaseDetails.builder()
            .caseReference(CaseLink.builder()
                .caseReference("1")
                .build())
            .build();
        final var bulkListCaseDetailsListValue = ListValue.<BulkListCaseDetails>builder()
            .value(bulkListCaseDetails).build();

        final CaseTask caseTask = caseDetails -> caseDetails;
        final User user = mock(User.class);

        doThrow(feignException(409, "some error"))
            .when(ccdUpdateService).submitEventWithRetry(
                "1",
                "Event Id",
                caseTask,
                user,
                SERVICE_AUTHORIZATION);

        final CaseTriggerService.TriggerResult triggerResult = caseTriggerService.caseTrigger(
            bulkListCaseDetailsListValue,
            "1",
            caseTask,
            mock(User.class),
            SERVICE_AUTHORIZATION);

        assertThat(triggerResult.isProcessed()).isFalse();
        assertThat(triggerResult.getListValueBulkListCaseDetails().getValue()).isEqualTo(bulkListCaseDetails);
    }
}
