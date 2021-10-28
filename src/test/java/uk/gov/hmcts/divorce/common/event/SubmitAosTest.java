package uk.gov.hmcts.divorce.common.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.service.SubmitAosService;
import uk.gov.hmcts.divorce.divorcecase.model.AcknowledgementOfService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.common.event.SubmitAos.SUBMIT_AOS;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Holding;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class SubmitAosTest {

    @Mock
    private SubmitAosService submitAosService;

    @InjectMocks
    private SubmitAos submitAos;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        submitAos.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SUBMIT_AOS);
    }

    @Test
    void shouldReturnErrorsIfAosValidationFails() {

        final AcknowledgementOfService acknowledgementOfService = AcknowledgementOfService.builder()
            .statementOfTruth(NO)
            .prayerHasBeenGiven(NO)
            .confirmReadPetition(NO)
            .build();

        final CaseData caseData = caseData();
        caseData.setAcknowledgementOfService(acknowledgementOfService);

        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = submitAos.aboutToSubmit(caseDetails, beforeDetails);

        assertThat(response.getData()).isSameAs(caseData);
        assertThat(response.getErrors())
            .containsExactly(
                "You must be authorised by the respondent to sign this statement.",
                "The respondent must have given their prayer.",
                "The respondent must have read the application for divorce.");
    }

    @Test
    void shouldCallSolicitorSubmitAosServiceAndCompleteIfValidAos() {

        final AcknowledgementOfService acknowledgementOfService = AcknowledgementOfService.builder()
            .statementOfTruth(YES)
            .prayerHasBeenGiven(YES)
            .confirmReadPetition(YES)
            .build();

        final CaseData caseData = caseData();
        caseData.setAcknowledgementOfService(acknowledgementOfService);
        final CaseData expectedCaseData = caseData();

        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        final CaseDetails<CaseData, State> expectedCaseDetails = new CaseDetails<>();
        expectedCaseDetails.setData(expectedCaseData);
        expectedCaseDetails.setState(Holding);

        when(submitAosService.submitAos(caseDetails)).thenReturn(expectedCaseDetails);

        final AboutToStartOrSubmitResponse<CaseData, State> response = submitAos.aboutToSubmit(caseDetails, beforeDetails);

        assertThat(response.getData()).isSameAs(expectedCaseData);
        assertThat(response.getState()).isEqualTo(Holding);
        assertThat(response.getErrors()).isNull();
    }
}
