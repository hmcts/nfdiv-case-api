package uk.gov.hmcts.divorce.caseworker.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.notification.InviteApplicantToCaseNotification;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getApplicant;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant1CaseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant2CaseData;

@ExtendWith(MockitoExtension.class)
class EmailUpdateServiceTest {
    @Mock
    InviteApplicantToCaseNotification inviteApplicantToCaseNotification;
    @Mock
    CcdAccessService ccdAccessService;
    @InjectMocks
    private EmailUpdateService emailUpdateService;

    @Test
    void shouldNotInviteWhenEmailForApplicantIsNull() {
        final CaseData caseData = validApplicant1CaseData();
        caseData.getApplicant1().setEmail(null);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        final CaseDetails<CaseData, State> newDetails = emailUpdateService.processEmailUpdate(details, details, true);

        verifyNoInteractions(inviteApplicantToCaseNotification);
    }

    @Test
    void shouldNotInviteWhenEmailForApplicantIsBlank() {
        final CaseData caseData = validApplicant1CaseData();
        caseData.getApplicant1().setEmail("");

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        final CaseDetails<CaseData, State> newDetails = emailUpdateService.processEmailUpdate(details, details, true);

        verifyNoInteractions(inviteApplicantToCaseNotification);
    }

    @Test
    void shouldNotInviteWhenApplicantIsRepresented() {
        final CaseData caseData = validApplicant1CaseData();
        caseData.getApplicant1().setSolicitorRepresented(YesOrNo.YES);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        final CaseDetails<CaseData, State> newDetails = emailUpdateService.processEmailUpdate(details, details, true);

        verifyNoInteractions(inviteApplicantToCaseNotification);
    }

    @Test
    void shouldNotInviteWhenApplicant2AndSoleApplicationNotIssuedYet() {
        final CaseData caseData = validApplicant2CaseData();
        caseData.setApplicationType(ApplicationType.SOLE_APPLICATION);
        caseData.getApplication().setIssueDate(null);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        final CaseDetails<CaseData, State> newDetails = emailUpdateService.processEmailUpdate(details, details, false);

        verifyNoInteractions(inviteApplicantToCaseNotification);
    }

    @Test
    void shouldInviteWhenApplicant2AndSoleApplicationIsIssued() {
        final CaseData caseData = validApplicant2CaseData();
        caseData.setApplicationType(ApplicationType.SOLE_APPLICATION);
        caseData.getApplication().setIssueDate(LocalDate.of(2021, 4, 28));

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        final CaseDetails<CaseData, State> newDetails = emailUpdateService.processEmailUpdate(details, details, false);

        verify(inviteApplicantToCaseNotification).send(caseData,TEST_CASE_ID,false);
    }

    @Test
    void shouldInviteApplicant2EvenWhenJointApplicationNotIssuedYet() {
        final CaseData caseData = validApplicant2CaseData();
        caseData.getApplication().setIssueDate(null);
        caseData.getApplicant2().setEmail(TEST_USER_EMAIL);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        final CaseDetails<CaseData, State> newDetails = emailUpdateService.processEmailUpdate(details, details, false);

        verify(inviteApplicantToCaseNotification).send(caseData,TEST_CASE_ID,false);
    }

    @Test
    void shouldSetCaseInviteForApplicant1() {
        final CaseData caseData = validApplicant1CaseData();

        caseData.setCaseInviteApp1(null);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        final CaseDetails<CaseData, State> newDetails = emailUpdateService.processEmailUpdate(details, details, true);

        assertThat(newDetails.getData().getCaseInviteApp1().accessCodeApplicant1()).isNotBlank();
        assertThat(newDetails.getData().getCaseInviteApp1().accessCodeApplicant1().length()).isEqualTo(8);
        assertThat(newDetails.getData().getCaseInviteApp1().accessCodeApplicant1()).doesNotContain("I", "O", "U", "0", "1");
    }

    @Test
    void shouldSetCaseInviteForApplicant2() {
        final CaseData caseData = validApplicant2CaseData();

        caseData.setCaseInvite(null);
        caseData.getApplicant2().setEmail(TEST_USER_EMAIL);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        final CaseDetails<CaseData, State> newDetails = emailUpdateService.processEmailUpdate(details, details, false);

        assertThat(newDetails.getData().getCaseInvite().accessCode()).isNotBlank();
        assertThat(newDetails.getData().getCaseInvite().accessCode().length()).isEqualTo(8);
        assertThat(newDetails.getData().getCaseInvite().accessCode()).doesNotContain("I", "O", "U", "0", "1");
    }

