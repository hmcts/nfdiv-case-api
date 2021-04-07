package uk.gov.hmcts.reform.divorce.caseapi.service.solicitor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.divorce.caseapi.util.CaseDataContext;
import uk.gov.hmcts.reform.divorce.caseapi.util.CaseDataUpdater;
import uk.gov.hmcts.reform.divorce.caseapi.util.CaseDataUpdaterChain;
import uk.gov.hmcts.reform.divorce.caseapi.util.CaseDataUpdaterChainFactory;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;

import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SolicitorCreatePetitionServiceTest {

    @Mock
    private ClaimsCost claimsCost;

    @Mock
    private SolicitorCourtDetails solicitorCourtDetails;

    @Mock
    private SolicitorOrganisationPolicyReference solicitorOrganisationPolicyReference;

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
            solicitorOrganisationPolicyReference);

        final CaseDataContext caseDataContext = CaseDataContext.builder()
            .caseData(caseData)
            .build();

        when(caseDataUpdaterChainFactory.createWith(caseDataUpdaters)).thenReturn(caseDataUpdaterChain);
        when(caseDataUpdaterChain.processNext(caseDataContext)).thenReturn(caseDataContext);

        final CaseData actualCaseData = solicitorCreatePetitionService.aboutToSubmit(caseData);

        assertThat(actualCaseData, is(caseData));

        verify(caseDataUpdaterChainFactory).createWith(caseDataUpdaters);
        verify(caseDataUpdaterChain).processNext(caseDataContext);

        verifyNoMoreInteractions(caseDataUpdaterChainFactory, caseDataUpdaterChain);
    }
}