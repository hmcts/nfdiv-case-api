package uk.gov.hmcts.divorce.solicitor.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.solicitor.client.organisation.OrganisationClient;
import uk.gov.hmcts.divorce.solicitor.client.organisation.OrganisationContactInformation;
import uk.gov.hmcts.divorce.solicitor.client.organisation.OrganisationsResponse;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.List;
import javax.servlet.http.HttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.common.event.Applicant2Approve.APPLICANT_2_APPROVE;
import static uk.gov.hmcts.divorce.common.event.Applicant2RequestChanges.APPLICANT_2_REQUEST_CHANGES;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_ORG_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
public class SolicitorSubmitJointApplicationServiceTest {

    @Mock
    private CcdUpdateService ccdUpdateService;

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private OrganisationClient organisationClient;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private SolicitorSubmitJointApplicationService solicitorSubmitJointApplicationService;

    @Test
    void shouldSubmitCcdApplicant2RequestChangesEventOnSubmittedCallbackIfApp2SolicitorHasRequestedChanges() {
        final User user = new User(TEST_AUTHORIZATION_TOKEN, UserDetails.builder().build());
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);

        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);

        final var caseData = caseData();
        caseData.getApplication().setApplicant2ConfirmApplicant1Information(YES);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        solicitorSubmitJointApplicationService.submitEventForApprovalOrRequestingChanges(caseDetails);

        verify(ccdUpdateService).submitEvent(caseDetails, APPLICANT_2_REQUEST_CHANGES, user, SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldSubmitCcdApplicant2ApproveEventOnSubmittedCallbackIfApp2SolicitorHasNotRequestedChanges() {
        final User user = new User(TEST_AUTHORIZATION_TOKEN, UserDetails.builder().build());
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);

        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);

        final var caseData = caseData();
        caseData.getApplication().setApplicant2ConfirmApplicant1Information(NO);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        solicitorSubmitJointApplicationService.submitEventForApprovalOrRequestingChanges(caseDetails);

        verify(ccdUpdateService).submitEvent(caseDetails, APPLICANT_2_APPROVE, user, SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldPopulateApplicant2SolicitorAddressIfApplicant2RepresentedAndContactInformationReturned() {
        OrganisationsResponse organisationsResponse = OrganisationsResponse
            .builder()
            .contactInformation(
                List.of(
                    OrganisationContactInformation.builder()
                    .addressLine1("1 Solicitor Court")
                    .build()
                )
            )
            .organisationIdentifier(TEST_ORG_ID)
            .build();

        final User user = new User(TEST_AUTHORIZATION_TOKEN, UserDetails.builder().build());
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
        when(request.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(organisationClient.getUserOrganisation(TEST_AUTHORIZATION_TOKEN, SERVICE_AUTHORIZATION))
            .thenReturn(organisationsResponse);

        final var caseData = caseData();
        caseData.getApplicant2().setSolicitor(
            Solicitor.builder()
                .address("DX address")
                .build()
        );
        caseData.getApplicant2().setSolicitorRepresented(YES);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        solicitorSubmitJointApplicationService.submitEventForApprovalOrRequestingChanges(caseDetails);
        assertThat(caseDetails.getData().getApplicant2().getSolicitor().getAddress()).isEqualTo("1 Solicitor Court");
    }

    @Test
    void shouldNotPopulateApplicant2SolicitorAddressIfApplicant2RepresentedAndContactInformationNotReturned() {
        OrganisationsResponse organisationsResponse = OrganisationsResponse
            .builder()
            .organisationIdentifier(TEST_ORG_ID)
            .build();

        final User user = new User(TEST_AUTHORIZATION_TOKEN, UserDetails.builder().build());
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
        when(request.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(organisationClient.getUserOrganisation(TEST_AUTHORIZATION_TOKEN, SERVICE_AUTHORIZATION))
            .thenReturn(organisationsResponse);

        final var caseData = caseData();
        caseData.getApplicant2().setSolicitor(
            Solicitor.builder()
                .address("DX address")
                .build()
        );
        caseData.getApplicant2().setSolicitorRepresented(YES);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        solicitorSubmitJointApplicationService.submitEventForApprovalOrRequestingChanges(caseDetails);
        assertThat(caseDetails.getData().getApplicant2().getSolicitor().getAddress()).isEqualTo("DX address");
    }
}
