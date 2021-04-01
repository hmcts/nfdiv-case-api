package uk.gov.hmcts.reform.divorce.caseapi.service.solicitor;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.caseapi.util.Handler;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;
import uk.gov.hmcts.reform.divorce.ccd.model.enums.ClaimsCostFrom;

import java.util.Set;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;

@Component
public class ClaimsCost implements Handler<CaseData> {

    @Override
    public void handle(final CaseData caseData) {

        final boolean isPetitionerClaimingCosts = YES.equals(caseData.getD8DivorceCostsClaim());
        final boolean claimsCostFromIsEmpty = isEmpty(caseData.getDivorceClaimFrom());

        if (isPetitionerClaimingCosts && claimsCostFromIsEmpty) {
            caseData.setDivorceClaimFrom(Set.of(ClaimsCostFrom.RESPONDENT));
        }
    }
}
