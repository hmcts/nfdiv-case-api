package uk.gov.hmcts.divorce.caseworker.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.citizen.notification.BailiffServiceSuccessfulNotification;
import uk.gov.hmcts.divorce.citizen.notification.BailiffServiceUnsuccessfulNotification;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.Bailiff;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerAddBailiffReturn.CASEWORKER_ADD_BAILIFF_RETURN;
import static uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType.BAILIFF;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAos;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Holding;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDate;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;

@ExtendWith(MockitoExtension.class)
class CaseworkerAddBailiffReturnTest {

    private static final long DUE_DATE_OFFSET = 16L;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @Mock
    private BailiffServiceUnsuccessfulNotification unsuccessfulNotification;

    @Mock
    private BailiffServiceSuccessfulNotification successfulNotification;

    @InjectMocks
    private CaseworkerAddBailiffReturn caseworkerAddBailiffReturn;

    @BeforeEach
    void setdueDateOffset() {
        ReflectionTestUtils.setField(caseworkerAddBailiffReturn, "dueDateOffsetDays", DUE_DATE_OFFSET);
    }

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerAddBailiffReturn.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_ADD_BAILIFF_RETURN);
    }

    @Test
    void shouldSetStateToHoldingAndDueDateToCertificateOfServiceDatePlusDueDateOffsetIfSuccessfullyServed() {

        final LocalDate certificateOfServiceDate = getExpectedLocalDate();

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

        assertThat(response.getState()).isEqualTo(Holding);
        assertThat(response.getData().getDueDate()).isEqualTo(certificateOfServiceDate.plusDays(DUE_DATE_OFFSET));
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
