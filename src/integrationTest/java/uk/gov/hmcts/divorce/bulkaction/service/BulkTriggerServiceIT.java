package uk.gov.hmcts.divorce.bulkaction.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ccd.sdk.type.CaseLink;
import uk.gov.hmcts.divorce.bulkaction.data.BulkListCaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.divorce.systemupdate.service.CaseDetailsUpdater;
import uk.gov.hmcts.divorce.systemupdate.service.CcdCaseDataContentProvider;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.bulkaction.ccd.event.CreateBulkList.CREATE_BULK_LIST;
import static uk.gov.hmcts.divorce.divorcecase.NoFaultDivorce.CASE_TYPE;
import static uk.gov.hmcts.divorce.divorcecase.NoFaultDivorce.JURISDICTION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_USER_USER_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.feignException;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@DirtiesContext
public class BulkTriggerServiceIT {

    private static final String DIVORCE_CASE_SUBMISSION_EVENT_SUMMARY = "No Fault Divorce case submission event";
    private static final String DIVORCE_CASE_SUBMISSION_EVENT_DESCRIPTION = "Submitting No Fault Divorce Case Event";

    @Autowired
    private BulkTriggerService bulkTriggerService;

    @MockBean
    private CoreCaseDataApi coreCaseDataApi;

    @MockBean
    private CcdCaseDataContentProvider ccdCaseDataContentProvider;

    @MockBean
    private CaseDetailsUpdater caseDetailsUpdater;

    @Test
    void shouldProcessAllBulkCasesAndReturnEmptyErrorsList() {

        final User user = systemUpdateUser();
        final StartEventResponse startEventResponse = getStartEventResponse();
        final CaseDataContent caseDataContent = mock(CaseDataContent.class);
        final uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> caseDetails = new uk.gov.hmcts.ccd.sdk.api.CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();
        caseDetails.setData(caseData);

        addStartEventMockForCaseIds(startEventResponse, "1", "2", "3", "4", "5");

        when(caseDetailsUpdater.updateCaseData(any(CaseTask.class), eq(startEventResponse))).thenReturn(caseDetails);
        when(ccdCaseDataContentProvider
            .createCaseDataContent(
                startEventResponse,
                DIVORCE_CASE_SUBMISSION_EVENT_SUMMARY,
                DIVORCE_CASE_SUBMISSION_EVENT_DESCRIPTION,
                caseData
            ))
            .thenReturn(caseDataContent);

        final List<BulkListCaseDetails> bulkListCaseDetails = getBulkListCaseDetailsForCaseIds("1", "2", "3", "4", "5");

        final List<BulkListCaseDetails> errors = bulkTriggerService.bulkTrigger(
            bulkListCaseDetails,
            "event Id",
            details -> details,
            user,
            SERVICE_AUTHORIZATION);

        assertThat(errors).isEmpty();

        verifySubmitEventForCaseIds(caseDataContent, "1", "2", "3", "4", "5");
    }

    @Test
    void shouldInvokeCcdMaximumThreeTimesWhenSubmitEventFails() {

        final User user = systemUpdateUser();
        final StartEventResponse startEventResponse = getStartEventResponse();
        final CaseDataContent caseDataContent = mock(CaseDataContent.class);
        final uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> caseDetails = new uk.gov.hmcts.ccd.sdk.api.CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();
        caseDetails.setData(caseData);

        addStartEventMockForCaseIds(startEventResponse, "1");

        when(caseDetailsUpdater.updateCaseData(any(CaseTask.class), eq(startEventResponse))).thenReturn(caseDetails);
        when(ccdCaseDataContentProvider
            .createCaseDataContent(
                startEventResponse,
                DIVORCE_CASE_SUBMISSION_EVENT_SUMMARY,
                DIVORCE_CASE_SUBMISSION_EVENT_DESCRIPTION,
                caseData))
            .thenReturn(caseDataContent);

        doThrow(feignException(409, "some error"))
            .when(coreCaseDataApi).submitEventForCaseWorker(
                SYSTEM_UPDATE_AUTH_TOKEN,
                SERVICE_AUTHORIZATION,
                SYSTEM_USER_USER_ID,
                JURISDICTION,
                CASE_TYPE,
                "1",
                true,
                caseDataContent
            );

        final List<BulkListCaseDetails> bulkListCaseDetails = getBulkListCaseDetailsForCaseIds("1");

        bulkTriggerService.bulkTrigger(
            bulkListCaseDetails,
            "event Id",
            details -> details,
            user,
            SERVICE_AUTHORIZATION);

        verify(coreCaseDataApi, times(3)).submitEventForCaseWorker(
            SYSTEM_UPDATE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            SYSTEM_USER_USER_ID,
            JURISDICTION,
            CASE_TYPE,
            "1",
            true,
            caseDataContent
        );
    }

