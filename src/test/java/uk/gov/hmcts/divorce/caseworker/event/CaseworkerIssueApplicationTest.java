package uk.gov.hmcts.divorce.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.caseworker.service.IssueApplicationService;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.State;
import uk.gov.hmcts.divorce.common.model.UserRole;

import java.util.Set;
import javax.servlet.http.HttpServletRequest;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerIssueApplication.ISSUE_APPLICATION;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class CaseworkerIssueApplicationTest {

    @Mock
    private IssueApplicationService issueApplicationService;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private CaseworkerIssueApplication caseworkerIssueApplication;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final Set<State> stateSet = Set.of(State.class.getEnumConstants());
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = new ConfigBuilderImpl<>(CaseData.class, stateSet);

        caseworkerIssueApplication.configure(configBuilder);

        assertThat(configBuilder.getEvents().get(0).getId(), is(ISSUE_APPLICATION));
    }

    @Test
    void shouldCallIssueApplicationServiceAndReturnCaseData() {

        final var auth = "authorization";
        final var caseData = caseData();
        final var expectedCaseData = CaseData.builder().build();

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(1L);
        details.setCreatedDate(LOCAL_DATE_TIME);

        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(auth);
        when(issueApplicationService
            .aboutToSubmit(
                caseData,
                details.getId(),
                details.getCreatedDate().toLocalDate(),
                auth))
            .thenReturn(expectedCaseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerIssueApplication.aboutToSubmit(details, null);

        assertThat(response.getData(), is(expectedCaseData));

        verify(issueApplicationService).aboutToSubmit(
            caseData,
            details.getId(),
            details.getCreatedDate().toLocalDate(),
            auth);

        verifyNoMoreInteractions(httpServletRequest, issueApplicationService);
    }
}
