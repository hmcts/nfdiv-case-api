package uk.gov.hmcts.divorce.caseworker.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
class CaseFlagsServiceTest {

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private CcdUpdateService ccdUpdateService;

    @InjectMocks
    private CaseFlagsService caseFlagsService;

    @Test
    void shouldSetSupplementaryDataForCaseFlags() {

        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(new User(TEST_SERVICE_AUTH_TOKEN, null));
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);

        caseFlagsService.setSupplementaryDataForCaseFlags(TEST_CASE_ID);

        verify(ccdUpdateService).submitSupplementaryDataToCcdForServiceID(
            TEST_CASE_ID.toString(),
            TEST_SERVICE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION
        );
    }
}
