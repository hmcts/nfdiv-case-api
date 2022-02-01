package uk.gov.hmcts.divorce.systemupdate.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.NoFaultDivorce.CASE_TYPE;
import static uk.gov.hmcts.divorce.divorcecase.NoFaultDivorce.JURISDICTION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.CASEWORKER_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.CASEWORKER_USER_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.feignException;

@ExtendWith(MockitoExtension.class)
public class CcdFetchCaseServiceTest {

    @Mock
    private CoreCaseDataApi coreCaseDataApi;

    @InjectMocks
    private CcdFetchCaseService ccdFetchCaseService;

    @Test
    void shouldReturnCaseDetailsIfValidCaseReferenceProvided() {
        final String caseId = String.valueOf(TEST_CASE_ID);
        final Map<String, Object> caseData = new HashMap<>();
        final CaseDetails caseDetails = CaseDetails.builder()
            .id(TEST_CASE_ID)
            .data(caseData)
            .build();
        final User user = new User(
            CASEWORKER_AUTH_TOKEN,
            UserDetails.builder()
                .id(CASEWORKER_USER_ID)
                .build());

        when(coreCaseDataApi.readForCaseWorker(
            CASEWORKER_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            CASEWORKER_USER_ID,
            JURISDICTION,
            CASE_TYPE,
            caseId
        )).thenReturn(caseDetails);

        CaseDetails response = ccdFetchCaseService.fetchCaseById(caseId, user, SERVICE_AUTHORIZATION);

        assertThat(response).isEqualTo(caseDetails);
    }

    @Test
    void shouldThrowCcdSearchCaseExceptionIfFetchCaseFails() {
        final User user = new User(
            CASEWORKER_AUTH_TOKEN,
            UserDetails.builder()
                .id(CASEWORKER_USER_ID)
                .build());
        final String caseId = String.valueOf(TEST_CASE_ID);

        doThrow(feignException(400, "Invalid case ID")).when(coreCaseDataApi)
            .readForCaseWorker(
                CASEWORKER_AUTH_TOKEN,
                SERVICE_AUTHORIZATION,
                CASEWORKER_USER_ID,
                JURISDICTION,
                CASE_TYPE,
                caseId);

        final CcdSearchCaseException exception = assertThrows(
            CcdSearchCaseException.class,
            () -> ccdFetchCaseService.fetchCaseById(caseId, user, SERVICE_AUTHORIZATION));

        assertThat(exception.getMessage())
            .contains(format("Fetch case failed for case ID: %s", caseId));
    }
}
