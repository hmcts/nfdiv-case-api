package uk.gov.hmcts.divorce.solicitor.service.updater;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.updater.CaseDataContext;
import uk.gov.hmcts.divorce.divorcecase.updater.CaseDataUpdaterChain;

import java.util.Set;

import static java.util.Collections.emptySet;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ClaimsCostFrom.APPLICANT_2;

@ExtendWith(MockitoExtension.class)
class ClaimsCostTest {

    @Mock
    private CaseDataContext caseDataContext;

    @Mock
    private CaseDataUpdaterChain caseDataUpdaterChain;

    @InjectMocks
    private ClaimsCost claimsCost;

    @Test
    void shouldSetClaimsCostFromApplicant2IfApplicant1ClaimingCostsAndClaimsCostFromIsEmpty() {

        final CaseData caseData = CaseData.builder()
            .application(Application.builder().divorceCostsClaim(YES).build())
            .build();

        setupMocks(caseData);

        final CaseDataContext result = claimsCost.updateCaseData(caseDataContext, caseDataUpdaterChain);

        assertThat(result, is(caseDataContext));
        assertThat(caseData.getApplication().getDivorceClaimFrom(), is(Set.of(APPLICANT_2)));
    }

    @Test
    void shouldNotSetClaimsCostFromApplicant2IfApplicant1ClaimingCostsAndClaimsCostFromIsNotEmpty() {

        final CaseData caseData = CaseData.builder()
            .application(Application.builder()
                .divorceCostsClaim(YES)
                .divorceClaimFrom(Set.of(APPLICANT_2))
                .build())
            .build();

        setupMocks(caseData);

        claimsCost.updateCaseData(caseDataContext, caseDataUpdaterChain);

        assertThat(caseData.getApplication().getDivorceClaimFrom(), is(Set.of(APPLICANT_2)));
    }

    @Test
    void shouldNotSetClaimsCostFromApplicant2IfApplicant1NotClaimingCosts() {

        final CaseData caseData = CaseData.builder()
            .application(Application.builder()
                .divorceCostsClaim(NO)
                .divorceClaimFrom(emptySet())
                .build())
            .build();

        setupMocks(caseData);

        claimsCost.updateCaseData(caseDataContext, caseDataUpdaterChain);

        assertThat(caseData.getApplication().getDivorceClaimFrom(), is(emptySet()));
    }

    private void setupMocks(final CaseData caseData) {
        when(caseDataContext.copyOfCaseData()).thenReturn(caseData);
        when(caseDataContext.handlerContextWith(caseData)).thenReturn(caseDataContext);
        when(caseDataUpdaterChain.processNext(caseDataContext)).thenReturn(caseDataContext);
    }
}
