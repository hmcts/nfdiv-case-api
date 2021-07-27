package uk.gov.hmcts.divorce.solicitor.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import java.util.Set;

import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ClaimsCostFrom.APPLICANT_2;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE_TIME;

@ExtendWith(MockitoExtension.class)
class ClaimsCostTest {

    @InjectMocks
    private ClaimsCost claimsCost;

    @Test
    void shouldSetClaimsCostFromApplicant2IfApplicant1ClaimingCostsAndClaimsCostFromIsEmpty() {

        final CaseData caseData = CaseData.builder()
            .application(Application.builder().divorceCostsClaim(YES).build())
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);

        final CaseDetails<CaseData, State> result = claimsCost.apply(caseDetails);

        assertThat(result.getData().getApplication().getDivorceClaimFrom()).isEqualTo(Set.of(APPLICANT_2));
    }

    @Test
    void shouldNotSetClaimsCostFromApplicant2IfApplicant1ClaimingCostsAndClaimsCostFromIsNotEmpty() {

        final CaseData caseData = CaseData.builder()
            .application(Application.builder()
                .divorceCostsClaim(YES)
                .divorceClaimFrom(Set.of(APPLICANT_2))
                .build())
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);

        final CaseDetails<CaseData, State> result = claimsCost.apply(caseDetails);

        assertThat(result.getData().getApplication().getDivorceClaimFrom()).isEqualTo(Set.of(APPLICANT_2));
    }

    @Test
    void shouldNotSetClaimsCostFromApplicant2IfApplicant1NotClaimingCosts() {

        final CaseData caseData = CaseData.builder()
            .application(Application.builder()
                .divorceCostsClaim(NO)
                .divorceClaimFrom(emptySet())
                .build())
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);

        final CaseDetails<CaseData, State> result = claimsCost.apply(caseDetails);

        assertThat(result.getData().getApplication().getDivorceClaimFrom()).isEmpty();
    }
}
