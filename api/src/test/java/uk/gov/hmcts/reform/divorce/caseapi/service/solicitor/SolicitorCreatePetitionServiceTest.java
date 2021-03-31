package uk.gov.hmcts.reform.divorce.caseapi.service.solicitor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class SolicitorCreatePetitionServiceTest {

    @Mock
    private ClaimsCost claimsCost;

    @Mock
    private SolicitorCourtDetails solicitorCourtDetails;

    @Mock
    private MiniPetitionDraft miniPetitionDraft;

    @Mock
    private SolicitorOrganisationPolicyReference solicitorOrganisationPolicyReference;

    @InjectMocks
    private SolicitorCreatePetitionService solicitorCreatePetitionService;

    @Test
    void shouldCompleteStepsToCreatePetition() {

        final CaseData caseData = mock(CaseData.class);

        final CaseData actualCaseData = solicitorCreatePetitionService.aboutToSubmit(caseData);

        assertThat(actualCaseData, is(caseData));

        verify(claimsCost).handle(caseData);
        verify(solicitorCourtDetails).handle(caseData);
        verify(miniPetitionDraft).handle(caseData);
        verify(solicitorOrganisationPolicyReference).handle(caseData);

        verifyNoMoreInteractions(claimsCost, solicitorCourtDetails, miniPetitionDraft, solicitorOrganisationPolicyReference);
    }
}