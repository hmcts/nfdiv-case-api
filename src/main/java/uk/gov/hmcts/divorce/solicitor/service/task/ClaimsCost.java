package uk.gov.hmcts.divorce.solicitor.service.task;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;

import java.util.Set;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ClaimsCostFrom.APPLICANT_2;

@Component
public class ClaimsCost implements CaseTask {

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {

        final CaseData caseData = caseDetails.getData();
        final Application application = caseData.getApplication();

        final boolean isApplicant1ClaimingCosts = YES.equals(application.getDivorceCostsClaim());
        final boolean claimsCostFromIsEmpty = isEmpty(application.getDivorceClaimFrom());

        if (isApplicant1ClaimingCosts && claimsCostFromIsEmpty) {
            application.setDivorceClaimFrom(Set.of(APPLICANT_2));
        }

        return caseDetails;
    }
}
