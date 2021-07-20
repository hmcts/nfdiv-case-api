package uk.gov.hmcts.divorce.systemupdate.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
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
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerIssueAos.CASEWORKER_ISSUE_AOS;
import static uk.gov.hmcts.divorce.divorcecase.NoFaultDivorce.CASE_TYPE;
import static uk.gov.hmcts.divorce.divorcecase.NoFaultDivorce.JURISDICTION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.CASEWORKER_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.CASEWORKER_USER_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.feignException;

@ExtendWith(MockitoExtension.class)
class CcdUpdateServiceTest {

    private static final String DIVORCE_CASE_SUBMISSION_EVENT_SUMMARY = "No Fault Divorce case submission event";
    private static final String DIVORCE_CASE_SUBMISSION_EVENT_DESCRIPTION = "Submitting No Fault Divorce Case Event";

    @Mock
    private CoreCaseDataApi coreCaseDataApi;

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private CcdCaseDataContentProvider ccdCaseDataContentProvider;

    @InjectMocks
    private CcdUpdateService ccdUpdateService;

    @Test
    void shouldSubmitEventForCaseAsTheCaseworker() {

        final User caseworkerDetails = getCaseworkerDetails();
        final Map<String, Object> caseData = new HashMap<>();
        final CaseDetails caseDetails = getCaseDetails(caseData);
        final StartEventResponse startEventResponse = getStartEventResponse();
        final CaseDataContent caseDataContent = mock(CaseDataContent.class);

        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(caseworkerDetails);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);

        when(coreCaseDataApi
            .startEventForCaseWorker(
                CASEWORKER_AUTH_TOKEN,
                SERVICE_AUTHORIZATION,
                CASEWORKER_USER_ID,
                JURISDICTION,
                CASE_TYPE,
                TEST_CASE_ID.toString(),
                CASEWORKER_ISSUE_AOS))
            .thenReturn(startEventResponse);

        when(ccdCaseDataContentProvider
            .createCaseDataContent(
                startEventResponse,
                DIVORCE_CASE_SUBMISSION_EVENT_SUMMARY,
                DIVORCE_CASE_SUBMISSION_EVENT_DESCRIPTION,
                caseData))
            .thenReturn(caseDataContent);

        ccdUpdateService.submitEvent(caseDetails, CASEWORKER_ISSUE_AOS);

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
    void shouldThrowCcdManagementExceptionIfSubmitEventFails() {

        final User caseworkerDetails = getCaseworkerDetails();
        final Map<String, Object> caseData = new HashMap<>();
        final CaseDetails caseDetails = getCaseDetails(caseData);
        final StartEventResponse startEventResponse = getStartEventResponse();

        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(caseworkerDetails);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);

        when(coreCaseDataApi
            .startEventForCaseWorker(
                CASEWORKER_AUTH_TOKEN,
                SERVICE_AUTHORIZATION,
                CASEWORKER_USER_ID,
                JURISDICTION,
                CASE_TYPE,
                TEST_CASE_ID.toString(),
                CASEWORKER_ISSUE_AOS))
            .thenReturn(startEventResponse);

        doThrow(feignException(422, "A reason")).when(ccdCaseDataContentProvider)
            .createCaseDataContent(
                startEventResponse,
                DIVORCE_CASE_SUBMISSION_EVENT_SUMMARY,
                DIVORCE_CASE_SUBMISSION_EVENT_DESCRIPTION,
                caseData);

        final CcdManagementException exception = assertThrows(
            CcdManagementException.class,
            () -> ccdUpdateService.submitEvent(caseDetails, CASEWORKER_ISSUE_AOS));

        assertThat(exception.getMessage())
            .contains(format("Submit Event Failed for Case ID: %s, Event ID: %s", TEST_CASE_ID, CASEWORKER_ISSUE_AOS));
    }

    @Test
    void shouldThrowCcdConflictExceptionIfSubmitEventFailsWithStatusConflict() {

        final User caseworkerDetails = getCaseworkerDetails();
        final Map<String, Object> caseData = new HashMap<>();
        final CaseDetails caseDetails = getCaseDetails(caseData);
        final StartEventResponse startEventResponse = getStartEventResponse();

        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(caseworkerDetails);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);

        when(coreCaseDataApi
            .startEventForCaseWorker(
                CASEWORKER_AUTH_TOKEN,
                SERVICE_AUTHORIZATION,
                CASEWORKER_USER_ID,
                JURISDICTION,
                CASE_TYPE,
                TEST_CASE_ID.toString(),
                CASEWORKER_ISSUE_AOS))
            .thenReturn(startEventResponse);

        doThrow(feignException(409, "A reason")).when(ccdCaseDataContentProvider)
            .createCaseDataContent(
                startEventResponse,
                DIVORCE_CASE_SUBMISSION_EVENT_SUMMARY,
                DIVORCE_CASE_SUBMISSION_EVENT_DESCRIPTION,
                caseData);

        final CcdConflictException exception = assertThrows(
            CcdConflictException.class,
            () -> ccdUpdateService.submitEvent(caseDetails, CASEWORKER_ISSUE_AOS));

        assertThat(exception.getMessage())
            .contains(format("Submit Event Failed for Case ID: %s, Event ID: %s", TEST_CASE_ID, CASEWORKER_ISSUE_AOS));
    }

    private CaseDetails getCaseDetails(final Map<String, Object> caseData) {
        return CaseDetails.builder()
            .id(TEST_CASE_ID)
            .data(caseData)
            .build();
    }

    private StartEventResponse getStartEventResponse() {
        return StartEventResponse.builder()
            .eventId(CASEWORKER_ISSUE_AOS)
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
}
