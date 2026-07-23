package uk.gov.hmcts.divorce.solicitor.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.common.service.InterimApplicationSubmissionService;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.DraftServiceApplicationAction;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.SolicitorPaymentMethod;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class ServiceApplicationDraftSubmissionServiceTest {

    @InjectMocks
    private ServiceApplicationDraftSubmissionService serviceApplicationSubmissionService;

    @Mock
    private ServiceApplicationFactory serviceApplicationFactory;

    @Mock
    private ServiceApplicationPaymentPreparationService paymentPreparationService;

    @Mock
    private InterimApplicationSubmissionService interimApplicationSubmissionService;

    @Test
    void shouldSubmitServiceApplicationFromInterimOptions() {
        InterimApplicationOptions originalOptions = InterimApplicationOptions.builder()
            .interimApplicationType(InterimApplicationType.DEEMED_SERVICE)
            .interimAppsPaymentMethod(SolicitorPaymentMethod.FEE_PAY_BY_ACCOUNT)
            .build();

        Applicant applicant = Applicant.builder()
            .interimApplicationOptions(originalOptions)
            .build();

        CaseData caseData = CaseData.builder()
            .applicant1(applicant)
            .build();

        AlternativeService serviceApplication = AlternativeService.builder().build();
        when(serviceApplicationFactory.createFromInterimOptions(originalOptions)).thenReturn(serviceApplication);

        DivorceDocument generatedDocument = DivorceDocument.builder().build();
        when(interimApplicationSubmissionService.generateServiceApplicationAnswerDocument(TEST_CASE_ID, applicant, caseData))
            .thenReturn(generatedDocument);

        serviceApplicationSubmissionService.submitFromInterimOptions(TEST_CASE_ID, caseData, applicant);

        assertThat(caseData.getAlternativeService()).isEqualTo(serviceApplication);
        assertThat(caseData.getAlternativeService().getServiceApplicationAnswers()).isEqualTo(generatedDocument);

        verify(serviceApplicationFactory).createFromInterimOptions(originalOptions);
        verify(paymentPreparationService).prepareDraftServiceApplicationFee(TEST_CASE_ID, applicant, originalOptions, serviceApplication);
        verify(interimApplicationSubmissionService).generateServiceApplicationAnswerDocument(TEST_CASE_ID, applicant, caseData);
    }

    @Test
    void shouldClearInterimOptionsAndAlternativeServiceWhenDraftActionIsWithdraw() {
        InterimApplicationOptions originalOptions = InterimApplicationOptions.builder()
            .draftServiceApplicationAction(DraftServiceApplicationAction.WITHDRAW)
            .build();

        Applicant applicant = Applicant.builder()
            .interimApplicationOptions(originalOptions)
            .build();

        CaseData caseData = CaseData.builder()
            .applicant1(applicant)
            .alternativeService(AlternativeService.builder().build())
            .build();

        serviceApplicationSubmissionService.submitFromInterimOptions(TEST_CASE_ID, caseData, applicant);

        assertThat(applicant.getInterimApplicationOptions()).isNull();
        assertThat(caseData.getAlternativeService()).isNull();

        verifyNoInteractions(serviceApplicationFactory);
        verifyNoInteractions(paymentPreparationService);
        verifyNoInteractions(interimApplicationSubmissionService);
    }
}
