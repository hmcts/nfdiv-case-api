package uk.gov.hmcts.divorce.legaladvisor.event;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralReferral;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralReferralDecision.APPROVE;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralReferralDecision.OTHER;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralReferralDecision.REFUSE;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralReferralType.EXPEDITED_CASE;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ExpeditedCase;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.legaladvisor.event.LegalAdvisorGeneralConsideration.LEGAL_ADVISOR_GENERAL_CONSIDERATION;
import static uk.gov.hmcts.divorce.legaladvisor.event.LegalAdvisorMakeDecision.LEGAL_ADVISOR_MAKE_DECISION;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDate;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SYSTEM_AUTHORISATION_TOKEN;

@ExtendWith(MockitoExtension.class)
class LegalAdvisorGeneralConsiderationTest {

    @Mock
    private Clock clock;

    @Mock
    private HttpServletRequest request;

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private CcdUpdateService ccdUpdateService;

    @InjectMocks
    private LegalAdvisorGeneralConsideration legalAdvisorGeneralConsideration;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        legalAdvisorGeneralConsideration.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(LEGAL_ADVISOR_GENERAL_CONSIDERATION);
    }

    @Test
    void shouldSetGeneralReferralDecisionDateAndCreateGeneralReferralsListAndBlankGeneralReferralFieldsWhenAboutToSubmitIsInvoked() {

        setMockClock(clock);

        final CaseData caseData = CaseData
            .builder()
            .generalReferral(
                GeneralReferral.builder()
                    .generalReferralDecision(APPROVE)
                    .generalReferralDecisionReason("approved")
                    .build())
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(12345L);
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            legalAdvisorGeneralConsideration.aboutToSubmit(caseDetails, null);

        final CaseData responseData = response.getData();
        final GeneralReferral responseGeneralReferral = responseData.getGeneralReferrals().get(0).getValue();
        assertThat(responseData.getGeneralReferrals()).hasSize(1);
        assertThat(responseGeneralReferral.getGeneralReferralDecisionDate()).isEqualTo(getExpectedLocalDate());
        assertThat(responseGeneralReferral.getGeneralReferralDecision()).isEqualTo(APPROVE);
        assertThat(responseGeneralReferral.getGeneralReferralDecisionReason()).isEqualTo("approved");
        assertThat(responseData.getGeneralReferral()).hasAllNullFieldsOrPropertiesExcept("generalReferralFee");
        assertThat(responseData.getGeneralReferral().getGeneralReferralFee()).hasAllNullFieldsOrProperties();
    }

    @Test
    void shouldNotSendEmailNotificationsIfGeneralReferralIsNotApproved() {

        setMockClock(clock);

        final CaseData caseData = CaseData
            .builder()
            .generalReferral(
                GeneralReferral.builder()
                    .generalReferralDecision(REFUSE)
                    .generalReferralDecisionReason("rejected")
                    .build())
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(12345L);
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            legalAdvisorGeneralConsideration.aboutToSubmit(caseDetails, null);

        final CaseData responseData = response.getData();
        final GeneralReferral responseGeneralReferral = responseData.getGeneralReferrals().get(0).getValue();
        assertThat(responseData.getGeneralReferrals()).hasSize(1);
        assertThat(responseGeneralReferral.getGeneralReferralDecisionDate()).isEqualTo(getExpectedLocalDate());
        assertThat(responseGeneralReferral.getGeneralReferralDecision()).isEqualTo(REFUSE);
        assertThat(responseGeneralReferral.getGeneralReferralDecisionReason()).isEqualTo("rejected");
        assertThat(responseData.getGeneralReferral()).hasAllNullFieldsOrPropertiesExcept("generalReferralFee");
        assertThat(responseData.getGeneralReferral().getGeneralReferralFee()).hasAllNullFieldsOrProperties();
    }

    @Test
    void shouldSetDecisionDateAndPreserveOldReferralsAndResetExistingGeneralReferralExcludingUrgentCaseFlagWhenAboutToSubmitIsInvoked() {

        setMockClock(clock);

        final List<ListValue<GeneralReferral>> generalReferrals = new ArrayList<>();
        generalReferrals.add(
            ListValue.<GeneralReferral>builder()
                .value(GeneralReferral.builder()
                    .generalReferralDecision(OTHER)
                    .generalReferralDecisionReason("reason")
                    .build())
                .build());

        final CaseData caseData = CaseData
            .builder()
            .generalReferral(
                GeneralReferral.builder()
                    .generalReferralDecision(APPROVE)
                    .generalReferralUrgentCase(YesOrNo.YES)
                    .generalReferralDecisionReason("approved")
                    .build())
            .generalReferrals(generalReferrals)
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            legalAdvisorGeneralConsideration.aboutToSubmit(caseDetails, null);

        final CaseData responseData = response.getData();
        assertThat(responseData.getGeneralReferrals()).hasSize(2);
        assertThat(responseData.getGeneralReferrals().get(0).getValue().getGeneralReferralDecisionDate()).isEqualTo(getExpectedLocalDate());
        assertThat(responseData.getGeneralReferrals().get(0).getValue().getGeneralReferralDecision()).isEqualTo(APPROVE);
        assertThat(responseData.getGeneralReferrals().get(0).getValue().getGeneralReferralDecisionReason()).isEqualTo("approved");

        assertThat(responseData.getGeneralReferrals().get(1).getValue().getGeneralReferralDecision()).isEqualTo(OTHER);
        assertThat(responseData.getGeneralReferrals().get(1).getValue().getGeneralReferralDecisionReason()).isEqualTo("reason");

        assertThat(responseData.getGeneralReferral().getGeneralReferralUrgentCase()).isEqualTo(YesOrNo.YES);
        assertThat(responseData.getGeneralReferral())
            .hasAllNullFieldsOrPropertiesExcept("generalReferralUrgentCase", "generalReferralFee");
        assertThat(responseData.getGeneralReferral().getGeneralReferralFee()).hasAllNullFieldsOrProperties();
    }

    @Test
    void shouldCallLegalAdvisorMakeDecisionEventWhenExpeditedCaseAndIsAJudgeAndSubmittedEventIsInvoked() {

        UserInfo userInfo = UserInfo.builder().name("test judge").roles(List.of(JUDGE.getRole())).build();
        final User user = new User(TEST_AUTHORIZATION_TOKEN, userInfo);

        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(request.getHeader(AUTHORIZATION)).thenReturn(TEST_SYSTEM_AUTHORISATION_TOKEN);
        when(idamService.retrieveUser(TEST_SYSTEM_AUTHORISATION_TOKEN)).thenReturn(user);

        final List<ListValue<GeneralReferral>> generalReferrals = new ArrayList<>();
        generalReferrals.add(
                ListValue.<GeneralReferral>builder()
                        .value(GeneralReferral.builder()
                                .generalReferralDecision(APPROVE)
                                .generalReferralDecisionReason("reason")
                                .generalReferralType(EXPEDITED_CASE)
                                .build())
                        .build());

        final CaseData caseData = CaseData
                .builder()
                .generalReferrals(generalReferrals)
                .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(ExpeditedCase);

        final SubmittedCallbackResponse response =
                legalAdvisorGeneralConsideration.submitted(caseDetails, caseDetails);

        verify(ccdUpdateService).submitEvent(caseDetails.getId(), LEGAL_ADVISOR_MAKE_DECISION, user, TEST_SERVICE_AUTH_TOKEN);
        verify(authTokenGenerator).generate();
    }

    @Test
    void shouldNotCallLegalAdvisorMakeDecisionEventWhenExpeditedCaseAndIsNotAJudgeAndSubmittedEventIsInvoked() {

        UserInfo userInfo = UserInfo.builder().name("Caseworker").roles(List.of(CASE_WORKER.getRole())).build();
        final User user = new User(TEST_AUTHORIZATION_TOKEN, userInfo);

        when(request.getHeader(AUTHORIZATION)).thenReturn(TEST_SYSTEM_AUTHORISATION_TOKEN);
        when(idamService.retrieveUser(TEST_SYSTEM_AUTHORISATION_TOKEN)).thenReturn(user);

        final List<ListValue<GeneralReferral>> generalReferrals = new ArrayList<>();
        generalReferrals.add(
                ListValue.<GeneralReferral>builder()
                        .value(GeneralReferral.builder()
                                .generalReferralDecision(APPROVE)
                                .generalReferralDecisionReason("reason")
                                .generalReferralType(EXPEDITED_CASE)
                                .build())
                        .build());

        final CaseData caseData = CaseData
                .builder()
                .generalReferrals(generalReferrals)
                .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(ExpeditedCase);

        final SubmittedCallbackResponse response =
                legalAdvisorGeneralConsideration.submitted(caseDetails, caseDetails);

        verifyNoInteractions(ccdUpdateService);
        verifyNoInteractions(authTokenGenerator);
    }
}
