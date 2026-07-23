package uk.gov.hmcts.divorce.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralReferralReason;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralReferralType;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerRejectGeneralApplication.INVALID_STATE_ERROR;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerRejectGeneralReferral.CASEWORKER_REJECT_GENERAL_REFERRAL;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingApplicant1Response;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingGeneralConsideration;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Holding;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseDataWithMarriageDate;

@ExtendWith(MockitoExtension.class)
class CaseworkerRejectGeneralReferralTest {

    @InjectMocks
    private CaseworkerRejectGeneralReferral caseworkerRejectGeneralReferral;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerRejectGeneralReferral.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_REJECT_GENERAL_REFERRAL);
    }

    @Test
    void shouldReturnErrorInAboutToStartWhenNoGeneralReferralExists() {
        final CaseData caseData = caseDataWithMarriageDate();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(Holding);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerRejectGeneralReferral.aboutToStart(caseDetails);

        assertThat(response.getErrors()).isNotNull();
        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors().getFirst()).isEqualTo("No general referral exists to reject.");
    }

    @Test
    void shouldNotReturnErrorInAboutToStartWhenGeneralReferralExists() {
        final CaseData caseData = caseDataWithMarriageDate();
        caseData.getGeneralReferral().setGeneralReferralReason(GeneralReferralReason.CASEWORKER_REFERRAL);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(Holding);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerRejectGeneralReferral.aboutToStart(caseDetails);

        assertThat(response.getErrors()).isEmpty();
        assertThat(response.getData().getApplication().getCurrentState()).isEqualTo(Holding);
    }

    @Test
    void shouldReturnValidationErrorWhenPreSubmissionStateSelectedByCaseworker() {
        CaseData caseData = CaseData.builder()
            .application(Application.builder()
                .stateToTransitionApplicationTo(AwaitingApplicant1Response)
                .build()
            ).build();

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerRejectGeneralReferral.midEvent(details, null);

        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors().getFirst()).isEqualTo(INVALID_STATE_ERROR);
    }

    @Test
    void shouldDeleteGeneralReferralInAboutToSubmit() {
        final CaseData caseData = caseDataWithMarriageDate();

        caseData.getGeneralReferral().setGeneralReferralType(GeneralReferralType.CASEWORKER_REFERRAL);
        caseData.getGeneralReferral().setGeneralReferralReason(GeneralReferralReason.CASEWORKER_REFERRAL);
        caseData.getGeneralReferral().setGeneralApplicationReferralDate(LocalDate.now());
        caseData.getApplication().setStateToTransitionApplicationTo(Holding);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(AwaitingGeneralConsideration);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerRejectGeneralReferral.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getGeneralReferral().getGeneralReferralType()).isNull();
        assertThat(response.getData().getGeneralReferral().getGeneralApplicationReferralDate()).isNull();
        assertThat(response.getData().getGeneralReferral().getGeneralReferralReason()).isNull();
        assertThat(response.getState()).isEqualTo(Holding);
        assertThat(response.getWarnings()).isNotNull();
        assertThat(response.getWarnings()).hasSize(1);
        assertThat(response.getWarnings().get(0)).isEqualTo(
            "You are about to delete the general referral. This action cannot be undone.");
    }
}
