package uk.gov.hmcts.divorce.systemupdate.event;

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
import uk.gov.hmcts.divorce.citizen.notification.conditionalorder.ApplyForConditionalOrderNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import javax.servlet.http.HttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemJointApplicantsApplyForConditionalOrder.SYSTEM_NOTIFY_APPLICANTS_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(SpringExtension.class)
public class SystemJointApplicantsApplyForConditionalOrderTest {

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private ApplyForConditionalOrderNotification notification;

    @InjectMocks
    private SystemJointApplicantsApplyForConditionalOrder underTest;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        underTest.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SYSTEM_NOTIFY_APPLICANTS_CONDITIONAL_ORDER);
    }

    @Test
    void shouldSetApplicant1NotifiedCanApplyForConditionalOrderToYes() {
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> details = CaseDetails.<CaseData, State>builder().id(1L).data(caseData).build();

        when(httpServletRequest.getHeader(AUTHORIZATION))
            .thenReturn("auth header");

        final AboutToStartOrSubmitResponse<CaseData, State> response = underTest.aboutToSubmit(details, details);

        verify(notification).sendToApplicant1(caseData, details.getId());
        verify(notification).sendToApplicant2(caseData, details.getId());
        assertThat(response.getData().getApplication().getJointApplicantsNotifiedCanApplyForConditionalOrder()).isEqualTo(YesOrNo.YES);
    }
}
