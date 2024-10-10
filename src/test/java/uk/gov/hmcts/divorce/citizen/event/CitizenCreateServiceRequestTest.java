package uk.gov.hmcts.divorce.citizen.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.divorce.citizen.event.CitizenCreateServiceRequest.CITIZEN_CREATE_SERVICE_REQUEST;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingFinalOrderPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPayment;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class CitizenCreateServiceRequestTest {
    @Mock
    private CitizenSubmitApplication citizenSubmitApplication;

    @Mock
    private RespondentApplyForFinalOrder respondentApplyForFinalOrder;

    @InjectMocks
    private CitizenCreateServiceRequest citizenCreateServiceRequest;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        citizenCreateServiceRequest.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CITIZEN_CREATE_SERVICE_REQUEST);
    }

    @Test
    public void shouldSetServiceRequestForApplicationPaymentIfCaseIsInAwaitingPayment() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = new CaseData();
        long caseId = TEST_CASE_ID;

        caseDetails.setState(AwaitingPayment);
        caseDetails.setData(caseData);
        caseDetails.setId(caseId);

        citizenCreateServiceRequest.aboutToSubmit(caseDetails, caseDetails);

        verify(citizenSubmitApplication).setServiceRequestReferenceForApplicationPayment(caseData, caseId);
    }

    @Test
    public void shouldSetServiceRequestForFinalOrderPaymentIfCaseIsInAwaitingFinalOrderPayment() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = new CaseData();
        long caseId = TEST_CASE_ID;

        caseDetails.setState(AwaitingFinalOrderPayment);
        caseDetails.setData(caseData);
        caseDetails.setId(caseId);

        citizenCreateServiceRequest.aboutToSubmit(caseDetails, caseDetails);

        verify(respondentApplyForFinalOrder).setServiceRequestReferenceForFinalOrderPayment(caseData, caseId);
    }
}
