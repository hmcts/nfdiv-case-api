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
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.SolicitorPaymentMethod;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class ServiceApplicationSubmissionServiceTest {

    @InjectMocks
    private ServiceApplicationSubmissionService serviceApplicationSubmissionService;

    @Mock
    private ServiceApplicationFactory serviceApplicationFactory;

    @Mock
    private ServiceApplicationPaymentPreparationService paymentPreparationService;

    @Mock
    private InterimApplicationSubmissionService interimApplicationSubmissionService;

    @Test
    void shouldSubmitServiceApplicationFromInterimOptionsAndArchiveApplicantOptions() {
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
        assertThat(applicant.getInterimApplicationOptions()).isEqualTo(new InterimApplicationOptions());
        assertThat(applicant.getInterimApplications()).hasSize(1);
        assertThat(applicant.getInterimApplications().getFirst().getValue().getOptions()).isEqualTo(originalOptions);

        verify(serviceApplicationFactory).createFromInterimOptions(originalOptions);
        verify(paymentPreparationService).prepareDraftServiceApplicationFee(TEST_CASE_ID, applicant, originalOptions, serviceApplication);
        verify(interimApplicationSubmissionService).generateServiceApplicationAnswerDocument(TEST_CASE_ID, applicant, caseData);
    }
}
