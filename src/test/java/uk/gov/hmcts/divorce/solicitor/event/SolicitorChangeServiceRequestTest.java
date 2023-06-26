package uk.gov.hmcts.divorce.solicitor.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.COURT_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.PERSONAL_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.SOLICITOR_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAos;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingService;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.solicitor.event.SolicitorChangeServiceRequest.SOLICITOR_CHANGE_SERVICE_REQUEST;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemIssueSolicitorServicePack.SYSTEM_ISSUE_SOLICITOR_SERVICE_PACK;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseDataWithStatementOfTruth;

@ExtendWith(MockitoExtension.class)
class SolicitorChangeServiceRequestTest {

    @Mock
    IdamService idamService;

    @Mock
    AuthTokenGenerator authTokenGenerator;

    @Mock
    CcdUpdateService ccdUpdateService;

    @InjectMocks
    private SolicitorChangeServiceRequest solicitorChangeServiceRequest;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        solicitorChangeServiceRequest.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SOLICITOR_CHANGE_SERVICE_REQUEST);
    }

    @Test
    void shouldThrowErrorIfPersonalService() {
        final CaseData caseData = caseDataWithStatementOfTruth();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(Submitted);

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        caseData.getApplication().setServiceMethod(PERSONAL_SERVICE);
        updatedCaseDetails.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = solicitorChangeServiceRequest.aboutToSubmit(
            updatedCaseDetails, caseDetails);

        assertThat(response.getWarnings()).isNull();
        assertThat(response.getErrors()).contains("You may not select Personal Service. Please select Solicitor or Court Service.");
    }

    @Test
    void shouldChangeStateToAwaitingAosForCourtService() {
        final CaseData caseData = caseDataWithStatementOfTruth();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(Submitted);

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        caseData.getApplication().setServiceMethod(COURT_SERVICE);
        updatedCaseDetails.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = solicitorChangeServiceRequest.aboutToSubmit(
            updatedCaseDetails, caseDetails);

        verifyNoInteractions(ccdUpdateService);

        assertThat(response.getWarnings()).isNull();
        assertThat(response.getErrors()).isNull();
        assertThat(response.getState()).isEqualTo(AwaitingAos);
    }

    @Test
    void shouldChangeStateToAwaitingServiceForSolicitorServiceAndGenerateSolicitorServicePack() {
        final User user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserDetails.builder().build());
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);

        final CaseData caseData = caseDataWithStatementOfTruth();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(Submitted);

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        caseData.getApplication().setServiceMethod(SOLICITOR_SERVICE);
        updatedCaseDetails.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = solicitorChangeServiceRequest.aboutToSubmit(
            updatedCaseDetails, caseDetails);

        verify(ccdUpdateService).submitEvent(updatedCaseDetails, SYSTEM_ISSUE_SOLICITOR_SERVICE_PACK, user, SERVICE_AUTHORIZATION);

        assertThat(response.getWarnings()).isNull();
        assertThat(response.getErrors()).isNull();
        assertThat(response.getState()).isEqualTo(AwaitingService);
    }
}
