package uk.gov.hmcts.divorce.api.service.solicitor;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.api.util.CaseDataContext;
import uk.gov.hmcts.divorce.api.util.CaseDataUpdater;
import uk.gov.hmcts.divorce.api.util.CaseDataUpdaterChain;
import uk.gov.hmcts.divorce.ccd.model.CaseData;

import java.util.Set;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.ccd.model.enums.ClaimsCostFrom.CO_APPLICANT;

@Component
public class ClaimsCost implements CaseDataUpdater {

    @Override
    public CaseDataContext updateCaseData(final CaseDataContext caseDataContext,
                                          final CaseDataUpdaterChain caseDataUpdaterChain) {

        final CaseData caseData = caseDataContext.copyOfCaseData();

        final boolean isApplicantClaimingCosts = YES.equals(caseData.getDivorceCostsClaim());
        final boolean claimsCostFromIsEmpty = isEmpty(caseData.getDivorceClaimFrom());

        if (isApplicantClaimingCosts && claimsCostFromIsEmpty) {
            caseData.setDivorceClaimFrom(Set.of(CO_APPLICANT));
        }

        return caseDataUpdaterChain.processNext(caseDataContext.handlerContextWith(caseData));
    }
}
