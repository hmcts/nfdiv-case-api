package uk.gov.hmcts.divorce.api.service.solicitor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.api.util.CaseDataContext;
import uk.gov.hmcts.divorce.api.util.CaseDataUpdater;
import uk.gov.hmcts.divorce.api.util.CaseDataUpdaterChain;
import uk.gov.hmcts.divorce.api.util.CaseDataUpdaterChainFactory;
import uk.gov.hmcts.divorce.ccd.model.CaseData;

import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.api.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.api.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class SolicitorCreatePetitionServiceTest {

    @Mock
    private ClaimsCost claimsCost;

    @Mock
    private SolicitorCourtDetails solicitorCourtDetails;

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
            solicitorCourtDetails);

        final CaseDataContext caseDataContext = CaseDataContext.builder()
            .caseData(caseData)
            .caseId(TEST_CASE_ID)
            .userAuthToken(TEST_AUTHORIZATION_TOKEN)
            .build();

        when(caseDataUpdaterChainFactory.createWith(caseDataUpdaters)).thenReturn(caseDataUpdaterChain);
        when(caseDataUpdaterChain.processNext(caseDataContext)).thenReturn(caseDataContext);

        final CaseData actualCaseData = solicitorCreatePetitionService.aboutToSubmit(
            caseData,
            TEST_CASE_ID,
            TEST_AUTHORIZATION_TOKEN
        );

        assertThat(actualCaseData, is(caseData));

        verify(caseDataUpdaterChainFactory).createWith(caseDataUpdaters);
        verify(caseDataUpdaterChain).processNext(caseDataContext);

        verifyNoMoreInteractions(caseDataUpdaterChainFactory, caseDataUpdaterChain);
    }
}
