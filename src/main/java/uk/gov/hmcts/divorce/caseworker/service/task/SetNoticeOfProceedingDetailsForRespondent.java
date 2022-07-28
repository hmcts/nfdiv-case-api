package uk.gov.hmcts.divorce.caseworker.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;

@Component
@Slf4j
public class SetNoticeOfProceedingDetailsForRespondent implements CaseTask {

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {

        final CaseData caseData = caseDetails.getData();

        if (caseData.getApplication().isCourtServiceMethod()) {
            log.info("Setting Notice Of Proceedings information. CaseID: {}", caseDetails.getId());
            caseData.getAcknowledgementOfService().setNoticeOfProceedings(caseData.getApplicant2());
        }

        return caseDetails;
    }
}
