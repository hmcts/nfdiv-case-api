package uk.gov.hmcts.divorce.solicitor.event;

import jakarta.servlet.http.HttpServletRequest;
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
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.solicitor.client.organisation.OrganisationClient;
import uk.gov.hmcts.divorce.solicitor.client.organisation.OrganisationContactInformation;
import uk.gov.hmcts.divorce.solicitor.client.organisation.OrganisationsResponse;
import uk.gov.hmcts.divorce.solicitor.event.page.HelpWithFeesPageForApplicant2;
import uk.gov.hmcts.divorce.solicitor.event.page.MarriageIrretrievablyBrokenForApplicant2;
import uk.gov.hmcts.divorce.solicitor.service.SolicitorSubmitJointApplicationService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.document.model.DocumentType.AMENDED_APPLICATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.APPLICATION;
import static uk.gov.hmcts.divorce.solicitor.event.SolicitorSubmitJointApplication.SOLICITOR_SUBMIT_JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_ORG_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.documentWithType;

@ExtendWith(MockitoExtension.class)
class SolicitorSubmitJointApplicationTest {

    @Mock
    private MarriageIrretrievablyBrokenForApplicant2 marriageIrretrievablyBrokenForApplicant2;

    @Mock
    private HelpWithFeesPageForApplicant2 helpWithFeesPageForApplicant2;

    @Mock
    private SolicitorSubmitJointApplicationService solicitorSubmitJointApplicationService;

    @Mock
    private OrganisationClient organisationClient;

    @Mock
    private HttpServletRequest request;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private SolicitorSubmitJointApplication solicitorSubmitJointApplication;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        solicitorSubmitJointApplication.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SOLICITOR_SUBMIT_JOINT_APPLICATION);
    }

    @Test
    void shouldInvokeSubmitEventForApprovalOrRequestingChangesOnSubmittedCallback() {
        final var caseData = caseData();
        caseData.getApplication().setApplicant2ConfirmApplicant1Information(YES);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        solicitorSubmitJointApplication.submitted(caseDetails, caseDetails);

        verify(solicitorSubmitJointApplicationService).submitEventForApprovalOrRequestingChanges(caseDetails);
    }

    @Test
    void shouldNotSetSolicitorAddressIfApplicant2NotRepresented() {
        final var caseData = caseData();
        caseData.getApplicant2().setSolicitor(
            Solicitor.builder()
                .address("DX address")
                .build()
        );
        caseData.getApplicant2().setSolicitorRepresented(NO);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        solicitorSubmitJointApplication.aboutToSubmit(caseDetails, caseDetails);
        verifyNoInteractions(organisationClient);
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

        AboutToStartOrSubmitResponse<CaseData, State> response = solicitorSubmitJointApplication.aboutToSubmit(caseDetails, caseDetails);
        assertThat(response.getData().getApplicant2().getSolicitor().getAddress()).isEqualTo("1 Solicitor Court");
    }

    @Test
    void shouldNotPopulateApplicant2SolicitorAddressIfApplicant2RepresentedAndContactInformationNotReturned() {
        OrganisationsResponse organisationsResponse = OrganisationsResponse
            .builder()
            .organisationIdentifier(TEST_ORG_ID)
            .build();

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

        AboutToStartOrSubmitResponse<CaseData, State> response = solicitorSubmitJointApplication.aboutToSubmit(caseDetails, caseDetails);
        assertThat(response.getData().getApplicant2().getSolicitor().getAddress()).isEqualTo("DX address");
    }

    @Test
    public void shouldSetApplicant1SolicitorAnswersLinkWhenDraftApplicationDocumentIsInDocumentsGenerated() {
        final CaseData caseData = caseData();
        caseData.setDivorceOrDissolution(DIVORCE);
        caseData.setApplicationType(JOINT_APPLICATION);

        final var documentListValue = documentWithType(APPLICATION);
        final var generatedDocuments = singletonList(documentListValue);
        caseData.getDocuments().setDocumentsGenerated(generatedDocuments);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = solicitorSubmitJointApplication.aboutToStart(details);

        assertThat(response.getData().getApplication().getApplicant1SolicitorAnswersLink())
            .isEqualTo(documentListValue.getValue().getDocumentLink());
    }

    @Test
    public void shouldNotSetApplicant1SolicitorAnswersLinkWhenDraftApplicationDocumentIsInDocumentsGenerated() {
        final CaseData caseData = caseData();
        caseData.setDivorceOrDissolution(DIVORCE);
        caseData.setApplicationType(JOINT_APPLICATION);

        final var documentListValue = documentWithType(AMENDED_APPLICATION);
        final var generatedDocuments = singletonList(documentListValue);
        caseData.getDocuments().setDocumentsGenerated(generatedDocuments);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = solicitorSubmitJointApplication.aboutToStart(details);

        assertThat(response.getData().getApplication().getApplicant1SolicitorAnswersLink()).isNull();
    }
}
