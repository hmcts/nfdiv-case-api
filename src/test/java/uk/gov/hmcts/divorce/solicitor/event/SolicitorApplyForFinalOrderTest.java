package uk.gov.hmcts.divorce.solicitor.event;

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
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.solicitor.event.SolicitorApplyForFinalOrder.SOLICITOR_FINAL_ORDER_REQUESTED;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
public class SolicitorApplyForFinalOrderTest {

    @InjectMocks
    private SolicitorApplyForFinalOrder solicitorApplyForFinalOrder;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        solicitorApplyForFinalOrder.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SOLICITOR_FINAL_ORDER_REQUESTED);
    }

    @Test
    public void shouldReturnErrorWhenApplyForFinalOrderIsNoAndMidEventIsInvoked() {
        final CaseData caseData = caseData();
        caseData.getFinalOrder().setDoesApplicantWantToApplyForFinalOrder(NO);

        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> response = solicitorApplyForFinalOrder.midEvent(details, beforeDetails);

        assertThat(response.getErrors()).containsExactly("You must select 'Yes' to apply for Final Order");
    }

    @Test
    public void shouldNotReturnErrorWhenApplyForFinalOrderIsYesAndMidEventIsInvoked() {
        final CaseData caseData = caseData();
        caseData.getFinalOrder().setDoesApplicantWantToApplyForFinalOrder(YES);

        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> response = solicitorApplyForFinalOrder.midEvent(details, beforeDetails);

        assertThat(response.getErrors()).isNull();
    }
}
