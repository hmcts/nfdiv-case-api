package uk.gov.hmcts.divorce.caseworker.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.caseworker.service.updater.MiniApplication;
import uk.gov.hmcts.divorce.caseworker.service.updater.RespondentSolicitorAosInvitation;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.updater.CaseDataContext;
import uk.gov.hmcts.divorce.divorcecase.updater.CaseDataUpdater;
import uk.gov.hmcts.divorce.divorcecase.updater.CaseDataUpdaterChain;
import uk.gov.hmcts.divorce.divorcecase.updater.CaseDataUpdaterChainFactory;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class CaseworkerIssueApplicationServiceTest {

    @Mock
    private MiniApplication miniApplication;

    @Mock
    private RespondentSolicitorAosInvitation respondentSolicitorAosInvitation;

    @Mock
    private CaseDataUpdaterChainFactory caseDataUpdaterChainFactory;

    @Mock
    private Clock clock;

    @InjectMocks
    private IssueApplicationService issueApplicationService;

    @Test
    void shouldGenerateMiniApplicationAndRespondentAosAndSetIssueDateWhenRespondentIsSolicitorRepresented() {

        final CaseData caseData = caseData();
        caseData.getApplicant2().setSolicitorRepresented(YES);
        caseData.getApplicant2().setSolicitor(Solicitor.builder().name("testsol").isDigital(YES).build());

        final CaseDataUpdaterChain caseDataUpdaterChain = mock(CaseDataUpdaterChain.class);

        final List<CaseDataUpdater> caseDataUpdaters = List.of(miniApplication, respondentSolicitorAosInvitation);

        final CaseDataContext caseDataContext = CaseDataContext.builder()
            .caseData(caseData)
            .caseId(TEST_CASE_ID)
            .createdDate(LOCAL_DATE)
            .userAuthToken(TEST_AUTHORIZATION_TOKEN)
            .build();

        final var instant = Instant.now();
        final var zoneId = ZoneId.systemDefault();

        when(clock.instant()).thenReturn(instant);
        when(clock.getZone()).thenReturn(zoneId);

        when(caseDataUpdaterChainFactory.createWith(caseDataUpdaters)).thenReturn(caseDataUpdaterChain);
        when(caseDataUpdaterChain.processNext(caseDataContext)).thenReturn(caseDataContext);

        final CaseData response = issueApplicationService.aboutToSubmit(
            caseData,
            TEST_CASE_ID,
            LOCAL_DATE,
            TEST_AUTHORIZATION_TOKEN
        );

        final var expectedDateTime = LocalDate.ofInstant(instant, zoneId);

        var expectedCaseData = caseData();
        expectedCaseData.setIssueDate(expectedDateTime);
        expectedCaseData.getApplicant2().setSolicitorRepresented(YES);
        expectedCaseData.getApplicant2().setSolicitor(Solicitor.builder().name("testsol").isDigital(YES).build());

        assertThat(response).isEqualTo(expectedCaseData);

        verify(caseDataUpdaterChainFactory).createWith(caseDataUpdaters);
        verify(caseDataUpdaterChain).processNext(caseDataContext);

        verifyNoMoreInteractions(caseDataUpdaterChainFactory, caseDataUpdaterChain);
    }

    @Test
    void shouldGenerateOnlyMiniApplicationAndSetIssueDateWhenRespondentIsNotSolicitorRepresented() {

        final CaseData caseData = caseData();
        caseData.getApplicant2().setSolicitorRepresented(NO);

        final CaseDataUpdaterChain caseDataUpdaterChain = mock(CaseDataUpdaterChain.class);

        final List<CaseDataUpdater> caseDataUpdaters = List.of(miniApplication);

        final CaseDataContext caseDataContext = CaseDataContext.builder()
            .caseData(caseData)
            .caseId(TEST_CASE_ID)
            .createdDate(LOCAL_DATE)
            .userAuthToken(TEST_AUTHORIZATION_TOKEN)
            .build();

        final var instant = Instant.now();
        final var zoneId = ZoneId.systemDefault();

        when(clock.instant()).thenReturn(instant);
        when(clock.getZone()).thenReturn(zoneId);

        when(caseDataUpdaterChainFactory.createWith(caseDataUpdaters)).thenReturn(caseDataUpdaterChain);
        when(caseDataUpdaterChain.processNext(caseDataContext)).thenReturn(caseDataContext);

        final CaseData response = issueApplicationService.aboutToSubmit(
            caseData,
            TEST_CASE_ID,
            LOCAL_DATE,
            TEST_AUTHORIZATION_TOKEN
        );

        final var expectedDateTime = LocalDate.ofInstant(instant, zoneId);

        var expectedCaseData = caseData();
        expectedCaseData.setIssueDate(expectedDateTime);
        expectedCaseData.getApplicant2().setSolicitorRepresented(NO);

        assertThat(response).isEqualTo(expectedCaseData);

        verify(caseDataUpdaterChainFactory).createWith(caseDataUpdaters);
        verify(caseDataUpdaterChain).processNext(caseDataContext);

        verifyNoMoreInteractions(caseDataUpdaterChainFactory, caseDataUpdaterChain);
    }
}