    @Test
    void shouldCompleteAllCasesEvenIfOneFailsAndReturnFailedCases() {

        final User user = systemUpdateUser();
        final StartEventResponse startEventResponse = getStartEventResponse();
        final CaseDataContent caseDataContent = mock(CaseDataContent.class);
        final uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> caseDetails = new uk.gov.hmcts.ccd.sdk.api.CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();
        caseDetails.setData(caseData);

        addStartEventMockForCaseIds(startEventResponse, "1", "2", "3", "4", "5");

        when(caseDetailsUpdater.updateCaseData(any(CaseTask.class), eq(startEventResponse))).thenReturn(caseDetails);
        when(ccdCaseDataContentProvider
            .createCaseDataContent(
                startEventResponse,
                DIVORCE_CASE_SUBMISSION_EVENT_SUMMARY,
                DIVORCE_CASE_SUBMISSION_EVENT_DESCRIPTION,
                caseData))
            .thenReturn(caseDataContent);

        doThrow(feignException(409, "some error"))
            .when(coreCaseDataApi).submitEventForCaseWorker(
                SYSTEM_UPDATE_AUTH_TOKEN,
                SERVICE_AUTHORIZATION,
                SYSTEM_USER_USER_ID,
                JURISDICTION,
                CASE_TYPE,
                "3",
                true,
                caseDataContent
            );

        final List<BulkListCaseDetails> bulkListCaseDetails = getBulkListCaseDetailsForCaseIds("1", "2", "3", "4", "5");

        final List<BulkListCaseDetails> errors = bulkTriggerService.bulkTrigger(
            bulkListCaseDetails,
            "event Id",
            details -> details,
            user,
            SERVICE_AUTHORIZATION);

        assertThat(errors).hasSize(1);
        assertThat(errors.get(0).getCaseReference().getCaseReference()).isEqualTo("3");

        verifySubmitEventForCaseIds(caseDataContent, "1", "2", "4", "5");
        verify(coreCaseDataApi, times(3)).submitEventForCaseWorker(
            SYSTEM_UPDATE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            SYSTEM_USER_USER_ID,
            JURISDICTION,
            CASE_TYPE,
            "3",
            true,
            caseDataContent
        );
    }

    private List<BulkListCaseDetails> getBulkListCaseDetailsForCaseIds(final String... caseIds) {
        return Arrays.stream(caseIds)
            .map(this::getBulkListCaseDetails).collect(toList());
    }

    private void verifySubmitEventForCaseIds(final CaseDataContent caseDataContent, final String... caseIds) {
        for (String caseId : caseIds) {
            verify(coreCaseDataApi).submitEventForCaseWorker(
                SYSTEM_UPDATE_AUTH_TOKEN,
                SERVICE_AUTHORIZATION,
                SYSTEM_USER_USER_ID,
                JURISDICTION,
                CASE_TYPE,
                caseId,
                true,
                caseDataContent
            );
        }
    }

    private void addStartEventMockForCaseIds(final StartEventResponse startEventResponse, final String... caseIds) {
        for (String caseId : caseIds) {
            when(coreCaseDataApi
                .startEventForCaseWorker(
                    SYSTEM_UPDATE_AUTH_TOKEN,
                    SERVICE_AUTHORIZATION,
                    SYSTEM_USER_USER_ID,
                    JURISDICTION,
                    CASE_TYPE,
                    caseId,
                    "event Id"))
                .thenReturn(startEventResponse);
        }
    }

    private BulkListCaseDetails getBulkListCaseDetails(final String caseId) {
        return BulkListCaseDetails.builder().caseReference(CaseLink.builder().caseReference(caseId).build()).build();
    }

    private User systemUpdateUser() {
        return new User(
            SYSTEM_UPDATE_AUTH_TOKEN,
            UserDetails.builder()
                .id(SYSTEM_USER_USER_ID)
                .build());
    }

    private StartEventResponse getStartEventResponse() {
        return StartEventResponse.builder()
            .eventId(CREATE_BULK_LIST)
            .token("startEventToken")
            .caseDetails(getCaseDetails())
            .build();
    }

    private CaseDetails getCaseDetails() {

        final HashMap<String, Object> caseData = new HashMap<>();
        caseData.put("dueDate", null);

        return CaseDetails.builder()
            .data(caseData)
            .build();
    }
}
