package uk.gov.hmcts.divorce.caseworker.service.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.caseworker.service.print.AosPackPrinter;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;

@Component
@Slf4j
@RequiredArgsConstructor
public class SendAosPackToRespondent implements CaseTask {

    private final AosPackPrinter aosPackPrinter;

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {

        final Long caseId = caseDetails.getId();
        final CaseData caseData = caseDetails.getData();

        final Application application = caseData.getApplication();
        final boolean isCourtService = application.isCourtServiceMethod();
        final boolean mustServeAnotherWay = application.mustServeAnotherWay(caseData.getApplicationType());

        if (isCourtService && !mustServeAnotherWay) {
            log.info("Sending respondent AoS pack to bulk print.  Case ID: {}", caseId);
            aosPackPrinter.sendAosLetterToRespondent(caseData, caseId);
        }

        return caseDetails;
    }
}
