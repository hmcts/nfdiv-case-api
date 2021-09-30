package uk.gov.hmcts.divorce.caseworker.event;

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
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerMakeBailiffDecision.CASEWORKER_MAKE_BAILIFF_DECISION;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceApplicationType.BAILIFF;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceApplicationType.DEEMED;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingBailiffService;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingConditionalOrder;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ServiceApplicationNotApproved;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class CaseworkerMakeBailiffDecisionTest {

    @InjectMocks
    private CaseworkerMakeBailiffDecision makeBailiffDecision;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        makeBailiffDecision.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_MAKE_BAILIFF_DECISION);
    }

    @Test
    void shouldChangeCaseStateToAwaitingBailiffServiceWhenServiceApplicationIsGrantedAndServiceTypeIsBailiff() {
        final CaseData caseData = caseData();
        caseData.getServiceApplication().setServiceApplicationGranted(YES);
        caseData.getServiceApplication().setServiceApplicationType(BAILIFF);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> aboutToStartOrSubmitResponse =
            makeBailiffDecision.aboutToSubmit(details, details);

        assertThat(aboutToStartOrSubmitResponse.getState()).isEqualTo(AwaitingBailiffService);
    }

    @Test
    void shouldChangeCaseStateToAwaitingConditionalOrderWhenServiceApplicationIsGrantedAndServiceTypeIsNotBailiff() {
        final CaseData caseData = caseData();
        caseData.getServiceApplication().setServiceApplicationGranted(YES);
        caseData.getServiceApplication().setServiceApplicationType(DEEMED);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> aboutToStartOrSubmitResponse =
            makeBailiffDecision.aboutToSubmit(details, details);

        assertThat(aboutToStartOrSubmitResponse.getState()).isEqualTo(AwaitingConditionalOrder);
    }

    @Test
    void shouldChangeCaseStateToServiceApplicationNotApprovedWhenServiceApplicationIsNotGranted() {
        final CaseData caseData = caseData();
        caseData.getServiceApplication().setServiceApplicationGranted(NO);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> aboutToStartOrSubmitResponse =
            makeBailiffDecision.aboutToSubmit(details, details);

        assertThat(aboutToStartOrSubmitResponse.getState()).isEqualTo(ServiceApplicationNotApproved);
    }
}
