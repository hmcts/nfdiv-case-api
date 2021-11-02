package uk.gov.hmcts.divorce.bulkaction.ccd.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.service.ScheduleCaseService;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.time.LocalDateTime;
import javax.servlet.http.HttpServletRequest;

import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.bulkaction.ccd.event.CaseworkerScheduleCase.CASEWORKER_SCHEDULE_CASE;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createBulkActionConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.CASEWORKER_AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
public class CaseworkerScheduleCaseTest {
    @Mock
    private ScheduleCaseService scheduleCaseService;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private CaseworkerScheduleCase scheduleCase;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<BulkActionCaseData, BulkActionState, UserRole> configBuilder = createBulkActionConfigBuilder();

        scheduleCase.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_SCHEDULE_CASE);
    }

    @Test
    void shouldSuccessfullyUpdateCasesInBulkWithCourtHearingDetails() {
        final CaseDetails<BulkActionCaseData, BulkActionState> details = new CaseDetails<>();
        details.setData(BulkActionCaseData.builder().build());
        details.setId(1L);

        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(CASEWORKER_AUTH_TOKEN);

        doNothing().when(scheduleCaseService).updateCourtHearingDetailsForCasesInBulk(details, CASEWORKER_AUTH_TOKEN);

        SubmittedCallbackResponse submittedCallbackResponse = scheduleCase.submitted(details, details);

        assertThat(submittedCallbackResponse).isNotNull();
        verify(scheduleCaseService).updateCourtHearingDetailsForCasesInBulk(details, CASEWORKER_AUTH_TOKEN);
    }

    @Test
    void shouldNotPopulateErrorMessageWhenHearingDateIsInFutureAndAboutToSubmitIsTriggered() {
        final CaseDetails<BulkActionCaseData, BulkActionState> details = new CaseDetails<>();
        details.setData(BulkActionCaseData
            .builder()
            .dateAndTimeOfHearing(LocalDateTime.now().plusDays(5))
            .build()
        );
        details.setId(1L);

        AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> response = scheduleCase.aboutToSubmit(details, details);

        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldNPopulateErrorMessageWhenHearingDateIsInPastAndAboutToSubmitIsTriggered() {
        final CaseDetails<BulkActionCaseData, BulkActionState> details = new CaseDetails<>();
        details.setData(BulkActionCaseData
            .builder()
            .dateAndTimeOfHearing(LocalDateTime.now().minusHours(5))
            .build()
        );
        details.setId(1L);

        AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> response = scheduleCase.aboutToSubmit(details, details);

        assertThat(response.getErrors()).containsExactly("Please enter hearing date and time which is in future");
    }
}
