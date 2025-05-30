package uk.gov.hmcts.divorce.systemupdate.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionCaseTypeConfig;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.task.PronounceCasesTask;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.convert.CaseDetailsConverter;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.bulkaction.ccd.event.CreateBulkList.CREATE_BULK_LIST;
import static uk.gov.hmcts.divorce.bulkaction.ccd.event.SystemRemoveFailedCases.SYSTEM_REMOVE_FAILED_CASES;
import static uk.gov.hmcts.divorce.divorcecase.NoFaultDivorce.JURISDICTION;
import static uk.gov.hmcts.divorce.divorcecase.NoFaultDivorce.getCaseType;
import static uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderCourt.BIRMINGHAM;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_USER_USER_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.feignException;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CcdUpdateServiceIT {

    private static final String DIVORCE_CASE_SUBMISSION_EVENT_SUMMARY = "No Fault Divorce case submission event";
    private static final String DIVORCE_CASE_SUBMISSION_EVENT_DESCRIPTION = "Submitting No Fault Divorce Case Event";

    @MockitoBean
    private CoreCaseDataApi coreCaseDataApi;

    @MockitoBean
    private CcdCaseDataContentProvider ccdCaseDataContentProvider;

    @MockitoBean
    private CaseDetailsConverter caseDetailsConverter;

    @MockitoBean
    private CaseDetailsUpdater caseDetailsUpdater;

    @MockitoBean
    private BulkCaseDetailsUpdater bulkCaseDetailsUpdater;

    @MockitoBean
    private PronounceCasesTask bulkCaseTask;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CcdUpdateService ccdUpdateService;

    @Test
    void shouldInvokeCcdMaximumThreeTimesWhenSubmitEventFailsForGivenCaseIdString() {

        final User user = systemUpdateUser();
        final StartEventResponse startEventResponse = getStartEventResponse();
        final CaseDataContent caseDataContent = mock(CaseDataContent.class);
        final uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> caseDetails = new uk.gov.hmcts.ccd.sdk.api.CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();
        caseDetails.setData(caseData);

        when(coreCaseDataApi
            .startEventForCaseWorker(
                SYSTEM_UPDATE_AUTH_TOKEN,
                SERVICE_AUTHORIZATION,
                SYSTEM_USER_USER_ID,
                JURISDICTION,
                getCaseType(),
                "1",
                CREATE_BULK_LIST
            )
        ).thenReturn(startEventResponse);

        when(caseDetailsUpdater.updateCaseData(any(CaseTask.class), eq(startEventResponse))).thenReturn(caseDetails);
        when(ccdCaseDataContentProvider
            .createCaseDataContent(
                startEventResponse,
                DIVORCE_CASE_SUBMISSION_EVENT_SUMMARY,
                DIVORCE_CASE_SUBMISSION_EVENT_DESCRIPTION,
                caseData
            )
        ).thenReturn(caseDataContent);

        doThrow(feignException(409, "some error"))
            .when(coreCaseDataApi).submitEventForCaseWorker(
                SYSTEM_UPDATE_AUTH_TOKEN,
                SERVICE_AUTHORIZATION,
                SYSTEM_USER_USER_ID,
                JURISDICTION,
                getCaseType(),
                "1",
                true,
                caseDataContent
            );

        assertThrows(
            CcdManagementException.class,
            () -> ccdUpdateService.submitEventWithRetry(
                "1",
                CREATE_BULK_LIST,
                details -> details,
                user,
                SERVICE_AUTHORIZATION));

        verify(ccdCaseDataContentProvider, times(3))
            .createCaseDataContent(
                startEventResponse,
                DIVORCE_CASE_SUBMISSION_EVENT_SUMMARY,
                DIVORCE_CASE_SUBMISSION_EVENT_DESCRIPTION,
                caseData);
    }

    @Test
    void shouldInvokeCcdMaximumThreeTimesWhenSubmitEventFailsForUpdateBulkCase() {
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
                SYSTEM_REMOVE_FAILED_CASES
            )
        ).thenReturn(startEventResponse);

        final uk.gov.hmcts.ccd.sdk.api.CaseDetails<BulkActionCaseData, BulkActionState> bulkCaseDetails =
            new uk.gov.hmcts.ccd.sdk.api.CaseDetails<>();
        final var bulkActionCaseData = BulkActionCaseData
            .builder()
            .court(BIRMINGHAM)
            .build();
        bulkCaseDetails.setData(bulkActionCaseData);
        when(bulkCaseDetailsUpdater.updateCaseData(bulkCaseTask, startEventResponse)).thenReturn(bulkCaseDetails);

        when(ccdCaseDataContentProvider
            .createCaseDataContent(
                startEventResponse,
                DIVORCE_CASE_SUBMISSION_EVENT_SUMMARY,
                DIVORCE_CASE_SUBMISSION_EVENT_DESCRIPTION,
                bulkActionCaseData
            )
        ).thenReturn(caseDataContent);

        doThrow(feignException(422, "some error"))
            .when(coreCaseDataApi).submitEventForCaseWorker(
                SYSTEM_UPDATE_AUTH_TOKEN,
                SERVICE_AUTHORIZATION,
                SYSTEM_USER_USER_ID,
                JURISDICTION,
                BulkActionCaseTypeConfig.getCaseType(),
                TEST_CASE_ID.toString(),
                true,
                caseDataContent
            );

        final CcdManagementException exception = assertThrows(
            CcdManagementException.class,
            () -> ccdUpdateService.updateBulkCaseWithRetries(
                bulkCaseTask,
                SYSTEM_REMOVE_FAILED_CASES,
                user,
                SERVICE_AUTHORIZATION,
                TEST_CASE_ID
            )
        );

        verify(ccdCaseDataContentProvider, times(3))
            .createCaseDataContent(
                startEventResponse,
                DIVORCE_CASE_SUBMISSION_EVENT_SUMMARY,
                DIVORCE_CASE_SUBMISSION_EVENT_DESCRIPTION,
                bulkActionCaseData);
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

    private StartEventResponse getStartEventResponse() {
        return StartEventResponse.builder()
            .eventId(CREATE_BULK_LIST)
            .token("startEventToken")
            .caseDetails(CaseDetails.builder().data(new HashMap<>()).build())
            .build();
    }
}
