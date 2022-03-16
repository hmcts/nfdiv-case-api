package uk.gov.hmcts.divorce.common.service.task;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;

@Component
public class SetApplicantOfflineStatus implements CaseTask {

    @Override
    public CaseDetails<CaseData, State> apply(CaseDetails<CaseData, State> details) {
        CaseData data = details.getData();
        data.getApplicant1().setOffline(NO);

        final var applicant2 = data.getApplicant2();
        final YesOrNo applicant2Offline;
        if (!applicant2.isRepresented() && isBlank(applicant2.getEmail())
            || applicant2.isRepresented() && !applicant2.getSolicitor().hasOrgId()
        ) {
            applicant2Offline = YES;
        } else {
            applicant2Offline = NO;
        }

        applicant2.setOffline(applicant2Offline);

        return details;
    }
}
