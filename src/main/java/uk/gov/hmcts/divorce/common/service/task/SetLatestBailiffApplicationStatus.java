package uk.gov.hmcts.divorce.common.service.task;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceOutcome;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;

import java.util.Optional;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;

@Component
public class SetLatestBailiffApplicationStatus implements CaseTask {

    @Override
    public CaseDetails<CaseData, State> apply(CaseDetails<CaseData, State> details) {
        CaseData data = details.getData();
        Optional<AlternativeServiceOutcome> outcomeOptional = data.getFirstAlternativeServiceOutcome();

        if (outcomeOptional.isPresent()) {

            AlternativeServiceOutcome outcome = outcomeOptional.get();

            if (isNotEmpty(outcome.getCertificateOfServiceDate()) && isNotEmpty(outcome.getSuccessfulServedByBailiff())) {
                data.getConditionalOrder().setLastApprovedServiceApplicationIsBailiffApplication(YES);
                data.getConditionalOrder().setCertificateOfServiceDate(outcome.getCertificateOfServiceDate());
                data.getConditionalOrder().setSuccessfulServedByBailiff(outcome.getSuccessfulServedByBailiff());
            } else {
                data.getConditionalOrder().setLastApprovedServiceApplicationIsBailiffApplication(NO);
            }
        }

        return details;
    }
}
