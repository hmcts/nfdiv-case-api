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
import uk.gov.hmcts.divorce.bulkaction.service.BulkCaseProcessingService;
import uk.gov.hmcts.divorce.bulkaction.task.BulkCaseCaseTaskFactory;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.User;

import javax.servlet.http.HttpServletRequest;

import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.bulkaction.ccd.event.CaseworkerDropCase.CASEWORKER_DROP_CASE;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemRemoveBulkCase.SYSTEM_REMOVE_BULK_CASE;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createBulkActionConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.CASEWORKER_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
public class CaseworkerDropCaseTest {
    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private IdamService idamService;

    @Mock
    private BulkCaseProcessingService bulkCaseProcessingService;

    @Mock
    private BulkCaseCaseTaskFactory bulkCaseCaseTaskFactory;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private CaseworkerDropCase caseworkerDropCase;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<BulkActionCaseData, BulkActionState, UserRole> configBuilder = createBulkActionConfigBuilder();

        caseworkerDropCase.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_DROP_CASE);
    }

    @Test
    void shouldSuccessfullyUnlinkCasesInBulkCase() {
        final CaseDetails<BulkActionCaseData, BulkActionState> details = new CaseDetails<>();
        details.setId(1L);

        final var user = mock(User.class);
        final var caseTask = mock(CaseTask.class);

        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(CASEWORKER_AUTH_TOKEN);
        when(idamService.retrieveUser(CASEWORKER_AUTH_TOKEN)).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(bulkCaseCaseTaskFactory.getCaseTask(details, SYSTEM_REMOVE_BULK_CASE)).thenReturn(caseTask);

        doNothing().when(bulkCaseProcessingService).updateAllBulkCases(
            details,
            SYSTEM_REMOVE_BULK_CASE,
            caseTask,
            user,
            TEST_SERVICE_AUTH_TOKEN
        );

        caseworkerDropCase.submitted(details, details);

        verify(bulkCaseProcessingService).updateAllBulkCases(
            details,
            SYSTEM_REMOVE_BULK_CASE,
            caseTask,
            user,
            TEST_SERVICE_AUTH_TOKEN
        );
    }
}
