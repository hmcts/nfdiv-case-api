package uk.gov.hmcts.divorce.systemupdate.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.systemupdate.convert.CaseDetailsConverter;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionCaseTypeConfig.CASE_TYPE;
import static uk.gov.hmcts.divorce.bulkaction.ccd.event.CreateBulkList.CREATE_BULK_LIST;
import static uk.gov.hmcts.divorce.divorcecase.NoFaultDivorce.JURISDICTION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_USER_USER_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.feignException;

@ExtendWith(MockitoExtension.class)
class CcdCreateServiceTest {

    private static final String DIVORCE_CASE_SUBMISSION_EVENT_SUMMARY = "No Fault Divorce case submission event";
    private static final String DIVORCE_CASE_SUBMISSION_EVENT_DESCRIPTION = "Submitting No Fault Divorce Case Event";

    @Mock
    private CoreCaseDataApi coreCaseDataApi;

    @Mock
    private CcdCaseDataContentProvider ccdCaseDataContentProvider;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @InjectMocks
    private CcdCreateService ccdCreateService;

    @Test
    void shouldSubmitEventForCaseAsSystemUpdateUser() {

        final User user = systemUpdateUser();
        final Map<String, Object> caseData = new HashMap<>();
        final CaseDetails caseDetails = getCaseDetails(caseData);
        final StartEventResponse startEventResponse = getStartEventResponse();
        final CaseDataContent caseDataContent = mock(CaseDataContent.class);

        when(coreCaseDataApi.startForCaseworker(
                SYSTEM_UPDATE_AUTH_TOKEN,
                SERVICE_AUTHORIZATION,
                SYSTEM_USER_USER_ID,
                JURISDICTION,
                CASE_TYPE,
                CREATE_BULK_LIST
            )
        ).thenReturn(startEventResponse);

        when(ccdCaseDataContentProvider
            .createCaseDataContent(
                startEventResponse,
                DIVORCE_CASE_SUBMISSION_EVENT_SUMMARY,
                DIVORCE_CASE_SUBMISSION_EVENT_DESCRIPTION,
                caseData
            )
        ).thenReturn(caseDataContent);

        ccdCreateService.createBulkCase(caseDetails, user, SERVICE_AUTHORIZATION);

        verify(coreCaseDataApi).submitForCaseworker(
            SYSTEM_UPDATE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            SYSTEM_USER_USER_ID,
            JURISDICTION,
            CASE_TYPE,
            true,
            caseDataContent
        );
    }

    @Test
    void shouldThrowCcdManagementExceptionIfSubmitEventFails() {

        final User user = systemUpdateUser();
        final Map<String, Object> caseData = new HashMap<>();
        final CaseDetails caseDetails = getCaseDetails(caseData);
        final StartEventResponse startEventResponse = getStartEventResponse();
        final CaseDataContent caseDataContent = mock(CaseDataContent.class);

        when(coreCaseDataApi.startForCaseworker(
                SYSTEM_UPDATE_AUTH_TOKEN,
                SERVICE_AUTHORIZATION,
                SYSTEM_USER_USER_ID,
                JURISDICTION,
                CASE_TYPE,
                CREATE_BULK_LIST
            )
        ).thenReturn(startEventResponse);

        when(ccdCaseDataContentProvider
            .createCaseDataContent(
                startEventResponse,
                DIVORCE_CASE_SUBMISSION_EVENT_SUMMARY,
                DIVORCE_CASE_SUBMISSION_EVENT_DESCRIPTION,
                caseData
            )
        ).thenReturn(caseDataContent);

        doThrow(feignException(422, "some error"))
            .when(coreCaseDataApi).submitForCaseworker(
                SYSTEM_UPDATE_AUTH_TOKEN,
                SERVICE_AUTHORIZATION,
                SYSTEM_USER_USER_ID,
                JURISDICTION,
                CASE_TYPE,
                true,
                caseDataContent
            );

        final CcdManagementException exception = assertThrows(
            CcdManagementException.class,
            () -> ccdCreateService.createBulkCase(caseDetails, user, SERVICE_AUTHORIZATION));

        assertThat(exception.getMessage())
            .contains("Bulk case creation failed");
    }

    private CaseDetails getCaseDetails(final Map<String, Object> caseData) {
        return CaseDetails.builder()
            .id(TEST_CASE_ID)
            .data(caseData)
            .build();
    }

    private StartEventResponse getStartEventResponse() {
        return StartEventResponse.builder()
            .eventId(CREATE_BULK_LIST)
            .token("startEventToken")
            .build();
    }

    private User systemUpdateUser() {
        return new User(
            SYSTEM_UPDATE_AUTH_TOKEN,
            UserDetails
                .builder()
                .id(SYSTEM_USER_USER_ID)
                .build()
        );
    }
}
