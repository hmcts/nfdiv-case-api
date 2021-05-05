package uk.gov.hmcts.divorce.solicitor.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.updater.CaseDataContext;
import uk.gov.hmcts.divorce.common.updater.CaseDataUpdater;
import uk.gov.hmcts.divorce.common.updater.CaseDataUpdaterChain;
import uk.gov.hmcts.divorce.common.updater.CaseDataUpdaterChainFactory;
import uk.gov.hmcts.divorce.solicitor.service.updater.ClaimsCost;
import uk.gov.hmcts.divorce.solicitor.service.updater.MiniPetitionDraft;
import uk.gov.hmcts.divorce.solicitor.service.updater.SolicitorCourtDetails;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class SolicitorCreatePetitionServiceTest {

    private static final LocalDateTime LOCAL_DATE_TIME = LocalDateTime.of(2021, 04, 28, 1, 0);

    @Mock
    private ClaimsCost claimsCost;

    @Mock
    private SolicitorCourtDetails solicitorCourtDetails;

    @Mock
    private MiniPetitionDraft miniPetitionDraft;

    @Mock
    private CaseDataUpdaterChainFactory caseDataUpdaterChainFactory;

    @InjectMocks
    private SolicitorCreatePetitionService solicitorCreatePetitionService;

    @Test
    void shouldCompleteStepsToCreatePetition() {

        final CaseData caseData = mock(CaseData.class);
        final CaseDataUpdaterChain caseDataUpdaterChain = mock(CaseDataUpdaterChain.class);

        final List<CaseDataUpdater> caseDataUpdaters = asList(
            claimsCost,
            solicitorCourtDetails,
            miniPetitionDraft);

        final CaseDataContext caseDataContext = CaseDataContext.builder()
            .caseData(caseData)
            .caseId(TEST_CASE_ID)
            .createdDate(LOCAL_DATE_TIME)
            .userAuthToken(TEST_AUTHORIZATION_TOKEN)
            .build();

        when(caseDataUpdaterChainFactory.createWith(caseDataUpdaters)).thenReturn(caseDataUpdaterChain);
        when(caseDataUpdaterChain.processNext(caseDataContext)).thenReturn(caseDataContext);

        final CaseData actualCaseData = solicitorCreatePetitionService.aboutToSubmit(
            caseData,
            TEST_CASE_ID,
            LOCAL_DATE_TIME,
            TEST_AUTHORIZATION_TOKEN
        );

        assertThat(actualCaseData, is(caseData));

        verify(caseDataUpdaterChainFactory).createWith(caseDataUpdaters);
        verify(caseDataUpdaterChain).processNext(caseDataContext);

        verifyNoMoreInteractions(caseDataUpdaterChainFactory, caseDataUpdaterChain);
    }
}
