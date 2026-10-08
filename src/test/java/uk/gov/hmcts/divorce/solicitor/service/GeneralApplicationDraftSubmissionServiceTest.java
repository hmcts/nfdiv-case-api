package uk.gov.hmcts.divorce.solicitor.service;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.citizen.event.CitizenWithdrawGeneralApplication;
import uk.gov.hmcts.divorce.common.service.CitizenGeneralApplicationSubmissionService;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.DraftApplicationAction;
import uk.gov.hmcts.divorce.divorcecase.model.FeeDetails;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplication;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralParties;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;
import uk.gov.hmcts.divorce.divorcecase.model.ServicePaymentMethod;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
public class GeneralApplicationDraftSubmissionServiceTest {

    @InjectMocks
    private GeneralApplicationDraftSubmissionService generalApplicationDraftSubmissionService;

    @Mock
    private CcdAccessService ccdAccessService;

    @Mock
    private GeneralApplicationFactory generalApplicationFactory;

    @Mock
    private GeneralApplicationPaymentPreparationService paymentPreparationService;

    @Mock
    private CitizenGeneralApplicationSubmissionService submissionService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private CitizenWithdrawGeneralApplication citizenWithdrawGeneralApplication;

    @Test
    void shouldBuildGeneralApplicationAndPopulateCaseDataWithDocumentWhenApplicant1() {
        CaseData caseData = CaseData.builder().applicationType(ApplicationType.SOLE_APPLICATION).build();

        InterimApplicationOptions options = InterimApplicationOptions.builder().build();
        Applicant applicant = Applicant.builder().interimApplicationOptions(options).build();

        GeneralApplication generalApplication = GeneralApplication.builder().build();
        DivorceDocument applicationDocument = DivorceDocument.builder().build();

        when(request.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(ccdAccessService.isApplicant1(TEST_AUTHORIZATION_TOKEN, TEST_CASE_ID)).thenReturn(true);
        when(generalApplicationFactory.createFromJourneyOptions(options, true, caseData.getApplicationType()))
            .thenReturn(generalApplication);
        when(submissionService.generateGeneralApplicationAnswerDocument(TEST_CASE_ID, applicant, caseData, generalApplication))
            .thenReturn(applicationDocument);

        CaseDetails<CaseData, State> details = CaseDetails.<CaseData, State>builder()
            .id(TEST_CASE_ID)
            .data(caseData)
            .build();

        generalApplicationDraftSubmissionService.buildGeneralApplication(details, caseData, applicant);

        assertThat(generalApplication.getGeneralApplicationDocument()).isEqualTo(applicationDocument);

        verify(generalApplicationFactory).createFromJourneyOptions(options, true, caseData.getApplicationType());
        verify(paymentPreparationService).prepareDraftGeneralApplicationFee(TEST_CASE_ID, applicant, options, generalApplication);
        verify(submissionService).generateGeneralApplicationAnswerDocument(TEST_CASE_ID, applicant, caseData, generalApplication);
    }

    @Test
    void shouldBuildGeneralApplicationAndPopulateCaseDataWithDocumentWhenNotApplicant1() {
        CaseData caseData = CaseData.builder().applicationType(ApplicationType.JOINT_APPLICATION).build();

        InterimApplicationOptions options = InterimApplicationOptions.builder().build();
        Applicant applicant = Applicant.builder().interimApplicationOptions(options).build();

        GeneralApplication generalApplication = GeneralApplication.builder().build();
        DivorceDocument applicationDocument = DivorceDocument.builder().build();

        when(request.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(ccdAccessService.isApplicant1(TEST_AUTHORIZATION_TOKEN, TEST_CASE_ID)).thenReturn(false);
        when(generalApplicationFactory.createFromJourneyOptions(options, false, caseData.getApplicationType()))
            .thenReturn(generalApplication);
        when(submissionService.generateGeneralApplicationAnswerDocument(TEST_CASE_ID, applicant, caseData, generalApplication))
            .thenReturn(applicationDocument);

        CaseDetails<CaseData, State> details = CaseDetails.<CaseData, State>builder().id(TEST_CASE_ID).data(caseData).build();

        generalApplicationDraftSubmissionService.buildGeneralApplication(details, caseData, applicant);

        assertThat(caseData.getGeneralApplication()).isEqualTo(generalApplication);
        assertThat(generalApplication.getGeneralApplicationDocument()).isEqualTo(applicationDocument);

        verify(generalApplicationFactory).createFromJourneyOptions(options, false, caseData.getApplicationType());
        verify(paymentPreparationService).prepareDraftGeneralApplicationFee(TEST_CASE_ID, applicant, options, generalApplication);
        verify(submissionService).generateGeneralApplicationAnswerDocument(TEST_CASE_ID, applicant, caseData, generalApplication);
    }

    @Test
    void shouldPropagateExceptionWhenPaymentPreparationFailsAfterSettingGeneralApplication() {
        CaseData caseData = CaseData.builder()
            .applicationType(ApplicationType.SOLE_APPLICATION)
            .build();

        InterimApplicationOptions options = InterimApplicationOptions.builder().build();
        Applicant applicant = Applicant.builder()
            .interimApplicationOptions(options)
            .build();

        GeneralApplication generalApplication = GeneralApplication.builder().build();

        when(request.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(ccdAccessService.isApplicant1(TEST_AUTHORIZATION_TOKEN, TEST_CASE_ID)).thenReturn(true);
        when(generalApplicationFactory.createFromJourneyOptions(options, true, caseData.getApplicationType()))
            .thenReturn(generalApplication);
        doThrow(new RuntimeException("payment failed"))
            .when(paymentPreparationService).prepareDraftGeneralApplicationFee(TEST_CASE_ID, applicant, options, generalApplication);

        CaseDetails<CaseData, State> details = CaseDetails.<CaseData, State>builder().id(TEST_CASE_ID).data(caseData).build();

        assertThatThrownBy(() -> generalApplicationDraftSubmissionService.buildGeneralApplication(details, caseData, applicant))
            .isExactlyInstanceOf(RuntimeException.class)
            .hasMessageContaining("payment failed");

        assertThat(caseData.getGeneralApplication()).isEqualTo(generalApplication);
        verify(submissionService, never()).generateGeneralApplicationAnswerDocument(anyLong(), any(), any(), any());
    }

    @Test
    void shouldRemoveHwfDraftWhenWithdrawActionAndServiceRequestIsBlank() {
        InterimApplicationOptions options = InterimApplicationOptions.builder()
            .draftApplicationAction(DraftApplicationAction.WITHDRAW)
            .build();

        Applicant applicant = Applicant.builder()
            .generalAppServiceRequest(null)
            .interimApplicationOptions(options)
            .build();

        GeneralApplication olderNonMatchingDraft = GeneralApplication.builder()
            .generalApplicationSubmittedOnline(YesOrNo.NO)
            .generalApplicationParty(GeneralParties.APPLICANT)
            .generalApplicationFee(FeeDetails.builder().paymentMethod(ServicePaymentMethod.FEE_PAY_BY_ACCOUNT).build())
            .build();

        GeneralApplication hwfDraft = GeneralApplication.builder()
            .generalApplicationSubmittedOnline(YesOrNo.NO)
            .generalApplicationParty(GeneralParties.APPLICANT)
            .generalApplicationFee(FeeDetails.builder().paymentMethod(ServicePaymentMethod.FEE_PAY_BY_HWF).build())
            .build();

        CaseData caseData = CaseData.builder()
            .applicant1(applicant)
            .generalApplications(new ArrayList<>(List.of(
                ListValue.<GeneralApplication>builder().value(olderNonMatchingDraft).build(),
                ListValue.<GeneralApplication>builder().value(hwfDraft).build()
            )))
            .build();

        CaseDetails<CaseData, State> details = CaseDetails.<CaseData, State>builder()
            .id(TEST_CASE_ID)
            .data(caseData)
            .build();

        when(citizenWithdrawGeneralApplication.handleRemovalOfGeneralApplication(caseData, 1))
            .thenAnswer(invocation -> {
                caseData.getGeneralApplications().remove(1);
                return null;
            });

        generalApplicationDraftSubmissionService.buildGeneralApplication(details, caseData, applicant);

        assertEquals(1, caseData.getGeneralApplications().size());
        assertThat(caseData.getGeneralApplications().getFirst().getValue()).isEqualTo(olderNonMatchingDraft);
        assertThat(applicant.getInterimApplicationOptions()).isNull();
    }
}
