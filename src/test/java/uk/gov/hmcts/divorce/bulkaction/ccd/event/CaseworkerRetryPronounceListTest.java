package uk.gov.hmcts.divorce.bulkaction.ccd.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.service.CasePronouncementService;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.divorce.bulkaction.ccd.event.CaseworkerRetryPronounceList.CASEWORKER_RETRY_PRONOUNCE_LIST;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createBulkActionConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class CaseworkerRetryPronounceListTest {

    @Mock
    private CasePronouncementService casePronouncementService;

    @InjectMocks
    private CaseworkerRetryPronounceList caseworkerRetryPronounceList;

    @Test
    void shouldAddConfigurationToConfigBuilder() {

        final ConfigBuilderImpl<BulkActionCaseData, BulkActionState, UserRole> configBuilder = createBulkActionConfigBuilder();

        caseworkerRetryPronounceList.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_RETRY_PRONOUNCE_LIST);
    }


    @Test
    void shouldUpdateBulkCaseAfterBulkTriggerForSubmittedCallback() {
        final CaseDetails<BulkActionCaseData, BulkActionState> details = new CaseDetails<>();
        details.setData(BulkActionCaseData.builder().build());
        details.setId(TEST_CASE_ID);

        doNothing().when(casePronouncementService).retryPronounceCases(details);

        SubmittedCallbackResponse submittedCallbackResponse = caseworkerRetryPronounceList.submitted(details, details);

        assertThat(submittedCallbackResponse).isNotNull();
        verify(casePronouncementService).retryPronounceCases(details);
    }
}
