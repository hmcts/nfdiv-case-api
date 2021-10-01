package uk.gov.hmcts.divorce.citizen.event;

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
import uk.gov.hmcts.divorce.citizen.notification.Applicant1ResubmitNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.divorce.citizen.event.CitizenApplicant1Resubmit.APPLICANT_1_RESUBMIT;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingApplicant1Response;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingApplicant2Response;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant2CaseData;

@ExtendWith(SpringExtension.class)
public class CitizenApplicant1ResubmitTest {

    @Mock
    private Applicant1ResubmitNotification applicant1ResubmitNotification;

    @InjectMocks
    private CitizenApplicant1Resubmit citizenApplicant1Resubmit;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        citizenApplicant1Resubmit.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(APPLICANT_1_RESUBMIT);
    }

    @Test
    void shouldNotifyApplicant1AndUpdateCaseStateAndSetApplicant2ConfirmationFieldsToNull() {
        CaseData caseData = validApplicant2CaseData();
        caseData.getApplication().setApplicant2ConfirmApplicant1Information(YesOrNo.NO);
        caseData.getApplication().setApplicant2ExplainsApplicant1IncorrectInformation("This information is incorrect.");

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenApplicant1Resubmit.aboutToSubmit(details, details);

        verify(applicant1ResubmitNotification).sendToApplicant1(caseData, details.getId());
        verify(applicant1ResubmitNotification).sendToApplicant2(caseData, details.getId());

        assertThat(response.getState()).isEqualTo(AwaitingApplicant2Response);
        assertThat(response.getData().getApplication().getApplicant2ConfirmApplicant1Information()).isEqualTo(null);
        assertThat(response.getData().getApplication().getApplicant2ExplainsApplicant1IncorrectInformation()).isEqualTo(null);
    }

    @Test
    void givenEventStartedWithInvalidCaseThenGiveValidationErrors() {
        CaseData caseData = caseData();

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenApplicant1Resubmit.aboutToSubmit(details, details);

        verifyNoInteractions(applicant1ResubmitNotification);
        assertThat(response.getState()).isEqualTo(AwaitingApplicant1Response);
    }
}
