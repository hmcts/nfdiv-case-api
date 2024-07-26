package uk.gov.hmcts.divorce.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.citizen.notification.GeneralApplicationReceivedNotification;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import java.time.Clock;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerAlternativeServiceApplication.CASEWORKER_SERVICE_RECEIVED;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDate;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class CaseworkerAlternativeServiceApplicationTest {

    @Mock
    private GeneralApplicationReceivedNotification generalApplicationReceivedNotification;

    @Mock
    private NotificationDispatcher notificationDispatcher;

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
        setMockClock(clock);

        CaseData caseData = caseData();
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder().id(TEST_CASE_ID).data(caseData).build();

        caseworkerAlternativeServiceApplication.aboutToSubmit(caseDetails, null);

        verify(notificationDispatcher).send(generalApplicationReceivedNotification, caseData, caseDetails.getId());
    }

    private CaseData caseData() {
        return CaseData.builder()
            .applicationType(ApplicationType.SOLE_APPLICATION)
            .build();
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

    static Stream<Arguments> provideTestArguments() {
        return Stream.of(
            Arguments.of(YesOrNo.NO, null, State.AwaitingServiceConsideration),
            Arguments.of(YesOrNo.YES, AlternativeServiceType.BAILIFF, State.AwaitingServicePayment),
            Arguments.of(YesOrNo.NO, AlternativeServiceType.BAILIFF, State.AwaitingBailiffReferral)
        );
    }

    @ParameterizedTest
    @MethodSource("provideTestArguments")
    void parameterizedAboutToSubmitCallbackTest(YesOrNo feeRequired, AlternativeServiceType serviceType, State expectedState) {

        setMockClock(clock);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder()
            .alternativeService(AlternativeService.builder()
                .alternativeServiceFeeRequired(feeRequired)
                .alternativeServiceType(serviceType)
                .build())
            .build();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerAlternativeServiceApplication.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getErrors()).isNull();
        assertThat(response.getWarnings()).isNull();
        assertThat(response.getData().getAlternativeService().getReceivedServiceAddedDate()).isEqualTo(getExpectedLocalDate());
        assertThat(response.getState()).isEqualTo(expectedState);
    }
}
