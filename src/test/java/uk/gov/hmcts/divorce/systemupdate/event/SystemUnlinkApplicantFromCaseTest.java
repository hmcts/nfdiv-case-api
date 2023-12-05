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
import uk.gov.hmcts.divorce.divorcecase.model.AcknowledgementOfService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Holding;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemUnlinkApplicantFromCase.SYSTEM_UNLINK_APPLICANT;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant1CaseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validJointApplicant1CaseData;

@ExtendWith(SpringExtension.class)
public class SystemUnlinkApplicantFromCaseTest {

    @Mock
    private CcdAccessService ccdAccessService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private IdamService idamService;

    @InjectMocks
    private SystemUnlinkApplicantFromCase systemUnlinkApplicantFromCase;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        systemUnlinkApplicantFromCase.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SYSTEM_UNLINK_APPLICANT);
    }

    @Test
    public void shouldUnlinkApplicant() {
        final var caseDetails = new CaseDetails<CaseData, State>();
        var caseData = caseData();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        User user = new User("dummy-user-token", UserInfo.builder().uid(TEST_AUTHORIZATION_TOKEN).build());
        when(request.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(idamService.retrieveUser(TEST_AUTHORIZATION_TOKEN)).thenReturn(user);

        systemUnlinkApplicantFromCase.aboutToSubmit(caseDetails, caseDetails);

        verify(ccdAccessService).unlinkUserFromCase(anyLong(), eq(TEST_AUTHORIZATION_TOKEN));
    }

    @Test
    public void shouldNotUnlinkApplicantIfJointApplicationAndPostSubmission() {
        final var caseDetails = new CaseDetails<CaseData, State>();
        var caseData = validJointApplicant1CaseData();
        caseDetails.setData(caseData);
        caseDetails.setState(Holding);
        caseDetails.setId(TEST_CASE_ID);

        User user = new User("dummy-user-token", UserInfo.builder().uid(TEST_AUTHORIZATION_TOKEN).build());
        when(request.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(idamService.retrieveUser(TEST_AUTHORIZATION_TOKEN)).thenReturn(user);

        final AboutToStartOrSubmitResponse<CaseData, State>  response =
            systemUnlinkApplicantFromCase.aboutToSubmit(caseDetails, caseDetails);

        verifyNoInteractions(ccdAccessService);
        assertThat(response.getErrors())
            .containsExactly(
                "The Joint Application has already been submitted.");
    }

    @Test
    public void shouldNotUnlinkApplicantIfSoleApplicationAndAOSSubmitted() {
        final var caseDetails = new CaseDetails<CaseData, State>();
        var caseData = validApplicant1CaseData();
        caseDetails.setData(caseData);
        caseDetails.setState(Holding);
        final AcknowledgementOfService acknowledgementOfService = AcknowledgementOfService.builder()
            .dateAosSubmitted(LocalDateTime.now())
            .build();
        caseData.setAcknowledgementOfService(acknowledgementOfService);
        caseDetails.setId(TEST_CASE_ID);

        User user = new User("dummy-user-token", UserInfo.builder().uid(TEST_AUTHORIZATION_TOKEN).build());
        when(request.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(idamService.retrieveUser(TEST_AUTHORIZATION_TOKEN)).thenReturn(user);

        final AboutToStartOrSubmitResponse<CaseData, State>  response =
            systemUnlinkApplicantFromCase.aboutToSubmit(caseDetails, caseDetails);

        verifyNoInteractions(ccdAccessService);
        assertThat(response.getErrors())
            .containsExactly(
                "The Acknowledgement Of Service has already been submitted.");
    }
}
