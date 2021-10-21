package uk.gov.hmcts.divorce.systemupdate.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionCaseTypeConfig;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.systemupdate.convert.CaseDetailsConverter;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.bulkaction.ccd.event.SystemRemoveFailedCases.SYSTEM_REMOVE_FAILED_CASES;
import static uk.gov.hmcts.divorce.divorcecase.NoFaultDivorce.CASE_TYPE;
import static uk.gov.hmcts.divorce.divorcecase.NoFaultDivorce.JURISDICTION;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemProgressHeldCase.SYSTEM_PROGRESS_HELD_CASE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.CASEWORKER_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.CASEWORKER_USER_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_USER_USER_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.feignException;

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
    void shouldSubmitEventForCaseAsTheCaseworker() {

        final User user = getCaseworkerDetails();
        final Map<String, Object> caseData = new HashMap<>();
        final CaseDetails caseDetails = getCaseDetails(caseData);
        final StartEventResponse startEventResponse = getStartEventResponse();
        final CaseDataContent caseDataContent = mock(CaseDataContent.class);

        when(coreCaseDataApi
            .startEventForCaseWorker(
                CASEWORKER_AUTH_TOKEN,
                SERVICE_AUTHORIZATION,
                CASEWORKER_USER_ID,
                JURISDICTION,
                CASE_TYPE,
                TEST_CASE_ID.toString(),
                SYSTEM_PROGRESS_HELD_CASE))
            .thenReturn(startEventResponse);

        when(ccdCaseDataContentProvider
            .createCaseDataContent(
                startEventResponse,
                DIVORCE_CASE_SUBMISSION_EVENT_SUMMARY,
                DIVORCE_CASE_SUBMISSION_EVENT_DESCRIPTION,
                caseData))
            .thenReturn(caseDataContent);

        ccdUpdateService.submitEvent(caseDetails, SYSTEM_PROGRESS_HELD_CASE, user, SERVICE_AUTHORIZATION);

        verify(coreCaseDataApi).submitEventForCaseWorker(
            CASEWORKER_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            CASEWORKER_USER_ID,
            JURISDICTION,
            CASE_TYPE,
            TEST_CASE_ID.toString(),
            true,
            caseDataContent);
    }

    @Test
    void shouldSubmitEventForUpdateBulkCaseAsSystemUpdateUser() {

        final User user = systemUpdateUser();
        final Map<String, Object> caseData = new HashMap<>();
        final CaseDetails caseDetails = getCaseDetails(caseData);
        final StartEventResponse startEventResponse = getStartEventResponse();
        final CaseDataContent caseDataContent = mock(CaseDataContent.class);

        when(coreCaseDataApi
            .startEventForCaseWorker(
                SYSTEM_UPDATE_AUTH_TOKEN,
                SERVICE_AUTHORIZATION,
                SYSTEM_USER_USER_ID,
                JURISDICTION,
                BulkActionCaseTypeConfig.CASE_TYPE,
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

        ccdUpdateService.updateBulkCaseWithRetries(caseDetails, SYSTEM_REMOVE_FAILED_CASES, user, SERVICE_AUTHORIZATION, TEST_CASE_ID);

        verify(coreCaseDataApi).submitEventForCaseWorker(
            SYSTEM_UPDATE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            SYSTEM_USER_USER_ID,
            JURISDICTION,
            BulkActionCaseTypeConfig.CASE_TYPE,
            TEST_CASE_ID.toString(),
            true,
            caseDataContent);
    }

    @Test
    void shouldThrowCcdManagementExceptionIfSubmitEventFails() {

        final User user = getCaseworkerDetails();
        final Map<String, Object> caseData = new HashMap<>();
        final CaseDetails caseDetails = getCaseDetails(caseData);
        final StartEventResponse startEventResponse = getStartEventResponse();

        when(coreCaseDataApi
            .startEventForCaseWorker(
                CASEWORKER_AUTH_TOKEN,
                SERVICE_AUTHORIZATION,
                CASEWORKER_USER_ID,
                JURISDICTION,
                CASE_TYPE,
                TEST_CASE_ID.toString(),
                SYSTEM_PROGRESS_HELD_CASE))
            .thenReturn(startEventResponse);

        doThrow(feignException(422, "A reason")).when(ccdCaseDataContentProvider)
            .createCaseDataContent(
                startEventResponse,
                DIVORCE_CASE_SUBMISSION_EVENT_SUMMARY,
                DIVORCE_CASE_SUBMISSION_EVENT_DESCRIPTION,
                caseData);

        final CcdManagementException exception = assertThrows(
            CcdManagementException.class,
            () -> ccdUpdateService.submitEvent(caseDetails, SYSTEM_PROGRESS_HELD_CASE, user, SERVICE_AUTHORIZATION));

        assertThat(exception.getMessage())
            .contains(format("Submit Event Failed for Case ID: %s, Event ID: %s", TEST_CASE_ID, SYSTEM_PROGRESS_HELD_CASE));
    }

    @Test
    void shouldThrowCcdConflictExceptionIfSubmitEventFailsWithStatusConflict() {

        final User user = getCaseworkerDetails();
        final Map<String, Object> caseData = new HashMap<>();
        final CaseDetails caseDetails = getCaseDetails(caseData);
        final StartEventResponse startEventResponse = getStartEventResponse();

        when(coreCaseDataApi
            .startEventForCaseWorker(
                CASEWORKER_AUTH_TOKEN,
                SERVICE_AUTHORIZATION,
                CASEWORKER_USER_ID,
                JURISDICTION,
                CASE_TYPE,
                TEST_CASE_ID.toString(),
                SYSTEM_PROGRESS_HELD_CASE))
            .thenReturn(startEventResponse);

        doThrow(feignException(409, "A reason")).when(ccdCaseDataContentProvider)
            .createCaseDataContent(
                startEventResponse,
                DIVORCE_CASE_SUBMISSION_EVENT_SUMMARY,
                DIVORCE_CASE_SUBMISSION_EVENT_DESCRIPTION,
                caseData);

        final CcdConflictException exception = assertThrows(
            CcdConflictException.class,
            () -> ccdUpdateService.submitEvent(caseDetails, SYSTEM_PROGRESS_HELD_CASE, user, SERVICE_AUTHORIZATION));

        assertThat(exception.getMessage())
            .contains(format("Submit Event Failed for Case ID: %s, Event ID: %s", TEST_CASE_ID, SYSTEM_PROGRESS_HELD_CASE));
    }

    @Test
    void shouldSubmitEventForCaseAsTheCaseworkerForCaseDetailsModel() {

        final uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> caseDetails = new uk.gov.hmcts.ccd.sdk.api.CaseDetails<>();
        caseDetails.setData(CaseData.builder().build());

        final User user = getCaseworkerDetails();
        final Map<String, Object> caseData = new HashMap<>();
        final CaseDetails reformCaseDetails = getCaseDetails(caseData);
        final StartEventResponse startEventResponse = getStartEventResponse();
        final CaseDataContent caseDataContent = mock(CaseDataContent.class);

        when(caseDetailsConverter.convertToReformModel(caseDetails)).thenReturn(reformCaseDetails);
        when(coreCaseDataApi
            .startEventForCaseWorker(
                CASEWORKER_AUTH_TOKEN,
                SERVICE_AUTHORIZATION,
                CASEWORKER_USER_ID,
                JURISDICTION,
                CASE_TYPE,
                TEST_CASE_ID.toString(),
                SYSTEM_PROGRESS_HELD_CASE))
            .thenReturn(startEventResponse);

        when(ccdCaseDataContentProvider
            .createCaseDataContent(
                startEventResponse,
                DIVORCE_CASE_SUBMISSION_EVENT_SUMMARY,
                DIVORCE_CASE_SUBMISSION_EVENT_DESCRIPTION,
                caseData))
            .thenReturn(caseDataContent);

        ccdUpdateService.submitEvent(caseDetails, SYSTEM_PROGRESS_HELD_CASE, user, SERVICE_AUTHORIZATION);

        verify(coreCaseDataApi).submitEventForCaseWorker(
            CASEWORKER_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            CASEWORKER_USER_ID,
            JURISDICTION,
            CASE_TYPE,
            TEST_CASE_ID.toString(),
            true,
            caseDataContent);
    }

    private CaseDetails getCaseDetails(final Map<String, Object> caseData) {
        return CaseDetails.builder()
            .id(TEST_CASE_ID)
            .data(caseData)
            .build();
    }

    private StartEventResponse getStartEventResponse() {
        return StartEventResponse.builder()
            .eventId(SYSTEM_PROGRESS_HELD_CASE)
            .token("startEventToken")
            .build();
    }

    private User getCaseworkerDetails() {
        return new User(
            CASEWORKER_AUTH_TOKEN,
            UserDetails.builder()
                .id(CASEWORKER_USER_ID)
                .build());
    }

    private User systemUpdateUser() {
        return new User(
            SYSTEM_UPDATE_AUTH_TOKEN,
            UserDetails.builder()
                .id(SYSTEM_USER_USER_ID)
                .build());
    }
}