    static Stream<Arguments> provideTestArguments() {
        return Stream.of(
            Arguments.of(ApplicationType.SOLE_APPLICATION, true, true, true, YesOrNo.YES, YesOrNo.YES, false),
            Arguments.of(ApplicationType.SOLE_APPLICATION, true, false, true, YesOrNo.YES, YesOrNo.YES, false),
            Arguments.of(ApplicationType.SOLE_APPLICATION, true, true, false, YesOrNo.YES, YesOrNo.YES, false),
            Arguments.of(ApplicationType.SOLE_APPLICATION, true, false, false, YesOrNo.YES, YesOrNo.YES, false),
            Arguments.of(ApplicationType.SOLE_APPLICATION, true, true, false, YesOrNo.NO, YesOrNo.YES, true),
            Arguments.of(ApplicationType.SOLE_APPLICATION, false, true, true, YesOrNo.YES, YesOrNo.YES, false),
            Arguments.of(ApplicationType.SOLE_APPLICATION, false, false, true, YesOrNo.YES, YesOrNo.YES, false),
            Arguments.of(ApplicationType.SOLE_APPLICATION, false, true, false, YesOrNo.YES, YesOrNo.YES, false),
            Arguments.of(ApplicationType.SOLE_APPLICATION, false, false, false, YesOrNo.YES, YesOrNo.YES, false),
            Arguments.of(ApplicationType.SOLE_APPLICATION, false, true, false, YesOrNo.YES, YesOrNo.NO, true),
            Arguments.of(ApplicationType.JOINT_APPLICATION, true, true, true, YesOrNo.YES, YesOrNo.YES, false),
            Arguments.of(ApplicationType.JOINT_APPLICATION, true, false, true, YesOrNo.YES, YesOrNo.YES, false),
            Arguments.of(ApplicationType.JOINT_APPLICATION, true, true, false, YesOrNo.YES, YesOrNo.YES, false),
            Arguments.of(ApplicationType.JOINT_APPLICATION, true, true, false, YesOrNo.NO, YesOrNo.YES, false),
            Arguments.of(ApplicationType.JOINT_APPLICATION, true, true, false, YesOrNo.YES, YesOrNo.NO, false),
            Arguments.of(ApplicationType.JOINT_APPLICATION, true, true, false, YesOrNo.NO, YesOrNo.NO, true),
            Arguments.of(ApplicationType.JOINT_APPLICATION, false, false, true, YesOrNo.YES, YesOrNo.YES, false),
            Arguments.of(ApplicationType.JOINT_APPLICATION, false, true, false, YesOrNo.YES, YesOrNo.YES, false),
            Arguments.of(ApplicationType.JOINT_APPLICATION, false, true, false, YesOrNo.NO, YesOrNo.YES, false),
            Arguments.of(ApplicationType.JOINT_APPLICATION, false, true, false, YesOrNo.YES, YesOrNo.NO, false),
            Arguments.of(ApplicationType.JOINT_APPLICATION, false, true, false, YesOrNo.NO, YesOrNo.NO, true)
        );
    }

