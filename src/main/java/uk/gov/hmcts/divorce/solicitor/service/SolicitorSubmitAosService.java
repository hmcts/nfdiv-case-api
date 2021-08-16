package uk.gov.hmcts.divorce.solicitor.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.common.service.HoldingPeriodService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import java.time.Clock;
import java.time.LocalDate;
import java.util.function.Function;

import static java.time.LocalDateTime.now;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Disputed;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Holding;
import static uk.gov.hmcts.divorce.divorcecase.task.CaseTaskRunner.caseTasks;

@Service
@Slf4j
public class SolicitorSubmitAosService {

    @Autowired
    private HoldingPeriodService holdingPeriodService;

    @Autowired
    private Clock clock;

    public CaseDetails<CaseData, State> submitAos(final CaseDetails<CaseData, State> caseDetails) {

        caseTasks(
            setSubmitAosState(),
            setSubmissionAndDueDate())
            .run(caseDetails);


        return caseDetails;
    }

    private Function<CaseDetails<CaseData, State>, CaseDetails<CaseData, State>> setSubmitAosState() {
        return details -> {
            if (NO.equals(details.getData().getAcknowledgementOfService().getJurisdictionAgree())) {
                details.setState(Disputed);
            } else {
                details.setState(Holding);
            }

            log.info("Setting submit AoS state: {}, for CaseID: {}", details.getState(), details.getId());

            return details;
        };
    }

    private Function<CaseDetails<CaseData, State>, CaseDetails<CaseData, State>> setSubmissionAndDueDate() {
        return details -> {
            if (Holding.equals(details.getState())) {
                final LocalDate issueDate = details.getData().getApplication().getIssueDate();
                details.getData().setDueDate(holdingPeriodService.getDueDateAfter(issueDate));
            }

            details.getData().getAcknowledgementOfService().setDateAosSubmitted(now(clock));
            return details;
        };
    }
}
