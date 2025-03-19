package uk.gov.hmcts.divorce.caseworker.service.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.caseworker.service.task.util.GenerateApplicationHelper;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;

@Component
@Slf4j
@RequiredArgsConstructor
public class RegenerateApplication implements CaseTask {

    private final GenerateApplicationHelper generateApplicationHelper;

    @Override
    public CaseDetails<CaseData, State> apply(CaseDetails<CaseData, State> caseDetails) {
        final Long caseId = caseDetails.getId();
        log.info("Executing handler to re-generate application for case id {} ", caseId);

        caseDetails =  generateApplicationHelper.generateApplicationDocument(caseDetails, true);

        return caseDetails;
    }
}
