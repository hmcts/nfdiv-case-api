package uk.gov.hmcts.divorce.solicitor.service.updater;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.updater.CaseDataContext;
import uk.gov.hmcts.divorce.common.updater.CaseDataUpdater;
import uk.gov.hmcts.divorce.common.updater.CaseDataUpdaterChain;

import java.util.Set;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.common.model.ClaimsCostFrom.RESPONDENT;

@Component
public class ClaimsCost implements CaseDataUpdater {

    @Override
    public CaseDataContext updateCaseData(final CaseDataContext caseDataContext,
                                          final CaseDataUpdaterChain caseDataUpdaterChain) {

        final CaseData caseData = caseDataContext.copyOfCaseData();

        final boolean isPetitionerClaimingCosts = YES.equals(caseData.getDivorceCostsClaim());
        final boolean claimsCostFromIsEmpty = isEmpty(caseData.getDivorceClaimFrom());

        if (isPetitionerClaimingCosts && claimsCostFromIsEmpty) {
            caseData.setDivorceClaimFrom(Set.of(RESPONDENT));
        }

        return caseDataUpdaterChain.processNext(caseDataContext.handlerContextWith(caseData));
    }
}
