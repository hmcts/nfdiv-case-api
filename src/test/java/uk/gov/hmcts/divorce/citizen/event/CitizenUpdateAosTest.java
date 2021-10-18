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
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.citizen.event.CitizenUpdateAos.CITIZEN_UPDATE_AOS;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;

@ExtendWith(MockitoExtension.class)
public class CitizenUpdateAosTest {

    @InjectMocks
    private CitizenUpdateAos citizenUpdateAos;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        citizenUpdateAos.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CITIZEN_UPDATE_AOS);
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

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenUpdateAos.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getAcknowledgementOfService().getDisputeApplication()).isNull();
        assertThat(response.getData().getAcknowledgementOfService().getConfirmDisputeApplication()).isNull();

    }
}
