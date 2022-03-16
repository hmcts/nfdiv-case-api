package uk.gov.hmcts.divorce.caseworker.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.caseworker.service.print.AosPackPrinter;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;

@Component
@Slf4j
public class SendAosPackToRespondent implements CaseTask {

    @Autowired
    private AosPackPrinter aosPackPrinter;

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {

        final Long caseId = caseDetails.getId();
        final CaseData caseData = caseDetails.getData();

        boolean isAddressKnown = isNull(caseData.getApplication().getApplicant1KnowsApplicant2Address())
            || YES.equals(caseData.getApplication().getApplicant1KnowsApplicant2Address());

        if (caseData.getApplicationType().isSole()
            && !caseData.getApplicant2().isRepresented()
            && !caseData.getApplicant2().isBasedOverseas()
            &&  isAddressKnown
        ) {
            log.info("Sending respondent AoS pack to bulk print.  Case ID: {}", caseId);
            aosPackPrinter.sendAosLetterToRespondent(caseData, caseId);

            log.info("Setting Notice Of Proceedings information. CaseID: {}", caseId);
            caseData.getAcknowledgementOfService().setNoticeOfProceedings(caseData.getApplicant2());
        }

        return caseDetails;
    }
}
