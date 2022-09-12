package uk.gov.hmcts.divorce.caseworker.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.common.service.HoldingPeriodService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;

import java.time.Clock;
import java.time.LocalDate;

@Component
@Slf4j
public class ResetAosFields implements CaseTask {

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {

        log.info("Resetting AOS fields. Case ID: {}", caseDetails.getId());

        caseDetails.getData().getApplicant2().setLegalProceedings(null);
        caseDetails.getData().getApplicant2().setLegalProceedingsDetails(null);
        caseDetails.getData().getApplicant2().setLegalProceedingsDetailsTranslated(null);
        caseDetails.getData().getApplicant2().setLegalProceedingsDetailsTranslatedTo(null);

        caseDetails.getData().setAcknowledgementOfService(null);

        return caseDetails;
    }
}
