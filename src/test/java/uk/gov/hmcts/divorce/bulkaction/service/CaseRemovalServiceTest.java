package uk.gov.hmcts.divorce.bulkaction.service;

import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.task.BulkCaseCaseTaskFactory;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.User;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.bulkaction.ccd.event.SystemUpdateCase.SYSTEM_UPDATE_BULK_CASE;
import static uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderCourt.BIRMIGHAM;
import static uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderCourt.BURY_ST_EDMUNDS;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemRemoveBulkCase.SYSTEM_REMOVE_BULK_CASE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SYSTEM_AUTHORISATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getBulkListCaseDetailsListValue;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getCaseLinkListValue;

@ExtendWith(MockitoExtension.class)
public class CaseRemovalServiceTest {

    @Mock
    private BulkTriggerService bulkTriggerService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private CcdUpdateService ccdUpdateService;

    @Mock
    private BulkCaseCaseTaskFactory bulkCaseCaseTaskFactory;

    @Mock
    private IdamService idamService;

    @InjectMocks
    private CaseRemovalService caseRemovalService;

    @Test
    void shouldSuccessfullyRemoveAllCasesSelectedForRemoval() {
        var bulkActionCaseData = BulkActionCaseData
            .builder()
            .dateAndTimeOfHearing(LocalDateTime.of(2021, 11, 10, 0, 0, 0))
            .court(BURY_ST_EDMUNDS)
            .bulkListCaseDetails(List.of(getBulkListCaseDetailsListValue("1")))
            .casesAcceptedToListForHearing(List.of(getCaseLinkListValue("1")))
            .build();

        var casesToRemove = List.of(getBulkListCaseDetailsListValue("2"));
        var user = mock(User.class);

        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
        when(idamService.retrieveUser(TEST_SYSTEM_AUTHORISATION_TOKEN)).thenReturn(user);

        var caseTask = mock(CaseTask.class);
        when(bulkCaseCaseTaskFactory.getCaseTask(bulkActionCaseData, SYSTEM_REMOVE_BULK_CASE)).thenReturn(caseTask);

        when(bulkTriggerService.bulkTrigger(
            casesToRemove,
            SYSTEM_REMOVE_BULK_CASE,
            caseTask,
            user,
            SERVICE_AUTHORIZATION
        )).thenReturn(emptyList());

        var bulkActionCaseDetails = CaseDetails
            .<BulkActionCaseData, BulkActionState>builder()
            .data(bulkActionCaseData)
            .build();

        caseRemovalService.removeCases(bulkActionCaseDetails, casesToRemove, TEST_SYSTEM_AUTHORISATION_TOKEN);

        verify(bulkTriggerService).bulkTrigger(
            eq(casesToRemove),
            eq(SYSTEM_REMOVE_BULK_CASE),
            any(CaseTask.class),
            eq(user),
            eq(SERVICE_AUTHORIZATION)
        );

        verify(ccdUpdateService).submitBulkActionEvent(
            eq(bulkActionCaseDetails),
            eq(SYSTEM_UPDATE_BULK_CASE),
            eq(user),
            eq(SERVICE_AUTHORIZATION)
        );

        assertThat(bulkActionCaseDetails.getData().getCasesAcceptedToListForHearing()).hasSize(1);
        assertThat(bulkActionCaseDetails.getData().getCasesAcceptedToListForHearing()).contains(getCaseLinkListValue("1"));

        assertThat(bulkActionCaseDetails.getData().getCasesToBeRemoved()).isEmpty();
    }

    @Test
    void shouldSuccessfullyRemoveOnlySomeCasesSelectedForRemoval() {
        var bulkActionCaseData = BulkActionCaseData
            .builder()
            .dateAndTimeOfHearing(LocalDateTime.of(2021, 11, 10, 0, 0, 0))
            .court(BURY_ST_EDMUNDS)
            .bulkListCaseDetails(List.of(getBulkListCaseDetailsListValue("1")))
            .casesAcceptedToListForHearing(
                List.of(
                    getCaseLinkListValue("1")
                )
            )
            .build();

        var casesToRemove =
            List.of(
                getBulkListCaseDetailsListValue("2"),
                getBulkListCaseDetailsListValue("3"),
                getBulkListCaseDetailsListValue("4")
            );
        var user = mock(User.class);

        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
        when(idamService.retrieveUser(TEST_SYSTEM_AUTHORISATION_TOKEN)).thenReturn(user);

        var caseTask = mock(CaseTask.class);
        when(bulkCaseCaseTaskFactory.getCaseTask(bulkActionCaseData, SYSTEM_REMOVE_BULK_CASE)).thenReturn(caseTask);

        when(bulkTriggerService.bulkTrigger(
            casesToRemove,
            SYSTEM_REMOVE_BULK_CASE,
            caseTask,
            user,
            SERVICE_AUTHORIZATION
        )).thenReturn(singletonList(getBulkListCaseDetailsListValue("3")));

        var bulkActionCaseDetails = CaseDetails
            .<BulkActionCaseData, BulkActionState>builder()
            .data(bulkActionCaseData)
            .build();

        caseRemovalService.removeCases(bulkActionCaseDetails, casesToRemove, TEST_SYSTEM_AUTHORISATION_TOKEN);

        verify(bulkTriggerService).bulkTrigger(
            eq(casesToRemove),
            eq(SYSTEM_REMOVE_BULK_CASE),
            any(CaseTask.class),
            eq(user),
            eq(SERVICE_AUTHORIZATION)
        );

        verify(ccdUpdateService).submitBulkActionEvent(
            eq(bulkActionCaseDetails),
            eq(SYSTEM_UPDATE_BULK_CASE),
            eq(user),
            eq(SERVICE_AUTHORIZATION)
        );

        assertThat(bulkActionCaseDetails.getData().getCasesAcceptedToListForHearing()).hasSize(1);
        assertThat(bulkActionCaseDetails.getData().getBulkListCaseDetails())
            .contains(getBulkListCaseDetailsListValue("1"));

        assertThat(bulkActionCaseDetails.getData().getCasesToBeRemoved()).hasSize(1);
        assertThat(bulkActionCaseDetails.getData().getCasesToBeRemoved())
            .contains(getBulkListCaseDetailsListValue("3"));
    }

