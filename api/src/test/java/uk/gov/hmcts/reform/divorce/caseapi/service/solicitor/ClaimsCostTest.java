package uk.gov.hmcts.reform.divorce.caseapi.service.solicitor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;

import java.util.Set;

import static java.util.Collections.emptySet;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.reform.divorce.ccd.model.enums.ClaimsCostFrom.CORRESPONDENT;
import static uk.gov.hmcts.reform.divorce.ccd.model.enums.ClaimsCostFrom.RESPONDENT;

@ExtendWith(MockitoExtension.class)
class ClaimsCostTest {

    @InjectMocks
    private ClaimsCost claimsCost;

    @Test
    void shouldSetClaimsCostFromRespondentIfPetitionerClaimingCostsAndClaimsCostFromIsEmpty() {

        final CaseData caseData = mock(CaseData.class);

        when(caseData.getD8DivorceCostsClaim()).thenReturn(YES);
        when(caseData.getDivorceClaimFrom()).thenReturn(null);

        final CaseData result = claimsCost.handle(caseData);

        assertThat(result, is(caseData));
        verify(caseData).setDivorceClaimFrom(Set.of(RESPONDENT));
        verifyNoMoreInteractions(caseData);
    }

    @Test
    void shouldNotSetClaimsCostFromRespondentIfPetitionerClaimingCostsAndClaimsCostFromIsNotEmpty() {

        final CaseData caseData = mock(CaseData.class);

        when(caseData.getD8DivorceCostsClaim()).thenReturn(YES);
        when(caseData.getDivorceClaimFrom()).thenReturn(Set.of(CORRESPONDENT));

        final CaseData result = claimsCost.handle(caseData);

        assertThat(result, is(caseData));
        verifyNoMoreInteractions(caseData);
    }

    @Test
    void shouldNotSetClaimsCostFromRespondentIfPetitionerNotClaimingCosts() {

        final CaseData caseData = mock(CaseData.class);

        when(caseData.getD8DivorceCostsClaim()).thenReturn(NO);
        when(caseData.getDivorceClaimFrom()).thenReturn(emptySet());

        final CaseData result = claimsCost.handle(caseData);

        assertThat(result, is(caseData));
        verifyNoMoreInteractions(caseData);
    }
}