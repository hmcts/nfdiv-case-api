package uk.gov.hmcts.divorce.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.caseworker.service.AlternativeServicePaymentService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerAlternativeServicePayment.CASEWORKER_SERVICE_PAYMENT;
import static uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType.BAILIFF;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingBailiffReferral;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingServicePayment;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class CaseworkerConfirmServicePaymentTest {

    @Mock
    private AlternativeServicePaymentService alternativeServicePaymentService;

    @InjectMocks
    private CaseworkerAlternativeServicePayment caseworkerAlternativeServicePayment;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerAlternativeServicePayment.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_SERVICE_PAYMENT);
    }

    @Test
    void shouldReturnCaseData() {
        final CaseData caseData = caseData();
        caseData.getAlternativeService().setAlternativeServiceType(BAILIFF);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setState(AwaitingServicePayment);
        details.setId(TEST_CASE_ID);

        when(alternativeServicePaymentService.getFeeAndSetOrderSummary(caseData, TEST_CASE_ID)).thenReturn(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerAlternativeServicePayment.aboutToStart(details);

        assertThat(response.getData()).isEqualTo(caseData);
    }

    @Test
    void shouldReturnState() {
        final CaseData caseData = caseData();
        caseData.getAlternativeService().setAlternativeServiceType(BAILIFF);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setState(AwaitingServicePayment);
        details.setId(TEST_CASE_ID);

        when(alternativeServicePaymentService.getState(caseData)).thenReturn(AwaitingBailiffReferral);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerAlternativeServicePayment.aboutToSubmit(details, details);

        assertThat(response.getState()).isEqualTo(AwaitingBailiffReferral);
    }
}
