package uk.gov.hmcts.divorce.citizen.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.HowToRespondApplication;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.citizen.event.CitizenApplicant2UpdateApplication.CITIZEN_APPLICANT2_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AosDrafted;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AosOverdue;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
public class CitizenApplicant2UpdateApplicationTest {

    @InjectMocks
    private CitizenApplicant2UpdateApplication citizenApplicant2UpdateApplication;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        citizenApplicant2UpdateApplication.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CITIZEN_APPLICANT2_UPDATE);
    }

    @Test
    void shouldSetDisputeApplicationFieldsToNullIfConfirmationIsNoAndInAosDraftedState() {
        final long caseId = TEST_CASE_ID;
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();
        caseDetails.setState(AosDrafted);
        caseData.getAcknowledgementOfService().setHowToRespondApplication(HowToRespondApplication.DISPUTE_DIVORCE);
        caseData.getAcknowledgementOfService().setConfirmDisputeApplication(YesOrNo.NO);

        caseDetails.setData(caseData);
        caseDetails.setId(caseId);

        final AboutToStartOrSubmitResponse<CaseData, State> response
            = citizenApplicant2UpdateApplication.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getAcknowledgementOfService().getHowToRespondApplication()).isNull();
        assertThat(response.getData().getAcknowledgementOfService().getConfirmDisputeApplication()).isNull();
    }

    @Test
    void shouldNotSetDisputeApplicationFieldsToNullIfConfirmationIsYesAndInAosDraftedState() {
        final long caseId = TEST_CASE_ID;
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();
        caseDetails.setState(AosDrafted);
        caseData.getAcknowledgementOfService().setHowToRespondApplication(HowToRespondApplication.DISPUTE_DIVORCE);
        caseData.getAcknowledgementOfService().setConfirmDisputeApplication(YesOrNo.YES);

        caseDetails.setData(caseData);
        caseDetails.setId(caseId);

        final AboutToStartOrSubmitResponse<CaseData, State> response
            = citizenApplicant2UpdateApplication.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getAcknowledgementOfService().getHowToRespondApplication()).isNotNull();
        assertThat(response.getData().getAcknowledgementOfService().getConfirmDisputeApplication()).isNotNull();
    }

    @Test
    void shouldNotSetDisputeApplicationFieldsToNullIfApplicationNotDisputedAndInAosDraftedState() {
        final long caseId = TEST_CASE_ID;
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();
        caseDetails.setState(AosDrafted);
        caseData.getAcknowledgementOfService().setHowToRespondApplication(HowToRespondApplication.WITHOUT_DISPUTE_DIVORCE);
        caseData.getAcknowledgementOfService().setConfirmDisputeApplication(YesOrNo.NO);

        caseDetails.setData(caseData);
        caseDetails.setId(caseId);

        final AboutToStartOrSubmitResponse<CaseData, State> response
            = citizenApplicant2UpdateApplication.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getAcknowledgementOfService().getHowToRespondApplication()).isNotNull();
        assertThat(response.getData().getAcknowledgementOfService().getConfirmDisputeApplication()).isNotNull();
    }

    @Test
    void shouldSetDisputeApplicationFieldsToNullIfConfirmationIsNoAndInAosOverdueState() {
        final long caseId = TEST_CASE_ID;
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();
        caseDetails.setState(AosOverdue);
        caseData.getAcknowledgementOfService().setHowToRespondApplication(HowToRespondApplication.DISPUTE_DIVORCE);
        caseData.getAcknowledgementOfService().setConfirmDisputeApplication(YesOrNo.NO);

        caseDetails.setData(caseData);
        caseDetails.setId(caseId);

        final AboutToStartOrSubmitResponse<CaseData, State> response
            = citizenApplicant2UpdateApplication.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getAcknowledgementOfService().getHowToRespondApplication()).isNull();
        assertThat(response.getData().getAcknowledgementOfService().getConfirmDisputeApplication()).isNull();
    }

    @Test
    void shouldNotSetDisputeApplicationFieldsToNullIfConfirmationIsYesAndInAosOverdueState() {
        final long caseId = TEST_CASE_ID;
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();
        caseDetails.setState(AosOverdue);
        caseData.getAcknowledgementOfService().setHowToRespondApplication(HowToRespondApplication.DISPUTE_DIVORCE);
        caseData.getAcknowledgementOfService().setConfirmDisputeApplication(YesOrNo.YES);

        caseDetails.setData(caseData);
        caseDetails.setId(caseId);

        final AboutToStartOrSubmitResponse<CaseData, State> response
            = citizenApplicant2UpdateApplication.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getAcknowledgementOfService().getHowToRespondApplication()).isNotNull();
        assertThat(response.getData().getAcknowledgementOfService().getConfirmDisputeApplication()).isNotNull();
    }

    @Test
    void shouldNotSetDisputeApplicationFieldsToNullIfApplicationNotDisputedAndInAosOverduedState() {
        final long caseId = TEST_CASE_ID;
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();
        caseDetails.setState(AosOverdue);
        caseData.getAcknowledgementOfService().setHowToRespondApplication(HowToRespondApplication.WITHOUT_DISPUTE_DIVORCE);
        caseData.getAcknowledgementOfService().setConfirmDisputeApplication(YesOrNo.NO);

        caseDetails.setData(caseData);
        caseDetails.setId(caseId);

        final AboutToStartOrSubmitResponse<CaseData, State> response
            = citizenApplicant2UpdateApplication.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getAcknowledgementOfService().getHowToRespondApplication()).isNotNull();
        assertThat(response.getData().getAcknowledgementOfService().getConfirmDisputeApplication()).isNotNull();
    }
}
