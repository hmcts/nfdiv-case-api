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
import uk.gov.hmcts.divorce.caseworker.service.task.SetHoldingDueDate;
import uk.gov.hmcts.divorce.citizen.notification.BailiffServiceSuccessfulNotification;
import uk.gov.hmcts.divorce.citizen.notification.BailiffServiceUnsuccessfulNotification;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.Bailiff;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerAddBailiffReturn.CASEWORKER_ADD_BAILIFF_RETURN;
import static uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType.BAILIFF;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAos;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingJsNullity;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Holding;
import static uk.gov.hmcts.divorce.divorcecase.model.SupplementaryCaseType.JUDICIAL_SEPARATION;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDate;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;

@ExtendWith(MockitoExtension.class)
class CaseworkerAddBailiffReturnTest {

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @Mock
    private BailiffServiceUnsuccessfulNotification unsuccessfulNotification;

    @Mock
    private BailiffServiceSuccessfulNotification successfulNotification;

    @Mock
    private SetHoldingDueDate setHoldingDueDate;

    @InjectMocks
    private CaseworkerAddBailiffReturn caseworkerAddBailiffReturn;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerAddBailiffReturn.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_ADD_BAILIFF_RETURN);
    }

    @Test
    void shouldSetStateToHoldingAndDueDateToHoldingDueDateIfSuccessfullyServed() {

        final LocalDate issueDate = getExpectedLocalDate();
        final LocalDate expectedDueDate = issueDate.plusDays(141);

        final CaseData caseData = CaseData.builder()
            .alternativeService(
                AlternativeService
                    .builder()
                    .serviceApplicationGranted(YES)
                    .alternativeServiceType(BAILIFF)
                    .bailiff(
                        Bailiff
                            .builder()
                            .successfulServedByBailiff(YES)
                            .build()
                    )
                    .build())
            .application(
                Application.builder()
                    .issueDate(issueDate)
                    .build()
            )
            .build();

        final CaseData expectedCaseData = CaseData.builder()
            .alternativeService(
                AlternativeService
                    .builder()
                    .serviceApplicationGranted(YES)
                    .alternativeServiceType(BAILIFF)
                    .bailiff(
                        Bailiff
                            .builder()
                            .successfulServedByBailiff(YES)
                            .build()
                    )
                    .build())
            .application(
                Application.builder()
                    .issueDate(issueDate)
                    .build())
            .dueDate(expectedDueDate)
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(12345L);

        final CaseDetails<CaseData, State> expectedCaseDetails = new CaseDetails<>();
        expectedCaseDetails.setData(expectedCaseData);
        expectedCaseDetails.setId(12345L);

        when(setHoldingDueDate.apply(caseDetails)).thenReturn(expectedCaseDetails);

        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerAddBailiffReturn.aboutToSubmit(caseDetails, null);

        assertThat(response.getState()).isEqualTo(Holding);
        assertThat(response.getData().getDueDate()).isEqualTo(expectedDueDate);
        verify(notificationDispatcher).send(successfulNotification, expectedCaseData, 12345L);
    }

    @Test
    void shouldSetStateToAwaitingJsNullityIfSuccessfullyServed() {

        final LocalDate issueDate = getExpectedLocalDate();

        final CaseData caseData = CaseData.builder()
            .alternativeService(
                AlternativeService
                    .builder()
                    .serviceApplicationGranted(YES)
                    .alternativeServiceType(BAILIFF)
                    .bailiff(
                        Bailiff
                            .builder()
                            .successfulServedByBailiff(YES)
                            .build()
                    )
                    .build())
            .application(
                Application.builder()
                    .issueDate(issueDate)
                    .build()
            )
            .supplementaryCaseType(JUDICIAL_SEPARATION)
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(12345L);

        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerAddBailiffReturn.aboutToSubmit(caseDetails, null);

        assertThat(response.getState()).isEqualTo(AwaitingJsNullity);
        verify(notificationDispatcher).send(successfulNotification, caseData, 12345L);
    }

    @Test
    void shouldSetStateToAwaitingAosIfNotSuccessfullyServed() {

        final LocalDate certificateOfServiceDate = getExpectedLocalDate();

        final CaseData caseData = CaseData.builder()
            .alternativeService(
                AlternativeService
                    .builder()
                    .alternativeServiceType(BAILIFF)
                    .bailiff(
                        Bailiff
                            .builder()
                            .successfulServedByBailiff(NO)
                            .certificateOfServiceDate(certificateOfServiceDate)
                            .build()
                    )
                    .build()
            )
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(12345L);

        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerAddBailiffReturn.aboutToSubmit(caseDetails, null);

        assertThat(response.getState()).isEqualTo(AwaitingAos);
        assertThat(response.getData().getDueDate()).isNull();
        verify(notificationDispatcher).send(unsuccessfulNotification, caseData, 12345L);
    }
}
