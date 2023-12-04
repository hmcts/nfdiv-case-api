package uk.gov.hmcts.divorce.systemupdate.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionCaseTypeConfig;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.convert.CaseDetailsConverter;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.bulkaction.ccd.event.SystemRemoveFailedCases.SYSTEM_REMOVE_FAILED_CASES;
import static uk.gov.hmcts.divorce.divorcecase.NoFaultDivorce.JURISDICTION;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemProgressHeldCase.SYSTEM_PROGRESS_HELD_CASE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.CASEWORKER_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.CASEWORKER_USER_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_USER_USER_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_ORG_ID;

@ExtendWith(MockitoExtension.class)
class CcdUpdateServiceTest {

    private static final String DIVORCE_CASE_SUBMISSION_EVENT_SUMMARY = "No Fault Divorce case submission event";
    private static final String DIVORCE_CASE_SUBMISSION_EVENT_DESCRIPTION = "Submitting No Fault Divorce Case Event";

    @Mock
    private CoreCaseDataApi coreCaseDataApi;

    @Mock
    private CcdCaseDataContentProvider ccdCaseDataContentProvider;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @InjectMocks
    private CcdUpdateService ccdUpdateService;

    @Test
    void shouldSubmitActionEvent() {

        final User user = systemUpdateUser();
        final Map<String, Object> caseData = new HashMap<>();
        final uk.gov.hmcts.ccd.sdk.api.CaseDetails<BulkActionCaseData, BulkActionState> caseDetails =
            new uk.gov.hmcts.ccd.sdk.api.CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(BulkActionCaseData.builder().build());

        final StartEventResponse startEventResponse = getStartEventResponse();
        final CaseDataContent caseDataContent = mock(CaseDataContent.class);

        when(coreCaseDataApi
            .startEventForCaseWorker(
                SYSTEM_UPDATE_AUTH_TOKEN,
                SERVICE_AUTHORIZATION,
                SYSTEM_USER_USER_ID,
                JURISDICTION,
                BulkActionCaseTypeConfig.getCaseType(),
                TEST_CASE_ID.toString(),
                SYSTEM_REMOVE_FAILED_CASES)
        )
            .thenReturn(startEventResponse);

        when(ccdCaseDataContentProvider
            .createCaseDataContent(
                startEventResponse,
                DIVORCE_CASE_SUBMISSION_EVENT_SUMMARY,
                DIVORCE_CASE_SUBMISSION_EVENT_DESCRIPTION,
                caseData))
            .thenReturn(caseDataContent);

        ccdUpdateService.submitBulkActionEvent(TEST_CASE_ID, SYSTEM_REMOVE_FAILED_CASES, user, SERVICE_AUTHORIZATION);

        verify(coreCaseDataApi).submitEventForCaseWorker(
            SYSTEM_UPDATE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            SYSTEM_USER_USER_ID,
            JURISDICTION,
            BulkActionCaseTypeConfig.getCaseType(),
            TEST_CASE_ID.toString(),
            true,
            caseDataContent);
    }

    @Test
    void shouldSubmitEventForUpdateBulkCaseAsSystemUpdateUser() {

        final User user = systemUpdateUser();
        final Map<String, Object> caseData = new HashMap<>();
        final StartEventResponse startEventResponse = getStartEventResponse();
        final CaseDataContent caseDataContent = mock(CaseDataContent.class);

        when(coreCaseDataApi
            .startEventForCaseWorker(
                SYSTEM_UPDATE_AUTH_TOKEN,
                SERVICE_AUTHORIZATION,
                SYSTEM_USER_USER_ID,
                JURISDICTION,
                BulkActionCaseTypeConfig.getCaseType(),
                TEST_CASE_ID.toString(),
                SYSTEM_REMOVE_FAILED_CASES)
        )
            .thenReturn(startEventResponse);

        when(ccdCaseDataContentProvider
            .createCaseDataContent(
                startEventResponse,
                DIVORCE_CASE_SUBMISSION_EVENT_SUMMARY,
                DIVORCE_CASE_SUBMISSION_EVENT_DESCRIPTION,
                caseData))
            .thenReturn(caseDataContent);

        ccdUpdateService.updateBulkCaseWithRetries(SYSTEM_REMOVE_FAILED_CASES, user, SERVICE_AUTHORIZATION, TEST_CASE_ID);

        verify(coreCaseDataApi).submitEventForCaseWorker(
            SYSTEM_UPDATE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            SYSTEM_USER_USER_ID,
            JURISDICTION,
            BulkActionCaseTypeConfig.getCaseType(),
            TEST_CASE_ID.toString(),
            true,
            caseDataContent);
    }

