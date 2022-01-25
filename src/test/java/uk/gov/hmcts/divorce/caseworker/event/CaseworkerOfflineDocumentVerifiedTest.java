package uk.gov.hmcts.divorce.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.caseworker.event.CaseworkerOfflineDocumentVerified;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerOfflineDocumentVerified.CASEWORKER_OFFLINE_DOCUMENT_VERIFIED;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAmendedApplication;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;

@ExtendWith(MockitoExtension.class)
public class CaseworkerOfflineDocumentVerifiedTest {

    @InjectMocks
    private CaseworkerOfflineDocumentVerified caseworkerOfflineDocumentVerified;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerOfflineDocumentVerified.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_OFFLINE_DOCUMENT_VERIFIED);
    }

    @Test
    void shouldSetStateToUserValueProvided() {
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        CaseData caseData = CaseData.builder()
            .application(Application.builder()
                .stateToTransitionApplicationTo(AwaitingAmendedApplication)
                .build())
            .build();
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerOfflineDocumentVerified.aboutToSubmit(details, details);

        assertThat(response.getState().getName()).isEqualTo(AwaitingAmendedApplication.getName());
    }
}
