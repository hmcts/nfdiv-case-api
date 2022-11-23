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
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.caseworker.service.notification.FinalOrderGrantedNotification;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateFinalOrder;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateFinalOrderCoverLetter;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerGrantFinalOrder.CASEWORKER_GRANT_FINAL_ORDER;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDateTime;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class CaseworkerGrantFinalOrderTest {

    @Mock
    private Clock clock;

    @Mock
    private GenerateFinalOrder generateFinalOrder;

    @Mock
    private GenerateFinalOrderCoverLetter generateFinalOrderCoverLetter;

    @Mock
    private FinalOrderGrantedNotification finalOrderGrantedNotification;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @InjectMocks
    private CaseworkerGrantFinalOrder caseworkerGrantFinalOrder;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerGrantFinalOrder.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_GRANT_FINAL_ORDER);
    }

    @Test
    void shouldPopulateFinalOrderGrantedDateAndSendEmailIfFinalOrderIsEligible() {
        final CaseData caseData = caseData();
        caseData.setFinalOrder(
            FinalOrder.builder()
                .granted(Set.of(FinalOrder.Granted.YES))
                .dateFinalOrderEligibleFrom(LocalDate.now())
                .build()
        );

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        setMockClock(clock);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerGrantFinalOrder.aboutToSubmit(details, details);

        assertThat(response.getData().getFinalOrder().getGrantedDate()).isNotNull();
        assertThat(response.getData().getFinalOrder().getGrantedDate()).isEqualTo(getExpectedLocalDateTime());

        verify(generateFinalOrder).apply(details);
    }

    @Test
    void shouldGenerateFinalOrderGrantedCoverLetterIfApplicantOrRespondentIsOffline() {
        final CaseData caseData = caseData();
        caseData.getApplicant1().setOffline(YesOrNo.YES);
        caseData.getApplicant2().setOffline(YesOrNo.YES);
        caseData.getApplicant2().setEmail("");
        caseData.setFinalOrder(
            FinalOrder.builder()
                .granted(Set.of(FinalOrder.Granted.YES))
                .dateFinalOrderEligibleFrom(LocalDate.now())
                .build()
        );

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        setMockClock(clock);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerGrantFinalOrder.aboutToSubmit(details, details);

        assertThat(response.getData().getFinalOrder().getGrantedDate()).isNotNull();
        assertThat(response.getData().getFinalOrder().getGrantedDate()).isEqualTo(getExpectedLocalDateTime());

        verify(generateFinalOrderCoverLetter).apply(details);
        verify(generateFinalOrder).apply(details);
    }

    @Test
    void shouldReturnErrorsIfDateFinalOrderEligibleFromIsInFuture() {
        final CaseData caseData = caseData();
        caseData.setFinalOrder(
            FinalOrder.builder()
                .granted(Set.of(FinalOrder.Granted.YES))
                .dateFinalOrderEligibleFrom(LocalDate.now().plusDays(1))
                .build()
        );

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        setMockClock(clock);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerGrantFinalOrder.aboutToSubmit(details, details);

        assertThat(response.getData().getFinalOrder().getGrantedDate()).isNull();
        assertThat(response.getErrors()).contains("Case is not yet eligible for Final Order");

        verifyNoInteractions(generateFinalOrder);
    }

    @Test
    void shouldSendNotificationWhenSubmittedCallbackIsInvoked() {
        final CaseData caseData = caseData();
        caseData.setFinalOrder(
            FinalOrder.builder()
                .granted(Set.of(FinalOrder.Granted.YES))
                .dateFinalOrderEligibleFrom(LocalDate.now())
                .build()
        );

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        caseworkerGrantFinalOrder.submitted(details, details);

        verify(notificationDispatcher).send(finalOrderGrantedNotification, caseData, TEST_CASE_ID);
    }
}
