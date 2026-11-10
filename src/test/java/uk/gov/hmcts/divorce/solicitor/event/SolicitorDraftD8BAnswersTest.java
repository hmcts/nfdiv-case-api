package uk.gov.hmcts.divorce.solicitor.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.payment.service.PaymentSetupService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.SolicitorPaymentMethod.FEES_HELP_WITH;
import static uk.gov.hmcts.divorce.divorcecase.model.SolicitorPaymentMethod.FEE_PAY_BY_ACCOUNT;
import static uk.gov.hmcts.divorce.solicitor.event.SolicitorDraftD8bAnswers.SOLICITOR_D8B_ANSWER;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.respondentWithDigitalSolicitor;

@ExtendWith(MockitoExtension.class)
class SolicitorDraftD8BAnswersTest {

    @Mock
    private PaymentSetupService paymentSetupService;

    @InjectMocks
    private SolicitorDraftD8bAnswers solicitorDraftD8BAnswers;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        solicitorDraftD8BAnswers.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SOLICITOR_D8B_ANSWER);
    }

    @Test
    void shouldSetOrderSummaryIfPaymentMethodIsPbaAboutToSubmitCallback() {
        Applicant respondent = respondentWithDigitalSolicitor();
        respondent.setInterimApplicationOptions(InterimApplicationOptions.builder().interimAppsPaymentMethod(FEE_PAY_BY_ACCOUNT).build());
        CaseData caseData = CaseData.builder().applicant2(respondent).build();

        CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder().id(TEST_CASE_ID).data(caseData).build();

        OrderSummary orderSummary = OrderSummary.builder().paymentTotal("240").build();

        when(paymentSetupService.createSolicitorD8bFeeOrderSummary(caseData, TEST_CASE_ID)).thenReturn(orderSummary);

        var response = solicitorDraftD8BAnswers.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getAcknowledgementOfService()
            .getDisputingFee().getOrderSummary()).isEqualTo(orderSummary);

        assertThat(response.getData().getApplicant2().getInterimApplicationOptions()
            .getInterimApplicationType()).isEqualTo(InterimApplicationType.SOLICITOR_D8B_ANSWERS);
        verify(paymentSetupService).createSolicitorD8bFeeOrderSummary(caseData, TEST_CASE_ID);
    }

    @Test
    void shouldNotSetOrderSummaryIfPaymentMethodIsHwfAboutToSubmitCallback() {
        Applicant respondent = respondentWithDigitalSolicitor();
        respondent.setInterimApplicationOptions(InterimApplicationOptions.builder().interimAppsPaymentMethod(FEES_HELP_WITH).build());
        CaseData caseData = CaseData.builder().applicant2(respondent).build();

        CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder().id(TEST_CASE_ID).data(caseData).build();

        solicitorDraftD8BAnswers.aboutToSubmit(caseDetails, caseDetails);

        verifyNoInteractions(paymentSetupService);
    }
}
