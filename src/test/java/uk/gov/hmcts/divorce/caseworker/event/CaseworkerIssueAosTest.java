package uk.gov.hmcts.divorce.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.divorce.caseworker.service.notification.NoticeOfProceedingsNotification;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.State;
import uk.gov.hmcts.divorce.common.model.UserRole;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerIssueAos.CASEWORKER_ISSUE_AOS;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class CaseworkerIssueAosTest {

    @Mock
    private NoticeOfProceedingsNotification noticeOfProceedingsNotification;

    @InjectMocks
    private CaseworkerIssueAos caseworkerIssueAos;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final Set<State> stateSet = Set.of(State.class.getEnumConstants());
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = new ConfigBuilderImpl<>(CaseData.class, stateSet);

        caseworkerIssueAos.configure(configBuilder);

        assertThat(configBuilder.getEvents())
            .extracting(Event::getId)
            .contains(CASEWORKER_ISSUE_AOS);
    }

    @Test
    void shouldSendNoticeOfProceedings() {

        final var caseData = mock(CaseData.class);
        final var caseDetails = CaseDetails.<CaseData, State>builder()
            .id(TEST_CASE_ID)
            .state(State.AwaitingAos)
            .data(caseData)
            .build();

        final SubmittedCallbackResponse submitted = caseworkerIssueAos.submitted(caseDetails, null);

        assertThat(submitted).isNotNull();
        verify(noticeOfProceedingsNotification).send(caseData, TEST_CASE_ID);
    }
}
