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

        final CaseData caseData = new CaseData();
        caseData.setD8DivorceCostsClaim(YES);
        caseData.setDivorceClaimFrom(null);

        claimsCost.handle(caseData);

        assertThat(caseData.getDivorceClaimFrom(), is(Set.of(RESPONDENT)));
    }

    @Test
    void shouldNotSetClaimsCostFromRespondentIfPetitionerClaimingCostsAndClaimsCostFromIsNotEmpty() {

        final CaseData caseData = new CaseData();
        caseData.setD8DivorceCostsClaim(YES);
        caseData.setDivorceClaimFrom(Set.of(CORRESPONDENT));

        claimsCost.handle(caseData);

        assertThat(caseData.getDivorceClaimFrom(), is(Set.of(CORRESPONDENT)));
    }

    @Test
    void shouldNotSetClaimsCostFromRespondentIfPetitionerNotClaimingCosts() {

        final CaseData caseData = new CaseData();
        caseData.setD8DivorceCostsClaim(NO);
        caseData.setDivorceClaimFrom(emptySet());

        claimsCost.handle(caseData);

        assertThat(caseData.getDivorceClaimFrom(), is(emptySet()));
    }
}