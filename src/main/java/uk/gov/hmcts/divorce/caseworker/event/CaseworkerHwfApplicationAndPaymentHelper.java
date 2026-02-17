package uk.gov.hmcts.divorce.caseworker.event;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.common.service.task.SetDefaultOrganisationPolicies;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import java.time.Clock;
import java.time.LocalDateTime;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingDocuments;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.divorcecase.task.CaseTaskRunner.caseTasks;

@Component
@RequiredArgsConstructor
public class CaseworkerHwfApplicationAndPaymentHelper {

    private final Clock clock;

    private final SetDefaultOrganisationPolicies setDefaultOrganisationPolicies;

    public State getState(CaseData caseData) {
        final boolean isSoleApplication =  nonNull(caseData.getApplicationType())
            && caseData.getApplicationType().isSole();
        final var application = caseData.getApplication();

        final boolean isApplicant1AwaitingDocuments = application.hasAwaitingApplicant1Documents()
            || isSoleApplication && application.isPersonalServiceMethod();
        final boolean isApplicant2AwaitingDocuments = application.hasAwaitingApplicant2Documents();

        boolean applicantIsAwaitingDocuments = isApplicant1AwaitingDocuments
            || (!isSoleApplication && isApplicant2AwaitingDocuments);

        return applicantIsAwaitingDocuments ? AwaitingDocuments : Submitted;
    }

    public CaseData setDateSubmittedAndDueDate(CaseData caseData) {

        if (null == caseData.getApplication().getDateSubmitted()) {
            caseData.getApplication().setDateSubmitted(LocalDateTime.now(clock));
        }

        if (null == caseData.getDueDate()) {
            caseData.setDueDate(caseData.getApplication().getDateOfSubmissionResponse());
        }

        return caseData;
    }

    public CaseDetails<CaseData, State> setRequiredCaseFieldsForPostSubmissionCase(CaseDetails<CaseData, State> details) {
        return caseTasks(setDefaultOrganisationPolicies).run(details);
    }
}
