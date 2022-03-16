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
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerChangeServiceRequest.CASEWORKER_CHANGE_SERVICE_REQUEST;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.COURT_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.SOLICITOR_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAos;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingService;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;

@ExtendWith(MockitoExtension.class)
class CaseworkerChangeServiceRequestTest {

    @InjectMocks
    private CaseworkerChangeServiceRequest caseworkerChangeServiceRequest;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerChangeServiceRequest.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_CHANGE_SERVICE_REQUEST);
    }

    @Test
    void shouldSetStateToAwaitingServiceIfSolicitorServiceIsChosen() {
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        final CaseData caseData = CaseData
            .builder()
            .application(Application
                .builder()
                .solServiceMethod(SOLICITOR_SERVICE)
                .build())
            .build();

        details.setData(caseData);
        details.setId(1L);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerChangeServiceRequest.aboutToSubmit(details, null);
        assertThat(response.getState()).isEqualTo(AwaitingService);
    }

    @Test
    void shouldSetStateToAwaitingAosIfCourtServiceIsChosen() {
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        final CaseData caseData = CaseData
            .builder()
            .application(Application
                .builder()
                .solServiceMethod(COURT_SERVICE)
                .build())
            .build();

        details.setData(caseData);
        details.setId(1L);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerChangeServiceRequest.aboutToSubmit(details, null);
        assertThat(response.getState()).isEqualTo(AwaitingAos);
    }
}
