package uk.gov.hmcts.divorce.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.citizen.notification.GeneralApplicationReceivedNotification;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;

import java.time.Clock;
import java.util.Objects;
import javax.servlet.http.HttpServletRequest;

import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerAlternativeServiceApplication.CASEWORKER_SERVICE_RECEIVED;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDate;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class CaseworkerAlternativeServiceApplicationTest {

    private static final String DUMMY_AUTH_TOKEN = "DUMMY_AUTH_TOKEN";

    @Mock
    private GeneralApplicationReceivedNotification generalApplicationReceivedNotification;

    @Mock
    private CcdAccessService ccdAccessService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private Clock clock;

    @InjectMocks
    private CaseworkerAlternativeServiceApplication caseworkerAlternativeServiceApplication;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerAlternativeServiceApplication.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_SERVICE_RECEIVED);
    }

    @Test
    void shouldSendApp1NotificationsOnAboutToSubmit() {
        setupMocks(clock);
        CaseData caseData = caseData();
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder().id(1L).data(caseData).build();

        caseworkerAlternativeServiceApplication.aboutToSubmit(caseDetails, null);

        verify(generalApplicationReceivedNotification).sendToApplicant1(caseData, 1L);
    }

    @Test
    void shouldSendApp2NotificationsOnAboutToSubmit() {
        setupMocks(clock);
        when(ccdAccessService.isApplicant1(DUMMY_AUTH_TOKEN, 1L)).thenReturn(false);
        CaseData caseData = caseData();
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder().id(1L).data(caseData).build();

        caseworkerAlternativeServiceApplication.aboutToSubmit(caseDetails, null);

        verify(generalApplicationReceivedNotification).sendToApplicant2(caseData, 1L);
    }

    private CaseData caseData() {
        return CaseData.builder()
            .applicationType(ApplicationType.SOLE_APPLICATION)
            .build();
    }

    private void setupMocks(Clock mockClock) {
        if (Objects.nonNull(mockClock)) {
            setMockClock(mockClock);
        }
        when(request.getHeader(eq(AUTHORIZATION))).thenReturn(DUMMY_AUTH_TOKEN);
        when(ccdAccessService.isApplicant1(DUMMY_AUTH_TOKEN, 1L)).thenReturn(true);
    }

    @Test
    void shouldSetReceivedServiceAddedDateToCurrentDateWhenAboutToSubmitCallbackIsInvoked() {

        setMockClock(clock);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerAlternativeServiceApplication.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getErrors()).isNull();
        assertThat(response.getWarnings()).isNull();

        assertThat(response.getData().getAlternativeService().getReceivedServiceAddedDate()).isEqualTo(getExpectedLocalDate());
    }
}
