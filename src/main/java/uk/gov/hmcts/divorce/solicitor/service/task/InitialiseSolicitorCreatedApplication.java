package uk.gov.hmcts.divorce.solicitor.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;

import java.time.LocalDate;

import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;

@Component
@Slf4j
public class InitialiseSolicitorCreatedApplication implements CaseTask {

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {
        final LocalDate createdDate = caseDetails.getCreatedDate().toLocalDate();
        caseDetails.getData().getApplication().setCreatedDate(createdDate);
        caseDetails.getData().getApplicant1().setSolicitorRepresented(YES);

        log.info("Setting application createdDate to {}, and applicant 1 SolicitorRepresented to Yes, for CaseId: {}, State: {}",
            createdDate,
            caseDetails.getId(),
            caseDetails.getState());

        return caseDetails;
    }
}
