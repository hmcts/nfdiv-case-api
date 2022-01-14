package uk.gov.hmcts.divorce.common.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.citizen.notification.Applicant2RequestChangesNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.divorce.common.event.Applicant2RequestChanges.APPLICANT_2_REQUEST_CHANGES;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingApplicant1Response;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingApplicant2Response;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(SpringExtension.class)
public class Applicant2RequestChangesTest {

    @Mock
    private Applicant2RequestChangesNotification applicant2RequestChangesNotification;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @InjectMocks
    private Applicant2RequestChanges applicant2RequestChanges;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        applicant2RequestChanges.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(APPLICANT_2_REQUEST_CHANGES);
    }

    @Test
    void shouldNotifyApplicant1AndUpdateCaseState() {
        CaseData caseData = caseData();
        caseData.getApplication().setApplicant2ConfirmApplicant1Information(YesOrNo.NO);
        caseData.getApplication().setApplicant2ExplainsApplicant1IncorrectInformation("Not correct!");

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = applicant2RequestChanges.aboutToSubmit(details, details);

        verify(notificationDispatcher).send(applicant2RequestChangesNotification, caseData, details.getId());
        assertThat(response.getState()).isEqualTo(AwaitingApplicant1Response);
    }

    @Test
    void givenEventStartedWithInvalidCaseThenGiveValidationErrors() {
        CaseData caseData = caseData();

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = applicant2RequestChanges.aboutToSubmit(details, details);

        verifyNoInteractions(notificationDispatcher);
        assertThat(response.getState()).isEqualTo(AwaitingApplicant2Response);
    }
}