    @ParameterizedTest
    @MethodSource("provideTestArguments")
    void parameterizedWillApplicantBeMadeOfflineTest(ApplicationType applicationType,
                                                     boolean isApplicant1,
                                                     boolean hadEmail,
                                                     boolean hasEmail,
                                                     YesOrNo isApp1Represented,
                                                     YesOrNo isApp2Represented,
                                                     boolean expectedResult) {
        var beforeDetails = getCaseDetails(applicationType, isApp1Represented, isApp2Represented, hadEmail);
        var afterDetails = getCaseDetails(applicationType, isApp1Represented, isApp2Represented, hasEmail);

        boolean result = emailUpdateService.willApplicantBeMadeOffline(afterDetails, beforeDetails, isApplicant1);

        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void shouldMakeSoleUnrepresentedApplicantOfflineWhenEmailIsRemoved() {
        var beforeDetails = getCaseDetails(SOLE_APPLICATION, YesOrNo.NO, YesOrNo.YES, true);
        var afterDetails = getCaseDetails(SOLE_APPLICATION, YesOrNo.NO, YesOrNo.YES, false);

        final CaseDetails<CaseData, State> newDetails =
            emailUpdateService.processEmailUpdate(afterDetails, beforeDetails, true);

        assertThat(newDetails.getData().getApplicant1().getOffline()).isEqualTo(YesOrNo.YES);
        verifyNoInteractions(inviteApplicantToCaseNotification);
        verify(ccdAccessService).removeUsersWithRole(TEST_CASE_ID,List.of(UserRole.CREATOR.getRole()));
    }

    @Test
    void shouldMakeSoleUnrepresentedRespondentOfflineWhenEmailIsRemoved() {
        var beforeDetails = getCaseDetails(SOLE_APPLICATION, YesOrNo.YES, YesOrNo.NO, true);
        var afterDetails = getCaseDetails(SOLE_APPLICATION, YesOrNo.YES, YesOrNo.NO, false);

        final CaseDetails<CaseData, State> newDetails =
            emailUpdateService.processEmailUpdate(afterDetails, beforeDetails, false);

        assertThat(newDetails.getData().getApplicant2().getOffline()).isEqualTo(YesOrNo.YES);
        verifyNoInteractions(inviteApplicantToCaseNotification);
        verify(ccdAccessService).removeUsersWithRole(TEST_CASE_ID,List.of(UserRole.APPLICANT_2.getRole()));
    }

    @Test
    void shouldMakeJointUnrepresentedApplicant1OfflineWhenEmailIsRemovedAndApplicant2IsNotRepresented() {
        var beforeDetails = getCaseDetails(JOINT_APPLICATION, YesOrNo.NO, YesOrNo.NO, true);
        var afterDetails = getCaseDetails(JOINT_APPLICATION, YesOrNo.NO, YesOrNo.NO, false);

        final CaseDetails<CaseData, State> newDetails =
            emailUpdateService.processEmailUpdate(afterDetails, beforeDetails, true);

        assertThat(newDetails.getData().getApplicant1().getOffline()).isEqualTo(YesOrNo.YES);
        assertThat(newDetails.getData().getApplicant2().getOffline()).isEqualTo(YesOrNo.YES);
        verifyNoInteractions(inviteApplicantToCaseNotification);
        verify(ccdAccessService).removeUsersWithRole(TEST_CASE_ID,List.of(UserRole.CREATOR.getRole(),UserRole.APPLICANT_2.getRole()));
    }

    @Test
    void shouldMakeJointUnrepresentedApplicant2OfflineWhenEmailIsRemovedAndApplicant1IsNotRepresented() {
        var beforeDetails = getCaseDetails(JOINT_APPLICATION, YesOrNo.NO, YesOrNo.NO, true);
        var afterDetails = getCaseDetails(JOINT_APPLICATION, YesOrNo.NO, YesOrNo.NO, false);

        final CaseDetails<CaseData, State> newDetails =
            emailUpdateService.processEmailUpdate(afterDetails, beforeDetails, false);

        assertThat(newDetails.getData().getApplicant1().getOffline()).isEqualTo(YesOrNo.YES);
        assertThat(newDetails.getData().getApplicant2().getOffline()).isEqualTo(YesOrNo.YES);
        verifyNoInteractions(inviteApplicantToCaseNotification);
        verify(ccdAccessService).removeUsersWithRole(TEST_CASE_ID,List.of(UserRole.CREATOR.getRole(),UserRole.APPLICANT_2.getRole()));
    }

    private CaseDetails<CaseData, State> getCaseDetails(ApplicationType applicationType,
                                                        YesOrNo isApp1Represented,
                                                        YesOrNo isApp2Represented,
                                                        boolean hasEmail) {
        final var details = new CaseDetails<CaseData, State>();
        final var data = CaseData.builder()
            .applicationType(applicationType)
            .applicant1(getApplicant())
            .applicant2(getApplicant())
            .divorceOrDissolution(DIVORCE)
            .build();

        data.getApplicant1().setSolicitorRepresented(isApp1Represented);
        data.getApplicant2().setSolicitorRepresented(isApp2Represented);

        data.getApplicant1().setOffline(YesOrNo.NO);
        data.getApplicant2().setOffline(YesOrNo.NO);

        if (!hasEmail) {
            data.getApplicant1().setEmail(null);
            data.getApplicant2().setEmail(null);
        }

        details.setData(data);
        details.setId(TEST_CASE_ID);

        return details;
    }
}
