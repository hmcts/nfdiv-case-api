package uk.gov.hmcts.divorce.solicitor.service;

import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAssignmentApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRoleWithOrganisation;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRolesRequest;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CREATOR;
import static uk.gov.hmcts.divorce.testutil.TestConstants.APP_1_SOL_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.APP_2_CITIZEN_USER_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.CASEWORKER_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.CASEWORKER_USER_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SOLICITOR_USER_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_USER_USER_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_ORG_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.feignException;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CcdAccessServiceIT {

    @Autowired
    private CcdAccessService ccdAccessService;

    @MockBean
    private CaseAssignmentApi caseAssignmentApi;

    @MockBean
    private IdamService idamService;

    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    @Test
    void shouldRetryRemovingRolesThreeTimesWhenRemovingRolesThrowsException() {
        when(idamService.retrieveUser(APP_1_SOL_AUTH_TOKEN)).thenReturn(solicitorUser());
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(systemUpdateUser());
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        doThrow(feignException(500, "some error"))
            .when(caseAssignmentApi).removeCaseUserRoles(
                SYSTEM_UPDATE_AUTH_TOKEN,
                TEST_SERVICE_AUTH_TOKEN,
                getCaseAssignmentUserRolesRequest(TEST_ORG_ID, CREATOR, SOLICITOR_USER_ID)
            );

        assertThrows(
            FeignException.class,
            () -> ccdAccessService.addApplicant1SolicitorRole(APP_1_SOL_AUTH_TOKEN, TEST_CASE_ID, TEST_ORG_ID)
        );

        verify(caseAssignmentApi, times(3))
            .removeCaseUserRoles(
                SYSTEM_UPDATE_AUTH_TOKEN,
                TEST_SERVICE_AUTH_TOKEN,
                getCaseAssignmentUserRolesRequest(TEST_ORG_ID, CREATOR, SOLICITOR_USER_ID)
            );
    }

    @Test
    void shouldRetryAddCaseRolesThreeTimesWhenAddingCaseRolesThrowsException() {
        when(idamService.retrieveUser(CASEWORKER_AUTH_TOKEN)).thenReturn(caseworkerUser());
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        doThrow(feignException(500, "some error"))
            .when(caseAssignmentApi).addCaseUserRoles(
                CASEWORKER_AUTH_TOKEN,
                TEST_SERVICE_AUTH_TOKEN,
                getCaseAssignmentUserRolesRequest(null, APPLICANT_2, APP_2_CITIZEN_USER_ID)
            );

        assertThrows(
            FeignException.class,
            () -> ccdAccessService.linkRespondentToApplication(CASEWORKER_AUTH_TOKEN, TEST_CASE_ID, APP_2_CITIZEN_USER_ID)
        );

        verify(caseAssignmentApi, times(3))
            .addCaseUserRoles(
                CASEWORKER_AUTH_TOKEN,
                TEST_SERVICE_AUTH_TOKEN,
                getCaseAssignmentUserRolesRequest(null, APPLICANT_2, APP_2_CITIZEN_USER_ID)
            );
    }

    @Test
    void shouldRetryRemoveCaseRolesThreeTimesWhenRemovingCaseRolesThrowsException() {
        when(idamService.retrieveUser(CASEWORKER_AUTH_TOKEN)).thenReturn(caseworkerUser());
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        doThrow(feignException(500, "some error"))
            .when(caseAssignmentApi).removeCaseUserRoles(
                CASEWORKER_AUTH_TOKEN,
                TEST_SERVICE_AUTH_TOKEN,
                getCaseAssignmentUserRolesRequest(null, APPLICANT_2, APP_2_CITIZEN_USER_ID)
            );

        assertThrows(
            FeignException.class,
            () -> ccdAccessService.linkRespondentToApplication(CASEWORKER_AUTH_TOKEN, TEST_CASE_ID, APP_2_CITIZEN_USER_ID)
        );

        verify(caseAssignmentApi, times(3))
            .removeCaseUserRoles(
                CASEWORKER_AUTH_TOKEN,
                TEST_SERVICE_AUTH_TOKEN,
                getCaseAssignmentUserRolesRequest(null, APPLICANT_2, APP_2_CITIZEN_USER_ID)
            );
    }

    private CaseAssignmentUserRolesRequest getCaseAssignmentUserRolesRequest(String orgId, UserRole role, String userId) {
        return CaseAssignmentUserRolesRequest.builder()
            .caseAssignmentUserRolesWithOrganisation(
                List.of(
                    CaseAssignmentUserRoleWithOrganisation.builder()
                        .organisationId(orgId)
                        .caseDataId(String.valueOf(TEST_CASE_ID))
                        .caseRole(role.getRole())
                        .userId(userId)
                        .build()
                )
            )
            .build();
    }

    private User caseworkerUser() {
        return getUser(CASEWORKER_AUTH_TOKEN, CASEWORKER_USER_ID);
    }

    private User solicitorUser() {
        return getUser(APP_1_SOL_AUTH_TOKEN, SOLICITOR_USER_ID);
    }

    private User systemUpdateUser() {
        return getUser(SYSTEM_UPDATE_AUTH_TOKEN, SYSTEM_USER_USER_ID);
    }

    private User getUser(String systemUpdateAuthToken, String systemUserUserId) {
        return new User(
            systemUpdateAuthToken,
            UserDetails.builder()
                .id(systemUserUserId)
                .build());
    }
}
