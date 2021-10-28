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
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.solicitor.service.task.AddMiniApplicationLink;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.common.event.UpdateAos.UPDATE_AOS;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;

@ExtendWith(MockitoExtension.class)
class UpdateAosTest {

    @Mock
    private AddMiniApplicationLink addMiniApplicationLink;

    @InjectMocks
    private UpdateAos updateAos;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        updateAos.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(UPDATE_AOS);
    }

    @Test
    void shouldCallAddMiniApplicationAndReturnCaseDataOnAboutToStart() {

        final CaseData expectedCaseData = CaseData.builder().build();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> updateCaseDetails = new CaseDetails<>();
        updateCaseDetails.setData(expectedCaseData);

        when(addMiniApplicationLink.apply(caseDetails)).thenReturn(updateCaseDetails);

        final AboutToStartOrSubmitResponse<CaseData, State> response = updateAos.aboutToStart(caseDetails);

        assertThat(response.getData()).isSameAs(expectedCaseData);

        verify(addMiniApplicationLink).apply(caseDetails);
    }


    @Test
    void shouldSetDisputeApplicationFieldsToNullIfConfirmationIsNo() {
        final long caseId = 1L;
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();
        caseData.getAcknowledgementOfService().setDisputeApplication(YesOrNo.YES);
        caseData.getAcknowledgementOfService().setConfirmDisputeApplication(YesOrNo.NO);

        caseDetails.setData(caseData);
        caseDetails.setId(caseId);

        final AboutToStartOrSubmitResponse<CaseData, State> response = updateAos.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getAcknowledgementOfService().getDisputeApplication()).isNull();
        assertThat(response.getData().getAcknowledgementOfService().getConfirmDisputeApplication()).isNull();

    }
}
