package uk.gov.hmcts.divorce.bulkaction.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.CaseLink;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.bulkaction.data.BulkListCaseDetails;
import uk.gov.hmcts.divorce.bulkaction.service.CaseTriggerService.TriggerResult;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.reform.idam.client.models.User;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;

@ExtendWith(MockitoExtension.class)
public class BulkTriggerServiceTest {

    @Mock
    private CaseTriggerService caseTriggerService;

    @InjectMocks
    private BulkTriggerService bulkTriggerService;

    @Test
    void shouldCallCaseTriggerServiceAndReturnAnyFailedCases() {

        final User user = mock(User.class);
        final CaseTask caseTask = details -> details;
        final ListValue<BulkListCaseDetails> bulkListCaseDetailsListValue1 =
            ListValue.<BulkListCaseDetails>builder().value(getBulkListCaseDetails("1")).build();
        final ListValue<BulkListCaseDetails> bulkListCaseDetailsListValue2 =
            ListValue.<BulkListCaseDetails>builder().value(getBulkListCaseDetails("2")).build();

        final List<ListValue<BulkListCaseDetails>> bulkListCaseDetails =
            Arrays.asList(bulkListCaseDetailsListValue1, bulkListCaseDetailsListValue2);

        when(caseTriggerService.caseTrigger(bulkListCaseDetailsListValue1, "event Id", caseTask, user, SERVICE_AUTHORIZATION))
            .thenReturn(new TriggerResult(true, bulkListCaseDetailsListValue1));
        when(caseTriggerService.caseTrigger(bulkListCaseDetailsListValue2, "event Id", caseTask, user, SERVICE_AUTHORIZATION))
            .thenReturn(new TriggerResult(false, bulkListCaseDetailsListValue2));

        final List<ListValue<BulkListCaseDetails>> errors = bulkTriggerService.bulkTrigger(
            bulkListCaseDetails,
            "event Id",
            caseTask,
            user,
            SERVICE_AUTHORIZATION);

        assertThat(errors).hasSize(1);
        assertThat(errors.get(0)).isEqualTo(bulkListCaseDetailsListValue2);
    }

    private BulkListCaseDetails getBulkListCaseDetails(final String caseId) {
        return BulkListCaseDetails.builder()
            .caseReference(CaseLink.builder()
                .caseReference(caseId)
                .build())
            .build();
    }
}
