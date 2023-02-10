package uk.gov.hmcts.divorce.systemupdate.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.divorce.common.notification.AwaitingFinalOrderNotification;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.systemupdate.service.task.GenerateApplyForFinalOrderDocument;
import uk.gov.hmcts.divorce.systemupdate.service.task.GenerateD36Form;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemProgressCaseToAwaitingFinalOrder.SYSTEM_PROGRESS_CASE_TO_AWAITING_FINAL_ORDER;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getApplicant;

@ExtendWith(MockitoExtension.class)
class SystemProgressCaseToAwaitingFinalOrderTest {

    @InjectMocks
    private SystemProgressCaseToAwaitingFinalOrder systemProgressCaseToAwaitingFinalOrder;

    @Mock
    private AwaitingFinalOrderNotification awaitingFinalOrderNotification;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @Mock
    private GenerateD36Form generateD36Form;

    @Mock
    private GenerateApplyForFinalOrderDocument generateApplyForFinalOrderDocument;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        systemProgressCaseToAwaitingFinalOrder.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SYSTEM_PROGRESS_CASE_TO_AWAITING_FINAL_ORDER);
    }

    @Test
    void shouldGenerateFinalOrderLettersIfApplicant1Offline() {
        final CaseData caseData = caseData();
        caseData.setApplicant1(getApplicant());
        caseData.setApplicationType(ApplicationType.SOLE_APPLICATION);
        caseData.getApplicant1().setOffline(YES);
        caseData.getApplicant2().setOffline(NO);
        caseData.getApplicant2().setEmail("test@email.com");
        final CaseDetails<CaseData, State> details = CaseDetails.<CaseData, State>builder().id(TEST_CASE_ID).data(caseData).build();

        systemProgressCaseToAwaitingFinalOrder.aboutToSubmit(details, details);

        verify(generateD36Form).generateD36Document(caseData, TEST_CASE_ID);
        verify(generateApplyForFinalOrderDocument).generateApplyForFinalOrder(
            caseData,
            TEST_CASE_ID,
            caseData.getApplicant1(),
            caseData.getApplicant2(),
            true
        );

        verifyNoMoreInteractions(generateD36Form);
        verifyNoMoreInteractions(generateApplyForFinalOrderDocument);
    }

    @Test
    void shouldGenerateFinalOrderLettersIfApplicant2OfflineInJointApplication() {
        final CaseData caseData = caseData();
        caseData.setApplicant1(getApplicant());
        caseData.setApplicationType(ApplicationType.JOINT_APPLICATION);
        caseData.getApplicant1().setOffline(NO);
        caseData.getApplicant2().setOffline(YES);
        caseData.getApplicant2().setEmail(null);
        final CaseDetails<CaseData, State> details = CaseDetails.<CaseData, State>builder().id(TEST_CASE_ID).data(caseData).build();

        systemProgressCaseToAwaitingFinalOrder.aboutToSubmit(details, details);

        verify(generateD36Form).generateD36Document(caseData, TEST_CASE_ID);
        verify(generateApplyForFinalOrderDocument).generateApplyForFinalOrder(
            caseData,
            TEST_CASE_ID,
            caseData.getApplicant2(),
            caseData.getApplicant1(),
            false
        );

        verifyNoMoreInteractions(generateD36Form);
        verifyNoMoreInteractions(generateApplyForFinalOrderDocument);
    }

    @Test
    void shouldNotGenerateFinalOrderLettersIfApplicantsAreOnline() {
        final CaseData caseData = caseData();
        caseData.setApplicant1(getApplicant());
        caseData.setApplicationType(ApplicationType.SOLE_APPLICATION);
        caseData.getApplicant1().setOffline(NO);
        caseData.getApplicant2().setOffline(NO);
        caseData.getApplicant2().setEmail("test@email.com");
        final CaseDetails<CaseData, State> details = CaseDetails.<CaseData, State>builder().data(caseData).build();

        systemProgressCaseToAwaitingFinalOrder.aboutToSubmit(details, details);

        verifyNoInteractions(generateD36Form);
        verifyNoInteractions(generateApplyForFinalOrderDocument);
    }

    @Test
    void shouldNotGenerateFinalOrderLettersForApplicant2IfSoleCase() {
        final CaseData caseData = caseData();
        caseData.setApplicant1(getApplicant());
        caseData.setApplicationType(ApplicationType.SOLE_APPLICATION);
        caseData.getApplicant1().setOffline(YES);
        caseData.getApplicant2().setOffline(YES);
        caseData.getApplicant2().setEmail("test@email.com");
        final CaseDetails<CaseData, State> details = CaseDetails.<CaseData, State>builder().data(caseData).build();

        systemProgressCaseToAwaitingFinalOrder.aboutToSubmit(details, details);

        verify(generateApplyForFinalOrderDocument).generateApplyForFinalOrder(
            caseData,
            TEST_CASE_ID,
            caseData.getApplicant1(),
            caseData.getApplicant2(),
            true
        );
        verifyNoMoreInteractions(generateApplyForFinalOrderDocument);
    }

    @Test
    void shouldSendNotifications() {
        final CaseData caseData = caseData();
        caseData.setApplicant1(getApplicant());
        caseData.setApplicationType(ApplicationType.SOLE_APPLICATION);
        final CaseDetails<CaseData, State> details = CaseDetails.<CaseData, State>builder().data(caseData).build();

        systemProgressCaseToAwaitingFinalOrder.submitted(details, details);

        verify(notificationDispatcher).send(awaitingFinalOrderNotification, caseData, details.getId());
        verifyNoMoreInteractions(notificationDispatcher);
    }
}