    @Test
    void shouldSubmitBulkActionEvent() {
        final User user = systemUpdateUser();
        final Map<String, Object> caseData = new HashMap<>();
        final uk.gov.hmcts.ccd.sdk.api.CaseDetails<BulkActionCaseData, BulkActionState> caseDetails =
            new uk.gov.hmcts.ccd.sdk.api.CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(BulkActionCaseData.builder().build());

        final StartEventResponse startEventResponse = getStartEventResponse();
        final CaseDataContent caseDataContent = mock(CaseDataContent.class);

        when(coreCaseDataApi
            .startEventForCaseWorker(
                SYSTEM_UPDATE_AUTH_TOKEN,
                SERVICE_AUTHORIZATION,
                SYSTEM_USER_USER_ID,
                JURISDICTION,
                BulkActionCaseTypeConfig.getCaseType(),
                TEST_CASE_ID.toString(),
                SYSTEM_REMOVE_FAILED_CASES)
        )
            .thenReturn(startEventResponse);

        when(ccdCaseDataContentProvider
            .createCaseDataContent(
                startEventResponse,
                DIVORCE_CASE_SUBMISSION_EVENT_SUMMARY,
                DIVORCE_CASE_SUBMISSION_EVENT_DESCRIPTION,
                caseData))
            .thenReturn(caseDataContent);

        ccdUpdateService.submitBulkActionEvent(TEST_CASE_ID, SYSTEM_REMOVE_FAILED_CASES, user, SERVICE_AUTHORIZATION);

        verify(coreCaseDataApi).submitEventForCaseWorker(
            SYSTEM_UPDATE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            SYSTEM_USER_USER_ID,
            JURISDICTION,
            BulkActionCaseTypeConfig.getCaseType(),
            TEST_CASE_ID.toString(),
            true,
            caseDataContent);
    }

    @Test
    public void shouldResetOrgsAssignedUsersToZeroSupplementaryDataOnCase() {
        ccdUpdateService.resetOrgAssignedUsersSupplementaryData(TEST_CASE_ID.toString(),
            CASEWORKER_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            TEST_ORG_ID);

        verify(coreCaseDataApi).submitSupplementaryData(
            CASEWORKER_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            TEST_CASE_ID.toString(),
            Map.of("supplementary_data_updates",
                Map.of("$set",
                    Map.of(
                        "orgs_assigned_users." + TEST_ORG_ID, "0",
                        "processed", true)))
        );
    }

    @Test
    public void shouldSetOrgsAssignedUsersSupplementaryDataOnCaseToNewValue() {
        ccdUpdateService.setOrgAssignedUsersSupplementaryData(TEST_CASE_ID.toString(),
            CASEWORKER_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            TEST_ORG_ID,
            "1");

        verify(coreCaseDataApi).submitSupplementaryData(
            CASEWORKER_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            TEST_CASE_ID.toString(),
            Map.of("supplementary_data_updates",
                Map.of("$set", Map.of(
                    "orgs_assigned_users." + TEST_ORG_ID, "1",
                    "processed", true)))
        );
    }

    @Test
    public void shouldIncrementOrgsAssignedUsersSupplementaryDataOnCaseToNewValue() {
        ccdUpdateService.incrementOrgAssignedUsersSupplementaryData(TEST_CASE_ID.toString(),
            CASEWORKER_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            TEST_ORG_ID);

        verify(coreCaseDataApi).submitSupplementaryData(
            CASEWORKER_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            TEST_CASE_ID.toString(),
            Map.of("supplementary_data_updates", Map.of("$inc", singletonMap("orgs_assigned_users." + TEST_ORG_ID, "1")))
        );
    }

    private StartEventResponse getStartEventResponse() {
        return StartEventResponse.builder()
            .eventId(SYSTEM_PROGRESS_HELD_CASE)
            .token("startEventToken")
            .caseDetails(getCaseDetails(new HashMap<>()))
            .build();
    }

    private User getCaseworkerDetails() {
        return new User(
            CASEWORKER_AUTH_TOKEN,
            UserInfo.builder()
                .uid(CASEWORKER_USER_ID)
                .build());
    }

    private CaseDetails getCaseDetails(final Map<String, Object> caseData) {
        return CaseDetails.builder()
            .id(TEST_CASE_ID)
            .data(caseData)
            .build();
    }

    private User systemUpdateUser() {
        return new User(
            SYSTEM_UPDATE_AUTH_TOKEN,
            UserInfo.builder()
                .uid(SYSTEM_USER_USER_ID)
                .build());
    }
}