    @Test
    void noCasesSuccessfullyRemoved() {
        var bulkActionCaseData = BulkActionCaseData
            .builder()
            .dateAndTimeOfHearing(LocalDateTime.of(2021, 11, 10, 0, 0, 0))
            .court(BIRMIGHAM)
            .bulkListCaseDetails(List.of(getBulkListCaseDetailsListValue("1")))
            .casesAcceptedToListForHearing(
                List.of(
                    getCaseLinkListValue("1")
                )
            )
            .build();

        var casesToRemove =
            List.of(
                getBulkListCaseDetailsListValue("2"),
                getBulkListCaseDetailsListValue("3"),
                getBulkListCaseDetailsListValue("4")
            );
        var user = mock(User.class);

        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
        when(idamService.retrieveUser(TEST_SYSTEM_AUTHORISATION_TOKEN)).thenReturn(user);

        var caseTask = mock(CaseTask.class);
        when(bulkCaseCaseTaskFactory.getCaseTask(bulkActionCaseData, SYSTEM_REMOVE_BULK_CASE)).thenReturn(caseTask);

        when(bulkTriggerService.bulkTrigger(
            casesToRemove,
            SYSTEM_REMOVE_BULK_CASE,
            caseTask,
            user,
            SERVICE_AUTHORIZATION
        )).thenReturn(casesToRemove);

        var bulkActionCaseDetails = CaseDetails
            .<BulkActionCaseData, BulkActionState>builder()
            .data(bulkActionCaseData)
            .build();

        caseRemovalService.removeCases(bulkActionCaseDetails, casesToRemove, TEST_SYSTEM_AUTHORISATION_TOKEN);

        verify(bulkTriggerService).bulkTrigger(
            eq(casesToRemove),
            eq(SYSTEM_REMOVE_BULK_CASE),
            any(CaseTask.class),
            eq(user),
            eq(SERVICE_AUTHORIZATION)
        );

        verify(ccdUpdateService).submitBulkActionEvent(
            eq(bulkActionCaseDetails),
            eq(SYSTEM_UPDATE_BULK_CASE),
            eq(user),
            eq(SERVICE_AUTHORIZATION)
        );

        assertThat(bulkActionCaseDetails.getData().getCasesAcceptedToListForHearing()).hasSize(1);
        assertThat(bulkActionCaseDetails.getData().getBulkListCaseDetails())
            .contains(getBulkListCaseDetailsListValue("1"));

        assertThat(bulkActionCaseDetails.getData().getCasesToBeRemoved()).hasSize(3);
        assertThat(bulkActionCaseDetails.getData().getCasesToBeRemoved())
            .contains(getBulkListCaseDetailsListValue("2"));
        assertThat(bulkActionCaseDetails.getData().getCasesToBeRemoved())
            .contains(getBulkListCaseDetailsListValue("3"));
        assertThat(bulkActionCaseDetails.getData().getCasesToBeRemoved())
            .contains(getBulkListCaseDetailsListValue("4"));
    }

    @Test
    void shouldThrowExceptionIfCcdUpdateFails() {
        var bulkActionCaseData = BulkActionCaseData
            .builder()
            .dateAndTimeOfHearing(LocalDateTime.of(2021, 11, 10, 0, 0, 0))
            .court(BIRMIGHAM)
            .bulkListCaseDetails(List.of(getBulkListCaseDetailsListValue("1"), getBulkListCaseDetailsListValue("2")))
            .casesAcceptedToListForHearing(List.of(getCaseLinkListValue("1")))
            .build();

        var casesToProcess = List.of(getBulkListCaseDetailsListValue("2"));
        var user = mock(User.class);

        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
        when(idamService.retrieveUser(TEST_SYSTEM_AUTHORISATION_TOKEN)).thenReturn(user);

        var caseTask = mock(CaseTask.class);
        when(bulkCaseCaseTaskFactory.getCaseTask(bulkActionCaseData, SYSTEM_REMOVE_BULK_CASE)).thenReturn(caseTask);

        when(bulkTriggerService.bulkTrigger(
            casesToProcess,
            SYSTEM_REMOVE_BULK_CASE,
            caseTask,
            user,
            SERVICE_AUTHORIZATION
        )).thenReturn(emptyList());

        var bulkActionCaseDetails = CaseDetails
            .<BulkActionCaseData, BulkActionState>builder()
            .data(bulkActionCaseData)
            .build();

        doThrow(FeignException.class)
            .when(ccdUpdateService)
            .submitBulkActionEvent(
                eq(bulkActionCaseDetails),
                eq(SYSTEM_UPDATE_BULK_CASE),
                eq(user),
                eq(SERVICE_AUTHORIZATION)
            );

        try {
            caseRemovalService.removeCases(
                bulkActionCaseDetails,
                List.of(getBulkListCaseDetailsListValue("2")),
                TEST_SYSTEM_AUTHORISATION_TOKEN);
        } catch (Exception e) {
            fail("No exception should be thrown by removeCases");
        }
    }
}
