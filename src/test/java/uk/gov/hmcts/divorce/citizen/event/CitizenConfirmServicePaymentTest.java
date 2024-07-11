package uk.gov.hmcts.divorce.citizen.event;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.citizen.event.CitizenAlternativeServicePayment.CITIZEN_SERVICE_PAYMENT;
import static uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType.BAILIFF;
import static uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType.DEEMED;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingBailiffReferral;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingServiceConsideration;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingServicePayment;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class CitizenConfirmServicePaymentTest {

    @InjectMocks
    private CitizenAlternativeServicePayment citizenAlternativeServicePayment;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        citizenAlternativeServicePayment.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CITIZEN_SERVICE_PAYMENT);
    }

    @Test
    void shouldSetAwaitingBailiffReferralWhenAlternativeServiceTypeIsBailiff() {

        final CaseData caseData = caseData();
        caseData.getAlternativeService().setAlternativeServiceType(BAILIFF);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setState(AwaitingServicePayment);
        details.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> aboutToStartOrSubmitResponse =
            citizenAlternativeServicePayment.aboutToSubmit(details, details);

        assertThat(aboutToStartOrSubmitResponse.getState()).isEqualTo(AwaitingBailiffReferral);
    }

    @Test
    void shouldSetAwaitingServiceConsiderationWhenAlternativeServiceTypeIsNotBailiff() {

        final CaseData caseData = caseData();
        caseData.getAlternativeService().setAlternativeServiceType(DEEMED);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setState(AwaitingServicePayment);
        details.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> aboutToStartOrSubmitResponse =
            citizenAlternativeServicePayment.aboutToSubmit(details, details);

        assertThat(aboutToStartOrSubmitResponse.getState()).isEqualTo(AwaitingServiceConsideration);
    }
}
