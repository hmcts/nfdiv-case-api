package uk.gov.hmcts.divorce.solicitor.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.solicitor.event.page.HelpWithFeesPageForApplicant2;
import uk.gov.hmcts.divorce.solicitor.event.page.MarriageIrretrievablyBrokenForApplicant2;
import uk.gov.hmcts.divorce.solicitor.service.SolicitorSubmitJointApplicationService;

import javax.servlet.http.HttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.solicitor.event.SolicitorSubmitJointApplication.SOLICITOR_SUBMIT_JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class SolicitorSubmitJointApplicationTest {

    @Mock
    private MarriageIrretrievablyBrokenForApplicant2 marriageIrretrievablyBrokenForApplicant2;

    @Mock
    private HelpWithFeesPageForApplicant2 helpWithFeesPageForApplicant2;

    @Mock
    private SolicitorSubmitJointApplicationService solicitorSubmitJointApplicationService;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private SolicitorSubmitJointApplication solicitorSubmitJointApplication;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        solicitorSubmitJointApplication.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SOLICITOR_SUBMIT_JOINT_APPLICATION);
    }

    @Test
    void shouldInvokeSubmitEventForApprovalOrRequestingChangesOnSubmittedCallback() {
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);

        final var caseData = caseData();
        caseData.getApplication().setApplicant2ConfirmApplicant1Information(YES);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        solicitorSubmitJointApplication.submitted(caseDetails, caseDetails);

        verify(solicitorSubmitJointApplicationService).submitEventForApprovalOrRequestingChanges(caseDetails, TEST_AUTHORIZATION_TOKEN);
    }
}
