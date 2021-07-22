package uk.gov.hmcts.divorce.caseworker.service.updater;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.caseworker.service.notification.NoticeOfProceedingsNotification;
import uk.gov.hmcts.divorce.caseworker.service.notification.PersonalServiceNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.COURT_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.PERSONAL_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAos;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.respondent;
import static uk.gov.hmcts.divorce.testutil.UpdaterTestUtil.caseDataContext;

@ExtendWith(MockitoExtension.class)
class SendAosNotificationsTest {

    @Mock
    private PersonalServiceNotification personalServiceNotification;

    @Mock
    private NoticeOfProceedingsNotification noticeOfProceedingsNotification;

    @InjectMocks
    private SendAosNotifications sendAosNotifications;

    @Test
    void shouldSendPersonalServiceNotificationIfPersonalServiceApplication() {

        final var caseData = caseData();
        caseData.getApplication().setSolServiceMethod(PERSONAL_SERVICE);
        final var caseDetails = CaseDetails.<CaseData, State>builder()
            .id(TEST_CASE_ID)
            .state(AwaitingAos)
            .data(caseData)
            .build();

        final var caseDataContext = caseDataContext(caseData);

        sendAosNotifications.accept(caseDataContext);

        assertThat(caseDataContext.getCaseData()).isEqualTo(caseData);
        verify(personalServiceNotification).send(caseData, TEST_CASE_ID);
        verifyNoInteractions(noticeOfProceedingsNotification);
    }

    @Test
    void shouldSendNoticeOfProceedingsIfNotPersonalServiceApplication() {

        final var caseData = caseData();
        caseData.getApplication().setSolServiceMethod(COURT_SERVICE);
        caseData.setApplicant2(respondent());
        final var caseDetails = CaseDetails.<CaseData, State>builder()
            .id(TEST_CASE_ID)
            .state(AwaitingAos)
            .data(caseData)
            .build();

        final var caseDataContext = caseDataContext(caseData);

        sendAosNotifications.accept(caseDataContext);

        assertThat(caseDataContext.getCaseData()).isEqualTo(caseData);
        verify(noticeOfProceedingsNotification).send(caseData, TEST_CASE_ID);
        verifyNoInteractions(personalServiceNotification);
    }
}