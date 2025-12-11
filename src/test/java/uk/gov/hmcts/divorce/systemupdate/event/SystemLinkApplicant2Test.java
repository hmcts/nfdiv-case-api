package uk.gov.hmcts.divorce.systemupdate.event;

import jakarta.servlet.http.HttpServletRequest;
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
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseInvite;
import uk.gov.hmcts.divorce.divorcecase.model.CaseInviteApp1;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemLinkApplicant2.SYSTEM_LINK_APPLICANT_2;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(SpringExtension.class)
class SystemLinkApplicant2Test {

    @Mock
    private CcdAccessService ccdAccessService;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private SystemLinkApplicant2 systemLinkApplicant2;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        systemLinkApplicant2.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SYSTEM_LINK_APPLICANT_2);
    }

    @Test
    void shouldRemoveAccessCodeAfterLinkingApplicationAndSetApplicant2OfflineNo() {
        final CaseData caseData = caseData();
        caseData.setCaseInvite(
            CaseInvite.builder()
                .accessCode("D8BC9AQR")
                .applicant2UserId("Applicant2Id")
                .build());
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        when(httpServletRequest.getHeader(AUTHORIZATION))
            .thenReturn("auth header");

        final AboutToStartOrSubmitResponse<CaseData, State> response = systemLinkApplicant2.aboutToSubmit(details, details);

        assertThat(response.getData().getCaseInvite().accessCode()).isNull();
        assertThat(response.getData().getApplicant2().getOffline()).isEqualTo(YesOrNo.NO);
        verify(ccdAccessService).linkRespondentToApplication(eq("auth header"), eq(TEST_CASE_ID), eq("Applicant2Id"));
    }

    @Test
    void shouldIgnoreRequestDataRelatingToApplicant1CaseInvite() {
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData());

        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        beforeDetails.setId(TEST_CASE_ID);
        beforeDetails.setData(caseData());

        details.getData().setCaseInvite(
            CaseInvite.builder()
                .accessCode("D8BC9AQR")
                .applicant2UserId("Applicant2Id")
                .build());

        CaseInviteApp1 app1BeforeCaseInvite = CaseInviteApp1.builder().accessCodeApplicant1("BEFORE").build();
        CaseInviteApp1 app1AfterCaseInvite = CaseInviteApp1.builder().accessCodeApplicant1("AFTER").build();
        beforeDetails.getData().setCaseInviteApp1(app1BeforeCaseInvite);
        details.getData().setCaseInviteApp1(app1AfterCaseInvite);

        when(httpServletRequest.getHeader(AUTHORIZATION))
            .thenReturn("auth header");

        final AboutToStartOrSubmitResponse<CaseData, State> response = systemLinkApplicant2.aboutToSubmit(details, beforeDetails);

        assertThat(response.getData().getCaseInvite().accessCode()).isNull();
        assertThat(response.getData().getApplicant2().getOffline()).isEqualTo(YesOrNo.NO);
        assertThat(response.getData().getCaseInviteApp1()).isEqualTo(app1BeforeCaseInvite);
        verify(ccdAccessService).linkRespondentToApplication(eq("auth header"), eq(TEST_CASE_ID), eq("Applicant2Id"));
    }
}
