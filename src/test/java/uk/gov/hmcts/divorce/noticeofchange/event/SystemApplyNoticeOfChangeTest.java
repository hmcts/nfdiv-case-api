package uk.gov.hmcts.divorce.noticeofchange.events;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.noticeofchange.client.AssignCaseAccessClient;
import uk.gov.hmcts.divorce.noticeofchange.event.SystemApplyNoticeOfChange;
import uk.gov.hmcts.divorce.noticeofchange.model.AcaRequest;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.noticeofchange.event.SystemApplyNoticeOfChange.NOTICE_OF_CHANGE_APPLIED;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
class SystemApplyNoticeOfChangeTest {

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private IdamService idamService;

    @Mock
    private AssignCaseAccessClient assignCaseAccessClient;

    @InjectMocks
    private SystemApplyNoticeOfChange systemApplyNoticeOfChange;

    @Test
    public void configure() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        systemApplyNoticeOfChange.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(NOTICE_OF_CHANGE_APPLIED);
    }

    @Test
    public void shouldApplyNoticeOfChangeByDelegatingToAssignCaseAccessClient() {
        final var details = new CaseDetails<CaseData, State>();
        final var beforeDetails = new CaseDetails<CaseData, State>();
        final User systemUser = mock(User.class);
        final AcaRequest acaRequest = AcaRequest.acaRequest(details);

        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(systemUser);
        when(systemUser.getAuthToken()).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        systemApplyNoticeOfChange.submitted(details, beforeDetails);

        verify(assignCaseAccessClient).applyNoticeOfChange(
            TEST_AUTHORIZATION_TOKEN, TEST_SERVICE_AUTH_TOKEN, acaRequest
        );
    }
}
