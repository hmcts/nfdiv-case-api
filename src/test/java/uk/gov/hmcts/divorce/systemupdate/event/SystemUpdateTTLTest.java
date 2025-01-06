package uk.gov.hmcts.divorce.systemupdate.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.TTL;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Draft;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemUpdateTTL.SYSTEM_UPDATE_TTL;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;

@ExtendWith(MockitoExtension.class)
class SystemUpdateTTLTest {
    private CaseDetails<CaseData, State> beforeDetails;

    @InjectMocks
    private SystemUpdateTTL systemUpdateTTL;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        systemUpdateTTL.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
                .extracting(Event::getId)
                .contains(SYSTEM_UPDATE_TTL);
    }

    @Test
    void shouldSetSystemTTLForDraftStateAndTTLNotSetAboutToStartCallback() {

        CaseDetails<CaseData, State> beforeDetails = getCaseDetails(Draft);

        final AboutToStartOrSubmitResponse<CaseData, State> response = systemUpdateTTL.aboutToStart(beforeDetails);

        assertThat(response.getData().getRetainAndDisposeTimeToLive()).isNotNull();
        assertThat(response.getData().getRetainAndDisposeTimeToLive().getSystemTTL()).isEqualTo(LocalDate.now().plusMonths(6));
        assertThat(response.getData().getRetainAndDisposeTimeToLive().getSuspended()).isEqualTo(YesOrNo.NO);
    }


    @Test
    void shouldNotSetSystemTTLForDraftStateWhenTTLIsAlreadySetAboutToStartCallback() {

        CaseDetails<CaseData, State> beforeDetails = getCaseDetails(Draft);
        LocalDate systemTTL = LocalDate.of(2024, 11, 5);
        beforeDetails.getData().setRetainAndDisposeTimeToLive(TTL.builder()
                .systemTTL(systemTTL)
                .suspended(YesOrNo.YES)
                .build());

        final AboutToStartOrSubmitResponse<CaseData, State> response = systemUpdateTTL.aboutToStart(beforeDetails);

        assertThat(response.getData().getRetainAndDisposeTimeToLive()).isNull();
    }

    private CaseDetails<CaseData, State> getCaseDetails(State state) {
        return CaseDetails.<CaseData, State>builder()
                .state(state)
                .data(CaseData.builder()

                        .build())
                .build();
    }
}
