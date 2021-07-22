package uk.gov.hmcts.divorce.caseworker.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.caseworker.service.updater.GenerateMiniApplication;
import uk.gov.hmcts.divorce.caseworker.service.updater.GenerateRespondentSolicitorAosInvitation;
import uk.gov.hmcts.divorce.caseworker.service.updater.SendAosNotifications;
import uk.gov.hmcts.divorce.caseworker.service.updater.SendAosPack;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.updater.CaseDataContext;

import java.time.Clock;
import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDate;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class CaseworkerIssueApplicationServiceTest {

    @Mock
    private GenerateMiniApplication generateMiniApplication;

    @Mock
    private GenerateRespondentSolicitorAosInvitation generateRespondentSolicitorAosInvitation;

    @Mock
    private SendAosPack sendAosPack;

    @Mock
    private SendAosNotifications sendAosNotifications;

    @Mock
    private Clock clock;

    @InjectMocks
    private IssueApplicationService issueApplicationService;

    @Test
    void shouldGenerateMiniApplicationAndRespondentAosAndSetIssueDateWhenRespondentIsSolicitorRepresented() {

        final CaseData caseData = caseData();
        caseData.getApplicant2().setSolicitorRepresented(YES);

        final Solicitor solicitor = Solicitor.builder()
            .name("testsol")
            .email(TEST_SOLICITOR_EMAIL)
            .isDigital(YES)
            .build();

        caseData.getApplicant2().setSolicitor(solicitor);

        final List<Function<CaseDataContext, CaseDataContext>> caseDataUpdaters = List.of(
            generateRespondentSolicitorAosInvitation,
            generateMiniApplication,
            sendAosPack,
            sendAosNotifications);

        final CaseDataContext caseDataContext = CaseDataContext.builder()
            .caseData(caseData)
            .caseId(TEST_CASE_ID)
            .createdDate(LOCAL_DATE)
            .userAuthToken(TEST_AUTHORIZATION_TOKEN)
            .build();

        setMockClock(clock);

        when(generateRespondentSolicitorAosInvitation.apply(caseDataContext)).thenReturn(caseDataContext);
        when(generateMiniApplication.apply(caseDataContext)).thenReturn(caseDataContext);
        when(sendAosPack.apply(caseDataContext)).thenReturn(caseDataContext);
        when(sendAosNotifications.apply(caseDataContext)).thenReturn(caseDataContext);

        final CaseData response = issueApplicationService.aboutToSubmit(
            caseData,
            TEST_CASE_ID,
            LOCAL_DATE,
            TEST_AUTHORIZATION_TOKEN
        );

        var expectedCaseData = caseData();
        expectedCaseData.getApplication().setIssueDate(getExpectedLocalDate());
        expectedCaseData.getApplicant2().setSolicitorRepresented(YES);
        expectedCaseData.getApplicant2().setSolicitor(solicitor);

        assertThat(response).isEqualTo(expectedCaseData);
    }

    @Test
    void shouldGenerateOnlyMiniApplicationAndSetIssueDateWhenRespondentIsNotSolicitorRepresented() {

        final CaseData caseData = caseData();
        caseData.getApplicant2().setSolicitorRepresented(NO);

        final List<Function<CaseDataContext, CaseDataContext>> caseDataUpdaters = List.of(
            generateMiniApplication,
            sendAosPack,
            sendAosNotifications);

        final CaseDataContext caseDataContext = CaseDataContext.builder()
            .caseData(caseData)
            .caseId(TEST_CASE_ID)
            .createdDate(LOCAL_DATE)
            .userAuthToken(TEST_AUTHORIZATION_TOKEN)
            .build();

        setMockClock(clock);

        when(generateMiniApplication.apply(caseDataContext)).thenReturn(caseDataContext);
        when(sendAosPack.apply(caseDataContext)).thenReturn(caseDataContext);
        when(sendAosNotifications.apply(caseDataContext)).thenReturn(caseDataContext);

        final CaseData response = issueApplicationService.aboutToSubmit(
            caseData,
            TEST_CASE_ID,
            LOCAL_DATE,
            TEST_AUTHORIZATION_TOKEN
        );

        var expectedCaseData = caseData();
        expectedCaseData.getApplication().setIssueDate(getExpectedLocalDate());
        expectedCaseData.getApplicant2().setSolicitorRepresented(NO);

        assertThat(response).isEqualTo(expectedCaseData);
    }
}
