package uk.gov.hmcts.divorce.common.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;

import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;

@Component
@Slf4j
public class ResetConditionalOrderFlags implements CaseTask {
    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {
        log.info("Resetting conditional order flags for case id {} ", caseDetails.getId());

        final CaseData data = caseDetails.getData();
        final boolean conditionalOrderInProgress = State.ConditionalOrderDrafted.equals(caseDetails.getState());


        resetConditionalOrderSubmittedFlags(data);
        if (!conditionalOrderInProgress) {
            resetConditionalOrderDraftedFlags(data);
        }

        return caseDetails;
    }

    private void resetConditionalOrderDraftedFlags(CaseData data) {
        data.getConditionalOrder().getConditionalOrderApplicant1Questions().setIsDrafted(NO);
        data.getConditionalOrder().getConditionalOrderApplicant1Questions().setStatementOfTruth(null);

        if (isJointApplication(data)) {
            data.getConditionalOrder().getConditionalOrderApplicant2Questions().setIsDrafted(NO);
            data.getConditionalOrder().getConditionalOrderApplicant2Questions().setStatementOfTruth(null);
        }
    }

    private void resetConditionalOrderSubmittedFlags(CaseData data) {
        data.getConditionalOrder().getConditionalOrderApplicant1Questions().setIsSubmitted(NO);
        data.getConditionalOrder().getConditionalOrderApplicant1Questions().setSubmittedDate(null);

        if (isJointApplication(data)) {
            data.getConditionalOrder().getConditionalOrderApplicant2Questions().setIsSubmitted(NO);
            data.getConditionalOrder().getConditionalOrderApplicant2Questions().setSubmittedDate(null);
        }
    }

    private boolean isJointApplication(CaseData data) {
        return data.getApplicationType() != null && !data.getApplicationType().isSole();
    }
}
