package uk.gov.hmcts.divorce.citizen.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.citizen.event.CitizenUpdateApplication.CITIZEN_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Draft;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemUpdateTTL.SYSTEM_UPDATE_TTL;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_USER_USER_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
class CitizenUpdateApplicationTest {

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private CcdUpdateService ccdUpdateService;

    @InjectMocks
    private CitizenUpdateApplication citizenUpdateApplication;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        citizenUpdateApplication.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CITIZEN_UPDATE);
    }

    @Test
    void shouldCallSystemUpdateTTLEventForDraftStateAndTTLNotSetAboutToSubmitCallback() {

        var userDetails = UserInfo.builder().uid(SYSTEM_USER_USER_ID).build();
        var user = new User(SYSTEM_UPDATE_AUTH_TOKEN, userDetails);
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        CaseDetails<CaseData, State> beforeDetails = getCaseDetails(Draft);

        citizenUpdateApplication.aboutToSubmit(beforeDetails, beforeDetails);

        verify(ccdUpdateService).submitEvent(beforeDetails.getId(), SYSTEM_UPDATE_TTL, user, TEST_SERVICE_AUTH_TOKEN);
    }

    @Test
    void shouldNotCallSystemUpdateTTLEventForDraftStateAndTTLNotSetAboutToSubmitCallback() {

        CaseDetails<CaseData, State> beforeDetails = getCaseDetails(Submitted);

        citizenUpdateApplication.aboutToSubmit(beforeDetails, beforeDetails);

        verifyNoInteractions(ccdUpdateService);
    }

    private CaseDetails<CaseData, State> getCaseDetails(State state) {
        return CaseDetails.<CaseData, State>builder()
                .id(TEST_CASE_ID)
                .state(state)
                .data(CaseData.builder()

                        .build())
                .build();
    }
}
